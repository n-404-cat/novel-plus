package com.java2nb.novel.dao;

import com.java2nb.novel.domain.OrderPayAuditDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderPayAuditDao {

    OrderPayAuditDO get(Long id);

    List<OrderPayAuditDO> list(Map<String, Object> map);

    int count(Map<String, Object> map);

    int updateAuditStatus(OrderPayAuditDO orderPayAudit);

    @Select("select * from order_pay_audit where id = #{id}")
    OrderPayAuditDO selectById(Long id);

    @Update("update order_pay set pay_status = 1, update_time = now() where out_trade_no = #{outTradeNo} and pay_status = 2")
    int updateOrderPaySuccess(@Param("outTradeNo") Long outTradeNo);

    @Select("select user_id as userId, total_amount as totalAmount from order_pay where out_trade_no = #{outTradeNo}")
    Map<String, Object> getOrderPayInfo(@Param("outTradeNo") Long outTradeNo);

    @Update("update user set account_balance = account_balance + #{amount} where id = #{userId}")
    int addUserBalance(@Param("userId") Long userId, @Param("amount") int amount);
}