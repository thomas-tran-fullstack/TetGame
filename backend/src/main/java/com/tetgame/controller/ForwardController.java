package com.tetgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forward all non-API routes to index.html for React Router SPA
 */
@Controller
public class ForwardController {

    /**
     * Forward all routes except /api/** to index.html
     * This allows React Router to handle client-side routing
     */
    @GetMapping({"", "/{path:(?!api|actuator|swagger-ui|v3|images|static).*}", "/**/{path:(?!api|actuator|swagger-ui|v3|images|static).*}"})
    public String forward() {
        return "forward:/index.html";
    }
}

