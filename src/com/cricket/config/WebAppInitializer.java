package com.cricket.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { DataSourceConfig.class, SecurityConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebMvcConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {

        MultipartConfigElement multipartConfig =
                new MultipartConfigElement(
                        null,              // location
                        100_000_000,       // maxFileSize (100MB)
                        100_000_000,       // maxRequestSize
                        0                  // fileSizeThreshold
                );

        registration.setMultipartConfig(multipartConfig);
    }
}
