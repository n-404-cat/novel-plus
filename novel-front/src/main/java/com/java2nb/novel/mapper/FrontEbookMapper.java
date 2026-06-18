package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.Ebook;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前台电子书查询。
 */
public interface FrontEbookMapper {

    /**
     * 查询上架电子书列表。
     *
     * @param keyword 关键词，支持书名、作者、ISBN、简介模糊检索
     * @return 电子书列表
     */
    List<Ebook> listDisplayable(@Param("keyword") String keyword);

    /**
     * 查询上架电子书详情。
     *
     * @param id 电子书ID
     * @return 电子书详情
     */
    Ebook getActiveById(@Param("id") Long id);
}
