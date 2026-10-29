package com.mgp.lojaonline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableAsync liga o envio de e-mail em segundo plano e @EnableScheduling a tarefa que cancela pedidos vencidos
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LojaonlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LojaonlineApplication.class, args);
    }
}
