package com.java2nb.novel.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EPUB 章节目录项。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EbookEpubChapterVO {

    private int index;

    private String title;

    private boolean active;
}
