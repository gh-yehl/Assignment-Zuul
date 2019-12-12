package com.fullstack;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@FeignClient("service-security")
public interface TokenValidationService {

    @RequestMapping(value = "/verifyToken", method= RequestMethod.GET)
    public Map<String, String> verifyToken(@RequestParam("token") String token);

}
