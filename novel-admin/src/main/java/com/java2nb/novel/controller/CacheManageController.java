package com.java2nb.novel.controller;

import com.java2nb.common.config.Constant;
import com.java2nb.common.utils.R;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台统一缓存管理
 */
@Controller
@RequestMapping("/novel/cacheManage")
public class CacheManageController {

    private final StringRedisTemplate redisTemplate;

    public CacheManageController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping()
    @RequiresPermissions("novel:cacheManage:cacheManage")
    String cacheManage() {
        return "novel/cacheManage/cacheManage";
    }

    @ApiOperation(value = "获取缓存项列表", notes = "获取缓存项列表")
    @ResponseBody
    @GetMapping("/items")
    @RequiresPermissions("novel:cacheManage:cacheManage")
    public R items() {
        return R.ok().put("data", buildCacheItems());
    }

    @ApiOperation(value = "清理缓存", notes = "清理缓存")
    @ResponseBody
    @PostMapping("/clear")
    @RequiresPermissions("novel:cacheManage:clear")
    public R clear(@RequestParam(value = "cacheCodes[]", required = false) String[] cacheCodesArray,
                   @RequestParam(value = "cacheCodes", required = false) String[] cacheCodes) {
        if ((cacheCodes == null || cacheCodes.length == 0) && cacheCodesArray != null && cacheCodesArray.length > 0) {
            cacheCodes = cacheCodesArray;
        }
        if (cacheCodes == null || cacheCodes.length == 0) {
            return R.error("请至少选择一个缓存项");
        }
        List<CacheItem> allItems = buildCacheItems();
        Map<String, CacheItem> itemMap = new LinkedHashMap<>();
        for (CacheItem item : allItems) {
            itemMap.put(item.getCode(), item);
        }

        int deletedKeys = 0;
        List<String> clearedCodes = new ArrayList<>();
        for (String code : cacheCodes) {
            CacheItem item = itemMap.get(code);
            if (item == null) {
                continue;
            }
            deletedKeys += deleteByPattern(item.getPattern());
            clearedCodes.add(code);
        }
        return R.ok()
            .put("clearedCodes", clearedCodes)
            .put("clearedItemCount", clearedCodes.size())
            .put("deletedKeyCount", deletedKeys);
    }

    private int deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        redisTemplate.delete(keys);
        return keys.size();
    }

    private List<CacheItem> buildCacheItems() {
        List<CacheItem> items = new ArrayList<>();
        items.add(new CacheItem("websiteInfo", "网站信息与模板缓存", com.java2nb.common.config.CacheKey.WEBSITE_INFO_KEY, "前台网站基础配置、模板生效相关缓存"));
        items.add(new CacheItem("paymentConfig", "支付配置缓存", com.java2nb.common.config.CacheKey.PAYMENT_CONFIG_KEY_PREFIX + "*", "支付宝等支付配置短缓存"));
        items.add(new CacheItem("indexBooks", "首页小说推荐缓存", com.java2nb.common.config.CacheKey.INDEX_BOOK_SETTINGS_KEY, "首页推荐小说设置缓存"));
        items.add(new CacheItem("indexNews", "首页新闻缓存", com.java2nb.common.config.CacheKey.INDEX_NEWS_KEY, "首页新闻列表缓存"));
        items.add(new CacheItem("indexLinks", "首页友情链接缓存", com.java2nb.common.config.CacheKey.INDEX_LINK_KEY, "首页友情链接缓存"));
        items.add(new CacheItem("clickRank", "首页点击榜缓存", com.java2nb.common.config.CacheKey.INDEX_CLICK_BANK_BOOK_KEY, "首页点击榜单缓存"));
        items.add(new CacheItem("newBooks", "首页新书榜缓存", com.java2nb.common.config.CacheKey.INDEX_NEW_BOOK_KEY, "首页新书榜缓存"));
        items.add(new CacheItem("updateBooks", "首页更新榜缓存", com.java2nb.common.config.CacheKey.INDEX_UPDATE_BOOK_KEY, "首页更新榜缓存"));
        items.add(new CacheItem("templateMode", "模板模式缓存", com.java2nb.common.config.CacheKey.TEMPLATE_DIR_KEY + "*", "PC/移动端模板模式缓存"));
        items.add(new CacheItem("sensitiveWord", "敏感词缓存", com.java2nb.common.config.CacheKey.SENSITIVE_WORD_KEY, "敏感词树与命中标记缓存"));
        items.add(new CacheItem("testParse", "爬虫规则测试缓存", com.java2nb.common.config.CacheKey.BOOK_TEST_PARSE + "*", "后台规则测试页缓存"));
        items.add(new CacheItem("searchEngine", "搜索引擎相关缓存", "es*", "ES 更新时间、转换锁、点击更新标记"));
        items.add(new CacheItem("bookVisit", "小说点击增量缓存", com.java2nb.common.config.CacheKey.BOOK_ADD_VISIT_COUNT, "累计点击量增量缓存"));
        items.add(new CacheItem("downloadLock", "小说下载锁", Constant.BOOK_IS_DOWNLOADING_KEY + "*", "后台小说下载中的互斥锁"));
        items.add(new CacheItem("aiPic", "AI 生成图片缓存", com.java2nb.common.config.CacheKey.AI_GEN_PIC + "*", "小说 AI 封面图缓存"));
        return items;
    }

    @Data
    @AllArgsConstructor
    public static class CacheItem {
        private String code;
        private String name;
        private String pattern;
        private String remark;
    }
}
