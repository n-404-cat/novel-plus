package com.java2nb.novel.core.config;

import com.java2nb.novel.core.utils.ThreadLocalUtil;
import com.java2nb.novel.core.utils.ProjectDirUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring6.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.Set;

/**
 * 动态模板解析配置。
 *
 * <p>作用：</p>
 * <p>根据当前请求线程里的模板名称，动态切换到对应模板目录，
 * 让后台修改模板名后，前台无需重启即可按新模板渲染页面。</p>
 */
@Configuration
public class DynamicTemplateResolverConfig {

    private static final String DEFAULT_TEMPLATE_NAME = "green";
    private static final Set<String> AVAILABLE_TEMPLATE_NAMES = Set.of("green", "orange", "blue", "dark");

    @Bean
    public SpringResourceTemplateResolver dynamicTemplateResolver(ApplicationContext applicationContext) {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver() {
            @Override
            protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate,
                String template, String resourceName, String characterEncoding, java.util.Map<String, Object> templateResolutionAttributes) {
                String templateName = normalizeTemplateName(ThreadLocalUtil.getTemplateName());
                String dynamicResourceName = "file:" + ProjectDirUtil.resolveProjectDir()
                    + "/templates/" + templateName + "/html/" + resourceName;
                return new SpringResourceTemplateResource(applicationContext, dynamicResourceName, characterEncoding);
            }
        };
        resolver.setApplicationContext(applicationContext);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        resolver.setCheckExistence(true);
        resolver.setOrder(1);
        return resolver;
    }

    private static String normalizeTemplateName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return DEFAULT_TEMPLATE_NAME;
        }
        return AVAILABLE_TEMPLATE_NAMES.contains(templateName) ? templateName : DEFAULT_TEMPLATE_NAME;
    }
}
