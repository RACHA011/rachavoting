package com.racha.rachavoting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    

}
