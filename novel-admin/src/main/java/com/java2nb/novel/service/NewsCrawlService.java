package com.java2nb.novel.service;

import com.java2nb.novel.vo.NewsCrawlResultVO;
import com.java2nb.novel.vo.NewsCrawlSourceVO;

import java.util.List;

/**
 * 后台新闻采集服务。
 */
public interface NewsCrawlService {

    List<NewsCrawlSourceVO> listBuiltInSources();

    NewsCrawlResultVO preview(String sourceCode, String url);

    NewsCrawlResultVO save(String sourceCode, String url, Integer catId, String catName, Integer status);
}
