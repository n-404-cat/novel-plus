CREATE TABLE IF NOT EXISTS `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` mediumtext COMMENT '公告正文HTML',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '公告类型：1文章公告，2首页滚动，3弹窗，4右下角',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0下架，1上架',
  `close_mode` tinyint NOT NULL DEFAULT 2 COMMENT '关闭策略：1关闭后永不提示，2下次打开重新提示',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `start_time` datetime DEFAULT NULL COMMENT '开始展示时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束展示时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_announcement_type_status_sort` (`type`, `status`, `sort`, `create_time`),
  KEY `idx_announcement_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 509, 300, '公告管理', 'novel/announcement', 'novel:announcement:announcement', '1', 'fa', '21'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 509 OR `perms` = 'novel:announcement:announcement'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 510, 509, '新增', NULL, 'novel:announcement:add', '2', NULL, '1'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 510 OR `perms` = 'novel:announcement:add'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 511, 509, '编辑', NULL, 'novel:announcement:edit', '2', NULL, '2'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 511 OR `perms` = 'novel:announcement:edit'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 512, 509, '删除', NULL, 'novel:announcement:remove', '2', NULL, '3'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 512 OR `perms` = 'novel:announcement:remove'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 513, 509, '批量删除', NULL, 'novel:announcement:batchRemove', '2', NULL, '4'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 513 OR `perms` = 'novel:announcement:batchRemove'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 514, 509, '详情', NULL, 'novel:announcement:detail', '2', NULL, '5'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 514 OR `perms` = 'novel:announcement:detail'
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 509
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 509
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 510
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 510
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 511
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 511
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 512
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 512
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 513
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 513
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 514
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 514
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT ur.role_id, am.menu_id
FROM `sys_user` u
JOIN `sys_user_role` ur ON u.user_id = ur.user_id
JOIN `sys_menu` am ON am.perms LIKE 'novel:announcement:%'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.role_id = ur.role_id AND rm.menu_id = am.menu_id
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, am.menu_id
FROM `sys_role_menu` rm
JOIN `sys_menu` nm ON rm.menu_id = nm.menu_id
JOIN `sys_menu` am ON am.perms LIKE 'novel:announcement:%'
WHERE nm.perms = 'novel:news:news'
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` exists_rm
      WHERE exists_rm.role_id = rm.role_id AND exists_rm.menu_id = am.menu_id
  );
