package com.bidding.system.bidding.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Gerencia a conexão com o banco MySQL usando o padrão Singleton: uma única instância de Connection reutilizada por todos os repositórios
public class Conexao {

    private static final String url = "jdbc:mysql://localhost:3306/db_bidding_system"; // URL de conexão JDBC: protocolo://host:porta/banco
    private static final String usuario = "root";          // usuário do banco de dados
    private static final String senha = "joaopauloor21";   // senha do banco — em produção, usar variáveis de ambiente ou application.properties
    private static Connection conn = null;                 // instância única de Connection compartilhada (Singleton)

    private Conexao() { // construtor privado: impede criação de instâncias externas
    }

    // synchronized garante que dois threads não criem conexões simultâneas em requisições concorrentes
    public static synchronized Connection conectar() {
        try {
            if (conn == null || conn.isClosed()) { // cria nova conexão se ainda não existir ou se a anterior tiver sido encerrada (ex: timeout do MySQL)
                conn = DriverManager.getConnection(url, usuario, senha); // abre a conexão com o banco usando as credenciais definidas acima
            }
        } catch (SQLException e) {
            e.printStackTrace(); // imprime a stack trace no console em caso de falha na conexão
        }
        return conn; // retorna a conexão ativa para ser usada pelo repositório chamador
    }
}
