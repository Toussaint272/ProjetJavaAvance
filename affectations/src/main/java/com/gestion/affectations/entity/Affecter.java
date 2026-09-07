package com.gestion.affectations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "affecter",
        uniqueConstraints = @UniqueConstraint(columnNames = {"codeemp", "codelieu", "date_affectation"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Affecter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codeemp", nullable = false)
    @NotNull(message = "L'employé est obligatoire")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codelieu", nullable = false)
    @NotNull(message = "Le lieu est obligatoire")
    private Lieu lieu;

    @Column(name = "date_affectation", nullable = false)
    @NotNull(message = "La date d'affectation est obligatoire")
    private LocalDate dateAffectation;
}