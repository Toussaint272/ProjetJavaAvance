package com.gestion.affectations.repository;

import com.gestion.affectations.entity.Affecter;
import com.gestion.affectations.entity.Employee;
import com.gestion.affectations.entity.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffecterRepository extends JpaRepository<Affecter, Long> {

    // Trouver les affectations d'un employé
    List<Affecter> findByEmployee(Employee employee);

    // Trouver les affectations d'un lieu
    List<Affecter> findByLieu(Lieu lieu);

    // Trouver les affectations d'un employé à une date donnée
    Optional<Affecter> findByEmployeeAndDateAffectation(Employee employee, LocalDate date);

    // Vérifier si une affectation existe
    boolean existsByEmployeeAndLieuAndDateAffectation(Employee employee, Lieu lieu, LocalDate date);

    // ✅ AJOUT : retrouver l'affectation exacte (pour éviter les doublons lors d'une mise à jour)
    Optional<Affecter> findByEmployeeAndLieuAndDateAffectation(Employee employee, Lieu lieu, LocalDate date);

    // Supprimer les affectations d'un employé
    void deleteByEmployee(Employee employee);

    // Supprimer les affectations d'un lieu
    void deleteByLieu(Lieu lieu);

    // ✅ CORRECTION : Utiliser codeFormate au lieu de codelieu
    @Query("SELECT a.lieu.codeFormate, COUNT(a) FROM Affecter a GROUP BY a.lieu.codeFormate")
    List<Object[]> countAffectationsByLieu();
}