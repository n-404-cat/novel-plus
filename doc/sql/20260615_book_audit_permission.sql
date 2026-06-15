INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 506, 57, '审核', NULL, 'novel:book:audit', '2', NULL, '7'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 506 OR `perms` = 'novel:book:audit'
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 506
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 506
);
