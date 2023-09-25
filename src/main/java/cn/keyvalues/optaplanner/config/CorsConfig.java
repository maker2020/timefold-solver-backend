// package cn.keyvalues.optaplanner.config;

// import java.util.Arrays;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// @Configuration
// public class CorsConfig {

//     // @Bean
//     // public CorsFilter corsFilter() {
//     //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//     //     CorsConfiguration config = new CorsConfiguration();

//     //     // 允许的来源，可以是具体的域名，也可以是通配符（*）
//     //     config.addAllowedOrigin("*");

//     //     // 允许的HTTP方法，例如GET、POST等
//     //     config.addAllowedMethod("*");

//     //     // 允许的请求头
//     //     config.addAllowedHeader("*");

//     //     // 是否允许发送Cookie
//     //     config.setAllowCredentials(false);

//     //     source.registerCorsConfiguration("/**", config);
//     //     return new CorsFilter(source);
//     // }

//     @Bean
//     public CorsConfiguration corsConfiguration() {
//         CorsConfiguration configuration = new CorsConfiguration();
//         configuration.setAllowedOrigins(Arrays.asList("http://allowed-origin.com"));
//         configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
//         configuration.setAllowCredentials(false);
//         configuration.setMaxAge(3600L);
//         return configuration;
//     }

// }