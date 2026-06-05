package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.UserDTO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository // marca a classe como componente de acesso a dados do Spring; permite injeção via @Autowired nos Services
public class UserRepository {

    // Insere um novo usuário na tabela "usuarios"; os campos já foram validados pelo UserService antes de chegar aqui
    public void register(UserDTO user) {
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("insert into usuarios (nome, email, senha, role) values (?, ?, ?, ?)"); // prepara o INSERT com ? para prevenir SQL Injection
            stmt.setString(1, user.getNome());   // substitui o 1º ? pelo nome do usuário
            stmt.setString(2, user.getEmail());  // substitui o 2º ? pelo e-mail
            stmt.setString(3, user.getSenha());  // substitui o 3º ? pela senha (armazenada em texto puro nesta versão)
            stmt.setString(4, user.getRole());   // substitui o 4º ? pela role ("FORNECEDOR" ou "COMPRADOR")

            int AffectedRows = stmt.executeUpdate(); // executa o INSERT e armazena o número de linhas afetadas
            if (AffectedRows == 0) { // verificação defensiva: se o INSERT não afetou nenhuma linha, algo falhou silenciosamente
                throw new SQLException("Falha na atualização - Nenhuma linha foi encontrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Autentica o usuário consultando email e senha no banco; retorna UserDTO sem id se as credenciais não baterem
    public UserDTO login(String email, String senha) {
        UserDTO user = new UserDTO(); // objeto vazio; só será populado se as credenciais forem válidas
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("select * from usuarios where email = ? and senha = ?"); // verifica email e senha na mesma query
            stmt.setString(1, email); // substitui o 1º ? pelo e-mail informado
            stmt.setString(2, senha); // substitui o 2º ? pela senha informada
            ResultSet rs = stmt.executeQuery(); // executa a query

            if (rs.next()) { // rs.next() retorna true apenas se email + senha correspondem a um registro existente
                user.setId(rs.getLong("id"));
                user.setNome(rs.getString("nome"));
                user.setEmail(rs.getString("email"));
                user.setSenha(rs.getString("senha"));
                user.setRole(rs.getString("role"));
            }
            // se rs.next() retornou false, o objeto "user" permanece sem id — o UserService detecta isso e lança 401
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    // Verifica se um e-mail já está cadastrado no banco; retorna true se existir, false caso contrário
    // Usado pelo UserService para evitar cadastros duplicados antes de chamar register()
    public boolean emailExiste(String email) {
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) from usuarios where email = ?" // COUNT(*) conta quantas linhas existem com esse e-mail
            );
            stmt.setString(1, email); // substitui o ? pelo e-mail a verificar
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // rs.getInt(1) lê a primeira coluna do resultado (o COUNT); se > 0, o e-mail já existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // retorna false em caso de erro ou se o e-mail não existir
    }

    // Verifica se um nome de usuário já está em uso; retorna true se existir, false caso contrário
    // Chamado pelo endpoint GET /api/autenticar/verificar-nome para feedback em tempo real no formulário de cadastro
    public boolean nomeExiste(String nome) {
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) from usuarios where nome = ?" // COUNT(*) conta quantas linhas existem com esse nome
            );
            stmt.setString(1, nome); // substitui o ? pelo nome a verificar
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // rs.getInt(1) lê o COUNT; se > 0, o nome já está em uso
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}