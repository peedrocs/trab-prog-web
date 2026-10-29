package com.mgp.lojaonline.dto;

import java.math.BigDecimal;

public record ProdutoVendido(String nome, Long quantidade, BigDecimal faturamento) {
}
