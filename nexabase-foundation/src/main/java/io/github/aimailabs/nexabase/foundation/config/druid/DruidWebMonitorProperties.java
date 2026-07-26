package io.github.aimailabs.nexabase.foundation.config.druid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Druid Web 监控配置属性
 */
@Data
@ConfigurationProperties(prefix = "nexabase.druid")
public class DruidWebMonitorProperties {

    /**
     * 是否全局开启 Druid 监控（包含 StatViewServlet 与 WebStatFilter）
     */
    private boolean enabled = true;

    /**
     * StatViewServlet 监控控制台配置
     */
    private StatView statView = new StatView();

    /**
     * WebStatFilter 请求拦截统计配置
     */
    private WebStatFilter webStatFilter = new WebStatFilter();

    @Data
    public static class StatView {
        /**
         * 是否开启 StatViewServlet
         */
        private boolean enabled = true;

        /**
         * 监控页面 URL 映射路径
         */
        private String urlPattern = "/druid/*";

        /**
         * 登录控制台的账号
         */
        private String loginUsername = "admin";

        /**
         * 登录控制台的密码
         */
        private String loginPassword = "nexabase.druid.2026";

        /**
         * 是否允许重置数据（建议生产环境关闭）
         */
        private String resetEnable = "false";

        /**
         * IP 允许访问列表（逗号分隔，为空则允许所有）
         */
        private String allow = "";

        /**
         * IP 拒绝访问列表（逗号分隔）
         */
        private String deny = "";
    }

    @Data
    public static class WebStatFilter {
        /**
         * 是否开启 WebStatFilter
         */
        private boolean enabled = true;

        /**
         * 拦截路径
         */
        private String urlPattern = "/*";

        /**
         * 排除路径（多个以逗号分隔）
         */
        private String exclusions = "*.js,*.css,*.svg,*.png,*.ico,/druid/*";

        /**
         * 是否启用单 URL 监控 Profiling
         */
        private String profileEnable = "true";

        /**
         * 是否启用 Session 统计
         */
        private String sessionStatEnable = "true";
    }
}
