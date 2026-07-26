package io.github.aimailabs.nexabase.foundation.config.druid;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Druid Web 监控自动配置类 (StatViewServlet 控制台与 WebStatFilter 状态统计拦截)
 */
@AutoConfiguration
@EnableConfigurationProperties(DruidWebMonitorProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(StatViewServlet.class)
@ConditionalOnProperty(prefix = "nexabase.druid", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DruidWebMonitorAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "nexabase.druid.stat-view", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet(DruidWebMonitorProperties properties) {
        DruidWebMonitorProperties.StatView config = properties.getStatView();
        ServletRegistrationBean<StatViewServlet> registrationBean = new ServletRegistrationBean<>(
                new StatViewServlet(), config.getUrlPattern());

        if (StringUtils.hasText(config.getLoginUsername())) {
            registrationBean.addInitParameter("loginUsername", config.getLoginUsername());
        }
        if (StringUtils.hasText(config.getLoginPassword())) {
            registrationBean.addInitParameter("loginPassword", config.getLoginPassword());
        }
        if (StringUtils.hasText(config.getResetEnable())) {
            registrationBean.addInitParameter("resetEnable", config.getResetEnable());
        }
        if (StringUtils.hasText(config.getAllow())) {
            registrationBean.addInitParameter("allow", config.getAllow());
        }
        if (StringUtils.hasText(config.getDeny())) {
            registrationBean.addInitParameter("deny", config.getDeny());
        }

        return registrationBean;
    }

    @Bean
    @ConditionalOnProperty(prefix = "nexabase.druid.web-stat-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter(DruidWebMonitorProperties properties) {
        DruidWebMonitorProperties.WebStatFilter config = properties.getWebStatFilter();
        FilterRegistrationBean<WebStatFilter> registrationBean = new FilterRegistrationBean<>(new WebStatFilter());

        registrationBean.addUrlPatterns(config.getUrlPattern());
        if (StringUtils.hasText(config.getExclusions())) {
            registrationBean.addInitParameter("exclusions", config.getExclusions());
        }
        if (StringUtils.hasText(config.getProfileEnable())) {
            registrationBean.addInitParameter("profileEnable", config.getProfileEnable());
        }
        if (StringUtils.hasText(config.getSessionStatEnable())) {
            registrationBean.addInitParameter("sessionStatEnable", config.getSessionStatEnable());
        }

        return registrationBean;
    }
}
