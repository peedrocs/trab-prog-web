package com.mgp.lojaonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe seu nome")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Informe uma senha")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    @Column(nullable = false)
    private String senha;

    // opcional: vazio ou 11 dígitos, com ou sem pontuação
    @Pattern(regexp = "^$|\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF inválido")
    @Column(length = 14)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil = Perfil.USER;

    private boolean ativo = true;
}
