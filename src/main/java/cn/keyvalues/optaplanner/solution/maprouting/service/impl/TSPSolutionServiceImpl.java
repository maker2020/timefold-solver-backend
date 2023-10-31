package cn.keyvalues.optaplanner.solution.maprouting.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.keyvalues.optaplanner.solution.maprouting.domain.entity.TSPSolutionEntity;
import cn.keyvalues.optaplanner.solution.maprouting.mapper.TSPSolutionMapper;
import cn.keyvalues.optaplanner.solution.maprouting.service.TSPSolutionService;

@Service
public class TSPSolutionServiceImpl extends ServiceImpl<TSPSolutionMapper,TSPSolutionEntity> implements TSPSolutionService{
    
}
