package com.jkingai.pokertutor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class SpaForwardingConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        // Serve the actual file if it exists, otherwise forward to index.html for SPA routing
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // Don't forward API requests
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        // Forward paths without file extensions to index.html (SPA routes)
                        if (!resourcePath.contains(".")) {
                            Resource indexResource = new ClassPathResource("/static/index.html");
                            if (indexResource.exists()) {
                                return indexResource;
                            }
                        }
                        return null;
                    }
                });
    }
}
