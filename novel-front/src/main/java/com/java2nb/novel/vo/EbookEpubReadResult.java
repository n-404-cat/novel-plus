package com.java2nb.novel.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * EPUB 在线阅读结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EbookEpubReadResult {

    private List<EbookEpubChapterVO> chapters = new ArrayList<>();

    private String content;

    private int chapterIndex;

    private String errorMessage;
}
