package com.bidding.system.bidding.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

// DTO de edital: trafega entre front-end e back-end como JSON e mapeia linhas da tabela "editais"
public class EditalDTO {

    private Long id;                    // identificador único gerado pelo banco (auto_increment)
    private String titulo;              // nome/título do edital
    private String descricao;           // descrição detalhada do objeto licitado
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // define o formato ISO 8601 sem timezone para serialização/desserialização da data
    private LocalDateTime data_fechamento; // prazo limite para recebimento de lances
    private String status;              // estado do edital: "ABERTO" (aceita lances) ou "FECHADO"

    // Construtor padrão exigido pelo Jackson para desserializar o JSON do corpo da requisição
    public EditalDTO() {
    }

    // Construtor completo utilizado ao montar o objeto após leitura do banco de dados
    public EditalDTO(Long id, String titulo, String descricao, LocalDateTime data_fechamento, String status) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.data_fechamento = data_fechamento;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getData_fechamento() {
        return data_fechamento;
    }

    public void setData_fechamento(LocalDateTime data_fechamento) {
        this.data_fechamento = data_fechamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}