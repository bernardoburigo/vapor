package com.example.trabalho1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Estúdio ou publicadora responsável por um ou mais {@link Jogo}. */
@Entity
@Table(name = "desenvolvedora")
@Getter
@Setter
@NoArgsConstructor
public class Desenvolvedora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "pais_origem", length = 100)
    private String paisOrigem;

    @Column(length = 255)
    private String site;
}
