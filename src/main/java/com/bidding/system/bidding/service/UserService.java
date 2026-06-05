package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.model.UserRequestDTO;
import com.bidding.system.bidding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service // registra esta classe como bean de serviço no contexto do Spring; permite injeção via @Autowired
public class UserService {

    @Autowired                   // injeta o bean UserRepository gerenciado pelo Spring
    private UserRepository repository;

    @Autowired                   // injeta o bean TokenService para gerar o JWT após login bem-sucedido
    private TokenService tokenService;

    // Registra um novo usuário após validar campos obrigatórios e unicidade do e-mail
    public void register(UserDTO user) {
        String message = "";
        if (user.getNome().isEmpty()) {
            message = "Nome não preenchido";        // campo nome ausente
        } else if (user.getEmail().isEmpty()) {
            message = "E-mail não preenchido";      // campo e-mail ausente
        } else if (user.getSenha().isEmpty()) {
            message = "Senha não preenchida";       // campo senha ausente
        } else if (user.getRole().isEmpty()) {
            user.setRole("FORNECEDOR");             // role padrão: se não informada pelo front-end, assume-se FORNECEDOR
        }
        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message); // lança 400 Bad Request com a mensagem de erro acumulada
        }
        if (repository.emailExiste(user.getEmail())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado"); // lança 409 Conflict se o e-mail já estiver em uso
        }
        repository.register(user); // persiste o usuário no banco após todas as validações passarem
    }

    // Autentica o usuário e retorna o token JWT; lança 400 se campos ausentes, 401 se credenciais incorretas
    public String logar(UserRequestDTO user) {
        String message = "";
        if (user.getEmail().isEmpty()) {
            message = "E-mail não preenchido";  // campo e-mail ausente no formulário de login
        } else if (user.getSenha().isEmpty()) {
            message = "Senha não preenchida";   // campo senha ausente no formulário de login
        }

        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message); // lança 400 Bad Request com a mensagem de erro
        }

        UserDTO loggedData = repository.login(user.getEmail(), user.getSenha()); // consulta o banco com email e senha; retorna UserDTO sem id se não encontrar
        if (loggedData == null || loggedData.getId() == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha incorretos."); // lança 401 Unauthorized se as credenciais não baterem
        }
        return tokenService.gerarToken(loggedData); // gera e retorna o token JWT assinado com id, nome e role do usuário
    }

    // Delega a verificação de unicidade de e-mail ao repositório; usado pelo endpoint GET /api/autenticar/verificar-email
    public boolean verificarEmail(String email) {
        return repository.emailExiste(email); // retorna true se o e-mail já estiver cadastrado
    }

    // Delega a verificação de unicidade de nome ao repositório; usado pelo endpoint GET /api/autenticar/verificar-nome
    public boolean verificarNome(String nome) {
        return repository.nomeExiste(nome); // retorna true se o nome já estiver em uso
    }
}