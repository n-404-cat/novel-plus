package com.java2nb.novel.entity;

import lombok.Data;

import java.util.Date;

/**
 * 敏感词库
 */
@Data
public class SensitiveWord {
    private Long id;
    private String word;
    private Date createTime;
}