package cn.keyvalues.optaplanner.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;


@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Optaplanner API")
                        .description("求解器示例程序")
                        .version("v1.0.0")
                        .license(new License()
                                .name("许可协议")
                                .url("https://optaplanner.keyvalues.cn"))
                        .contact(new Contact()
                                .name("hufangshuai")
                                .email("hufs@keyvalues.cn")))
                .externalDocs(new ExternalDocumentation()
                        .description("克沃斯规划求解器")
                        .url("https://optaplanner.keyvalues.cn"));
    }
 }
