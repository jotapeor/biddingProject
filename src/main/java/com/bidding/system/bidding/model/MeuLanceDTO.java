package com.bidding.system.bidding.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class MeuLanceDTO {

    private Long idLance;
    private double valor;
    /**
     * Data e hora em que o lance foi registrado no servidor.
     * {@code @JsonFormat} garante serialização no padrão ISO 8601 sem timezone.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataLance;
    private Long idEdital;
    private String tituloEdital;
    private String statusEdital;
    private boolean vencedor;

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
