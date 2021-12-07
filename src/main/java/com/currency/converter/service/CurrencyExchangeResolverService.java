package com.currency.converter.service;

import com.currency.converter.exchangeapi.client.ExchangeAPIClient;
import com.currency.converter.model.Currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyExchangeResolverService {

    private ExchangeAPIClient exchangeAPIClient;

    @Autowired
    public CurrencyExchangeResolverService(ExchangeAPIClient exchangeAPIClient) {
        this.exchangeAPIClient = exchangeAPIClient;
    }

    private Currency resolve(Currency currency) {
        return exchangeAPIClient.getConvertedCurrency(currency).orElse(null);
    }

    public String getConvertedCurrency(Currency currency){
        Currency resultCurrency = resolve(currency);
        return resultCurrency.getResultCurrency();
    }

}
