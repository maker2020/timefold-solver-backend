package cn.keyvalues.optaplanner.utils.mybatisplus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

/**
 * <p>created time:2023/4/25 19:34</p>
 * <p>des:
 *     代码生成器（新）
 * </p>
 *
 * @author Ya Shi
 */
public class CodeGenerator {

    /**
     * 数据源配置
     */
    private static final DataSourceConfig.Builder DATA_SOURCE_CONFIG = new DataSourceConfig
            .Builder("jdbc:postgresql://172.16.21.161:5432/optaplanner?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
                    , "postgres", "postgres")
            .dbQuery(new PostgreSqlQuery());

    /**
     * 输出路径
     */
    private static final String outputDir = System.getProperty("user.dir") + "/src/main/java";

    public static void main(String[] args) {

        FastAutoGenerator.create(DATA_SOURCE_CONFIG)
                .globalConfig(builder -> {
                    builder.author("generator v3.5.3.1") // 设置作者
                            .enableSpringdoc()
                            // .enableSwagger() // 开启 swagger 模式
                            .outputDir(outputDir)   // 指定输出目录
                            .disableOpenDir();   //禁止打开输出目录
                })
                .packageConfig(builder -> {
                    // builder.parent("com.ya.boottest.fruit.autoCode"); // 设置父包名
                    builder.parent("cn.keyvalues.optaplanner.common"); // 设置父包名
                    builder.entity("entity"); // 实体包路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("constraint_definition") // 设置需要生成的表名
                            .controllerBuilder().enableFileOverride().enableRestStyle().enableHyphenStyle()
                            .serviceBuilder().enableFileOverride()
                            .entityBuilder().enableFileOverride().enableLombok().idType(IdType.ASSIGN_ID)
                            .mapperBuilder().enableFileOverride(); 
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}


