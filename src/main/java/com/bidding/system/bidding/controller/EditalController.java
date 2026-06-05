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

@RestController                       // combina @Controller + @ResponseBody: todos os retornos são serializados como JSON no corpo da resposta
@RequestMapping("/api/editais")       // prefixo base de todas as rotas deste controller; todas exigem token JWT no header Authorization
public class EditalController {

    @Autowired                        // injeta o bean EditalService gerenciado pelo Spring
    private EditalService editalService;

    @Autowired                        // injeta o bean TokenService para extrair dados do JWT quando necessário
    private TokenService tokenService;

    @Autowired                        // injeta o bean LanceService para as rotas de lances vinculadas ao edital
    private LanceService lanceService;

    @PostMapping("/criar")            // mapeia POST /api/editais/criar
    public String criarEdital(@RequestBody EditalDTO edital, @RequestHeader("Authorization") String authHeader) { // @RequestBody desserializa o JSON; @RequestHeader extrai o token do header
        String token = authHeader.replace("Bearer ", ""); // remove o prefixo "Bearer " para obter o token puro
        UserDTO usuarioLogado = tokenService.extrairClaim(token); // extrai id, nome e role do payload do JWT sem consultar o banco
        editalService.novoEdital(edital, usuarioLogado); // delega a validação (role COMPRADOR, campos obrigatórios) e a persistência ao EditalService
        return "Edital criado com sucesso";
    }

    @GetMapping                       // mapeia GET /api/editais
    public List<EditalDTO> listaEdital(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "urgente", required = false, defaultValue = "false") boolean urgente // parâmetro opcional: ?urgente=true filtra editais fechando em até 48h; false retorna todos
    ) {
        String token = authHeader.replace("Bearer ", ""); // remove o prefixo "Bearer " para obter o token puro
        return editalService.listaEdital(token, urgente); // valida o token e retorna a lista (filtrada ou completa)
    }

    @GetMapping("/{id}")              // mapeia GET /api/editais/{id}
    public EditalDTO buscarEdital(
            @PathVariable Long id,    // @PathVariable extrai o segmento {id} da URL e converte para Long
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", ""); // remove o prefixo "Bearer " para obter o token puro
        return editalService.buscarEdital(id, token); // valida o token e retorna o edital; lança 404 se não encontrado
    }

    @PostMapping("/{id}/lances")      // mapeia POST /api/editais/{id}/lances
    public String novoLance(@PathVariable Long id, @RequestHeader("Authorization") String authHeader, @RequestBody LanceDTO lance) {
        String token = authHeader.replace("Bearer ", ""); // remove o prefixo "Bearer " para obter o token puro
        lanceService.novoLance(id, lance, token); // valida o token, a role (FORNECEDOR), o status do edital e o prazo antes de persistir
        return "Lance feito com sucesso!";
    }

    @GetMapping("/{id}/lances")       // mapeia GET /api/editais/{id}/lances
    public List<LanceDTO> listarLances(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", ""); // remove o prefixo "Bearer " para obter o token puro
        return lanceService.listarLances(id, token); // aplica as regras de visibilidade (ABERTO/FECHADO × FORNECEDOR/COMPRADOR) e retorna os lances
    }
}