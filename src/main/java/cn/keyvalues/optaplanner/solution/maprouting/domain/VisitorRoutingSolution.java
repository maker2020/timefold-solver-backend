package cn.keyvalues.optaplanner.solution.maprouting.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

import cn.keyvalues.optaplanner.common.CircularRefRelease;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PlanningSolution
@Getter
@Setter
@ToString
public class VisitorRoutingSolution extends AbstractPersistable implements CircularRefRelease{    

    // @ProblemFactCollectionProperty
    // protected List<Location> locationList;

    // @ProblemFactCollectionProperty
    // protected List<VisitorBase> visitorBases;

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

    @Override
    public Map<String, Object> releaseCircular() {
        return getNoEachReferenceSolution(this, null);
    }

    /**
     * <h4>重装循环引用对象为简单对象</h4>
     * 
     * <p>由于循环引用，直接序列化后的格式不利于前端使用数据，此方法专门去除不必要循环引用后拼装对象，
     * 再用fastjson序列化（因为servlet会默认objectmaper)。</p>
     * @param extraData 额外数据。例如API策略，计算时间限时
     * @return solution json data
     */
    public static Map<String,Object> getNoEachReferenceSolution(VisitorRoutingSolution solution_,Map<String,Object> extraData){
        Map<String,Object> solution=new HashMap<>();
        // solution输入事实
        if(extraData!=null){
            solution.putAll(extraData);
        }
        solution.put("customers", getNoEachReferenceCustomers(solution_.getCustomerList()));
        solution.put("visitors", getNoEachReferenceVisitors(solution_.getVisitorList()));
        return solution;
    }
    
    public static List<Map<String,Object>> getNoEachReferenceCustomers(List<Customer> customers){
        List<Map<String,Object>> customerList_=new ArrayList<>();
        for(Customer c:customers){
            Map<String,Object> customer=new HashMap<>();
            customer.put("location", c.getLocation());
            // customer.put("tactics",c.getTactics());
            customerList_.add(customer);
        }
        return customerList_;
    }

    public static List<Map<String,Object>> getNoEachReferenceVisitors(List<Visitor> visitors){
        List<Map<String,Object>> visitorList_=new ArrayList<>();
        for(Visitor v:visitors){
            Map<String,Object> visitor=new HashMap<>();
            visitor.put("base", v.getBase());
            visitor.put("customers", getNoEachReferenceCustomers(v.getCustomers()));
            // 访问路径及详细信息。
            visitor.put("routingInfo", v.getOptimalRelatedMap());
            visitorList_.add(visitor);
        }
        return visitorList_;
    }
    
}
