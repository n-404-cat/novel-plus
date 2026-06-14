package com.java2nb.novel.service.impl;

import com.java2nb.novel.entity.SensitiveWord;
import com.java2nb.novel.entity.WebsiteInfo;
import com.java2nb.novel.mapper.SensitiveWordMapper;
import com.java2nb.novel.mapper.WebsiteInfoMapper;
import com.java2nb.novel.service.SensitiveWordFilterService;
import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SensitiveWordFilterServiceImpl implements SensitiveWordFilterService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private WebsiteInfoMapper websiteInfoMapper;

    @Autowired
    private CacheService cacheService;

    private volatile Map<Object, Object> dfaMap = new HashMap<>();

    private WebsiteInfo getWebsiteInfo() {
        WebsiteInfo websiteInfo = cacheService.getObject(CacheKey.WEBSITE_INFO_KEY, WebsiteInfo.class);
        if (websiteInfo != null) {
            return websiteInfo;
        }
        websiteInfo = websiteInfoMapper.selectByPrimaryKey(1L).orElseGet(WebsiteInfo::new);
        return websiteInfo;
    }

    @Override
    public String filterNovelContent(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        WebsiteInfo websiteInfo = getWebsiteInfo();
        if (websiteInfo == null || websiteInfo.getNovelSensitiveWordEnabled() == null || websiteInfo.getNovelSensitiveWordEnabled() == 0) {
            return content;
        }
        return replaceSensitiveWord(content);
    }

    @Override
    public String filterNewsContent(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        WebsiteInfo websiteInfo = getWebsiteInfo();
        if (websiteInfo == null || websiteInfo.getNewsSensitiveWordEnabled() == null || websiteInfo.getNewsSensitiveWordEnabled() == 0) {
            return content;
        }
        return replaceSensitiveWord(content);
    }

    private String replaceSensitiveWord(String content) {
        initDfaMapIfNecessary();
        Map<Object, Object> currentMap = dfaMap;
        if (currentMap == null || currentMap.isEmpty()) {
            return content;
        }
        StringBuilder result = new StringBuilder(content.length());
        int length = content.length();
        for (int i = 0; i < length; i++) {
            int matchLength = checkSensitiveWord(content, i, currentMap);
            if (matchLength > 0) {
                for (int j = 0; j < matchLength; j++) {
                    result.append('*');
                }
                i = i + matchLength - 1;
            } else {
                result.append(content.charAt(i));
            }
        }
        return result.toString();
    }

    private int checkSensitiveWord(String text, int beginIndex, Map<Object, Object> map) {
        boolean matchFlag = false;
        int matchLength = 0;
        Map<Object, Object> nowMap = map;
        for (int i = beginIndex; i < text.length(); i++) {
            char word = text.charAt(i);
            nowMap = (Map<Object, Object>) nowMap.get(word);
            if (nowMap != null) {
                matchLength++;
                if ("1".equals(nowMap.get("isEnd"))) {
                    matchFlag = true;
                }
            } else {
                break;
            }
        }
        if (!matchFlag) {
            matchLength = 0;
        }
        return matchLength;
    }

    private void initDfaMapIfNecessary() {
        String flag = cacheService.get(CacheKey.SENSITIVE_WORD_KEY);
        if (flag != null && dfaMap != null && !dfaMap.isEmpty()) {
            return;
        }
        synchronized (this) {
            flag = cacheService.get(CacheKey.SENSITIVE_WORD_KEY);
            if (flag != null && dfaMap != null && !dfaMap.isEmpty()) {
                return;
            }
            List<SensitiveWord> list = sensitiveWordMapper.selectAll();
            Map<Object, Object> newDfaMap = new HashMap<>(Math.max(list.size(), 16));
            for (SensitiveWord sw : list) {
                String word = sw.getWord();
                Map<Object, Object> nowMap = newDfaMap;
                for (int i = 0; i < word.length(); i++) {
                    char keyChar = word.charAt(i);
                    Object wordMap = nowMap.get(keyChar);
                    if (wordMap != null) {
                        nowMap = (Map<Object, Object>) wordMap;
                    } else {
                        Map<Object, Object> newWordMap = new HashMap<>();
                        newWordMap.put("isEnd", "0");
                        nowMap.put(keyChar, newWordMap);
                        nowMap = newWordMap;
                    }
                    if (i == word.length() - 1) {
                        nowMap.put("isEnd", "1");
                    }
                }
            }
            dfaMap = newDfaMap;
            cacheService.set(CacheKey.SENSITIVE_WORD_KEY, "1", 3600);
        }
    }
}