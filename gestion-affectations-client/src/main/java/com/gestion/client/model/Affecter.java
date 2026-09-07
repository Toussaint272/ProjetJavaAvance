package com.gestion.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Affecter {
    private Long id;
    private String codeemp;
    private String nomEmploye;
    private String prenomEmploye;
    private String posteEmploye;
    private String codelieu;
    private String designationLieu;
    private String provinceLieu;
    private LocalDate dateAffectation;

    // ✅ AJOUTER DES GETTERS/SETTERS MANUELS SI NÉCESSAIRE
    // Lombok @Data devrait les générer automatiquement
}