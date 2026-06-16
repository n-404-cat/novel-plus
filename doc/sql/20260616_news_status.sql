ALTER TABLE `news`
ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架' AFTER `content`;

UPDATE `news`
SET `status` = 1
WHERE `status` IS NULL;
