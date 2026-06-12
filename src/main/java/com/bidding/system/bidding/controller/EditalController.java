package com.bidding.system.bidding.controller;

import com.bidding.system.bidding.model.EditalDTO;
import com.bidding.system.bidding.model.LanceDTO;
import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.service.EditalService;
import com.bidding.system.bidding.service.LanceService;
import com.bidding.system.bidding.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/editais")
public class EditalController {

    @Autowired
    private EditalService editalService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private LanceService lanceService;

    @PostMapping("/criar")
    public String criarEdital(@RequestBody EditalDTO edital, @RequestHeader("Authorization") String authHeader) {
        // Remove o prefixo "Bearer " para obter o token JWT puro antes de passá-lo ao TokenService
        String token = authHeader.replace("Bearer ", "");
        // Extrai id, nome e role do payload JWT sem consultar o banco de dados
        UserDTO usuarioLogado = tokenService.extrairClaim(token);
        editalService.novoEdital(edital, usuarioLogado);
        return "Edital criado com sucesso";
    }

    @GetMapping
    public List<EditalDTO> listaEdital(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "urgente", required = false, defaultValue = "false") boolean urgente
    ) {
        // Remove o prefixo "Bearer " para obter o token JWT puro
        String token = authHeader.replace("Bearer ", "");
        return editalService.listaEdital(token, urgente);
    }

    @GetMapping("/{id}")
    public EditalDTO buscarEdital(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        // Remove o prefixo "Bearer " para obter o token JWT puro
        String token = authHeader.replace("Bearer ", "");
        return editalService.buscarEdital(id, token);
    }

    @PostMapping("/{id}/lances")
    public String novoLance(@PathVariable Long id, @RequestHeader("Authorization") String authHeader, @RequestBody LanceDTO lance) {
        // Remove o prefixo "Bearer " para obter o token JWT puro
        String token = authHeader.replace("Bearer ", "");
        lanceService.novoLance(id, lance, token);
        return "Lance feito com sucesso!";
    }

    @GetMapping("/{id}/lances")
    public List<LanceDTO> listarLances(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        // Remove o prefixo "Bearer " para obter o token JWT puro
        String token = authHeader.replace("Bearer ", "");
        return lanceService.listarLances(id, token);
    }
}