package com.java2nb.novel.service.impl;

import com.github.pagehelper.PageHelper;
import com.java2nb.novel.core.utils.Constants;
import com.java2nb.novel.entity.Ebook;
import com.java2nb.novel.mapper.FrontEbookMapper;
import com.java2nb.novel.service.EbookService;
import com.java2nb.novel.vo.EbookEpubChapterVO;
import com.java2nb.novel.vo.EbookEpubReadResult;
import com.java2nb.novel.vo.EbookTxtReadResult;
import io.github.xxyopen.model.page.PageBean;
import io.github.xxyopen.model.page.builder.pagehelper.PageBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 前台电子书服务实现。
 */
@Service
@RequiredArgsConstructor
public class EbookServiceImpl implements EbookService {

    private static final int TXT_PAGE_CHARS = 12000;

    private static final Set<String> SUPPORT_FORMATS = Set.of("pdf", "txt", "epub", "azw3", "mobi");

    private static final Pattern BODY_PATTERN = Pattern.compile("(?is)<body[^>]*>(.*)</body>");

    private static final Pattern TITLE_PATTERN = Pattern.compile("(?is)<(?:title|h1|h2)[^>]*>(.*?)</(?:title|h1|h2)>");

    private static final Pattern IMAGE_SRC_PATTERN = Pattern.compile("(?is)<img([^>]*?)\\s+src=[\"']([^\"']+)[\"']([^>]*)>");

    private static final Pattern EPUB_IMAGE_REF_PATTERN = Pattern.compile("(?i)(\\s(?:src|href|xlink:href)=[\"'])([^\"']+)([\"'])");

    private final FrontEbookMapper ebookMapper;

    @Value("${pic.save.path:}")
    private String picSavePath;

    @Override
    public PageBean<Ebook> listByPage(int page, int pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        // 前台检索只查询已上架数据，关键词为空时保持原有列表逻辑。
        List<Ebook> ebooks = ebookMapper.listDisplayable(StringUtils.trimToNull(keyword));
        ebooks.forEach(this::normalizeReadFormat);
        return PageBuilder.build(ebooks);
    }

    @Override
    public Ebook queryEbookInfo(Long ebookId) {
        Ebook ebook = ebookMapper.getActiveById(ebookId);
        normalizeReadFormat(ebook);
        return ebook;
    }

    @Override
    public EbookTxtReadResult readTxtContent(Ebook ebook, int pageNo) {
        if (ebook == null || !"txt".equalsIgnoreCase(ebook.getFileFormat())) {
            return new EbookTxtReadResult("", 1, false, false);
        }
        Path txtPath = resolveLocalFilePath(ebook.getFileUrl());
        if (txtPath == null || !Files.exists(txtPath)) {
            return new EbookTxtReadResult("TXT 文件不存在，请联系管理员检查文件同步。", 1, false, false);
        }
        int safePageNo = Math.max(pageNo, 1);
        try (BufferedReader reader = Files.newBufferedReader(txtPath, StandardCharsets.UTF_8)) {
            return readTxtPage(reader, safePageNo);
        } catch (IOException utf8Exception) {
            try (BufferedReader reader = Files.newBufferedReader(txtPath, Charset.forName("GBK"))) {
                // TXT 文件编码来源不可控，UTF-8 失败后回退 GBK，兼容常见中文电子书。
                return readTxtPage(reader, safePageNo);
            } catch (IOException gbkException) {
                return new EbookTxtReadResult("TXT 文件读取失败，请联系管理员检查文件编码。", 1, false, false);
            }
        }
    }

    @Override
    public EbookEpubReadResult readEpubContent(Ebook ebook, int chapterIndex) {
        EbookEpubReadResult result = new EbookEpubReadResult();
        if (ebook == null || !"epub".equalsIgnoreCase(ebook.getFileFormat())) {
            result.setContent("");
            return result;
        }
        Path epubPath = resolveLocalFilePath(ebook.getFileUrl());
        if (epubPath == null || !Files.exists(epubPath)) {
            result.setErrorMessage("EPUB 文件不存在，请联系管理员检查文件同步。");
            return result;
        }
        try (ZipFile zipFile = new ZipFile(epubPath.toFile(), StandardCharsets.UTF_8)) {
            String opfPath = findOpfPath(zipFile);
            if (StringUtils.isBlank(opfPath)) {
                result.setErrorMessage("EPUB 目录文件缺失，无法在线解析。");
                return result;
            }
            List<String> chapterPaths = findEpubChapterPaths(zipFile, opfPath);
            if (chapterPaths.isEmpty()) {
                result.setErrorMessage("EPUB 未找到可阅读章节。");
                return result;
            }
            int safeChapterIndex = Math.min(Math.max(chapterIndex, 0), chapterPaths.size() - 1);
            List<EbookEpubChapterVO> chapters = new ArrayList<>();
            for (int i = 0; i < chapterPaths.size(); i++) {
                String html = readZipEntry(zipFile, chapterPaths.get(i));
                chapters.add(new EbookEpubChapterVO(i, extractChapterTitle(html, i + 1), i == safeChapterIndex));
            }
            // EPUB 是 zip 包，浏览器无法直接读取内部章节；这里在服务端解析 spine 并输出安全 HTML。
            String chapterPath = chapterPaths.get(safeChapterIndex);
            result.setChapters(chapters);
            result.setChapterIndex(safeChapterIndex);
            result.setContent(sanitizeEpubHtml(zipFile, chapterPath, readZipEntry(zipFile, chapterPath)));
            return result;
        } catch (Exception e) {
            result.setErrorMessage("EPUB 在线解析失败，请联系管理员检查文件或等待转换服务处理。");
            return result;
        }
    }

