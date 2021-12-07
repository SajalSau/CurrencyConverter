package com.currency.converter.controller;

import com.currency.converter.model.Currency;
import com.currency.converter.service.CurrencyExchangeResolverService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController("CurrencyConverter")
@RequestMapping("/utilityServices")
public class CurrencyConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyConverter.class);

    private HttpServletRequest httpServletRequest;
    private CurrencyExchangeResolverService currencyExchangeResolverService;

    @Autowired
    public CurrencyConverter(HttpServletRequest httpServletRequest, CurrencyExchangeResolverService currencyExchangeResolverService) {
        this.currencyExchangeResolverService = currencyExchangeResolverService;
        this.httpServletRequest = httpServletRequest;
    }

    @ApiOperation(value = "Convert the input", nickname = "convert", response = Currency.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Conversion happened successfully", response = Currency.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @PostMapping(value = "/convert/currency",
            produces = {"application/json"},
            consumes = {"application/json"})
    public ResponseEntity<String> convert(@ApiParam(value = "Source Currency", required = true) @Valid @RequestBody Currency currency) {

        String resultCurrency = currencyExchangeResolverService.getConvertedCurrency(currency);
        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI())).body(resultCurrency);

    }

}
