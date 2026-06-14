package com.java2nb.novel.service;

import com.java2nb.novel.domain.SensitiveWordDO;

import java.util.List;
import java.util.Map;

/**
 * 敏感词库
 */
public interface SensitiveWordService {

    SensitiveWordDO get(Long id);

    List<SensitiveWordDO> list(Map<String, Object> map);

    int count(Map<String, Object> map);

    int save(SensitiveWordDO sensitiveWord);

    int update(SensitiveWordDO sensitiveWord);

    int remove(Long id);

    int batchRemove(Long[] ids);
}