package com.koyokoyo.community.community.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {

    @Bean
    public SimpleDateFormat s()
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
