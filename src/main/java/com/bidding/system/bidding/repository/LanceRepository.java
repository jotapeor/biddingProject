package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.LanceDTO;
import com.bidding.system.bidding.model.MeuLanceDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LanceRepository {

    public int novoLance(LanceDTO lance) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "insert into lances (valor, data_lance, id_edital, id_usuario, vencedor) values (?, ?, ?, ?, FALSE)"
            );
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

    public void resetarVencedores(Long idEdital) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE lances SET vencedor = FALSE WHERE id_edital = ?"
            );
            stmt.setLong(1, idEdital);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void marcarVencedor(Long idLanceVencedor) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE lances SET vencedor = TRUE WHERE id = ?"
            );
            stmt.setLong(1, idLanceVencedor);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<LanceDTO> getLancesByEdital(Long idEdital) {
        List<LanceDTO> lances = new ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select l.*, u.nome as nome_fornecedor from lances l " +
                            "join usuarios u on l.id_usuario = u.id " +
                            "where l.id_edital = ? order by l.data_lance desc"
            );
            stmt.setLong(1, idEdital);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LanceDTO lance = new LanceDTO();
                lance.setId(rs.getLong("id"));
                lance.setValor(rs.getDouble("valor"));
                lance.setData_lance(rs.getTimestamp("data_lance").toLocalDateTime());
                lance.setId_edital(rs.getLong("id_edital"));
                lance.setId_usuario(rs.getLong("id_usuario"));
                lance.setNome_fornecedor(rs.getString("nome_fornecedor"));
                lance.setVencedor(rs.getBoolean("vencedor")); // Lê o campo vencedor persistido no banco
                lances.add(lance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lances;
    }

    public List<LanceDTO> getLancesByEditalAndUsuario(Long idEdital, Long idUsuario) {
        List<LanceDTO> lances = new ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select l.*, u.nome as nome_fornecedor from lances l " +
                            "join usuarios u on l.id_usuario = u.id " +
                            "where l.id_edital = ? and l.id_usuario = ? order by l.data_lance desc"
            );
            stmt.setLong(1, idEdital);
            stmt.setLong(2, idUsuario);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LanceDTO lance = new LanceDTO();
                lance.setId(rs.getLong("id"));
                lance.setValor(rs.getDouble("valor"));
                lance.setData_lance(rs.getTimestamp("data_lance").toLocalDateTime());
                lance.setId_edital(rs.getLong("id_edital"));
                lance.setId_usuario(rs.getLong("id_usuario"));
                lance.setNome_fornecedor(rs.getString("nome_fornecedor"));
                lance.setVencedor(rs.getBoolean("vencedor"));
                lances.add(lance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lances;
    }

    public List<MeuLanceDTO> getMeusLances(Long idUsuario) {
        List<MeuLanceDTO> lista = new ArrayList<>();
        try {
            Connection conn = Conexao.conectar();
            String sql =
                    "select l.id as id_lance, l.valor, l.data_lance, l.vencedor, " +
                            "e.id as id_edital, e.titulo, e.status " +
                            "from lances l " +
                            "join editais e on l.id_edital = e.id " +
                            "where l.id_usuario = ? " +
                            "order by l.data_lance desc";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MeuLanceDTO dto = new MeuLanceDTO();
                dto.setIdLance(rs.getLong("id_lance"));
                dto.setValor(rs.getDouble("valor"));
                dto.setDataLance(rs.getTimestamp("data_lance").toLocalDateTime());
                dto.setIdEdital(rs.getLong("id_edital"));
                dto.setTituloEdital(rs.getString("titulo"));
                dto.setStatusEdital(rs.getString("status"));
                dto.setVencedor(rs.getBoolean("vencedor")); // Campo persistido: lido diretamente do banco
                lista.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Long getIdLanceVencedor(Long idEdital) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select id from lances where id_edital = ? order by valor desc, id asc limit 1"
            );
            stmt.setLong(1, idEdital);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retorna null se não houver lances para o edital
    }

    public Long getIdFornecedorDoLance(Long idLance) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select id_usuario from lances where id = ?"
            );
            stmt.setLong(1, idLance);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id_usuario");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int contarLancesPorFornecedor(Long idEdital, Long idUsuario) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) as total from lances where id_edital = ? and id_usuario = ?"
            );
            stmt.setLong(1, idEdital);
            stmt.setLong(2, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}