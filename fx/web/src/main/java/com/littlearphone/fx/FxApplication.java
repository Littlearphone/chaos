package com.littlearphone.fx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class FxApplication {
    public static void main(String[] args) {
        SpringApplication.run(FxApplication.class, args);
    }
    
    /**
     * <a href="https://www.cnblogs.com/Baker-Street/p/12918295.html">https://www.cnblogs.com/Baker-Street/p/12918295.html</a>
     */
    @Configuration
    public static class WebMvcConfig implements WebMvcConfigurer {
        /**
         * 增加图片转换器
         */
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(new BufferedImageHttpMessageConverter());
        }
    }
}
