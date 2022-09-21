package com.yrys.weather.controllers;

import com.yrys.weather.service.DataExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class GeoRestController {

    @Autowired
    DataExtractorService dataExtractorService;

    @GetMapping("/cities/population/{city}")
    public ResponseEntity<?> getData(@PathVariable String city) {
        return ResponseEntity.status(HttpStatus.OK).body(dataExtractorService.extractCityPopulation(city));
    }
}
