package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.EditalDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository // marca a classe como componente de acesso a dados do Spring; permite injeção via @Autowired nos Services
public class EditalRepository {

    // Insere um novo edital na tabela "editais"; retorna o número de linhas afetadas (1 = sucesso, 0 = falha)
    public int novoEdital(EditalDTO edital) {
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("insert into editais (titulo, descricao, data_fechamento, status) values (?, ?, ?, ?)"); // prepara o INSERT com parâmetros posicionais (?) para prevenir SQL Injection
            stmt.setString(1, edital.getTitulo());                                    // substitui o 1º ? pelo título do edital
            stmt.setString(2, edital.getDescricao());                                 // substitui o 2º ? pela descrição
            stmt.setTimestamp(3, Timestamp.valueOf(edital.getData_fechamento()));      // converte LocalDateTime para java.sql.Timestamp (tipo aceito pelo DATETIME do MySQL)
            stmt.setString(4, edital.getStatus());                                    // substitui o 4º ? pelo status (sempre "ABERTO" na criação, definido pelo EditalService)

            return stmt.executeUpdate(); // executa o INSERT e retorna quantas linhas foram inseridas
        } catch (SQLException e) {
            e.printStackTrace(); // imprime o erro no console; o EditalService trata o retorno 0 como falha
        }

        return 0; // retorna 0 se ocorreu exceção (nenhuma linha foi inserida)
    }

    // Retorna todos os editais da tabela; a filtragem por urgência é feita em memória pelo EditalService para reutilizar esta query base
    public List<EditalDTO> listaEdital() {
        List<EditalDTO> listaEdital = new ArrayList<>(); // lista que vai acumular os editais lidos do banco
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("select * from editais"); // busca todos os editais sem filtro
            ResultSet rs = stmt.executeQuery(); // executa a query e armazena o resultado em rs

            while (rs.next()) { // itera linha por linha do resultado enquanto houver registros
                EditalDTO edital = new EditalDTO();                                          // cria um objeto vazio para mapear a linha atual
                edital.setId(rs.getLong("id"));                                              // lê a coluna "id" da linha atual e atribui ao objeto
                edital.setTitulo(rs.getString("titulo"));                                    // lê a coluna "titulo"
                edital.setDescricao(rs.getString("descricao"));                              // lê a coluna "descricao"
                edital.setData_fechamento(
                        rs.getTimestamp("data_fechamento").toLocalDateTime()                 // converte java.sql.Timestamp para LocalDateTime do Java
                );
                edital.setStatus(rs.getString("status"));                                    // lê a coluna "status"
                listaEdital.add(edital);                                                     // adiciona o edital mapeado à lista de retorno
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaEdital; // retorna a lista completa de editais (vazia se não houver registros ou se ocorreu erro)
    }

    // Busca um edital específico pelo id; retorna null se não encontrado (o EditalService lança 404 nesse caso)
    public EditalDTO getById(Long id) {
        EditalDTO edital = null; // começa como null; só é populado se o id existir no banco
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("select * from editais where id = ?"); // query com filtro por id, usando ? para prevenir SQL Injection
            stmt.setLong(1, id); // substitui o ? pelo id recebido como parâmetro
            ResultSet rs = stmt.executeQuery(); // executa a query
            if (rs.next()) { // rs.next() retorna true apenas se existir uma linha com o id informado
                edital = new EditalDTO();                                                     // cria o objeto somente quando o edital existe
                edital.setId(rs.getLong("id"));
                edital.setTitulo(rs.getString("titulo"));
                edital.setDescricao(rs.getString("descricao"));
                edital.setData_fechamento(
                        rs.getTimestamp("data_fechamento").toLocalDateTime()                 // converte Timestamp para LocalDateTime
                );
                edital.setStatus(rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edital; // retorna o edital encontrado ou null se não existir
    }
}