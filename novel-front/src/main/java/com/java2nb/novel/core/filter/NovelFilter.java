package com.java2nb.novel.core.filter;

import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.utils.*;
import com.java2nb.novel.entity.WebsiteInfo;
import com.java2nb.novel.mapper.WebsiteInfoMapper;
import io.github.xxyopen.util.UUIDUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * 项目核心过滤器
 * @author Administrator
 */
public class NovelFilter implements Filter {

    private static final String DEFAULT_TEMPLATE_NAME = "green";
    private static final long WEBSITE_INFO_CACHE_SECONDS = 300L;

    /**
     * 当前已确认完整可用的模板集合。
     */
    private static final Set<String> AVAILABLE_TEMPLATE_NAMES = Set.of("green", "orange", "blue", "dark");

    /**
     * 本地图片保存路径
     * */
    private String picSavePath;

    @Override
    public void init(FilterConfig filterConfig){
        picSavePath = filterConfig.getInitParameter("picSavePath");
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        String requestUri = req.getRequestURI();

        //本地图片访问处理
        if (requestUri.contains(Constants.LOCAL_PIC_PREFIX)) {
            //缓存10天
            resp.setDateHeader("expires", System.currentTimeMillis()+60*60*24*10*1000);
            OutputStream out = resp.getOutputStream();
            InputStream input = new FileInputStream(picSavePath + requestUri);
            byte[] b = new byte[4096];
            for (int n; (n = input.read(b)) != -1; ) {
                out.write(b, 0, n);
            }
            input.close();
            out.close();
            return;

        }


        String userMark = CookieUtil.getCookie(req,Constants.USER_CLIENT_MARK_KEY);
        if(userMark == null){
            userMark = UUIDUtil.getUUID32();
            CookieUtil.setCookie(resp,Constants.USER_CLIENT_MARK_KEY,userMark);
        }
        ThreadLocalUtil.setClientId(userMark);

        CacheService cacheService = SpringUtil.getBean(CacheService.class);
        WebsiteInfo websiteInfo = resolveWebsiteInfo(cacheService);
        // 每次请求都刷新 application 级网站信息，这样后台修改站点配置后无需重启前台即可生效。
        req.getServletContext().setAttribute("website", websiteInfo);

        String deviceMode = resolveDeviceMode(req, cacheService, userMark);
        String templateName = normalizeTemplateName(websiteInfo.getTemplateName());
        ThreadLocalUtil.setTemplateName(templateName);
        ThreadLocalUtil.setTemplateDir(buildTemplateDir(deviceMode));
        resp.setHeader("X-Template-Name", templateName);

        if (isTemplateStaticRequest(requestUri) && writeTemplateStatic(req, resp, templateName, requestUri)) {
            ThreadLocalUtil.clear();
            return;
        }

        try {
            filterChain.doFilter(servletRequest,servletResponse);
        } finally {
            ThreadLocalUtil.clear();
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * 读取当前网站配置，作为模板和站点展示信息的动态来源。
     */
    private WebsiteInfo resolveWebsiteInfo(CacheService cacheService) {
        WebsiteInfo cachedWebsiteInfo = cacheService.getObject(CacheKey.WEBSITE_INFO_KEY, WebsiteInfo.class);
        if (cachedWebsiteInfo != null) {
            cachedWebsiteInfo.setTemplateName(normalizeTemplateName(cachedWebsiteInfo.getTemplateName()));
            return cachedWebsiteInfo;
        }

        WebsiteInfoMapper websiteInfoMapper = SpringUtil.getBean(WebsiteInfoMapper.class);
        WebsiteInfo websiteInfo = websiteInfoMapper.selectByPrimaryKey(1L).orElseGet(() -> {
            WebsiteInfo fallback = new WebsiteInfo();
            fallback.setName("小说精品屋");
            fallback.setLogo("/images/logo.png");
            fallback.setLogoDark("/images/logo_white.png");
            fallback.setTemplateName(DEFAULT_TEMPLATE_NAME);
            return fallback;
        });
        websiteInfo.setTemplateName(normalizeTemplateName(websiteInfo.getTemplateName()));
        // 网站信息改为短期缓存，后台清理缓存后立即失效，未清理时也能在超时后自恢复。
        cacheService.setObject(CacheKey.WEBSITE_INFO_KEY, websiteInfo, WEBSITE_INFO_CACHE_SECONDS);
        return websiteInfo;
    }

    /**
     * 归一化模板名称，避免后台误填后导致模板目录不存在。
     */
    private String normalizeTemplateName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return DEFAULT_TEMPLATE_NAME;
        }
        return AVAILABLE_TEMPLATE_NAMES.contains(templateName) ? templateName : DEFAULT_TEMPLATE_NAME;
    }

