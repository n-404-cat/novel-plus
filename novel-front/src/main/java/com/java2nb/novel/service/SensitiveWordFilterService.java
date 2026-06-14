package com.java2nb.novel.service;

public interface SensitiveWordFilterService {
    
    /**
     * 替换小说敏感词
     */
    String filterNovelContent(String content);
    
    /**
     * 替换新闻敏感词
     */
    String filterNewsContent(String content);
}