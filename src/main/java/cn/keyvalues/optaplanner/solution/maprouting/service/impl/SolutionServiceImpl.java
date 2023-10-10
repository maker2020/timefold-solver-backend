package cn.keyvalues.optaplanner.solution.maprouting.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.keyvalues.optaplanner.solution.maprouting.domain.entity.SolutionEntity;
import cn.keyvalues.optaplanner.solution.maprouting.mapper.SolutionMapper;
import cn.keyvalues.optaplanner.solution.maprouting.service.SolutionService;

@Service
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper,SolutionEntity> implements SolutionService{
    
}
