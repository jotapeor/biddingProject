package com.bidding.system.bidding.model;

// DTO completo de usuário: usado no cadastro (POST /registrar) e como payload extraído do JWT pelo TokenService
public class UserDTO {

    private Long id;       // identificador único gerado pelo banco (auto_increment)
    private String nome;   // nome de exibição do usuário
    private String email;  // e-mail usado no login; deve ser único na tabela usuarios
    private String senha;  // senha em texto puro (sem hash nesta versão)
    private String role;   // perfil do usuário: "FORNECEDOR" (envia lances) ou "COMPRADOR" (cria editais)

    // Construtor padrão exigido pelo Jackson para desserializar o JSON do corpo da requisição
    public UserDTO() {
    }

    // Construtor completo utilizado ao montar o objeto após leitura do banco de dados
    public UserDTO(Long id, String nome, String email, String senha, String role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}