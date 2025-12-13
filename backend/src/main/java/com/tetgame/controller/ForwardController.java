package com.tetgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forward all non-API routes to index.html for SPA
 */
@Controller
public class ForwardController {

    @GetMapping("/")
    public String index() {
        return "forward:/templates/index.html";
    }

    /**
     * Forward all routes except /api/** to index.html
     * This allows frontend to handle client-side routing
     */
    @GetMapping({"/{path:(?!api|actuator|swagger-ui|v3|images|static|css|js).*}", "/**/{path:(?!api|actuator|swagger-ui|v3|images|static|css|js).*}"})
    public String forward() {
        return "forward:/templates/index.html";
    }
}

