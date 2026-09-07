package com.gestion.affectations.repository;

import com.gestion.affectations.entity.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LieuRepository extends JpaRepository<Lieu, Long> {

    // Recherche par code formaté
    Optional<Lieu> findByCodeFormate(String codeFormate);

    // Recherche par désignation
    List<Lieu> findByDesignationContainingIgnoreCase(String designation);

    // Recherche par province
    List<Lieu> findByProvince(String province);
}