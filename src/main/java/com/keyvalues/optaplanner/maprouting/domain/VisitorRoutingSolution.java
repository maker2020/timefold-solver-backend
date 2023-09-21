package com.keyvalues.optaplanner.maprouting.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import com.keyvalues.optaplanner.common.persistence.jackson.JacksonUniqueIdGenerator;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PlanningSolution
@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class VisitorRoutingSolution extends AbstractPersistable{    

    @ProblemFactCollectionProperty
    protected List<Location> locationList;

    @ProblemFactCollectionProperty
    protected List<VisitorBase> visitorBases;

    @PlanningEntityCollectionProperty
    protected List<Visitor> visitorList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "customerList")
    protected List<Customer> customerList;

    @PlanningScore
    protected HardSoftLongScore score;

    public VisitorRoutingSolution(){
        this(0);
    }

    public VisitorRoutingSolution(long id){
        super(id);
    }

    /**
     * <p>由于循环引用，直接序列化后的格式不利于前端使用数据，此方法专门去除不必要循环引用后拼装对象，
     * 再用fastjson序列化（因为servlet会默认objectmaper)。</p>
     * 如果不用这个，要直接返回，则必须用ObjectMapper作为序列化json框架，因为目前搭配了@IdentifyInfo为循环引用标识
     * @param extraData 额外数据。例如API策略，计算时间限时
     * @return solution json data
     */
    public String getNoEachReferenceSolution(Map<String,Object> extraData){
        Map<String,Object> solution=new HashMap<>();
        // solution输入事实
        if(extraData!=null){
            solution.putAll(extraData);
        }
        // 点/客户 列表
        solution.put("locationList", locationList);
        
        // 访问者/车辆 列表及其访问点的 顺序
        List<Map<String,Object>> visitorList__=new ArrayList<>();
        for(Visitor visitor:visitorList){
            Map<String,Object> visitorObj=new HashMap<>();
            visitorObj.put("base", visitor.getBase());
            List<Customer> customers = visitor.getCustomers();
            List<Map<String,Object>> customerList=new ArrayList<>();
            for(Customer c:customers){
                Map<String,Object> customer=new HashMap<>();
                customer.put("location", c.getLocation());
                customerList.add(customer);
            }
            visitorObj.put("visitRouting", customerList);
            visitorList__.add(visitorObj);
        }
        solution.put("visitorList", visitorList__);
        return JSON.toJSONString(solution,SerializerFeature.DisableCircularReferenceDetect);
    }
    
}
