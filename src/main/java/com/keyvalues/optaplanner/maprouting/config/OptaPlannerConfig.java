package com.keyvalues.optaplanner.maprouting.config;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;

@Configuration
public class OptaPlannerConfig {
    
    @Bean
    SolverConfig solverConfig(){
        return SolverConfig.createFromXmlResource("optaplanner/maproutingSolverConfig.xml");
    }

    @Bean
    SolverFactory<MapRoutingSolution> solverFactory() {
        // 构建 SolverFactory
        return SolverFactory.create(solverConfig());
    }

    @Bean
    SolverManager<MapRoutingSolution, Long> solverManager() {
        return SolverManager.create(solverFactory(), new SolverManagerConfig());
    }

}
