package com.ethan.apigatewaytry.config;

import com.ethan.apigatewaytry.interceptor.SignatureVerifyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class SpringBootConfig extends WebMvcConfigurerAdapter {

    @Autowired
    SignatureVerifyInterceptor signatureVerifyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(signatureVerifyInterceptor).addPathPatterns("/**");
    }

}
