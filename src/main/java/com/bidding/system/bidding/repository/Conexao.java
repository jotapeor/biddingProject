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
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(url, usuario, senha);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
