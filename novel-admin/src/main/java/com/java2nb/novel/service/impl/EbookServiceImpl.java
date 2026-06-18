package com.java2nb.novel.service.impl;

import com.java2nb.novel.dao.EbookDao;
import com.java2nb.novel.domain.EbookDO;
import com.java2nb.novel.service.EbookService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 电子书后台服务实现。
 */
@Service
public class EbookServiceImpl implements EbookService {

    private static final Set<String> SUPPORT_FORMATS = Set.of("pdf", "txt", "epub", "azw3", "mobi");

    @Autowired
    private EbookDao ebookDao;

    @Override
    public EbookDO get(Long id) {
        return ebookDao.get(id);
    }

    @Override
    public List<EbookDO> list(Map<String, Object> map) {
        return ebookDao.list(map);
    }

    @Override
    public int count(Map<String, Object> map) {
        return ebookDao.count(map);
    }

    @Override
    public int save(EbookDO ebook) {
        fillDefaultValue(ebook);
        ebook.setCreateTime(new Date());
        return ebookDao.save(ebook);
    }

    @Override
    public int update(EbookDO ebook) {
        fillDefaultValue(ebook);
        ebook.setUpdateTime(new Date());
        return ebookDao.update(ebook);
    }

    @Override
    public int remove(Long id) {
        return ebookDao.remove(id);
    }

    @Override
    public int batchRemove(Long[] ids) {
        return ebookDao.batchRemove(ids);
    }

    private void fillDefaultValue(EbookDO ebook) {
        if (ebook.getStatus() == null) {
            ebook.setStatus(1);
        }
        if (ebook.getSort() == null) {
            ebook.setSort(0);
        }
        if (ebook.getFileSize() == null) {
            ebook.setFileSize(0L);
        }
        // 文件格式用于前台选择阅读器，必须在服务端统一小写并限制范围。
        String format = normalizeFormat(ebook.getFileFormat(), ebook.getFileUrl());
        if (!SUPPORT_FORMATS.contains(format)) {
            throw new IllegalArgumentException("暂只支持 PDF、TXT、EPUB、AZW3、MOBI 格式");
        }
        ebook.setFileFormat(format);
    }

    private String normalizeFormat(String fileFormat, String fileUrl) {
        if (StringUtils.isNotBlank(fileFormat)) {
            return fileFormat.trim().toLowerCase(Locale.ROOT);
        }
        if (StringUtils.isBlank(fileUrl) || !fileUrl.contains(".")) {
            return "";
        }
        return StringUtils.substringAfterLast(fileUrl, ".").toLowerCase(Locale.ROOT);
    }
}
