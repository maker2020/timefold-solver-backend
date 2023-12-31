package cn.keyvalues.optaplanner.mybatis.handler.maprouting;

import java.util.List;

import org.apache.ibatis.type.MappedTypes;

import com.alibaba.fastjson.TypeReference;

import cn.keyvalues.optaplanner.mybatis.handler.JsonListTypeHandler;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Customer;

@MappedTypes(List.class)
public class CustomerListTypeHandler extends JsonListTypeHandler<Customer>{

    @Override
    protected TypeReference<List<Customer>> specificType() {
        return new TypeReference<List<Customer>>(){};
    }
    
}
