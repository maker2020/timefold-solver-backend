package cn.keyvalues.optaplanner.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import com.alibaba.fastjson.JSON;

import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo;

@MappedTypes(ConstraintDefineVo.class)
public class ConstraintDefinitionHandler extends BaseTypeHandler<ConstraintDefineVo>{

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ConstraintDefineVo parameter, JdbcType jdbcType) throws SQLException {
        String json = JSON.toJSONString(parameter);
        ps.setString(i, json);
    }

    @Override
    public ConstraintDefineVo getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public ConstraintDefineVo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public ConstraintDefineVo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private ConstraintDefineVo parseJson(String json) throws SQLException {
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, ConstraintDefineVo.class);
    }
    
}
