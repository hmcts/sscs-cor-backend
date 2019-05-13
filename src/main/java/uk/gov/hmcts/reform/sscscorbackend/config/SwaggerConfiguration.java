package uk.gov.hmcts.reform.sscscorbackend.config;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.Application;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Bean
    public Docket api(IdamService idamService, @Value("${swagger.defaultTokens}") boolean defaultTokens) {
        String authorizationToken = "authToken";
        String serviceAuthorizationToken = "serviceAuthToken";
        if (defaultTokens) {
            IdamTokens idamTokens = idamService.getIdamTokens();
            authorizationToken = idamTokens.getIdamOauth2Token();
            serviceAuthorizationToken = idamTokens.getServiceAuthorization();
        }

        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(asList(
                        new ParameterBuilder()
                                .name("Authorization")
                                .description("Auth Header")
                                .modelRef(new ModelRef("string"))
                                .parameterType("header")
                                .required(true)
                                .defaultValue(authorizationToken)
                                .build(),
                        new ParameterBuilder()
                                .name("ServiceAuthorization")
                                .description("Service auth header")
                                .modelRef(new ModelRef("string"))
                                .parameterType("header")
                                .required(true)
                                .defaultValue(serviceAuthorizationToken)
                                .build()))
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage(Application.class.getPackage().getName() + ".controllers"))
                .paths(PathSelectors.any())
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
