package com.tetgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forward all non-API routes to index.html for React Router SPA
 */
@Controller
public class ForwardController {

    /**
     * Forward all routes except /api/** and static resources to index.html
     * This allows React Router to handle client-side routing
     */
    @GetMapping({"/", "/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
