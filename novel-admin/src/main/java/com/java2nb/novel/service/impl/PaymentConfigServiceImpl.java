package com.java2nb.novel.service.impl;

import com.java2nb.novel.dao.PaymentConfigDao;
import com.java2nb.novel.domain.PaymentConfigDO;
import com.java2nb.novel.service.PaymentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 支付配置服务实现
 */
@Service
public class PaymentConfigServiceImpl implements PaymentConfigService {

    @Autowired
    private PaymentConfigDao paymentConfigDao;

    @Override
    public PaymentConfigDO getByChannelCode(String channelCode) {
        return paymentConfigDao.getByChannelCode(channelCode);
    }

    @Override
    public int saveOrUpdate(PaymentConfigDO paymentConfig, Long operatorUserId) {
        Date now = new Date();
        PaymentConfigDO existing = null;
        if (paymentConfig.getId() != null) {
            existing = paymentConfigDao.get(paymentConfig.getId());
        }
        if (existing == null) {
            existing = paymentConfigDao.getByChannelCode(paymentConfig.getChannelCode());
        }

        // 支付配置首期只开放支付宝单渠道，但仍保留 channelCode 维度，为后续多渠道扩展预留结构。
        if (existing == null) {
            paymentConfig.setCreateTime(now);
            paymentConfig.setCreateUserId(operatorUserId);
            paymentConfig.setUpdateTime(now);
            paymentConfig.setUpdateUserId(operatorUserId);
            return paymentConfigDao.save(paymentConfig);
        }
        paymentConfig.setId(existing.getId());
        paymentConfig.setChannelCode(existing.getChannelCode());
        paymentConfig.setCreateTime(existing.getCreateTime());
        paymentConfig.setCreateUserId(existing.getCreateUserId());
        paymentConfig.setUpdateTime(now);
        paymentConfig.setUpdateUserId(operatorUserId);
        return paymentConfigDao.update(paymentConfig);
    }
}
