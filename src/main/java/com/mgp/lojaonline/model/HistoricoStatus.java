package com.mgp.lojaonline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

// cada mudanca de status vira uma linha da linha do tempo do pedido
@Entity
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(nullable = false)
    private LocalDateTime data;

    protected HistoricoStatus() {
    }

    public HistoricoStatus(Pedido pedido, StatusPedido status) {
        this.pedido = pedido;
        this.status = status;
        this.data = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public LocalDateTime getData() {
        return data;
    }
}
