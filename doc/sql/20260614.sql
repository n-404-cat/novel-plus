ALTER TABLE `website_info`
ADD COLUMN `novel_sensitive_word_enabled` tinyint(1) DEFAULT 0 COMMENT '小说敏感词替换开启状态（0:关闭 1:开启）',
ADD COLUMN `news_sensitive_word_enabled` tinyint(1) DEFAULT 0 COMMENT '新闻敏感词替换开启状态（0:关闭 1:开启）';

CREATE TABLE `sensitive_word` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `word` varchar(255) NOT NULL COMMENT '敏感词',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库';

INSERT INTO `sys_menu` (`parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`, `gmt_create`, `gmt_modified`) VALUES (76, '敏感词管理', 'novel/sensitiveWord', 'novel:sensitiveWord:sensitiveWord', 1, 'fa fa-ban', 6, '2026-06-14 10:00:00', '2026-06-14 10:00:00');
SET @menu_id = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`, `gmt_create`, `gmt_modified`) VALUES (@menu_id, '新增', '', 'novel:sensitiveWord:add', 2, '', 0, '2026-06-14 10:00:00', '2026-06-14 10:00:00');
INSERT INTO `sys_menu` (`parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`, `gmt_create`, `gmt_modified`) VALUES (@menu_id, '编辑', '', 'novel:sensitiveWord:edit', 2, '', 0, '2026-06-14 10:00:00', '2026-06-14 10:00:00');
INSERT INTO `sys_menu` (`parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`, `gmt_create`, `gmt_modified`) VALUES (@menu_id, '删除', '', 'novel:sensitiveWord:remove', 2, '', 0, '2026-06-14 10:00:00', '2026-06-14 10:00:00');
INSERT INTO `sys_menu` (`parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`, `gmt_create`, `gmt_modified`) VALUES (@menu_id, '批量删除', '', 'novel:sensitiveWord:batchRemove', 2, '', 0, '2026-06-14 10:00:00', '2026-06-14 10:00:00');
ALTER TABLE payment_config ADD COLUMN alipay_personal_enabled tinyint(1) DEFAULT 1 COMMENT '个人支付宝是否启用，0：停用，1：启用' AFTER wechat_enabled;
ALTER TABLE payment_config ADD COLUMN wechat_personal_enabled tinyint(1) DEFAULT 1 COMMENT '个人微信是否启用，0：停用，1：启用' AFTER alipay_personal_enabled;
