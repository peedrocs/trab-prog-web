package com.mgp.lojaonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome da categoria")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String nome;
}
