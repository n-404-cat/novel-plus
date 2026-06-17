(function () {
    function storageKey(prefix, id) {
        return prefix + '_' + id;
    }

    function shouldShow(item, prefix) {
        if (!item || !item.id) {
            return false;
        }
        // 关闭后永不提示的公告才写入本地存储；下次重新提示只关闭当前页面。
        return item.closeMode !== 1 || localStorage.getItem(storageKey(prefix, item.id)) !== '1';
    }

    function closeAnnouncement($node, item, prefix) {
        if (item.closeMode === 1) {
            localStorage.setItem(storageKey(prefix, item.id), '1');
        }
        $node.remove();
    }

    function renderPopup(item) {
        if (!shouldShow(item, 'announcement_popup_closed')) {
            return;
        }
        var $mask = $('<div class="announcement-popup-mask"></div>');
        var $box = $('<div class="announcement-popup-box"></div>');
        var $close = $('<button class="announcement-close" type="button">×</button>');
        $box.append($close);
        $box.append('<h3>' + item.title + '</h3>');
        $box.append('<div class="announcement-popup-content">' + (item.content || '') + '</div>');
        $mask.append($box);
        $('body').append($mask);
        $close.on('click', function () {
            closeAnnouncement($mask, item, 'announcement_popup_closed');
        });
    }

    function renderCorner(item) {
        if (!shouldShow(item, 'announcement_corner_closed')) {
            return;
        }
        var $box = $('<div class="announcement-corner-box"></div>');
        var $close = $('<button class="announcement-close" type="button">×</button>');
        $box.append($close);
        $box.append('<h4>' + item.title + '</h4>');
        $box.append('<div class="announcement-corner-content">' + (item.content || '') + '</div>');
        $('body').append($box);
        $close.on('click', function () {
            closeAnnouncement($box, item, 'announcement_corner_closed');
        });
    }

    function renderRolling(items) {
        var $bar = $('#announcementBar');
        var $inner = $('#announcementScrollInner');
        if (!$bar.length || !$inner.length) {
            return;
        }
        if ($inner.find('a').length > 0) {
            $bar.show();
            return;
        }
        if (!items || items.length === 0) {
            $bar.hide();
            return;
        }
        $inner.empty();
        $.each(items, function (index, item) {
            // 首页服务端模型偶发未渲染时，使用公告接口兜底补齐标题，避免只显示“公告”空栏。
            $('<a></a>')
                .attr('href', '/about/announcementInfo-' + item.id + '.html')
                .text(item.title || '')
                .appendTo($inner);
        });
        $bar.show();
    }

    $(function () {
        $.getJSON('/announcement/rolling', function (data) {
            if (data.code === 200) {
                renderRolling(data.data);
            }
        });
        $.getJSON('/announcement/popup', function (data) {
            if (data.code === 200 && data.data) {
                renderPopup(data.data);
            }
        });
        $.getJSON('/announcement/corner', function (data) {
            if (data.code === 200 && data.data) {
                renderCorner(data.data);
            }
        });
    });
})();