    /**
     * 解析当前访问端应该使用 PC 还是 mobile 模板，并保留用户切换偏好。
     */
    private String resolveDeviceMode(HttpServletRequest req, CacheService cacheService, String userMark) {
        String to = req.getParameter("to");
        if("pc".equals(to)){
            cacheService.set(CacheKey.TEMPLATE_DIR_KEY+userMark,"pc",60*60*24);
            return "pc";
        }
        if("mobile".equals(to)){
            cacheService.set(CacheKey.TEMPLATE_DIR_KEY+userMark,"mobile",60*60*24);
            return "mobile";
        }
        String cachedMode = cacheService.get(CacheKey.TEMPLATE_DIR_KEY+userMark);
        if ("pc".equals(cachedMode) || "mobile".equals(cachedMode)) {
            return cachedMode;
        }
        return BrowserUtil.isMobile(req) ? "mobile" : "pc";
    }

    /**
     * 组装当前请求应访问的模板视图前缀。
     */
    private String buildTemplateDir(String deviceMode) {
        if ("mobile".equals(deviceMode)) {
            return "mobile/";
        }
        return "";
    }

    /**
     * 判断当前请求是否属于模板静态资源。
     */
    private boolean isTemplateStaticRequest(String requestUri) {
        return requestUri.startsWith("/css/")
            || requestUri.startsWith("/images/")
            || requestUri.startsWith("/javascript/")
            || requestUri.startsWith("/layui/")
            || requestUri.startsWith("/mobile/")
            || requestUri.startsWith("/static/")
            || requestUri.startsWith("/fonts/")
            || "/favicon.ico".equals(requestUri)
            || "/mang.html".equals(requestUri)
            || "/mang.png".equals(requestUri)
            || "/HotBook.apk".equals(requestUri)
            || "/IMG_1470.JPG".equals(requestUri);
    }

    /**
     * 按当前模板动态输出静态资源，避免后台切换模板后仍需重启前台。
     */
    private boolean writeTemplateStatic(HttpServletRequest req, HttpServletResponse resp, String templateName, String requestUri)
        throws IOException {
        File themedFile = resolveTemplateStaticFile(templateName, requestUri);
        if (!themedFile.exists() && !DEFAULT_TEMPLATE_NAME.equals(templateName)) {
            themedFile = resolveTemplateStaticFile(DEFAULT_TEMPLATE_NAME, requestUri);
        }
        if (!themedFile.exists() || !themedFile.isFile()) {
            return false;
        }

        String mimeType = req.getServletContext().getMimeType(themedFile.getName());
        if (mimeType != null) {
            resp.setContentType(mimeType);
        }
        // 模板静态资源需要跟随当前模板即时切换，这里禁用浏览器强缓存，避免切模板后仍命中旧资源。
        resp.setHeader("X-Template-Name", templateName);
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0L);
        try (InputStream input = new FileInputStream(themedFile);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int n; (n = input.read(buffer)) != -1; ) {
                out.write(buffer, 0, n);
            }
            out.flush();
        }
        return true;
    }

    private File resolveTemplateStaticFile(String templateName, String requestUri) {
        String projectDir = ProjectDirUtil.resolveProjectDir();
        return new File(projectDir + "/templates/" + templateName + "/static" + requestUri);
    }
}
