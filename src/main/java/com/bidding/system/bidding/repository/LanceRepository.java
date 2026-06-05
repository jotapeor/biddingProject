package com.bidding.system.bidding.repository;

import com.bidding.system.bidding.model.LanceDTO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository // marca a classe como componente de acesso a dados do Spring; permite injeção via @Autowired nos Services
public class LanceRepository {

    // Insere um novo lance na tabela "lances"; retorna o número de linhas afetadas (1 = sucesso, 0 = falha)
    public int novoLance(LanceDTO lance) {
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("insert into lances (valor, data_lance, id_edital, id_usuario) values (?, ?, ?, ?)"); // prepara o INSERT com ? para prevenir SQL Injection
            stmt.setDouble(1, lance.getValor());                                       // substitui o 1º ? pelo valor monetário do lance
            stmt.setTimestamp(2, Timestamp.valueOf(lance.getData_lance()));             // converte LocalDateTime para java.sql.Timestamp (compatível com DATETIME do MySQL)
            stmt.setLong(3, lance.getId_edital());                                     // substitui o 3º ? pelo id do edital ao qual o lance pertence
            stmt.setLong(4, lance.getId_usuario());                                    // substitui o 4º ? pelo id do fornecedor extraído do JWT

            return stmt.executeUpdate(); // executa o INSERT e retorna quantas linhas foram inseridas
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // retorna 0 se ocorreu exceção (nenhuma linha foi inserida)
    }

    // Retorna todos os lances de um edital ordenados do mais recente ao mais antigo
    // Usado quando o edital está FECHADO (todos veem tudo) ou quando o usuário é COMPRADOR com edital ABERTO
    public java.util.List<LanceDTO> getLancesByEdital(Long idEdital) {
        java.util.List<LanceDTO> lances = new java.util.ArrayList<>(); // lista que vai acumular os lances lidos do banco
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("select * from lances where id_edital = ? order by data_lance desc"); // busca lances do edital, do mais novo para o mais antigo
            stmt.setLong(1, idEdital); // substitui o ? pelo id do edital
            java.sql.ResultSet rs = stmt.executeQuery(); // executa a query e armazena o resultado
            while (rs.next()) { // itera linha por linha enquanto houver lances
                LanceDTO lance = new LanceDTO();                                                      // cria um objeto vazio para mapear a linha atual
                lance.setId(rs.getLong("id"));
                lance.setValor(rs.getDouble("valor"));
                lance.setData_lance(rs.getTimestamp("data_lance").toLocalDateTime()); // converte Timestamp para LocalDateTime
                lance.setId_edital(rs.getLong("id_edital"));
                lance.setId_usuario(rs.getLong("id_usuario"));
                lances.add(lance); // adiciona o lance mapeado à lista de retorno
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lances;
    }

    // Retorna apenas os lances de um fornecedor específico dentro de um edital
    // Usado quando o edital está ABERTO e o usuário logado é FORNECEDOR: ele só vê os próprios lances para não ter vantagem competitiva
    public java.util.List<LanceDTO> getLancesByEditalAndUsuario(Long idEdital, Long idUsuario) {
        java.util.List<LanceDTO> lances = new java.util.ArrayList<>(); // lista que vai acumular os lances lidos do banco
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("select * from lances where id_edital = ? and id_usuario = ? order by data_lance desc"); // filtra por edital E por usuário para exibir só os próprios lances
            stmt.setLong(1, idEdital);   // substitui o 1º ? pelo id do edital
            stmt.setLong(2, idUsuario);  // substitui o 2º ? pelo id do fornecedor logado
            java.sql.ResultSet rs = stmt.executeQuery(); // executa a query
            while (rs.next()) { // itera linha por linha enquanto houver lances
                LanceDTO lance = new LanceDTO();
                lance.setId(rs.getLong("id"));
                lance.setValor(rs.getDouble("valor"));
                lance.setData_lance(rs.getTimestamp("data_lance").toLocalDateTime()); // converte Timestamp para LocalDateTime
                lance.setId_edital(rs.getLong("id_edital"));
                lance.setId_usuario(rs.getLong("id_usuario"));
                lances.add(lance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lances;
    }

    // Retorna todos os lances de um fornecedor (por idUsuario) com JOIN na tabela editais para trazer título e status em uma única query
    // Usado pelo LanceService.getMeusLances() para montar o painel "Meus Lances"
    public java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> getMeusLances(Long idUsuario) {
        java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> lista = new java.util.ArrayList<>();
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            String sql = "SELECT l.id as id_lance, l.valor, l.data_lance, e.id as id_edital, e.titulo, e.status " +
                    "FROM lances l " +
                    "JOIN editais e ON l.id_edital = e.id " +  // JOIN traz título e status do edital sem precisar de uma segunda query
                    "WHERE l.id_usuario = ? " +                // filtra apenas os lances do fornecedor logado
                    "ORDER BY l.data_lance DESC";              // ordena do mais recente ao mais antigo
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idUsuario); // substitui o ? pelo id do fornecedor logado
            java.sql.ResultSet rs = stmt.executeQuery(); // executa a query
            while (rs.next()) { // itera linha por linha enquanto houver lances
                com.bidding.system.bidding.model.MeuLanceDTO dto = new com.bidding.system.bidding.model.MeuLanceDTO();
                dto.setIdLance(rs.getLong("id_lance"));
                dto.setValor(rs.getDouble("valor"));
                dto.setDataLance(rs.getTimestamp("data_lance").toLocalDateTime()); // converte Timestamp para LocalDateTime
                dto.setIdEdital(rs.getLong("id_edital"));
                dto.setTituloEdital(rs.getString("titulo"));    // título vem do JOIN com editais
                dto.setStatusEdital(rs.getString("status"));    // status vem do JOIN com editais
                lista.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Retorna o menor valor de lance de um edital; usado pelo LanceService para determinar o vencedor após o fechamento
    public Double getMenorLanceByEdital(Long idEdital) {
        Double menorValor = null; // começa como null; só é populado se existirem lances no edital
        try {
            Connection conn = Conexao.conectar(); // obtém a conexão ativa com o banco
            PreparedStatement stmt = conn.prepareStatement("SELECT MIN(valor) as menor_valor FROM lances WHERE id_edital = ?"); // MIN() retorna o menor valor de "valor" entre todos os lances do edital
            stmt.setLong(1, idEdital); // substitui o ? pelo id do edital
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) { // sempre haverá uma linha de retorno (com NULL se não houver lances)
                double val = rs.getDouble("menor_valor");
                if (!rs.wasNull()) { // rs.wasNull() verifica se o valor lido foi NULL no banco (edital sem lances); se não for null, atribui o valor
                    menorValor = val;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menorValor; // retorna o menor valor ou null se o edital não tiver lances
    }
}