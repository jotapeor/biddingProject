package com.bidding.system.bidding.controller;

import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.model.UserRequestDTO;
import com.bidding.system.bidding.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController                         // combina @Controller + @ResponseBody: todos os retornos são serializados como JSON no corpo da resposta
@RequestMapping("/api/autenticar")      // prefixo base de todas as rotas deste controller; rotas públicas (sem token JWT)
public class UserController {

    @Autowired                          // injeta o bean UserService gerenciado pelo Spring
    private UserService userService;

    @PostMapping("/registrar")          // mapeia POST /api/autenticar/registrar
    public String registrar(@RequestBody UserDTO user) { // @RequestBody desserializa o JSON do corpo da requisição para UserDTO
        userService.register(user);     // delega a validação e a persistência ao UserService
        return "Cadastro realizado com sucesso!";
    }

    @PostMapping("/logar")              // mapeia POST /api/autenticar/logar
    public String logar(@RequestBody UserRequestDTO user) { // @RequestBody desserializa apenas email e senha (sem id, nome e role)
        return userService.logar(user); // autentica o usuário e retorna o token JWT assinado para ser usado nas próximas requisições
    }

    @GetMapping("/verificar-email")     // mapeia GET /api/autenticar/verificar-email?email=<valor>
    public boolean verificarEmail(@RequestParam String email) { // @RequestParam vincula o parâmetro de query "email" ao argumento
        return userService.verificarEmail(email); // retorna true se o e-mail já estiver cadastrado, false caso contrário
    }

    @GetMapping("/verificar-nome")      // mapeia GET /api/autenticar/verificar-nome?nome=<valor>
    public boolean verificarNome(@RequestParam String nome) { // @RequestParam vincula o parâmetro de query "nome" ao argumento
        return userService.verificarNome(nome); // retorna true se o nome já estiver em uso, false caso contrário
    }
}