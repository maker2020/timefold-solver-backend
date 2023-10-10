package cn.keyvalues.optaplanner.solution.maprouting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.solution.maprouting.domain.entity.Storehouse;
import cn.keyvalues.optaplanner.solution.maprouting.service.IStorehouseService;
import cn.keyvalues.optaplanner.utils.mybatisplus.PageUtil;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author generator v3.5.3.1
 * @since 2023-10-09
 */
@RestController
@RequestMapping("/storehouse")
public class StorehouseController {

    @Autowired
    IStorehouseService service;

    @GetMapping("/list")
    public Result<?> list(){
        Page<Storehouse> page = PageUtil.getPage();
        IPage<Storehouse> pageInfo=service.page(page);
        return Result.OK(pageInfo);
    }

    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Long id){
        if(service.removeById(id)){
            return Result.OK();
        }
        return Result.failed(null);
    }

    @PutMapping("/saveOrUpdate")
    public Result<?> update(@RequestBody Storehouse storehouse){
        return Result.OK(service.saveOrUpdate(storehouse));
    }

}
