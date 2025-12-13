package com.tetgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve frontend HTML files (static resources)
 */
@Controller
public class ForwardController {

    /**
     * Serve index page from static/templates/index.html
     * Spring's ResourceHandlerRegistry maps /templates/** to classpath:/static/templates/
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/templates/index.html";
    }
}

