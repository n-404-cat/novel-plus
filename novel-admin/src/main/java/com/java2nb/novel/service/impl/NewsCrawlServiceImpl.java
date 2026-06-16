package com.java2nb.novel.service.impl;

import com.java2nb.common.exception.BusinessException;
import com.java2nb.novel.domain.NewsDO;
import com.java2nb.novel.service.NewsCrawlService;
import com.java2nb.novel.service.NewsService;
import com.java2nb.novel.vo.NewsCrawlResultVO;
import com.java2nb.novel.vo.NewsCrawlSourceVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 后台新闻内置采集实现。
 */
@Service
public class NewsCrawlServiceImpl implements NewsCrawlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsCrawlServiceImpl.class);

    private static final String FILE_VISIT_PREFIX = "/files/";

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    private static final List<BuiltInNewsSource> BUILT_IN_SOURCES = Collections.unmodifiableList(Arrays.asList(
        new BuiltInNewsSource("generic", "通用新闻页", "https://example.com/news.html", Collections.emptyList()),
        new BuiltInNewsSource("qq_news", "腾讯新闻", "https://news.qq.com/", Collections.singletonList("qq.com")),
        new BuiltInNewsSource("sina_news", "新浪新闻", "https://news.sina.com.cn/", Collections.singletonList("sina.com.cn"))
    ));

    private static final Pattern TITLE_PATTERN = Pattern.compile("(?is)<title[^>]*>(.*?)</title>");

    private static final Pattern TENCENT_DATA_PATTERN = Pattern.compile("(?is)window\\.DATA\\s*=\\s*(\\{.*?\\})\\s*;\\s*</script>");

    private static final Pattern TENCENT_IMAGE_PLACEHOLDER_PATTERN = Pattern.compile("<!--\\s*IMG_(\\d+)\\s*-->");

    private static final Pattern UNSAFE_TAG_PATTERN = Pattern.compile(
        "(?is)<(?!/?(?:p|div|br|h[1-6]|strong|b|em|i|u|blockquote|ul|ol|li|a|img|table|thead|tbody|tr|th|td)\\b)[^>]+>");

    private static final List<Pattern> CONTENT_PATTERNS = Arrays.asList(
        Pattern.compile("(?is)<article[^>]*>(.*?)</article>"),
        Pattern.compile("(?is)<div[^>]+id=[\"']?article[\"']?[^>]*>(.*?)</div>"),
        Pattern.compile("(?is)<div[^>]+class=[\"'][^\"']*(article|content|detail|main-content)[^\"']*[\"'][^>]*>(.*?)</div>")
    );

    private final NewsService newsService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${java2nb.uploadPath}")
    private String uploadPath;

    public NewsCrawlServiceImpl(NewsService newsService) {
        this.newsService = newsService;
        this.restTemplate.getMessageConverters().stream()
            .filter(StringHttpMessageConverter.class::isInstance)
            .map(StringHttpMessageConverter.class::cast)
            .findFirst()
            .ifPresent(converter -> converter.setDefaultCharset(StandardCharsets.UTF_8));
    }

    @Override
    public List<NewsCrawlSourceVO> listBuiltInSources() {
        return BUILT_IN_SOURCES.stream()
            .map(source -> new NewsCrawlSourceVO(source.sourceCode, source.sourceName, source.demoUrl))
            .toList();
    }

    @Override
    public NewsCrawlResultVO preview(String sourceCode, String url) {
        BuiltInNewsSource source = getSource(sourceCode);
        checkUrl(source, url);
        String html = fetchHtml(url);
        if (StringUtils.isBlank(html)) {
            throw new BusinessException(500, "采集失败：目标页面无内容或访问超时");
        }

        NewsCrawlResultVO result = new NewsCrawlResultVO();
        result.setSourceCode(source.sourceCode);
        result.setSourceName(source.sourceName);
        result.setSourceUrl(url);
        result.setTitle(extractTitle(html));
        result.setContent(extractContent(html, url));
        if (StringUtils.isBlank(result.getTitle()) || StringUtils.isBlank(result.getContent())) {
            throw new BusinessException(500, "采集失败：未解析到标题或正文，请换一个新闻详情页地址");
        }
        return result;
    }

    @Override
    public NewsCrawlResultVO save(String sourceCode, String url, Integer catId, String catName, Integer status) {
        NewsCrawlResultVO result = preview(sourceCode, url);
        NewsDO news = new NewsDO();
        news.setCatId(catId);
        news.setCatName(catName);
        news.setSourceName(result.getSourceName());
        news.setTitle(result.getTitle());
        news.setContent(result.getContent());
        news.setStatus(status == null ? 1 : status);
        newsService.save(news);
        result.setNewsId(news.getId());
        return result;
    }

    private BuiltInNewsSource getSource(String sourceCode) {
        return BUILT_IN_SOURCES.stream()
            .filter(source -> source.sourceCode.equals(sourceCode))
            .findFirst()
            .orElseThrow(() -> new BusinessException(500, "请选择有效的内置采集源"));
    }

    private String fetchHtml(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            // 使用浏览器 UA 减少普通新闻站点直接拒绝默认 Java 客户端的概率。
            headers.add("user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new BusinessException(500, "采集失败：目标页面访问失败或超时");
        }
    }

    private void checkUrl(BuiltInNewsSource source, String url) {
        if (StringUtils.isBlank(url)) {
            throw new BusinessException(500, "请输入新闻详情页地址");
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(500, "新闻详情页地址格式不正确");
        }
        String scheme = StringUtils.defaultString(uri.getScheme()).toLowerCase(Locale.ROOT);
        String host = StringUtils.defaultString(uri.getHost()).toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new BusinessException(500, "新闻详情页地址只支持 http 或 https");
        }
        if (StringUtils.isBlank(host)) {
            throw new BusinessException(500, "新闻详情页地址缺少域名");
        }
        // 内置源带域名白名单时必须匹配，避免后台采集接口被当成任意 URL 代理使用。
        if (!source.allowedHosts.isEmpty() && source.allowedHosts.stream().noneMatch(host::endsWith)) {
            throw new BusinessException(500, "当前地址不属于所选内置采集源");
        }
    }

    private String extractTitle(String html) {
        Matcher matcher = TITLE_PATTERN.matcher(html);
        if (matcher.find()) {
            return cleanPlainText(matcher.group(1));
        }
        return "";
    }

    private String extractContent(String html, String pageUrl) {
        String contentHtml = extractTencentOriginContent(html);
        for (Pattern pattern : CONTENT_PATTERNS) {
            if (StringUtils.isNotBlank(contentHtml)) {
                break;
            }
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                contentHtml = matcher.group(matcher.groupCount());
                break;
            }
        }
        if (StringUtils.isBlank(contentHtml)) {
            contentHtml = html;
        }

        String content = sanitizeContentHtml(contentHtml, pageUrl);
        if (content.length() > 20000) {
            return content.substring(0, 20000);
        }
        return content;
    }

    private String extractTencentOriginContent(String html) {
        Matcher matcher = TENCENT_DATA_PATTERN.matcher(html);
        if (!matcher.find()) {
            return "";
        }
        try {
            JSONObject data = JSON.parseObject(matcher.group(1));
            JSONObject originContent = data.getJSONObject("originContent");
            if (originContent == null) {
                return "";
            }
            String text = originContent.getString("text");
            if (StringUtils.isBlank(text)) {
                return "";
            }
            // 腾讯新闻正文里图片不是真实 img 标签，而是 IMG_0 这类注释占位符，真实地址在 originAttribute。
            return replaceTencentImagePlaceholders(text, data.getJSONObject("originAttribute"));
        } catch (Exception e) {
            LOGGER.warn("腾讯新闻 window.DATA 解析失败，回退通用 HTML 解析", e);
            return "";
        }
    }

    private String replaceTencentImagePlaceholders(String html, JSONObject originAttribute) {
        if (originAttribute == null) {
            return html;
        }
        Matcher matcher = TENCENT_IMAGE_PLACEHOLDER_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            JSONObject image = originAttribute.getJSONObject("IMG_" + matcher.group(1));
            String imageUrl = "";
            String imageDesc = "";
            if (image != null) {
                imageUrl = StringUtils.defaultIfBlank(image.getString("url"),
                    StringUtils.defaultIfBlank(image.getString("origUrl"), image.getString("imgurl0")));
                imageDesc = StringUtils.defaultString(image.getString("desc"));
            }
            String replacement = StringUtils.isBlank(imageUrl) ? "" :
                "<img src=\"" + escapeHtml(imageUrl) + "\" alt=\"" + escapeHtml(imageDesc) + "\"/>";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String sanitizeContentHtml(String contentHtml, String pageUrl) {
        // 新闻正文要尽量保持原文结构，但后台采集属于非可信输入，必须先移除脚本和危险标签。
        String html = contentHtml.replaceAll("(?is)<!DOCTYPE[^>]*>", "")
            .replaceAll("(?is)<!--.*?-->", "")
            .replaceAll("(?is)<(script|style|iframe|object|embed|form|input|button|textarea|select|link|meta)[^>]*>.*?</\\1>",
                "")
            .replaceAll("(?is)<(script|style|iframe|object|embed|form|input|button|textarea|select|link|meta)[^>]*/?>",
                "");

        html = replaceImagesWithLocalFiles(html, pageUrl);
        html = sanitizeOpeningTags(html);
        html = sanitizeClosingTags(html);
        // 只移除非白名单标签，不能再用 <[^>]+> 全量清理，否则图片和段落会被二次删除。
        html = UNSAFE_TAG_PATTERN.matcher(html).replaceAll("");
        html = html.replaceAll("(?i)<br\\s*/?>\\s*(<br\\s*/?>\\s*)+", "<br/>");
        return html.trim();
    }

    private String replaceImagesWithLocalFiles(String html, String pageUrl) {
        Pattern imagePattern = Pattern.compile("(?is)<img\\b([^>]*)>");
        Matcher matcher = imagePattern.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String src = extractAttribute(matcher.group(1), "src");
            String alt = cleanPlainText(extractAttribute(matcher.group(1), "alt"));
            String resolvedSrc = resolveUrl(pageUrl, src);
            String localSrc = downloadImage(resolvedSrc);
            String replacement = "";
            String imageSrc = StringUtils.defaultIfBlank(localSrc, resolvedSrc);
            if (StringUtils.isNotBlank(imageSrc)) {
                // 保留图片本身即可，避免源站已有段落包裹时生成嵌套 p，导致富文本编辑器解析异常。
                replacement = "<img src=\"" + escapeHtml(imageSrc) + "\" alt=\"" + escapeHtml(alt)
                    + "\" style=\"max-width:100%;height:auto;\"/>";
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String sanitizeOpeningTags(String html) {
        Pattern tagPattern = Pattern.compile("(?is)<(p|div|br|h[1-6]|strong|b|em|i|u|blockquote|ul|ol|li|a|img|table|thead|tbody|tr|th|td)\\b([^>]*)>");
        Matcher matcher = tagPattern.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String tagName = matcher.group(1).toLowerCase(Locale.ROOT);
            String attributes = sanitizeAttributes(tagName, matcher.group(2));
            String replacement = ("br".equals(tagName) || "img".equals(tagName)) ? "<" + tagName + attributes + "/>" :
                "<" + tagName + attributes + ">";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String sanitizeClosingTags(String html) {
        Pattern closePattern = Pattern.compile("(?is)</(p|div|h[1-6]|strong|b|em|i|u|blockquote|ul|ol|li|a|table|thead|tbody|tr|th|td)>");
        Matcher matcher = closePattern.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("</" + matcher.group(1).toLowerCase(Locale.ROOT) + ">"));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String sanitizeAttributes(String tagName, String rawAttributes) {
        if ("img".equals(tagName)) {
            String src = extractAttribute(rawAttributes, "src");
            if (!isSafeImageSrc(src)) {
                return "";
            }
            String alt = cleanPlainText(extractAttribute(rawAttributes, "alt"));
            return " src=\"" + escapeHtml(src) + "\" alt=\"" + escapeHtml(alt)
                + "\" style=\"max-width:100%;height:auto;\"";
        }
        if (!"a".equals(tagName)) {
            return "";
        }
        String href = extractAttribute(rawAttributes, "href");
        if (StringUtils.isBlank(href)) {
            return "";
        }
        String normalized = href.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("javascript:") || normalized.startsWith("data:")) {
            return "";
        }
        return " href=\"" + escapeHtml(href.trim()) + "\" target=\"_blank\" rel=\"nofollow noopener\"";
    }

    private boolean isSafeImageSrc(String src) {
        if (StringUtils.startsWith(src, FILE_VISIT_PREFIX)) {
            return true;
        }
        String normalized = StringUtils.defaultString(src).trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private String extractAttribute(String attributes, String name) {
        if (StringUtils.isBlank(attributes)) {
            return "";
        }
        Pattern attrPattern = Pattern.compile("(?is)\\b" + Pattern.quote(name)
            + "\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))");
        Matcher matcher = attrPattern.matcher(attributes);
        if (!matcher.find()) {
            return "";
        }
        for (int i = 2; i <= 4; i++) {
            if (matcher.group(i) != null) {
                return StringEscapeUtils.unescapeHtml4(matcher.group(i));
            }
        }
        return "";
    }

    private String resolveUrl(String pageUrl, String imageUrl) {
        if (StringUtils.isBlank(imageUrl)) {
            return "";
        }
        try {
            return URI.create(pageUrl).resolve(imageUrl.trim()).toString();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private String downloadImage(String imageUrl) {
        if (StringUtils.isBlank(imageUrl)) {
            return "";
        }
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET,
                new HttpEntity<>(buildBrowserHeaders()), byte[].class);
            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                return "";
            }
            String extension = normalizeImageExtension(imageUrl,
                response.getHeaders().getContentType() == null ? "" : response.getHeaders().getContentType().toString());
            Date currentDate = new Date();
            String relativeDir = String.format("%tY/%tm/%td", currentDate, currentDate, currentDate);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            File saveFile = new File(normalizeUploadPath(), relativeDir + File.separator + fileName);
            File parentFile = saveFile.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                LOGGER.warn("新闻图片采集目录创建失败: {}", parentFile.getAbsolutePath());
                return "";
            }
            Files.copy(new java.io.ByteArrayInputStream(body), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (!isValidDownloadedImage(saveFile, extension)) {
                Files.deleteIfExists(saveFile.toPath());
                return "";
            }
            return FILE_VISIT_PREFIX + relativeDir.replace(File.separatorChar, '/') + "/" + fileName;
        } catch (Exception e) {
            // 单张图片失败不能影响整篇新闻采集，避免源站防盗链或临时 403 导致正文全部失败。
            LOGGER.warn("新闻图片采集失败: {}", imageUrl, e);
            return "";
        }
    }

    private HttpHeaders buildBrowserHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36");
        headers.add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        return headers;
    }

    private String normalizeUploadPath() throws IOException {
        if (StringUtils.isBlank(uploadPath)) {
            throw new IOException("上传目录未配置");
        }
        return uploadPath.endsWith(File.separator) ? uploadPath : uploadPath + File.separator;
    }

    private boolean isValidDownloadedImage(File saveFile, String extension) throws IOException {
        if ("webp".equals(extension)) {
            byte[] header = Files.readAllBytes(saveFile.toPath());
            return header.length > 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
        }
        return ImageIO.read(saveFile) != null;
    }

    private String normalizeImageExtension(String imageUrl, String contentType) {
        String extensionByContentType = imageExtensionFromContentType(contentType);
        if (StringUtils.isNotBlank(extensionByContentType)) {
            return extensionByContentType;
        }
        String extension = FilenameUtils.getExtension(URI.create(imageUrl).getPath());
        extension = StringUtils.defaultIfBlank(extension, "jpg").toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return "jpg";
        }
        return extension;
    }

    private String imageExtensionFromContentType(String contentType) {
        String normalized = StringUtils.defaultString(contentType).toLowerCase(Locale.ROOT);
        if (normalized.contains("image/jpeg")) {
            return "jpg";
        }
        if (normalized.contains("image/png")) {
            return "png";
        }
        if (normalized.contains("image/gif")) {
            return "gif";
        }
        if (normalized.contains("image/webp")) {
            return "webp";
        }
        return "";
    }

    private String cleanPlainText(String text) {
        return StringEscapeUtils.unescapeHtml4(StringUtils.defaultString(text))
            .replaceAll("[\\u00a0\\s]+", " ")
            .trim();
    }

    private String escapeHtml(String text) {
        return StringEscapeUtils.escapeHtml4(text);
    }

    private static class BuiltInNewsSource {

        private final String sourceCode;

        private final String sourceName;

        private final String demoUrl;

        private final List<String> allowedHosts;

        private BuiltInNewsSource(String sourceCode, String sourceName, String demoUrl, List<String> allowedHosts) {
            this.sourceCode = sourceCode;
            this.sourceName = sourceName;
            this.demoUrl = demoUrl;
            this.allowedHosts = allowedHosts;
        }
    }
}
