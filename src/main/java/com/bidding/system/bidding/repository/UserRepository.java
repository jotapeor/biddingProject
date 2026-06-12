package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.UserDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class UserRepository {

    public void register(UserDTO user) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "insert into usuarios (nome, email, senha, role) values (?, ?, ?, ?)"
            );
            stmt.setString(1, user.getNome());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getSenha()); // Armazenada em texto puro nesta versão didática; em produção deve ser hasheada (BCrypt)
            stmt.setString(4, user.getRole());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                // Salvaguarda defensiva: lança exceção se o INSERT não afetou nenhuma linha (ex: trigger bloqueando)
                throw new SQLException("Falha na atualização - Nenhuma linha foi encontrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserDTO login(String email, String senha) {
        UserDTO user = new UserDTO(); // Objeto vazio; só será populado se as credenciais forem válidas
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select * from usuarios where email = ? and senha = ?"
            );
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) { // rs.next() retorna true apenas se email + senha correspondem a um registro
                user.setId(rs.getLong("id"));
                user.setNome(rs.getString("nome"));
                user.setEmail(rs.getString("email"));
                user.setSenha(rs.getString("senha"));
                user.setRole(rs.getString("role"));
            }
            // Se rs.next() retornou false, o objeto "user" permanece sem id — o UserService lança 401
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean emailExiste(String email) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) from usuarios where email = ?"
            );
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // rs.getInt(1) lê a primeira coluna (o resultado do COUNT); > 0 significa que o e-mail existe
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean nomeExiste(String nome) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) from usuarios where nome = ?"
            );
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}