package com.bidding.system.bidding.controller;

import com.bidding.system.bidding.model.MeuLanceDTO;
import com.bidding.system.bidding.service.LanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lances")
public class LanceController {

    @Autowired
    private LanceService lanceService;

    @GetMapping("/meus-lances")
    public List<MeuLanceDTO> getMeusLances(@RequestHeader("Authorization") String authHeader) {
        // Remove o prefixo "Bearer " para obter o token JWT puro antes de passá-lo ao LanceService
        String token = authHeader.replace("Bearer ", "");
        return lanceService.getMeusLances(token);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public String deletarLance(@org.springframework.web.bind.annotation.PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        lanceService.deletarLance(id, token);
        return "Lance deletado com sucesso";
    }
}
