package com.bidding.system.bidding.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

// DTO enriquecido para o painel "Meus Lances" do FORNECEDOR:
// agrega dados do lance e do edital correspondente em um único objeto, evitando múltiplas consultas ao banco
public class MeuLanceDTO {

    private Long idLance;        // identificador do lance
    private double valor;        // valor ofertado pelo fornecedor
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // formato ISO 8601 sem timezone para serialização/desserialização da data
    private LocalDateTime dataLance;    // data e hora em que o lance foi registrado
    private Long idEdital;       // identificador do edital ao qual o lance pertence (vem do JOIN no repositório)
    private String tituloEdital; // título do edital (vem do JOIN com a tabela editais no repositório)
    private String statusEdital; // status atual do edital: "ABERTO" ou "FECHADO" (vem do JOIN)
    private boolean vencedor;    // calculado em memória pelo LanceService: true se este lance tem o menor valor no edital FECHADO

    // Construtor padrão exigido pelo Jackson para desserializar o JSON do corpo da requisição
    public MeuLanceDTO() {
    }

    public Long getIdLance() {
        return idLance;
    }

    public void setIdLance(Long idLance) {
        this.idLance = idLance;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataLance() {
        return dataLance;
    }

    public void setDataLance(LocalDateTime dataLance) {
        this.dataLance = dataLance;
    }

    public Long getIdEdital() {
        return idEdital;
    }

    public void setIdEdital(Long idEdital) {
        this.idEdital = idEdital;
    }

    public String getTituloEdital() {
        return tituloEdital;
    }

    public void setTituloEdital(String tituloEdital) {
        this.tituloEdital = tituloEdital;
    }

    public String getStatusEdital() {
        return statusEdital;
    }

    public void setStatusEdital(String statusEdital) {
        this.statusEdital = statusEdital;
    }

    public boolean isVencedor() {
        return vencedor;
    }

    public void setVencedor(boolean vencedor) {
        this.vencedor = vencedor;
    }
}
