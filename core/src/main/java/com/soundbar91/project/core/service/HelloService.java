package com.soundbar91.project.core.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String getGreeting(String name) {
        if (name == null || name.isBlank()) {
            return "Hello, Guest!";
        }
        return "Hello, " + name + "!";
    }
}
