package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.EditalDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EditalRepository {

    public int novoEdital(EditalDTO edital) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("insert into editais (titulo, descricao, data_fechamento, status) values (?, ?, ?, ?)");
            stmt.setString(1, edital.getTitulo());
            stmt.setString(2, edital.getDescricao());
            stmt.setTimestamp(3, Timestamp.valueOf(edital.getData_fechamento()));
            stmt.setString(4, edital.getStatus());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<EditalDTO> listaEdital() {
        List<EditalDTO> listaEdital = new ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("select * from editais");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                EditalDTO edital = new EditalDTO();
                edital.setId(rs.getLong("id"));
                edital.setTitulo(rs.getString("titulo"));
                edital.setDescricao(rs.getString("descricao"));
                edital.setData_fechamento(
                        rs.getTimestamp("data_fechamento").toLocalDateTime()
                );
                edital.setStatus(rs.getString("status"));
                listaEdital.add(edital);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaEdital;
    }

    public EditalDTO getById(Long id) {
        EditalDTO edital = null;
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement("select * from editais where id = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                edital = new EditalDTO();
                edital.setId(rs.getLong("id"));
                edital.setTitulo(rs.getString("titulo"));
                edital.setDescricao(rs.getString("descricao"));
                edital.setData_fechamento(
                        rs.getTimestamp("data_fechamento").toLocalDateTime()
                );
                edital.setStatus(rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edital;
    }
}