package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.EditalDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EditalRepository {

    public int novoEdital(EditalDTO edital) {
        try {
            Connection conn = Conexao.conectar();
            // Parâmetros posicionais (?) substituem concatenação de strings, prevenindo SQL Injection
            PreparedStatement stmt = conn.prepareStatement(
                    "insert into editais (titulo, descricao, data_fechamento, status) values (?, ?, ?, ?)"
            );
            stmt.setString(1, edital.getTitulo());
            stmt.setString(2, edital.getDescricao());
            // Converte LocalDateTime (Java 8+) para java.sql.Timestamp, que é o tipo aceito pelo DATETIME do MySQL
            stmt.setTimestamp(3, Timestamp.valueOf(edital.getData_fechamento()));
            stmt.setString(4, edital.getStatus());
            return stmt.executeUpdate(); // Retorna 1 se uma linha foi inserida com sucesso
        } catch (SQLException e) {
            e.printStackTrace(); // O EditalService interpreta o retorno 0 como falha e lança HTTP 500
        }
        return 0;
    }

    public List<EditalDTO> listaEdital() {
        List<EditalDTO> listaEdital = new ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("select * from editais");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { // Itera linha a linha no ResultSet enquanto houver registros
                EditalDTO edital = new EditalDTO();
                edital.setId(rs.getLong("id"));
                edital.setTitulo(rs.getString("titulo"));
                edital.setDescricao(rs.getString("descricao"));
                // Converte java.sql.Timestamp (retornado pelo JDBC) de volta para LocalDateTime do Java
                edital.setData_fechamento(rs.getTimestamp("data_fechamento").toLocalDateTime());
                edital.setStatus(rs.getString("status"));
                Long vencedor = rs.getLong("vencedor");
                // rs.wasNull() verifica se o último valor lido era NULL no banco (getLong retorna 0 para NULL)
                if (!rs.wasNull()) {
                    edital.setVencedor(vencedor);
                }
                listaEdital.add(edital);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaEdital;
    }

    public EditalDTO getById(Long id) {
        EditalDTO edital = null; // Permanece null se o ID não existir no banco
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("select * from editais where id = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) { // rs.next() retorna true apenas se existir exatamente um registro com este ID
                edital = new EditalDTO();
                edital.setId(rs.getLong("id"));
                edital.setTitulo(rs.getString("titulo"));
                edital.setDescricao(rs.getString("descricao"));
                edital.setData_fechamento(rs.getTimestamp("data_fechamento").toLocalDateTime());
                edital.setStatus(rs.getString("status"));
                Long vencedor = rs.getLong("vencedor");
                if (!rs.wasNull()) {
                    edital.setVencedor(vencedor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edital;
    }

    public List<EditalDTO> getEditaisAbertosExpirados() {
        List<EditalDTO> lista = new ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select * from editais where data_fechamento <= ? and status like 'ABERTO%'"
            );
            // Compara a data de fechamento com o momento atual do servidor para determinar expiração
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EditalDTO edital = new EditalDTO();
                edital.setId(rs.getLong("id"));
                edital.setTitulo(rs.getString("titulo"));
                edital.setDescricao(rs.getString("descricao"));
                edital.setData_fechamento(rs.getTimestamp("data_fechamento").toLocalDateTime());
                edital.setStatus(rs.getString("status"));
                Long vencedor = rs.getLong("vencedor");
                if (!rs.wasNull()) {
                    edital.setVencedor(vencedor);
                }
                lista.add(edital);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int contarLancesByEdital(Long idEdital) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) as total from lances where id_edital = ?"
            );
            stmt.setLong(1, idEdital);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total"); // COUNT(*) retorna 0 se não houver lances, nunca NULL
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void prorrogarEdital(Long idEdital) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "update editais set data_fechamento = date_add(data_fechamento, interval 3 day), " +
                            "status = 'ABERTO (ADIADO EM 3 DIAS DEVIDO À FALTA DE LANCES)' where id = ?"
            );
            stmt.setLong(1, idEdital);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarStatusEdital(Long idEdital, String novoStatus) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "update editais set status = ? where id = ?"
            );
            stmt.setString(1, novoStatus);
            stmt.setLong(2, idEdital);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarVencedorEdital(Long idEdital, Long idVencedor) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "update editais set vencedor = ? where id = ?"
            );
            stmt.setLong(1, idVencedor);
            stmt.setLong(2, idEdital);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}