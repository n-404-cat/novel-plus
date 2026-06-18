package com.java2nb.novel.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TXT 电子书分页阅读结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EbookTxtReadResult {

    private String content;

    private int pageNo;

    private boolean hasPrev;

    private boolean hasNext;
}
