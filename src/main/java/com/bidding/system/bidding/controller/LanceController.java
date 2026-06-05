package com.bidding.system.bidding.controller;

import com.bidding.system.bidding.model.MeuLanceDTO;
import com.bidding.system.bidding.service.LanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController                    // combina @Controller + @ResponseBody: todos os retornos são serializados como JSON no corpo da resposta
@RequestMapping("/api/lances")     // prefixo base das rotas deste controller; separado de EditalController pois trata da visão pessoal do FORNECEDOR
public class LanceController {

    @Autowired                     // injeta o bean LanceService gerenciado pelo Spring
    private LanceService lanceService;

    @GetMapping("/meus-lances")    // mapeia GET /api/lances/meus-lances
    public List<MeuLanceDTO> getMeusLances(@RequestHeader("Authorization") String authHeader) { // @RequestHeader extrai o token JWT do header Authorization
        String token = authHeader.replace("Bearer ", ""); // remove o prefixo "Bearer " para obter o token puro
        return lanceService.getMeusLances(token); // valida o token, verifica se é FORNECEDOR e retorna os lances com o campo "vencedor" calculado
    }
}
