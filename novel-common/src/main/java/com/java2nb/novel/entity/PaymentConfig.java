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

    private String remark;

    private Date createTime;

    private Long createUserId;

    private Date updateTime;

    private Long updateUserId;
}
