package com.java2nb.novel.service;

import com.java2nb.novel.entity.PaymentConfig;

/**
 * 前台支付配置读取服务
 */
public interface PaymentConfigService {

    String CHANNEL_CODE_ALIPAY = "ALIPAY";

    PaymentConfig getAlipayConfig();

    boolean isAlipayEnabled();
}
