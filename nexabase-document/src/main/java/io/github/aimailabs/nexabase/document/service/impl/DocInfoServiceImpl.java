package io.github.aimailabs.nexabase.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.document.api.event.DocumentChangedEvent;
import io.github.aimailabs.nexabase.document.entity.DocContent;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.mapper.DocInfoMapper;
import io.github.aimailabs.nexabase.document.repository.DocContentRepository;
import io.github.aimailabs.nexabase.document.service.DocInfoService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档服务实现类。
 * <p>
 * 核心职责：
 * <ul>
 *   <li>管理文档元数据（MySQL）与正文内容（MongoDB）</li>
 *   <li>文档变更时投递事件至 MQ，触发向量化与索引同步</li>
 *   <li>确保同分类下文档标题唯一</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocInfoServiceImpl implements DocInfoService {

    private final DocInfoMapper docInfoMapper;
    private final DocContentRepository docContentRepository;
    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE_NAME = "document.exchange";
    public static final String ROUTING_KEY = "document.changed";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocInfo create(DocInfo docInfo, String content) {
        // 同一分类下，文档标题不允许重复（排除已逻辑删除的记录）
        boolean titleExists = docInfoMapper.exists(new LambdaQueryWrapper<DocInfo>()
                .eq(DocInfo::getCategoryId, docInfo.getCategoryId())
                .eq(DocInfo::getTitle, docInfo.getTitle())
                .eq(DocInfo::getIsDeleted, 0));
        if (titleExists) {
            throw new BusinessException(ResultCode.CONFLICT, "同分类下文档标题「" + docInfo.getTitle() + "」已存在，请更换标题");
        }

        docInfoMapper.insert(docInfo);

        // 存储正文到 MongoDB
        DocContent docContent = new DocContent();
        docContent.setDocumentId(docInfo.getId());
        docContent.setContent(content);
        docContentRepository.save(docContent);

        // 异步投递向量化与索引同步事件
        DocumentChangedEvent event = new DocumentChangedEvent(docInfo.getId(), "CREATE", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
        log.info("文档创建成功，docId={}, 已投递 CREATE 事件", docInfo.getId());

        return docInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocInfo update(Long id, DocInfo docInfo, String content, Long userId) {
        DocInfo existDoc = docInfoMapper.selectById(id);
        if (existDoc == null || existDoc.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "文档不存在或已被删除");
        }

        // 标题修改时检查同分类唯一性
        if (docInfo.getTitle() != null && !docInfo.getTitle().equals(existDoc.getTitle())) {
            boolean titleExists = docInfoMapper.exists(new LambdaQueryWrapper<DocInfo>()
                    .eq(DocInfo::getCategoryId, existDoc.getCategoryId())
                    .eq(DocInfo::getTitle, docInfo.getTitle())
                    .eq(DocInfo::getIsDeleted, 0)
                    .ne(DocInfo::getId, id));
            if (titleExists) {
                throw new BusinessException(ResultCode.CONFLICT, "同分类下文档标题「" + docInfo.getTitle() + "」已存在");
            }
            existDoc.setTitle(docInfo.getTitle());
        }

        // 更新其他可选字段
        if (docInfo.getStatus() != null) {
            existDoc.setStatus(docInfo.getStatus());
        }
        if (docInfo.getFileId() != null) {
            existDoc.setFileId(docInfo.getFileId());
        }
        existDoc.setUpdatedBy(userId);

        docInfoMapper.updateById(existDoc);

        // 更新正文（若提供）
        if (content != null) {
            DocContent docContent = docContentRepository.findById(id).orElse(null);
            if (docContent == null) {
                docContent = new DocContent();
                docContent.setDocumentId(id);
            }
            docContent.setContent(content);
            docContentRepository.save(docContent);
        }

        // 投递 UPDATE 事件
        DocumentChangedEvent event = new DocumentChangedEvent(id, "UPDATE", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
        log.info("文档更新成功，docId={}, 已投递 UPDATE 事件", id);

        return existDoc;
    }

    @Override
    public DocInfo getById(Long id) {
        DocInfo doc = docInfoMapper.selectById(id);
        if (doc == null || doc.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "文档不存在或已被删除");
        }
        return doc;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        DocInfo doc = docInfoMapper.selectById(id);
        if (doc == null || doc.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.CONFLICT, "文档不存在或已被删除，无需重复操作");
        }
        doc.setIsDeleted(1);
        doc.setUpdatedBy(userId);
        docInfoMapper.updateById(doc);

        // 删除 MongoDB 正文或软删除，这里简单执行物理删除以释放空间
        docContentRepository.deleteById(id);

        // 发送删除事件
        DocumentChangedEvent event = new DocumentChangedEvent(id, "DELETE", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
        log.info("文档删除成功，docId={}, 已投递 DELETE 事件", id);
    }
}
