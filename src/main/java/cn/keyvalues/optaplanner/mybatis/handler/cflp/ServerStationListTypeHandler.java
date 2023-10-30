package cn.keyvalues.optaplanner.mybatis.handler.cflp;

import java.util.List;

import org.apache.ibatis.type.MappedTypes;

import com.alibaba.fastjson.TypeReference;

import cn.keyvalues.optaplanner.mybatis.handler.JsonListTypeHandler;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;



@MappedTypes(List.class)
public class ServerStationListTypeHandler extends JsonListTypeHandler<ServerStation>{

    @Override
    protected TypeReference<List<ServerStation>> specificType() {
        return new TypeReference<List<ServerStation>>(){};
    }
    
}
