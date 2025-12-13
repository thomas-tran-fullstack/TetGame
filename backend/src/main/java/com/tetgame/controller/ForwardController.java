package com.tetgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve frontend HTML files directly (avoid forward loops)
 */
@Controller
public class ForwardController {

    /**
     * Serve index page
     */
    @GetMapping("/")
    public String index() {
        return "index";  // Thymeleaf/ViewResolver sẽ tìm templates/index.html
    }
}

