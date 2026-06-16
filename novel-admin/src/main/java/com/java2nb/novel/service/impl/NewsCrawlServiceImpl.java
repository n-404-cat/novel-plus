package com.java2nb.novel.service.impl;

import com.java2nb.common.exception.BusinessException;
import com.java2nb.novel.domain.NewsDO;
import com.java2nb.novel.service.NewsCrawlService;
import com.java2nb.novel.service.NewsService;
import com.java2nb.novel.vo.NewsCrawlResultVO;
import com.java2nb.novel.vo.NewsCrawlSourceVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 后台新闻内置采集实现。
 */
@Service
public class NewsCrawlServiceImpl implements NewsCrawlService {

    private static final List<BuiltInNewsSource> BUILT_IN_SOURCES = Collections.unmodifiableList(Arrays.asList(
        new BuiltInNewsSource("generic", "通用新闻页", "https://example.com/news.html", Collections.emptyList()),
        new BuiltInNewsSource("qq_news", "腾讯新闻", "https://news.qq.com/", Collections.singletonList("qq.com")),
        new BuiltInNewsSource("sina_news", "新浪新闻", "https://news.sina.com.cn/", Collections.singletonList("sina.com.cn"))
    ));

    private static final Pattern TITLE_PATTERN = Pattern.compile("(?is)<title[^>]*>(.*?)</title>");

    private static final List<Pattern> CONTENT_PATTERNS = Arrays.asList(
        Pattern.compile("(?is)<article[^>]*>(.*?)</article>"),
        Pattern.compile("(?is)<div[^>]+id=[\"']?article[\"']?[^>]*>(.*?)</div>"),
        Pattern.compile("(?is)<div[^>]+class=[\"'][^\"']*(article|content|detail|main-content)[^\"']*[\"'][^>]*>(.*?)</div>")
    );

    private final NewsService newsService;

    private final RestTemplate restTemplate = new RestTemplate();

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
        result.setContent(extractContent(html));
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

    private String extractContent(String html) {
        String contentHtml = "";
        for (Pattern pattern : CONTENT_PATTERNS) {
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                contentHtml = matcher.group(matcher.groupCount());
                break;
            }
        }
        if (StringUtils.isBlank(contentHtml)) {
            contentHtml = html;
        }

        contentHtml = contentHtml.replaceAll("(?is)<script[^>]*>.*?</script>", "")
            .replaceAll("(?is)<style[^>]*>.*?</style>", "")
            .replaceAll("(?is)<(p|div|br|h[1-6])[^>]*>", "\n")
            .replaceAll("(?is)</(p|div|h[1-6])>", "\n")
            .replaceAll("(?is)<[^>]+>", "");

        String[] lines = StringEscapeUtils.unescapeHtml4(contentHtml).split("\\r?\\n");
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            String text = cleanPlainText(line);
            if (text.length() >= 8) {
                builder.append("<p>").append(escapeHtml(text)).append("</p>");
            }
        }
        String content = builder.toString();
        if (content.length() > 20000) {
            return content.substring(0, 20000);
        }
        return content;
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
