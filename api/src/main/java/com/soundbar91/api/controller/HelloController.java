package com.soundbar91.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soundbar91.domain.service.HelloService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @GetMapping
    public String hello(@RequestParam(required = false) String name) {
        return helloService.getGreeting(name);
    }
}
