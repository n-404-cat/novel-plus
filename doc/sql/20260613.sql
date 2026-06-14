ALTER TABLE `book` ADD COLUMN `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核意见' AFTER `status`;

ALTER TABLE `payment_config` CHANGE `qr_code_url` `alipay_qr_code_url` varchar(255) COMMENT '支付宝收款二维码';
ALTER TABLE `payment_config` ADD COLUMN `wechat_qr_code_url` varchar(255) COMMENT '微信收款二维码' AFTER `alipay_qr_code_url`;