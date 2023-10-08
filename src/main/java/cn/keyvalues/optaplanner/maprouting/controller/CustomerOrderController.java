package cn.keyvalues.optaplanner.maprouting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.maprouting.domain.entity.CustomerOrder;
import cn.keyvalues.optaplanner.maprouting.service.ICustomerOrderService;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author generator v3.5.3.1
 * @since 2023-10-08
 */
@RestController
@RequestMapping("/customer-order")
public class CustomerOrderController {

    @Autowired
    ICustomerOrderService service;

    @GetMapping("/list")
    public Result<?> list(){
        return Result.OK(service.list());
    }

    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Long id){
        if(service.removeById(id)){
            return Result.OK();
        }
        return Result.failed(null);
    }

    @PutMapping("/saveOrUpdate")
    public Result<?> update(@RequestBody CustomerOrder storehouse){
        return Result.OK(service.saveOrUpdate(storehouse));
    }

}
