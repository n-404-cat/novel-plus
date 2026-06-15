INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 507, 300, '缓存管理', 'novel/cacheManage', 'novel:cacheManage:cacheManage', '1', 'fa', '20'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 507 OR `perms` = 'novel:cacheManage:cacheManage'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 508, 507, '清理缓存', NULL, 'novel:cacheManage:clear', '2', NULL, '1'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 508 OR `perms` = 'novel:cacheManage:clear'
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 507
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 507
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 508
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 508
);
