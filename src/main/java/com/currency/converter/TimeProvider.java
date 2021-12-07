package com.currency.converter;

import org.springframework.stereotype.Component;

@Component
public class TimeProvider {

    public long currentTime() {
        return System.currentTimeMillis();
    }
}
