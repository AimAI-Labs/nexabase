package io.github.aimailabs.nexabase.document.service.impl;

import io.github.aimailabs.nexabase.document.api.event.DocumentChangedEvent;
import io.github.aimailabs.nexabase.document.entity.DocContent;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.mapper.DocInfoMapper;
import io.github.aimailabs.nexabase.document.repository.DocContentRepository;
import io.github.aimailabs.nexabase.document.service.DocInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        docInfoMapper.insert(docInfo);

        DocContent docContent = new DocContent();
        docContent.setDocumentId(docInfo.getId());
        docContent.setContent(content);
        docContentRepository.save(docContent);

        // 异步投递向量化与索引同步事件
        DocumentChangedEvent event = new DocumentChangedEvent(docInfo.getId(), "CREATE", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);

        return docInfo;
    }

    @Override
    public DocInfo getById(Long id) {
        return docInfoMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        DocInfo doc = docInfoMapper.selectById(id);
        if (doc != null) {
            doc.setIsDeleted(1);
            doc.setUpdatedBy(userId);
            docInfoMapper.updateById(doc);

            // 删除 MongoDB 正文或软删除，这里简单执行物理删除以释放空间
            docContentRepository.deleteById(id);

            // 发送删除事件
            DocumentChangedEvent event = new DocumentChangedEvent(id, "DELETE", System.currentTimeMillis());
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
        }
    }
}
