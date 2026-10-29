package com.mgp.lojaonline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender enviador;
    private final String remetente;

    public EmailService(JavaMailSender enviador, @Value("${loja.email.remetente}") String remetente) {
        this.enviador = enviador;
        this.remetente = remetente;
    }

    // roda em outra thread, entao a tela nao fica esperando o servidor de e-mail
    @Async
    public void enviar(String destinatario, String assunto, String texto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);

        try {
            enviador.send(mensagem);
        } catch (MailException e) {
            log.error("Não foi possível enviar o e-mail para {}: {}", destinatario, e.getMessage());
        }
    }
}
