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
        if (user.getNome() == null || user.getNome().trim().isEmpty()) {
            message += "O nome não pode ser vazio. ";
        } else if (user.getNome().trim().length() < 3) {
            message += "Insira um nome válido (mínimo de 3 letras). ";
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            message += "O e-mail não pode ser vazio. ";
        } else if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            // Regex básico de e-mail: garante a presença de um caractere '@' seguido de domínio
            message += "Insira um e-mail válido (ex: usuario@email.com). ";
        }
        if (user.getSenha() == null || user.getSenha().trim().isEmpty()) {
            message += "A senha não pode ser vazia. ";
        } else {
            String s = user.getSenha();
            if (s.length() < 8) {
                message += "A senha deve ter pelo menos 8 caracteres. ";
            } else if (!s.matches(".*[A-Z].*")) {
                message += "A senha deve conter pelo menos uma letra maiúscula. ";
            } else if (!s.matches(".*[0-9].*")) {
                message += "A senha deve conter pelo menos um número. ";
            } else if (!s.matches(".*[!@#$%^&*(),.?\":{}|<>\\-_+=\\[\\]].*")) {
                message += "A senha deve conter pelo menos um caractere especial. ";
            }
        }
        if (user.getConfirmarSenha() == null || user.getConfirmarSenha().trim().isEmpty()) {
            message += "A confirmação de senha não pode ser vazia. ";
        } else if (!user.getSenha().equals(user.getConfirmarSenha())) {
            message += "As senhas não coincidem. ";
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            // Role padrão: o registro público é exclusivo para FORNECEDORs; COMPRADORs são cadastrados manualmente
            user.setRole("FORNECEDOR");
        }
        if (!message.trim().isEmpty()) {
            // Lança 400 Bad Request com todas as mensagens de erro acumuladas
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message.trim());
        }
        if (repository.emailExiste(user.getEmail())) {
            // Lança 409 Conflict especificamente para duplicidade de e-mail (semântica HTTP correta)
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado");
        }
        repository.register(user); // Persiste o usuário após todas as validações passarem
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
            // O repositório retorna objeto sem ID quando as credenciais não batem; lançamos 401
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha incorretos.");
        }
        return tokenService.gerarToken(loggedData); // Gera e retorna o token JWT assinado
    }

    public boolean verificarEmail(String email) {
        return repository.emailExiste(email);
    }

    public boolean verificarNome(String nome) {
        return repository.nomeExiste(nome);
    }
}