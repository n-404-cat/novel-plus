package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.OrderPayAudit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderPayAuditMapper {

    @Insert("""
        insert into order_pay_audit (
            out_trade_no,
            voucher_path,
            payer_account,
            ocr_result,
            audit_status,
            create_time,
            update_time
        ) values (
            #{outTradeNo},
            #{voucherPath},
            #{payerAccount},
            #{ocrResult},
            #{auditStatus},
            #{createTime},
            #{updateTime}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderPayAudit record);

    @Update("""
        update order_pay_audit
           set audit_status = #{auditStatus},
               audit_user_id = #{auditUserId},
               audit_time = #{auditTime},
               audit_remark = #{auditRemark},
               update_time = #{updateTime}
         where id = #{id}
        """)
    int updateAuditStatus(OrderPayAudit record);

    @Update("""
        update order_pay
           set pay_status = 1,
               update_time = now()
         where out_trade_no = #{outTradeNo} and pay_status = 2
        """)
    int updateOrderPaySuccess(@Param("outTradeNo") Long outTradeNo);

    @Update("""
        update user
           set account_balance = account_balance + #{amount}
         where id = #{userId}
        """)
    int addUserBalance(@Param("userId") Long userId, @Param("amount") int amount);

    @Select("select user_id as userId, total_amount as totalAmount from order_pay where out_trade_no = #{outTradeNo}")
    Map<String, Object> getOrderPayInfo(@Param("outTradeNo") Long outTradeNo);

    @Select("""
        select id, out_trade_no, voucher_path, payer_account, ocr_result, 
               audit_status, audit_user_id, audit_time, audit_remark, create_time, update_time
          from order_pay_audit
         where out_trade_no = #{outTradeNo}
    """)
    @Results(id = "OrderPayAuditResult", value = {
        @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
        @Result(column = "out_trade_no", property = "outTradeNo", jdbcType = JdbcType.BIGINT),
        @Result(column = "voucher_path", property = "voucherPath", jdbcType = JdbcType.VARCHAR),
        @Result(column = "payer_account", property = "payerAccount", jdbcType = JdbcType.VARCHAR),
        @Result(column = "ocr_result", property = "ocrResult", jdbcType = JdbcType.VARCHAR),
        @Result(column = "audit_status", property = "auditStatus", jdbcType = JdbcType.TINYINT),
        @Result(column = "audit_user_id", property = "auditUserId", jdbcType = JdbcType.BIGINT),
        @Result(column = "audit_time", property = "auditTime", jdbcType = JdbcType.TIMESTAMP),
        @Result(column = "audit_remark", property = "auditRemark", jdbcType = JdbcType.VARCHAR),
        @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
        @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP)
    })
    OrderPayAudit selectByOutTradeNo(@Param("outTradeNo") Long outTradeNo);

    @Select("""
        select id, out_trade_no, voucher_path, payer_account, ocr_result, 
               audit_status, audit_user_id, audit_time, audit_remark, create_time, update_time
          from order_pay_audit
         where id = #{id}
    """)
    @Results(value = {
        @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
        @Result(column = "out_trade_no", property = "outTradeNo", jdbcType = JdbcType.BIGINT),
        @Result(column = "voucher_path", property = "voucherPath", jdbcType = JdbcType.VARCHAR),
        @Result(column = "payer_account", property = "payerAccount", jdbcType = JdbcType.VARCHAR),
        @Result(column = "ocr_result", property = "ocrResult", jdbcType = JdbcType.VARCHAR),
        @Result(column = "audit_status", property = "auditStatus", jdbcType = JdbcType.TINYINT),
        @Result(column = "audit_user_id", property = "auditUserId", jdbcType = JdbcType.BIGINT),
        @Result(column = "audit_time", property = "auditTime", jdbcType = JdbcType.TIMESTAMP),
        @Result(column = "audit_remark", property = "auditRemark", jdbcType = JdbcType.VARCHAR),
        @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
        @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP)
    })
    OrderPayAudit selectById(@Param("id") Long id);

    @Select("""
        <script>
        select id, out_trade_no, voucher_path, payer_account, ocr_result, 
               audit_status, audit_user_id, audit_time, audit_remark, create_time, update_time
          from order_pay_audit
        <where>
            <if test="outTradeNo != null">
                and out_trade_no = #{outTradeNo}
            </if>
            <if test="auditStatus != null">
                and audit_status = #{auditStatus}
            </if>
        </where>
        order by create_time desc
        <if test="offset != null and limit != null">
            limit #{offset}, #{limit}
        </if>
        </script>
    """)
    List<OrderPayAudit> list(Map<String, Object> params);

    @Select("""
        <script>
        select count(*)
          from order_pay_audit
        <where>
            <if test="outTradeNo != null">
                and out_trade_no = #{outTradeNo}
            </if>
            <if test="auditStatus != null">
                and audit_status = #{auditStatus}
            </if>
        </where>
        </script>
    """)
    int count(Map<String, Object> params);
}