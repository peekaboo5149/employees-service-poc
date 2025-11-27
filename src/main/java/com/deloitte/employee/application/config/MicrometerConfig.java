package com.deloitte.employee.application.config;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MicrometerConfig {

    @Bean
    public Tracer tracer(){
        return Tracer.NOOP;
    }

}
