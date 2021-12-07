package com.currency.converter.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Currency {

    @NotBlank
    private String inputCurrency;
    private String resultCurrency;
    @NotNull
    private BigDecimal monetaryValue;

    public String getInputCurrency() {
        return inputCurrency;
    }

    public void setInputCurrency(String inputCurrency) {
        this.inputCurrency = inputCurrency;
    }

    public String getResultCurrency() {
        return resultCurrency;
    }

    public void setResultCurrency(String resultCurrency) {
        this.resultCurrency = resultCurrency;
    }

    public BigDecimal getMonetaryValue() {
        return monetaryValue;
    }

    public void setMonetaryValue(BigDecimal monetaryValue) {
        this.monetaryValue = monetaryValue;
    }
}
