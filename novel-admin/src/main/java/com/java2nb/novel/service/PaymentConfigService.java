package com.java2nb.novel.service;

import com.java2nb.novel.domain.PaymentConfigDO;

/**
 * 支付配置服务
 */
public interface PaymentConfigService {

    String CHANNEL_CODE_ALIPAY = "ALIPAY";

    PaymentConfigDO getByChannelCode(String channelCode);

    int saveOrUpdate(PaymentConfigDO paymentConfig, Long operatorUserId);
}
