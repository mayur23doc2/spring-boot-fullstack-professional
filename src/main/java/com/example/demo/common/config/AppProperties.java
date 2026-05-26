package com.example.demo.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        List<String> corsAllowedOrigins,
        int defaultPageSize,
        int maxPageSize
) {}
