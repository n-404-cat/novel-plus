package com.java2nb.novel.controller;

import com.java2nb.novel.entity.Ebook;
import com.java2nb.novel.service.EbookService;
import io.github.xxyopen.model.page.PageBean;
import io.github.xxyopen.model.resp.RestResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 前台电子书接口。
 */
@RequestMapping("ebook")
@RestController
@RequiredArgsConstructor
public class EbookController {

    private static final long PDF_RATE_WINDOW_MILLIS = 60_000L;
    private static final int PDF_RATE_MAX_REQUESTS = 60;
    private static final long PDF_RATE_MAX_BYTES = 120L * 1024L * 1024L;
    private static final Map<String, PdfAccessCounter> PDF_ACCESS_COUNTERS = new ConcurrentHashMap<>();

    private final EbookService ebookService;

    @GetMapping("listByPage")
    public RestResult<PageBean<Ebook>> listByPage(@RequestParam(value = "curr", defaultValue = "1") int page,
        @RequestParam(value = "limit", defaultValue = "12") int pageSize,
        @RequestParam(value = "keyword", required = false) String keyword) {
        // 关键词只影响电子书列表，不放宽上架状态限制，避免下架资源被搜索出来。
        return RestResult.ok(ebookService.listByPage(page, pageSize, keyword));
    }

    @GetMapping("pdf-{ebookId}.pdf")
    public void previewPdf(@PathVariable("ebookId") Long ebookId, HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        Ebook ebook = ebookService.queryEbookInfo(ebookId);
        File pdfFile = ebookService.resolveLocalFile(ebook);
        if (ebook == null || !"pdf".equalsIgnoreCase(ebook.getFileFormat()) || pdfFile == null
            || !pdfFile.exists() || !pdfFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!isFromCurrentReadPage(ebookId, request)) {
            // PDF 原文件不允许被直接打开，必须从对应阅读页 iframe 受控加载。
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (exceedPdfRateLimit(clientIp(request), pdfFile.length())) {
            response.sendError(429, "PDF preview rate limit exceeded");
            return;
        }
        response.setContentType("application/pdf");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);
        response.setHeader("Content-Disposition", "inline");
        response.setContentLengthLong(pdfFile.length());
        try (InputStream input = new FileInputStream(pdfFile);
             OutputStream output = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            for (int length; (length = input.read(buffer)) != -1; ) {
                output.write(buffer, 0, length);
            }
            output.flush();
        }
    }

    private boolean isFromCurrentReadPage(Long ebookId, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return StringUtils.isNotBlank(referer) && referer.contains("/ebook/read-" + ebookId + ".html");
    }

    private boolean exceedPdfRateLimit(String clientIp, long fileBytes) {
        long now = System.currentTimeMillis();
        PdfAccessCounter counter = PDF_ACCESS_COUNTERS.computeIfAbsent(clientIp, key -> new PdfAccessCounter(now));
        synchronized (counter) {
            if (now - counter.windowStart > PDF_RATE_WINDOW_MILLIS) {
                counter.windowStart = now;
                counter.requests = 0;
                counter.bytes = 0L;
            }
            counter.requests++;
            counter.bytes += Math.max(fileBytes, 0L);
            return counter.requests > PDF_RATE_MAX_REQUESTS || counter.bytes > PDF_RATE_MAX_BYTES;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwardedFor)) {
            return StringUtils.substringBefore(forwardedFor, ",").trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private static class PdfAccessCounter {
        private long windowStart;
        private int requests;
        private long bytes;

        private PdfAccessCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
