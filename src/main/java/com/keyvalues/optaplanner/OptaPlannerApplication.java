package com.keyvalues.optaplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * 1.算法优化调整
 * 2.1提前组合并得到地图API的距离
 * 2.2分配后调用：优化约束减少冗余的枚举
 */
@Slf4j
@SpringBootApplication
public class OptaPlannerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OptaPlannerApplication.class, args);
        log.info("DEFAULT swagger-ui URL-------------http://localhost:8080/swagger-ui.html");
    }

}
