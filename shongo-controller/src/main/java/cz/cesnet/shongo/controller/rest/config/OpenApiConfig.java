package cz.cesnet.shongo.controller.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;
import org.springframework.web.servlet.resource.WebJarsResourceResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configures OpenApi and SwaggerUI using springdoc.
 *
 * @author Filip Karnis
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Shongo API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer"
)
@ComponentScan(basePackages = {"org.springdoc"})
@Import({
        org.springdoc.core.SpringDocConfiguration.class,
        org.springdoc.webmvc.core.SpringDocWebMvcConfiguration.class,
        org.springdoc.webmvc.ui.SwaggerConfig.class,
        org.springdoc.core.SwaggerUiConfigProperties.class,
        org.springdoc.core.SwaggerUiOAuthProperties.class,
        org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
})
class OpenApiConfig implements WebMvcConfigurer
{

    private static final String PET_STORE_URL = "https://petstore.swagger.io/v2/swagger.json";
    private static final String SPRINGDOC_OPENAPI_URL = "/v3/api-docs";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/**/*.html")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(false)
                .addResolver(new WebJarsResourceResolver())
                .addResolver(new PathResourceResolver())
                .addTransformer(new IndexPageTransformer());
    }

    /**
     * Replaces the default openapi config URL in swagger with the one generated by springdoc.
     */
    public static class IndexPageTransformer implements ResourceTransformer
    {
        @Override
        public Resource transform(
                HttpServletRequest request,
                Resource resource,
                ResourceTransformerChain transformerChain)
                throws IOException
        {
            if (resource.getURL().toString().endsWith("/index.html")) {
                String html = getHtmlContent(resource);
                html = overwritePetStore(html);
                return new TransformedResource(resource, html.getBytes());
            }
            else {
                return resource;
            }
        }

        private String overwritePetStore(String html)
        {
            return html.replace(PET_STORE_URL, SPRINGDOC_OPENAPI_URL);
        }

        private String getHtmlContent(Resource resource)
        {
            try {
                InputStream inputStream = resource.getInputStream();
                java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                String content = s.next();
                inputStream.close();
                return content;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}