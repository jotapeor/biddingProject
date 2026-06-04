package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.model.UserRequestDTO;
import com.bidding.system.bidding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    public void register(UserDTO user) {
        String message = "";
        if (user.getNome().isEmpty()) {
            message = "Nome não preenchido";
        } else if (user.getEmail().isEmpty()) {
            message = "E-mail não preenchido";
        } else if (user.getSenha().isEmpty()) {
            message = "Senha não preenchida";
        } else if (user.getRole().isEmpty()) {
            user.setRole("FORNECEDOR");
        }
        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message);
        }
        if (repository.emailExiste(user.getEmail())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado");
        }
        repository.register(user);
    }

    public String logar(UserRequestDTO user) {
        String message = "";
        if (user.getEmail().isEmpty()) {
            message = "E-mail não preenchido";
        } else if (user.getSenha().isEmpty()) {
            message = "Senha não preenchida";
        }

        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message);
        }

        UserDTO loggedData = repository.login(user.getEmail(), user.getSenha());
        if (loggedData == null || loggedData.getId() == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha incorretos.");
        }
        return tokenService.gerarToken(loggedData);
    }

    public boolean verificarEmail(String email) {
        return repository.emailExiste(email);
    }

    public boolean verificarNome(String nome) {
        return repository.nomeExiste(nome);
    }
}