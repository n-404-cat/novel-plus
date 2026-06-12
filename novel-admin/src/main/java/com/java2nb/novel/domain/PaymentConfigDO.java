package com.java2nb.novel.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.java2nb.common.jsonserializer.LongToStringSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付配置表
 */
@Data
public class PaymentConfigDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = LongToStringSerializer.class)
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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonSerialize(using = LongToStringSerializer.class)
    private Long createUserId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonSerialize(using = LongToStringSerializer.class)
    private Long updateUserId;
}
