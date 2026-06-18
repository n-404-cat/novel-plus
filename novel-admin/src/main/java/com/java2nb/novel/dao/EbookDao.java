package com.java2nb.novel.dao;

import com.java2nb.common.annotation.SanitizeMap;
import com.java2nb.novel.domain.EbookDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 电子书后台 DAO。
 */
@Mapper
public interface EbookDao {

    EbookDO get(Long id);

    List<EbookDO> list(@SanitizeMap Map<String, Object> map);

    int count(Map<String, Object> map);

    int save(EbookDO ebook);

    int update(EbookDO ebook);

    int remove(Long id);

    int batchRemove(Long[] ids);
}
