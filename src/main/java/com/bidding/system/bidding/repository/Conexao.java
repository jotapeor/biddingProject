package com.bidding.system.bidding.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String defaultUrl = "jdbc:mysql://mysql-bidding-joaopauloor2004-5c0b.l.aivencloud.com:17607/defaultdb?sslMode=REQUIRED";
    private static final String defaultUsuario = "avnadmin";
    private static final String defaultSenha = "";
    private static Connection conn = null;

    private Conexao() {
    }

    public static synchronized Connection conectar() {
        try {
            if (conn == null || conn.isClosed()) {
                String url = System.getenv("DB_URL");
                if (url == null || url.isEmpty()) url = defaultUrl;
                
                String usuario = System.getenv("DB_USER");
                if (usuario == null || usuario.isEmpty()) usuario = defaultUsuario;
                
                String senha = System.getenv("DB_PASSWORD");
                if (senha == null || senha.isEmpty()) senha = defaultSenha;

                conn = DriverManager.getConnection(url, usuario, senha);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
