package cn.keyvalues.optaplanner.mybatis.handler;

import java.util.List;

import org.apache.ibatis.type.MappedTypes;

import com.alibaba.fastjson.TypeReference;

import cn.keyvalues.optaplanner.solution.maprouting.domain.Visitor;

@MappedTypes(List.class)
public class VisitorListTypeHandler extends JsonListTypeHandler<Visitor>{

    @Override
    protected TypeReference<List<Visitor>> specificType() {
        return new TypeReference<List<Visitor>>(){};
    }
    
}