    @Override
    public File resolveLocalFile(Ebook ebook) {
        if (ebook == null) {
            return null;
        }
        Path path = resolveLocalFilePath(ebook.getFileUrl());
        if (path == null) {
            return null;
        }
        try {
            File baseDir = new File(picSavePath).getCanonicalFile();
            File file = path.toFile().getCanonicalFile();
            // 电子书文件路径来自数据库，仍然做一次根目录校验，避免脏数据绕出上传目录。
            if (!file.toPath().startsWith(baseDir.toPath())) {
                return null;
            }
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    private EbookTxtReadResult readTxtPage(BufferedReader reader, int pageNo) throws IOException {
        long skipChars = (long) (pageNo - 1) * TXT_PAGE_CHARS;
        while (skipChars > 0) {
            long skipped = reader.skip(skipChars);
            if (skipped <= 0) {
                break;
            }
            skipChars -= skipped;
        }
        char[] buffer = new char[TXT_PAGE_CHARS + 1];
        int length = reader.read(buffer);
        if (length <= 0) {
            return new EbookTxtReadResult("当前页暂无内容。", pageNo, pageNo > 1, false);
        }
        boolean hasNext = length > TXT_PAGE_CHARS;
        int contentLength = Math.min(length, TXT_PAGE_CHARS);
        return new EbookTxtReadResult(toHtml(new String(buffer, 0, contentLength)), pageNo, pageNo > 1, hasNext);
    }

    private Path resolveLocalFilePath(String fileUrl) {
        if (StringUtils.isBlank(fileUrl) || StringUtils.isBlank(picSavePath)) {
            return null;
        }
        if (fileUrl.startsWith(Constants.LOCAL_PIC_PREFIX)) {
            return Path.of(picSavePath + fileUrl);
        }
        if (fileUrl.startsWith("/files/")) {
            return Path.of(picSavePath + fileUrl.replaceFirst("/files/", ""));
        }
        return null;
    }

    private void normalizeReadFormat(Ebook ebook) {
        if (ebook == null) {
            return;
        }
        String urlFormat = extractFileFormat(ebook.getFileUrl());
        if (SUPPORT_FORMATS.contains(urlFormat)) {
            // 历史数据可能把 PDF 等文件误存成 MOBI；阅读器选择以实际文件后缀兜底纠正。
            ebook.setFileFormat(urlFormat);
            return;
        }
        if (StringUtils.isNotBlank(ebook.getFileFormat())) {
            ebook.setFileFormat(ebook.getFileFormat().trim().toLowerCase(Locale.ROOT));
        }
    }

    private String extractFileFormat(String fileUrl) {
        if (StringUtils.isBlank(fileUrl) || !fileUrl.contains(".")) {
            return "";
        }
        return StringUtils.substringAfterLast(fileUrl, ".").toLowerCase(Locale.ROOT);
    }

    private String findOpfPath(ZipFile zipFile) throws Exception {
        String containerXml = readZipEntry(zipFile, "META-INF/container.xml");
        Document document = parseXml(containerXml);
        NodeList rootFiles = document.getElementsByTagNameNS("*", "rootfile");
        if (rootFiles.getLength() == 0) {
            return "";
        }
        return ((Element) rootFiles.item(0)).getAttribute("full-path");
    }

    private List<String> findEpubChapterPaths(ZipFile zipFile, String opfPath) throws Exception {
        Document document = parseXml(readZipEntry(zipFile, opfPath));
        String baseDir = parentZipDir(opfPath);
        Map<String, String> manifest = new HashMap<>();
        NodeList items = document.getElementsByTagNameNS("*", "item");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String id = item.getAttribute("id");
            String href = item.getAttribute("href");
            if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(href)) {
                manifest.put(id, resolveZipPath(baseDir, href));
            }
        }
        List<String> chapterPaths = new ArrayList<>();
        NodeList itemRefs = document.getElementsByTagNameNS("*", "itemref");
        for (int i = 0; i < itemRefs.getLength(); i++) {
            String idRef = ((Element) itemRefs.item(i)).getAttribute("idref");
            String path = manifest.get(idRef);
            if (StringUtils.isNotBlank(path) && zipFile.getEntry(path) != null) {
                chapterPaths.add(path);
            }
        }
        return chapterPaths;
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // 禁用外部实体，避免解析用户上传 EPUB 时触发 XXE 风险。
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private String readZipEntry(ZipFile zipFile, String entryName) throws IOException {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) {
            return "";
        }
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String resolveZipPath(String baseDir, String href) {
        String cleanHref = StringUtils.substringBefore(href, "#");
        if (cleanHref.startsWith("/")) {
            cleanHref = cleanHref.substring(1);
        }
        Path path = StringUtils.isBlank(baseDir) ? Path.of(cleanHref) : Path.of(baseDir).resolve(cleanHref);
        return path.normalize().toString().replace("\\", "/");
    }

    private String extractChapterTitle(String html, int index) {
        Matcher matcher = TITLE_PATTERN.matcher(html == null ? "" : html);
        if (matcher.find()) {
            String title = StringEscapeUtils.unescapeHtml4(matcher.group(1).replaceAll("(?is)<[^>]+>", "")).trim();
            if (StringUtils.isNotBlank(title)) {
                return title;
            }
        }
        return "第 " + index + " 章";
    }

    private String sanitizeEpubHtml(ZipFile zipFile, String chapterPath, String html) {
        String body = html == null ? "" : html;
        Matcher bodyMatcher = BODY_PATTERN.matcher(body);
        if (bodyMatcher.find()) {
            body = bodyMatcher.group(1);
        }
        body = inlineEpubImages(zipFile, chapterPath, body);
        body = body.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        body = body.replaceAll("(?is)<style[^>]*>.*?</style>", "");
        body = body.replaceAll("(?is)<(?:iframe|object|embed)[^>]*>.*?</(?:iframe|object|embed)>", "");
        body = body.replaceAll("(?i)\\s+on\\w+\\s*=\\s*(['\"]).*?\\1", "");
        body = body.replaceAll("(?i)javascript:", "");
        return body;
    }

    private String inlineEpubImages(ZipFile zipFile, String chapterPath, String html) {
        String htmlWithImg = inlineHtmlImgTags(zipFile, chapterPath, html);
        return inlineSvgImageRefs(zipFile, chapterPath, htmlWithImg);
    }

    private String inlineHtmlImgTags(ZipFile zipFile, String chapterPath, String html) {
        Matcher matcher = IMAGE_SRC_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        String chapterDir = parentZipDir(chapterPath);
        while (matcher.find()) {
            String src = matcher.group(2);
            if (StringUtils.startsWithAny(src, "http://", "https://", "data:", "#")) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            String imagePath = resolveZipPath(chapterDir, src);
            ZipEntry imageEntry = zipFile.getEntry(imagePath);
            if (imageEntry == null) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            try (InputStream inputStream = zipFile.getInputStream(imageEntry)) {
                String mimeType = guessImageMimeType(imagePath);
                String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(inputStream.readAllBytes());
                String replacement = "<img" + matcher.group(1) + " src=\"" + dataUrl + "\"" + matcher.group(3) + ">";
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
            } catch (IOException e) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String inlineSvgImageRefs(ZipFile zipFile, String chapterPath, String html) {
        Matcher matcher = EPUB_IMAGE_REF_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        String chapterDir = parentZipDir(chapterPath);
        while (matcher.find()) {
            String ref = matcher.group(2);
            if (StringUtils.startsWithAny(ref, "http://", "https://", "data:", "#") || !isImagePath(ref)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            String imagePath = resolveZipPath(chapterDir, ref);
            ZipEntry imageEntry = zipFile.getEntry(imagePath);
            if (imageEntry == null) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            try (InputStream inputStream = zipFile.getInputStream(imageEntry)) {
                // EPUB 里的 SVG/image 常引用包内图片，转成 data URL 才能在站内页面直接显示。
                String dataUrl = "data:" + guessImageMimeType(imagePath) + ";base64,"
                    + Base64.getEncoder().encodeToString(inputStream.readAllBytes());
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(1) + dataUrl + matcher.group(3)));
            } catch (IOException e) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private boolean isImagePath(String path) {
        String ext = extractFileFormat(path);
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "gif".equals(ext)
            || "webp".equals(ext) || "svg".equals(ext);
    }

    private String guessImageMimeType(String path) {
        String ext = extractFileFormat(path);
        if ("png".equals(ext)) {
            return "image/png";
        }
        if ("gif".equals(ext)) {
            return "image/gif";
        }
        if ("webp".equals(ext)) {
            return "image/webp";
        }
        if ("svg".equals(ext)) {
            return "image/svg+xml";
        }
        return "image/jpeg";
    }

    private String parentZipDir(String entryPath) {
        if (StringUtils.isBlank(entryPath) || !entryPath.contains("/")) {
            return "";
        }
        return StringUtils.substringBeforeLast(entryPath, "/");
    }

    private String toHtml(String text) {
        String escaped = StringEscapeUtils.escapeHtml4(text == null ? "" : text);
        return escaped.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>");
    }
}
