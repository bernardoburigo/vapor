package com.example.trabalho1.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de Jogo — responsabilidade de Bruno Baldessar.
 * Declarada aqui para satisfazer a FK de Avaliacao.
 */
@Entity
@Table(name = "jogo")
@Getter
@Setter
@NoArgsConstructor
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "data_lancamento")
    private LocalDate dataLancamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "desenvolvedora_id", nullable = false)
    private Desenvolvedora desenvolvedora;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "jogo_categoria",
        joinColumns = @JoinColumn(name = "jogo_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();
}
