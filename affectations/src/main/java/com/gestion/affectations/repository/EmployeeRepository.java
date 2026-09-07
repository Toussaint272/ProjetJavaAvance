package com.gestion.affectations.repository;

import com.gestion.affectations.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByCodeFormate(String codeFormate);

    /**
     * Recherche insensible à la casse avec LIKE (%keyword%) sur :
     * - le code formaté
     * - le nom
     * - le prénom
     * - le poste
     */
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.codeFormate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.poste) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchByCodeOrNom(@Param("keyword") String keyword);
}
