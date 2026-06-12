package com.bidding.system.bidding.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String url = "jdbc:mysql://localhost:3306/db_bidding_system";
    private static final String usuario = "root";
    private static final String senha = "joaopauloor21";
    private static Connection conn = null;

    private Conexao() {
    }

    public static synchronized Connection conectar() {
        try {
            // Cria nova conexão se ainda não existir ou se a anterior foi encerrada pelo servidor MySQL
            if (conn == null || conn.isClosed()) {
                // Abre a conexão usando as credenciais e URL definidas como constantes da classe
                conn = DriverManager.getConnection(url, usuario, senha);
            }
        } catch (SQLException e) {
            // Imprime a stack trace no console; o repositório chamador receberá null e deverá tratar o erro
            e.printStackTrace();
        }
        // Retorna a conexão ativa (ou null em caso de falha) para o repositório que solicitou
        return conn;
    }
}
