package com.java2nb.novel.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 敏感词库
 */
public class SensitiveWordDO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;
    // 敏感词
    private String word;
    // 创建时间
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}