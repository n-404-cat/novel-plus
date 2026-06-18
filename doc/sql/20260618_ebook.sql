CREATE TABLE IF NOT EXISTS `ebook` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `book_name` varchar(200) NOT NULL COMMENT '书名',
  `author_name` varchar(100) DEFAULT NULL COMMENT '作者',
  `isbn` varchar(50) DEFAULT NULL COMMENT 'ISBN',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面地址',
  `file_url` varchar(255) NOT NULL COMMENT '电子书文件地址',
  `file_format` varchar(20) NOT NULL COMMENT '文件格式：pdf/txt/epub/azw3/mobi',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小，单位字节',
  `intro` text COMMENT '简介',
  `author_intro` text COMMENT '作者简介',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0下架，1上架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_ebook_status_sort` (`status`, `sort`, `create_time`),
  KEY `idx_ebook_name_author` (`book_name`, `author_name`),
  KEY `idx_ebook_isbn` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子书表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 520, 300, '电子书管理', 'novel/ebook', 'novel:ebook:ebook', '1', 'fa', '22'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 520 OR `perms` = 'novel:ebook:ebook'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 521, 520, '新增', NULL, 'novel:ebook:add', '2', NULL, '1'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 521 OR `perms` = 'novel:ebook:add'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 522, 520, '编辑', NULL, 'novel:ebook:edit', '2', NULL, '2'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 522 OR `perms` = 'novel:ebook:edit'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 523, 520, '删除', NULL, 'novel:ebook:remove', '2', NULL, '3'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 523 OR `perms` = 'novel:ebook:remove'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 524, 520, '批量删除', NULL, 'novel:ebook:batchRemove', '2', NULL, '4'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 524 OR `perms` = 'novel:ebook:batchRemove'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 525, 520, '详情', NULL, 'novel:ebook:detail', '2', NULL, '5'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 525 OR `perms` = 'novel:ebook:detail'
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu`
WHERE `perms` LIKE 'novel:ebook:%'
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.role_id = 1 AND rm.menu_id = `sys_menu`.menu_id
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT ur.role_id, em.menu_id
FROM `sys_user` u
JOIN `sys_user_role` ur ON u.user_id = ur.user_id
JOIN `sys_menu` em ON em.perms LIKE 'novel:ebook:%'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.role_id = ur.role_id AND rm.menu_id = em.menu_id
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, em.menu_id
FROM `sys_role_menu` rm
JOIN `sys_menu` nm ON rm.menu_id = nm.menu_id
JOIN `sys_menu` em ON em.perms LIKE 'novel:ebook:%'
WHERE nm.perms = 'novel:book:book'
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` exists_rm
      WHERE exists_rm.role_id = rm.role_id AND exists_rm.menu_id = em.menu_id
  );
