package com.gestion.affectations.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codeemp")
    @JsonIgnore  // ← CACHE L'ID DANS LA RÉPONSE JSON
    private Long id;

    @Column(name = "code_formate", unique = true, length = 20)
    private String codeFormate;  // EMP-001

    @Column(name = "nom", nullable = false, length = 50)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères")
    private String nom;

    @Column(name = "prenom", nullable = false, length = 50)
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne doit pas dépasser 50 caractères")
    private String prenom;

    @Column(name = "poste", nullable = false, length = 50)
    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 50, message = "Le poste ne doit pas dépasser 50 caractères")
    private String poste;

    @JsonIgnore
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Affecter> affectations;
}