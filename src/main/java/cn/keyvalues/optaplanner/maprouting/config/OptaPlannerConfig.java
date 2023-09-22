package cn.keyvalues.optaplanner.maprouting.config;

import java.util.UUID;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.keyvalues.optaplanner.maprouting.domain.VisitorRoutingSolution;

@Configuration
public class OptaPlannerConfig {
    
    @Bean
    SolverConfig solverConfig(){
        return SolverConfig.createFromXmlResource("optaplanner/visitorRoutingSolverConfig.xml");
    }

    @Bean
    SolverFactory<VisitorRoutingSolution> solverFactory() {
        // 构建 SolverFactory
        return SolverFactory.create(solverConfig());
    }

    @Bean
    SolverManager<VisitorRoutingSolution, UUID> solverManager() {
        return SolverManager.create(solverFactory(), new SolverManagerConfig());
    }

}
