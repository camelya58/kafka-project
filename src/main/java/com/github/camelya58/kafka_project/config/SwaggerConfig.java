package com.github.camelya58.kafka_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Class SwaggerConfig
 *
 * @author Kamila Meshcheryakova
 * created 21.06.2021
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.github.camelya58.kafka_project.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /* Describe APIs */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Kafka Rest APIs")
                .version("1.0-SNAPSHOT")
                .build();
    }
}
