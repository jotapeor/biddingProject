package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.LanceDTO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class LanceRepository {

    public int novoLance(LanceDTO lance) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("insert into lances (valor, data_lance, id_edital, id_usuario) values (?, ?, ?, ?)");
            stmt.setDouble(1, lance.getValor());
            stmt.setTimestamp(2, Timestamp.valueOf(lance.getData_lance()));
            stmt.setLong(3, lance.getId_edital());
            stmt.setLong(4, lance.getId_usuario());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public java.util.List<LanceDTO> getLancesByEdital(Long idEdital) {
        java.util.List<LanceDTO> lances = new java.util.ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("select * from lances where id_edital = ? order by data_lance desc");
            stmt.setLong(1, idEdital);
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LanceDTO lance = new LanceDTO();
                lance.setId(rs.getLong("id"));
                lance.setValor(rs.getDouble("valor"));
                lance.setData_lance(rs.getTimestamp("data_lance").toLocalDateTime());
                lance.setId_edital(rs.getLong("id_edital"));
                lance.setId_usuario(rs.getLong("id_usuario"));
                lances.add(lance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lances;
    }

    public java.util.List<LanceDTO> getLancesByEditalAndUsuario(Long idEdital, Long idUsuario) {
        java.util.List<LanceDTO> lances = new java.util.ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("select * from lances where id_edital = ? and id_usuario = ? order by data_lance desc");
            stmt.setLong(1, idEdital);
            stmt.setLong(2, idUsuario);
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LanceDTO lance = new LanceDTO();
                lance.setId(rs.getLong("id"));
                lance.setValor(rs.getDouble("valor"));
                lance.setData_lance(rs.getTimestamp("data_lance").toLocalDateTime());
                lance.setId_edital(rs.getLong("id_edital"));
                lance.setId_usuario(rs.getLong("id_usuario"));
                lances.add(lance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lances;
    }

    public java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> getMeusLances(Long idUsuario) {
        java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> lista = new java.util.ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            String sql = "SELECT l.id as id_lance, l.valor, l.data_lance, e.id as id_edital, e.titulo, e.status " +
                         "FROM lances l " +
                         "JOIN editais e ON l.id_edital = e.id " +
                         "WHERE l.id_usuario = ? " +
                         "ORDER BY l.data_lance DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idUsuario);
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                com.bidding.system.bidding.model.MeuLanceDTO dto = new com.bidding.system.bidding.model.MeuLanceDTO();
                dto.setIdLance(rs.getLong("id_lance"));
                dto.setValor(rs.getDouble("valor"));
                dto.setDataLance(rs.getTimestamp("data_lance").toLocalDateTime());
                dto.setIdEdital(rs.getLong("id_edital"));
                dto.setTituloEdital(rs.getString("titulo"));
                dto.setStatusEdital(rs.getString("status"));
                lista.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Double getMenorLanceByEdital(Long idEdital) {
        Double menorValor = null;
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("SELECT MIN(valor) as menor_valor FROM lances WHERE id_edital = ?");
            stmt.setLong(1, idEdital);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double val = rs.getDouble("menor_valor");
                if (!rs.wasNull()) {
                    menorValor = val;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menorValor;
    }
}