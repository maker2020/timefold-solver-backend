package cn.keyvalues.optaplanner.maprouting.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.keyvalues.optaplanner.maprouting.domain.entity.SolutionEntity;
import cn.keyvalues.optaplanner.maprouting.mapper.SolutionMapper;
import cn.keyvalues.optaplanner.maprouting.service.SolutionService;

@Service
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper,SolutionEntity> implements SolutionService{
    
}
