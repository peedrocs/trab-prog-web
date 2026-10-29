package com.mgp.lojaonline.exception;

// a mensagem desta excecao e mostrada para o usuario na tela
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
