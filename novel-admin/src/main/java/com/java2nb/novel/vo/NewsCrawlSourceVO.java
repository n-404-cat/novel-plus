package com.java2nb.novel.vo;

import java.io.Serializable;

/**
 * 后台新闻采集源展示对象。
 */
public class NewsCrawlSourceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceCode;

    private String sourceName;

    private String demoUrl;

    public NewsCrawlSourceVO() {
    }

    public NewsCrawlSourceVO(String sourceCode, String sourceName, String demoUrl) {
        this.sourceCode = sourceCode;
        this.sourceName = sourceName;
        this.demoUrl = demoUrl;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDemoUrl() {
        return demoUrl;
    }

    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }
}
