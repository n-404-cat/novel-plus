UPDATE crawl_source
SET crawl_rule = '{
  "bookListUrl": "http://m.mcxs.info/xclass/{catId}/{page}.html",
  "catIdRule": {
    "catId1": "1",
    "catId2": "2",
    "catId3": "3",
    "catId4": "4",
    "catId5": "6",
    "catId6": "5",
    "catId7": "7"
  },
  "bookIdPatten": "href=\\"/(\\\\d+_\\\\d+)/\\"",
  "pagePatten": "value=\\"(\\\\d+)/\\\\d+\\"",
  "totalPagePatten": "value=\\"\\\\d+/(\\\\d+)\\"",
  "bookDetailUrl": "http://m.mcxs.info/{bookId}/",
  "bookNamePatten": "<meta property=\\"og:novel:book_name\\" content=\\"([^\\"]+)\\"",
  "authorNamePatten": "<li class=\\"author\\">作者：<a href=\\"/author/\\\\d+/\\">([^<]+)</a>",
  "picUrlPatten": "<img src=\\"([^\\"]+)\\" onerror=\\"this.src=",
  "picUrlPrefix": "http://m.mcxs.info",
  "statusPatten": "<li class=\\"\\">状态：([^<]+)</li>",
  "bookStatusRule": {
    "连载": 0,
    "全本": 1,
    "完结": 1
  },
  "visitCountPatten": "<li class=\\"\\">点击：(\\\\d+)</li>",
  "descStart": "<p class=\\"review\\">",
  "descEnd": "</p>",
  "filterDesc": "<span class=\\"longview\\"></span>|简介：\\\\s*",
  "upadateTimePatten": "<li class=\\"\\">更新：([0-9:\\\\-T\\\\s]+)</li>",
  "upadateTimeFormatPatten": "yyyy-MM-dd''T''HH:mm:ss",
  "bookIndexUrl": "http://m.mcxs.info/{bookId}/all.html",
  "indexIdPatten": "<p><a href=\\"/\\\\d+_\\\\d+/(\\\\d+)\\\\.html\\">[^<]+</a></p>",
  "indexNamePatten": "<p><a href=\\"/\\\\d+_\\\\d+/\\\\d+\\\\.html\\">([^<]+)</a></p>",
  "bookContentUrl": "http://m.mcxs.info/{bookId}/{indexId}.html",
  "contentStart": "<div id=\\"chaptercontent\\" class=\\"Readarea ReadAjax_content\\">",
  "contentEnd": "<script>pumpkinbm();",
  "filterContent": "<p>\\\\s*<a href=\\"javascript:postError[^<]+</a>\\\\s*</p>|<p>天才一秒记住本站地址：\\[梦书中文\\]http://m.mcxs.info/最快更新！无广告！</p>|（本章未完，请点击下一页继续阅读）|<p>\\\\s*<a href=\\"javascript:addBookMark[^<]+</a>\\\\s*</p>"
}',
    update_time = NOW()
WHERE id = 5;
