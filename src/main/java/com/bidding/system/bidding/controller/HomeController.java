package com.bidding.system.bidding.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "A API do sistema de Bidding está online e rodando perfeitamente!";
    }

    @GetMapping("/api/home")
    public String apiHome() {
        return "Bem-vindo ao sistema de Bidding! A rota /api/home está funcionando.";
    }
}
