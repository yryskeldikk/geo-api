package com.yrys.weather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class DataExtractorService {

    @Value("${app.api.country.url}")
    private String countryUrl;

    @Value("${app.api.weather.url}")
    private String weatherUrl;

    @Autowired
    RestTemplate restTemplate;

    public Object extractCityPopulation(String cityName) {
        try {
            String url = countryUrl + "/population/cities";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> params = new HashMap<>();
            params.put("city", cityName);
            HttpEntity request = new HttpEntity(params, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, request, Map.class);
            Map data = resp.getBody();
            return data;
        } catch (HttpClientErrorException ex) {
            return ex.getResponseBodyAsString();
        }
    }
}
