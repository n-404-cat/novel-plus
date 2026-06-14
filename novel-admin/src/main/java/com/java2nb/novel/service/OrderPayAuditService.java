package com.java2nb.novel.service;

import com.java2nb.novel.domain.OrderPayAuditDO;

import java.util.List;
import java.util.Map;

public interface OrderPayAuditService {

    OrderPayAuditDO get(Long id);

    List<OrderPayAuditDO> list(Map<String, Object> map);

    int count(Map<String, Object> map);

    int audit(OrderPayAuditDO orderPayAudit);
}