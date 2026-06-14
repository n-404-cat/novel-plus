package com.java2nb.novel.dao;

import com.java2nb.novel.domain.SensitiveWordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 敏感词库
 */
@Mapper
public interface SensitiveWordDao {

    SensitiveWordDO get(Long id);

    List<SensitiveWordDO> list(Map<String, Object> map);

    int count(Map<String, Object> map);

    int save(SensitiveWordDO sensitiveWord);

    int update(SensitiveWordDO sensitiveWord);

    int remove(Long id);

    int batchRemove(Long[] ids);
}