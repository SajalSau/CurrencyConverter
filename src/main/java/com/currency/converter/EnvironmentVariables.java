package com.currency.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Component
public class EnvironmentVariables {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentVariables.class);

    @Value("${ECM_LOOKUP_FAILED_EXPIRE:5000}")
    public long failedExpire;

    @Value("${ECM_LOOKUP_UNKNOWN_EXPIRE:60000}")
    public long unknownExpire;

    @Value("${ECM_LOOKUP_SUCCESSFUL_EXPIRE:0}")
    public long successfulExpire;

    @Value( "${currency.exchange.baseUrl}" )
    public String exchangeUrl;

    @Value( "${currency.exchange.apikey}" )
    public String apiKey;

}
