package com.java2nb.novel.service.impl;

import com.java2nb.novel.dao.AnnouncementDao;
import com.java2nb.novel.domain.AnnouncementDO;
import com.java2nb.novel.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 公告后台服务实现。
 */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementDao announcementDao;

    @Override
    public AnnouncementDO get(Long id) {
        return announcementDao.get(id);
    }

    @Override
    public List<AnnouncementDO> list(Map<String, Object> map) {
        return announcementDao.list(map);
    }

    @Override
    public int count(Map<String, Object> map) {
        return announcementDao.count(map);
    }

    @Override
    public int save(AnnouncementDO announcement) {
        fillDefaultValue(announcement);
        announcement.setCreateTime(new Date());
        return announcementDao.save(announcement);
    }

    @Override
    public int update(AnnouncementDO announcement) {
        fillDefaultValue(announcement);
        announcement.setUpdateTime(new Date());
        return announcementDao.update(announcement);
    }

    @Override
    public int remove(Long id) {
        return announcementDao.remove(id);
    }

    @Override
    public int batchRemove(Long[] ids) {
        return announcementDao.batchRemove(ids);
    }

    private void fillDefaultValue(AnnouncementDO announcement) {
        // 公告前台展示依赖这些默认值，后台未填写时也要保持可预测行为。
        if (announcement.getType() == null) {
            announcement.setType(1);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        if (announcement.getCloseMode() == null) {
            announcement.setCloseMode(2);
        }
        if (announcement.getSort() == null) {
            announcement.setSort(0);
        }
    }
}
