package cn.keyvalues.optaplanner.config;

import ai.timefold.solver.core.config.solver.SolverConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OptaBeanConfig {
    
    @Bean("tspConfig")
    SolverConfig tspSolverConfig(){
        return SolverConfig.createFromXmlResource("optaplanner/visitorRoutingSolverConfig.xml");
    }

    @Bean("cflpConfig")
    SolverConfig cflpSolverConfig(){
        return SolverConfig.createFromXmlResource("optaplanner/facilityLocationSolverConfig.xml");
    }

    // @Bean("tspFactory")
    // SolverFactory<VisitorRoutingSolution> solverFactory() {
    //     // 构建 SolverFactory
    //     return SolverFactory.create(solverConfig());
    // }

    // @Bean("tspManager")
    // SolverManager<VisitorRoutingSolution, UUID> solverManager() {
    //     return SolverManager.create(solverFactory(), new SolverManagerConfig());
    // }

}
