package com.mgp.lojaonline.dto;

// motivoBloqueio vem preenchido quando o registro tem vinculo e nao pode ser excluido
public record RegistroExclusao(Long id, String nome, String detalhe, boolean ativo, String motivoBloqueio,
                               boolean podeDesativar) {

    public boolean podeExcluir() {
        return motivoBloqueio == null;
    }
}
