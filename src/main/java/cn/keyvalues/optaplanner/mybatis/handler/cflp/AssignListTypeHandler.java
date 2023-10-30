package cn.keyvalues.optaplanner.mybatis.handler.cflp;

import java.util.List;

import org.apache.ibatis.type.MappedTypes;

import com.alibaba.fastjson.TypeReference;

import cn.keyvalues.optaplanner.mybatis.handler.JsonListTypeHandler;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;


@MappedTypes(List.class)
public class AssignListTypeHandler extends JsonListTypeHandler<Assign>{

    @Override
    protected TypeReference<List<Assign>> specificType() {
        return new TypeReference<List<Assign>>(){};
    }
    
}
