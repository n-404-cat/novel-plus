package com.java2nb.novel.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.java2nb.novel.dao.SensitiveWordDao;
import com.java2nb.novel.domain.SensitiveWordDO;
import com.java2nb.novel.service.SensitiveWordService;
import com.java2nb.common.config.CacheKey;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {
    @Autowired
    private SensitiveWordDao sensitiveWordDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public SensitiveWordDO get(Long id) {
        return sensitiveWordDao.get(id);
    }

    @Override
    public List<SensitiveWordDO> list(Map<String, Object> map) {
        return sensitiveWordDao.list(map);
    }

    @Override
    public int count(Map<String, Object> map) {
        return sensitiveWordDao.count(map);
    }

    @Override
    public int save(SensitiveWordDO sensitiveWord) {
        int count = sensitiveWordDao.save(sensitiveWord);
        clearCache();
        return count;
    }

    @Override
    public int update(SensitiveWordDO sensitiveWord) {
        int count = sensitiveWordDao.update(sensitiveWord);
        clearCache();
        return count;
    }

    @Override
    public int remove(Long id) {
        int count = sensitiveWordDao.remove(id);
        clearCache();
        return count;
    }

    @Override
    public int batchRemove(Long[] ids) {
        int count = sensitiveWordDao.batchRemove(ids);
        clearCache();
        return count;
    }

    private void clearCache() {
        redisTemplate.delete(CacheKey.SENSITIVE_WORD_KEY);
    }
}