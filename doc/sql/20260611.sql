ALTER TABLE `website_info`
    ADD COLUMN `template_name` varchar(50) NOT NULL DEFAULT 'green' COMMENT '前台模板名称' AFTER `logo_dark`;

UPDATE `website_info`
SET `template_name` = 'green'
WHERE `template_name` IS NULL OR `template_name` = '';
