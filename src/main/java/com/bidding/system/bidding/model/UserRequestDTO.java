package com.bidding.system.bidding.model;

// DTO mínimo de login: contém apenas os campos necessários para autenticar o usuário.
// Separado de UserDTO para que o front-end não precise enviar id, nome e role ao fazer login.
// Recebido via @RequestBody no endpoint POST /api/autenticar/logar.
public class UserRequestDTO {

    private String email; // e-mail informado no formulário de login
    private String senha; // senha informada no formulário de login

    // Construtor padrão exigido pelo Jackson para desserializar o JSON do corpo da requisição
    public UserRequestDTO() {
    }

    public UserRequestDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

}