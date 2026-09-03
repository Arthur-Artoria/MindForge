package cn.artoria.mind_forge.auth;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.artoria.mind_forge.common.web.ApiErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class AuthOpenApiConfig {

    @Bean
    OpenApiCustomizer logoutOpenApiCustomizer() {

        return openApi -> {
            Components components = ensureComponents(openApi);

            Operation logoutOperation = new Operation()
                    .operationId("logout")
                    .summary("退出当前session")
                    .tags(List.of("Authentication"))
                    .addSecurityItem(new SecurityRequirement().addList("sessionCookie"))
                    .addParametersItem(new Parameter().$ref("#/components/parameters/csrfHeader"))
                    .responses(new ApiResponses()
                            .addApiResponse("204", new ApiResponse().description("退出成功"))
                            .addApiResponse("403", jsonErrorResponse("CSRF token 缺失、过期或错误")));

            components.addSecuritySchemes(
                    "sessionCookie",
                    new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.COOKIE)
                            .name("JSESSIONID"))
                    .addParameters(
                            "csrfHeader",
                            new HeaderParameter()
                                    .name("X-CSRF-TOKEN")
                                    .description("与当前 Session 绑定的 CSRF token")
                                    .required(true)
                                    .schema(new StringSchema()));

            ModelConverters
                    .getInstance()
                    .read(ApiErrorResponse.class)
                    .forEach(components::addSchemas);

            openApi.path("/api/auth/logout", new PathItem().post(logoutOperation));
        };
    }

    private ApiResponse jsonErrorResponse(String description) {
        var schema = new Schema<ApiErrorResponse>().$ref("#/components/schemas/ApiErrorResponse");
        var mediaType = new MediaType().schema(schema);
        var content = new Content().addMediaType("application/json", mediaType);

        return new ApiResponse()
                .description(description)
                .content(content);

    }

    private Components ensureComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.components(new Components());
        }

        return openApi.getComponents();
    }
}
