package com.java2nb.novel.core.listener;

import com.java2nb.novel.core.config.WebsiteProperties;
import com.java2nb.novel.entity.WebsiteInfo;
import com.java2nb.novel.mapper.WebsiteInfoMapper;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;


/**
 * 启动监听器
 *
 * @author xiongxiaoyang
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StarterListener implements ServletContextInitializer {

    /**
     * 当前前台内置支持的模板名称。
     */
    private static final java.util.Set<String> SUPPORTED_TEMPLATE_NAMES =
        java.util.Set.of("green", "orange", "blue", "dark");

    private final WebsiteProperties websiteProperties;
    private final WebsiteInfoMapper websiteInfoMapper;

    @Override
    public void onStartup(ServletContext servletContext) {
        WebsiteInfo websiteInfo = websiteInfoMapper.selectByPrimaryKey(1L).orElseGet(() -> {
            WebsiteInfo fallback = new WebsiteInfo();
            fallback.setName(websiteProperties.getName());
            fallback.setDomain(websiteProperties.getDomain());
            fallback.setKeyword(websiteProperties.getKeyword());
            fallback.setDescription(websiteProperties.getDescription());
            fallback.setQq(websiteProperties.getQq());
            fallback.setLogo("/images/logo.png");
            fallback.setLogoDark("/images/logo_white.png");
            fallback.setTemplateName("green");
            return fallback;
        });

        if (websiteInfo.getTemplateName() == null
            || websiteInfo.getTemplateName().isBlank()
            || !SUPPORTED_TEMPLATE_NAMES.contains(websiteInfo.getTemplateName())) {
            // 网站配置未指定模板，或者模板名不在受支持范围内时，统一回退到 green，
            // 避免生产模式读取到不存在的模板目录导致整站模板解析失败。
            websiteInfo.setTemplateName("green");
        }

        servletContext.setAttribute("website", websiteInfo);
    }
}
