package com.java2nb.novel.service;

import com.java2nb.novel.entity.Ebook;
import com.java2nb.novel.vo.EbookEpubReadResult;
import com.java2nb.novel.vo.EbookTxtReadResult;
import io.github.xxyopen.model.page.PageBean;

import java.io.File;

/**
 * 前台电子书服务。
 */
public interface EbookService {

    /**
     * 分页查询上架电子书。
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @param keyword 检索关键词
     * @return 电子书分页
     */
    PageBean<Ebook> listByPage(int page, int pageSize, String keyword);

    /**
     * 查询电子书详情。
     *
     * @param ebookId 电子书ID
     * @return 电子书详情
     */
    Ebook queryEbookInfo(Long ebookId);

    /**
     * 分页读取 TXT 内容，其他格式不读取正文。
     *
     * @param ebook 电子书
     * @param pageNo 页码
     * @return TXT 分页正文
     */
    EbookTxtReadResult readTxtContent(Ebook ebook, int pageNo);

    /**
     * 读取 EPUB 章节目录和正文。
     *
     * @param ebook 电子书
     * @param chapterIndex 章节序号
     * @return EPUB 阅读结果
     */
    EbookEpubReadResult readEpubContent(Ebook ebook, int chapterIndex);

    /**
     * 解析电子书源文件在服务器上的真实文件。
     *
     * @param ebook 电子书
     * @return 本地文件；无法解析时返回 null
     */
    File resolveLocalFile(Ebook ebook);
}
