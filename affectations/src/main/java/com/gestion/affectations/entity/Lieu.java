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
@Table(name = "lieu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codelieu")
    @JsonIgnore  // ← CACHE L'ID DANS LA RÉPONSE JSON
    private Long id;

    @Column(name = "code_formate", unique = true, length = 20)
    private String codeFormate;  // LIEU-001

    @Column(name = "designation", nullable = false, length = 100)
    @NotBlank(message = "La désignation est obligatoire")
    @Size(max = 100, message = "La désignation ne doit pas dépasser 100 caractères")
    private String designation;

    @Column(name = "province", nullable = false, length = 50)
    @NotBlank(message = "La province est obligatoire")
    @Size(max = 50, message = "La province ne doit pas dépasser 50 caractères")
    private String province;

    @JsonIgnore
    @OneToMany(mappedBy = "lieu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Affecter> affectations;
}