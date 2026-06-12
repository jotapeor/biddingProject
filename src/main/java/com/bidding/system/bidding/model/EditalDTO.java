package com.bidding.system.bidding.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EditalDTO {

    private Long id;
    private String titulo;
    private String descricao;
    /**
     * Data e hora limite para recebimento de lances.
     *
     * <p>{@code @JsonFormat} define o padrão ISO 8601 sem timezone ({@code yyyy-MM-dd'T'HH:mm:ss})
     * para serialização e desserialização, garantindo compatibilidade entre o JSON da API
     * e o {@link LocalDateTime} do Java. O timezone é omitido intencionalmente porque a
     * aplicação opera em um único fuso horário (servidor local).
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime data_fechamento;
    private String status;
    private Long vencedor;

    public EditalDTO() {
    }

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

    public Long getVencedor() {
        return vencedor;
    }

    public void setVencedor(Long vencedor) {
        this.vencedor = vencedor;
    }
}