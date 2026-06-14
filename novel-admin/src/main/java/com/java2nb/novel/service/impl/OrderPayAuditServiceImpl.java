package com.java2nb.novel.service.impl;

import com.java2nb.novel.domain.OrderPayAuditDO;
import com.java2nb.novel.dao.OrderPayAuditDao;
import com.java2nb.novel.service.OrderPayAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderPayAuditServiceImpl implements OrderPayAuditService {

    @Autowired
    private OrderPayAuditDao orderPayAuditDao;

    @Override
    public OrderPayAuditDO get(Long id) {
        return orderPayAuditDao.get(id);
    }

    @Override
    public List<OrderPayAuditDO> list(Map<String, Object> map) {
        return orderPayAuditDao.list(map);
    }

    @Override
    public int count(Map<String, Object> map) {
        return orderPayAuditDao.count(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int audit(OrderPayAuditDO orderPayAudit) {
        orderPayAudit.setUpdateTime(new Date());
        orderPayAudit.setAuditTime(new Date());
        int rows = orderPayAuditDao.updateAuditStatus(orderPayAudit);
        
        if (orderPayAudit.getAuditStatus() != null && orderPayAudit.getAuditStatus() == 1) {
            OrderPayAuditDO dbRecord = orderPayAuditDao.selectById(orderPayAudit.getId());
            if (dbRecord != null && dbRecord.getOutTradeNo() != null) {
                Long outTradeNo = dbRecord.getOutTradeNo();
                int updateRow = orderPayAuditDao.updateOrderPaySuccess(outTradeNo);
                if (updateRow > 0) {
                    Map<String, Object> orderInfo = orderPayAuditDao.getOrderPayInfo(outTradeNo);
                    if (orderInfo != null && orderInfo.get("userId") != null && orderInfo.get("totalAmount") != null) {
                        Long userId = ((Number) orderInfo.get("userId")).longValue();
                        int totalAmount = ((Number) orderInfo.get("totalAmount")).intValue();
                        orderPayAuditDao.addUserBalance(userId, totalAmount * 100);
                    }
                }
            }
        }
        return rows;
    }
}