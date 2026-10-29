package com.mgp.lojaonline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Pedido pedido;

    @Column(nullable = false, unique = true)
    private Long idMercadoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento forma;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    // status como o Mercado Pago devolve: pending, approved, rejected, cancelled, refunded...
    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime data;

    private LocalDateTime atualizadoEm;

    @Column(length = 1000)
    private String pixCopiaECola;

    // imagem do QR Code em base64, do jeito que vem do Mercado Pago
    @Lob
    private String pixQrCode;

    @Column(length = 500)
    private String linkBoleto;

    public boolean isAprovado() {
        return "approved".equals(status);
    }

    public String getStatusDescricao() {
        return switch (status) {
            case "pending" -> "Pendente";
            case "in_process" -> "Em análise";
            case "approved" -> "Aprovado";
            case "rejected" -> "Recusado";
            case "cancelled" -> "Cancelado";
            case "refunded" -> "Estornado";
            case "charged_back" -> "Contestado";
            default -> status;
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Long getIdMercadoPago() {
        return idMercadoPago;
    }

    public void setIdMercadoPago(Long idMercadoPago) {
        this.idMercadoPago = idMercadoPago;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public void setForma(FormaPagamento forma) {
        this.forma = forma;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getPixCopiaECola() {
        return pixCopiaECola;
    }

    public void setPixCopiaECola(String pixCopiaECola) {
        this.pixCopiaECola = pixCopiaECola;
    }

    public String getPixQrCode() {
        return pixQrCode;
    }

    public void setPixQrCode(String pixQrCode) {
        this.pixQrCode = pixQrCode;
    }

    public String getLinkBoleto() {
        return linkBoleto;
    }

    public void setLinkBoleto(String linkBoleto) {
        this.linkBoleto = linkBoleto;
    }
}
