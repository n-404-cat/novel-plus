CREATE TABLE IF NOT EXISTS `payment_config`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `channel_code`    varchar(32) NOT NULL COMMENT '支付渠道编码，如 ALIPAY',
    `channel_name`    varchar(50) NOT NULL COMMENT '支付渠道名称',
    `app_id`          varchar(128)         DEFAULT NULL COMMENT '应用ID',
    `public_key`      text COMMENT '支付宝公钥',
    `private_key`     text COMMENT '商户私钥',
    `notify_url`      varchar(255)         DEFAULT NULL COMMENT '异步通知地址',
    `return_url`      varchar(255)         DEFAULT NULL COMMENT '支付回跳地址',
    `gateway_url`     varchar(255)         DEFAULT NULL COMMENT '支付网关地址',
    `sign_type`       varchar(20)          DEFAULT 'RSA2' COMMENT '签名方式',
    `charset`         varchar(20)          DEFAULT 'utf-8' COMMENT '字符集',
    `pay_environment` varchar(20)          DEFAULT 'sandbox' COMMENT '运行环境，sandbox 或 production',
    `enabled`         tinyint(1)           DEFAULT '1' COMMENT '是否启用，0：停用，1：启用',
    `remark`          varchar(255)         DEFAULT NULL COMMENT '备注',
    `create_time`     datetime             DEFAULT NULL COMMENT '创建时间',
    `create_user_id`  bigint(20)           DEFAULT NULL COMMENT '创建人ID',
    `update_time`     datetime             DEFAULT NULL COMMENT '更新时间',
    `update_user_id`  bigint(20)           DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_channel_code` (`channel_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='支付配置表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
VALUES (502, 500, '支付配置', 'novel/paymentConfig', 'novel:paymentConfig:paymentConfig', '1', 'fa', '7');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
VALUES (503, 502, '修改', null, 'novel:paymentConfig:edit', '2', null, '6');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (1, 502);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (1, 503);
