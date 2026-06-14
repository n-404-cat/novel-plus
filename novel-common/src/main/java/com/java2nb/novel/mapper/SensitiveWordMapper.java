package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface SensitiveWordMapper {

    @Select("select id, word, create_time from sensitive_word")
    @Results(id = "SensitiveWordResult", value = {
        @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
        @Result(column = "word", property = "word", jdbcType = JdbcType.VARCHAR),
        @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP)
    })
    List<SensitiveWord> selectAll();
}