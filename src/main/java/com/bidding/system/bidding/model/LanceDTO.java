package com.bidding.system.bidding.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

// DTO de lance: na entrada o front-end envia só o "valor"; os demais campos são preenchidos pelo LanceService
public class LanceDTO {

    private Long id;           // identificador único gerado pelo banco (auto_increment)
    private double valor;      // valor monetário ofertado no lance, informado pelo fornecedor
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // formato ISO 8601 sem timezone para serialização/desserialização da data
    private LocalDateTime data_lance;  // momento exato do lance, preenchido pelo servidor com LocalDateTime.now()
    private Long id_edital;    // referência ao edital ao qual o lance pertence, preenchida pelo LanceService
    private Long id_usuario;   // referência ao fornecedor que enviou o lance, extraída do JWT pelo LanceService

    // Construtor padrão exigido pelo Jackson para desserializar o JSON do corpo da requisição
    public LanceDTO() {
    }

    // Construtor completo utilizado ao montar o objeto após leitura do banco de dados
    public LanceDTO(Long id, double valor, LocalDateTime data_lance, Long id_edital, Long id_usuario) {
        this.id = id;
        this.valor = valor;
        this.data_lance = data_lance;
        this.id_edital = id_edital;
        this.id_usuario = id_usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDateTime getData_lance() {
        return data_lance;
    }

    public void setData_lance(LocalDateTime data_lance) {
        this.data_lance = data_lance;
    }

    public Long getId_edital() {
        return id_edital;
    }

    public void setId_edital(Long id_edital) {
        this.id_edital = id_edital;
    }

    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }
}