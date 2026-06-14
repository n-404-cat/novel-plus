package com.java2nb.novel.entity;

import lombok.Data;

import java.util.Date;

/**
 * 支付配置表
 */
@Data
public class PaymentConfig {

    private Long id;

    private String channelCode;

    private String channelName;

    private String appId;

    private String publicKey;

    private String privateKey;

    private String notifyUrl;

    private String returnUrl;

    private String gatewayUrl;

    private String signType;

    private String charset;

    private String payEnvironment;

    private Integer enabled;

    /**
     * 原生微信支付启停状态
     */
    private Integer wechatEnabled;

    /**
     * 个人支付宝启停状态
     */
    private Integer alipayPersonalEnabled;

    /**
     * 个人微信启停状态
     */
    private Integer wechatPersonalEnabled;

    private String remark;

    private String alipayQrCodeUrl;

    private String wechatQrCodeUrl;

    private Date createTime;

    private Long createUserId;

    private Date updateTime;

    private Long updateUserId;
}
