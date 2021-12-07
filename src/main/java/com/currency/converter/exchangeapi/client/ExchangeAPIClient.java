package com.currency.converter.exchangeapi.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.currency.converter.EnvironmentVariables;
import com.currency.converter.TimeProvider;
import com.currency.converter.model.Currency;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class ExchangeAPIClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeAPIClient.class);

    class ExchangeApiResult {
        long expires;
        Currency currency;

        ExchangeApiResult(Currency currency, long timeout) {
            this.expires = timeout > 0 ? timeProvider.currentTime() + timeout : 0;
            this.currency = currency;
        }
    }

    private EnvironmentVariables environmentVariables;
    private TimeProvider timeProvider;


    private final LoadingCache<Currency, ExchangeApiResult> exchangeApiCache = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .build(exchangeApiLoader());

    @Autowired
    public ExchangeAPIClient(
            EnvironmentVariables environmentVariables,
            TimeProvider timeProvider
    ) {
        this.environmentVariables = environmentVariables;
        this.timeProvider = timeProvider;
    }

    ExchangeApiResult forCurrency(Currency currency) {
        return new ExchangeApiResult(currency, environmentVariables.successfulExpire);
    }

    ExchangeApiResult forUnknownCurrency() {
        return new ExchangeApiResult(null, environmentVariables.unknownExpire);
    }

    ExchangeApiResult forLookupFailure() {
        return new ExchangeApiResult(null, environmentVariables.failedExpire);
    }


    public Optional<Currency> getConvertedCurrency(Currency currency) {

        try {
            ExchangeApiResult result = exchangeApiCache.get(currency);
            if (result.expires != 0 && result.expires < timeProvider.currentTime()) {
                LOGGER.info("Currency cache expiration reached for {}", currency.getInputCurrency());
                result.expires = 0;
                exchangeApiCache.refresh(currency);
                result = exchangeApiCache.get(currency);
            }
            return Optional.ofNullable(result.currency);
        } catch (ExecutionException e) {
            LOGGER.error("Failed to read currency result ({}) from cache", currency, e);
            return Optional.empty();
        }
    }

    private CacheLoader<Currency, ExchangeApiResult> exchangeApiLoader() {
        return new CacheLoader<Currency, ExchangeApiResult>() {
            @Override
            public ExchangeApiResult load(Currency currency) {
                LOGGER.info("Loading target currency from currency exchange using input currency: {}", currency.getInputCurrency());
                String url = "url not set";

                try {
                    /**
                     * base param is not included in the current subscription
                     */
                    url = environmentVariables.exchangeUrl + "?access_key=" + environmentVariables.apiKey;
                    //url = url+"&base={base}";
                    url = url + "&symbols={symbols}";
                    //url = url.replaceFirst("\\{base}", base.getInputCurrency());
                    url = url.replaceFirst("\\{symbols}", currency.getResultCurrency());

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Accept", "text/plain");
                    headers.add("Content-Type", "application/json");
                    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

                    RestTemplate restTemplate = new RestTemplate();
                    LOGGER.info("Request: GET {}", url);
                    ResponseEntity<String> response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );
                    LOGGER.info("Response for GET {}: status={}, body={}", url, response.getStatusCode(), response.getBody());
                    if (HttpStatus.OK.equals(response.getStatusCode())) {
                        String result = response.getBody();
                        if (result != null && !result.isEmpty()) {
                            JSONParser parse = new JSONParser();
                            JSONObject jobj = (JSONObject) parse.parse(result);
                            JSONObject targetRates = (JSONObject) jobj.get("rates");
                            LOGGER.info("Target rates found from the exchange API :" + targetRates);
                            JSONArray resultArray = new JSONArray();
                            JSONObject obj = new JSONObject();
                            String[] targetCurrencies = currency.getResultCurrency().split(",");
                            for (int i = 0; i < targetCurrencies.length; i++) {
                                Object targetRate = targetRates.get(targetCurrencies[i]);
                                LOGGER.info("individual currency rate :" + targetRate);
                                obj.put(targetCurrencies[i], BigDecimal.valueOf(Double.valueOf(targetRate.toString())).multiply(currency.getMonetaryValue()));
                            }
                            JSONObject rates = new JSONObject();
                            JSONObject monetaryValues = new JSONObject();
                            rates.put("rates", targetRates);
                            monetaryValues.put("monetaryValues", obj);
                            resultArray.add(rates);
                            resultArray.add(monetaryValues);
                            currency.setResultCurrency(resultArray.toJSONString());
                            return forCurrency(currency);
                        } else {
                            LOGGER.error("Currency exchange responded with result currency, which seem to be empty. status={}, body={}", response.getStatusCode(), response.getBody());
                            return forUnknownCurrency();
                        }
                    } else if (HttpStatus.NO_CONTENT.equals(response.getStatusCode()) || HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
                        LOGGER.warn("Currency exchange could not find value by given input currency. status={}, body={}", response.getStatusCode(), response.getBody());
                        return forUnknownCurrency();
                    } else {
                        LOGGER.error("Currency Exchange returned unknown response. status={}, body={}", response.getStatusCode(), response.getBody());
                        return forLookupFailure();
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load currency from currency exchange by input {} into cache using url: {}", url, e);
                    return forLookupFailure();
                }
            }
        };
    }


}
