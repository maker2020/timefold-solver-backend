package cn.keyvalues.optaplanner.solution.maprouting.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.keyvalues.optaplanner.solution.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Visitor;
import cn.keyvalues.optaplanner.solution.maprouting.domain.VisitorRoutingSolution;

/**
 * 循环引用重新封装简单对象
 */
public class CircularRefUtil {

    /**
     * <h4>重装循环引用对象为简单对象</h4>
     * 
     * <p>由于循环引用，直接序列化后的格式不利于前端使用数据，此方法专门去除不必要循环引用后拼装对象，
     * 再用fastjson序列化（因为servlet会默认objectmaper)。</p>
     * @param extraData 额外数据。例如API策略，计算时间限时
     * @return solution json data
     */
    public static String getNoEachReferenceSolution(VisitorRoutingSolution solution_,Map<String,Object> extraData){
        Map<String,Object> solution=new HashMap<>();
        // solution输入事实
        if(extraData!=null){
            solution.putAll(extraData);
        }
        String customersJSON = CircularRefUtil.getNoEachReferenceCustomers(solution_.getCustomerList());
        String visitorsJSON = CircularRefUtil.getNoEachReferenceVisitors(solution_.getVisitorList());
        solution.put("customers", JSON.parse(customersJSON));
        solution.put("visitors", JSON.parse(visitorsJSON));
        return JSON.toJSONString(solution,SerializerFeature.DisableCircularReferenceDetect);
    }
    
    public static String getNoEachReferenceCustomers(List<Customer> customers){
        List<Map<String,Object>> customerList_=new ArrayList<>();
        for(Customer c:customers){
            Map<String,Object> customer=new HashMap<>();
            customer.put("location", c.getLocation());
            // customer.put("tactics",c.getTactics());
            customerList_.add(customer);
        }
        return JSON.toJSONString(customerList_,SerializerFeature.DisableCircularReferenceDetect);
    }

    public static String getNoEachReferenceVisitors(List<Visitor> visitors){
        List<Map<String,Object>> visitorList_=new ArrayList<>();
        for(Visitor v:visitors){
            Map<String,Object> visitor=new HashMap<>();
            visitor.put("base", v.getBase());
            String customersJSON = getNoEachReferenceCustomers(v.getCustomers());
            visitor.put("customers", JSON.parse(customersJSON));
            visitorList_.add(visitor);
        }
        return JSON.toJSONString(visitorList_,SerializerFeature.DisableCircularReferenceDetect);
    }

}
