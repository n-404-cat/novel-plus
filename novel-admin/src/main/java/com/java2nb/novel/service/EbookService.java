package com.java2nb.novel.service;

import com.java2nb.novel.domain.EbookDO;

import java.util.List;
import java.util.Map;

/**
 * 电子书后台服务。
 */
public interface EbookService {

    EbookDO get(Long id);

    List<EbookDO> list(Map<String, Object> map);

    int count(Map<String, Object> map);

    int save(EbookDO ebook);

    int update(EbookDO ebook);

    int remove(Long id);

    int batchRemove(Long[] ids);
}
