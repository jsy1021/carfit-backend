package backend.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CarFit API",
                version = "v1",
                description = "CarFit 차량 관리 및 유가 정보 서비스 API 명세서"
        ),
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication", //스키마 이름
        type = SecuritySchemeType.HTTP, //인증 방식 (HTTP 헤더 기반)
        scheme = "bearer",              //Bearer 토큰 방식 명시
        bearerFormat = "JWT"            //JWT 토큰 형식
)
public class SwaggerConfig {
}
