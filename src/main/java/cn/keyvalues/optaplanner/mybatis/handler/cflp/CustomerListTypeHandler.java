package cn.keyvalues.optaplanner.mybatis.handler.cflp;

import java.util.List;

import org.apache.ibatis.type.MappedTypes;

import com.alibaba.fastjson.TypeReference;

import cn.keyvalues.optaplanner.mybatis.handler.JsonListTypeHandler;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;


@MappedTypes(List.class)
public class CustomerListTypeHandler extends JsonListTypeHandler<Customer>{

    @Override
    protected TypeReference<List<Customer>> specificType() {
        return new TypeReference<List<Customer>>(){};
    }
    
}
