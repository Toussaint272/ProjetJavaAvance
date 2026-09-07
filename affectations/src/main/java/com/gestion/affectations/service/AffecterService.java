package com.gestion.affectations.service;

import com.gestion.affectations.entity.Affecter;
import com.gestion.affectations.entity.Employee;
import com.gestion.affectations.entity.Lieu;
import com.gestion.affectations.repository.AffecterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AffecterService {

    private final AffecterRepository affecterRepository;
    private final EmployeeService employeeService;
    private final LieuService lieuService;

    public AffecterService(AffecterRepository affecterRepository,
                           EmployeeService employeeService,
                           LieuService lieuService) {
        this.affecterRepository = affecterRepository;
        this.employeeService = employeeService;
        this.lieuService = lieuService;
    }

    // CREATE
    public Affecter createAffectation(String codeemp, String codelieu, LocalDate date) {
        Employee employee = employeeService.getEmployeeByCode(codeemp);
        Lieu lieu = lieuService.getLieuByCode(codelieu);

        if (affecterRepository.existsByEmployeeAndLieuAndDateAffectation(employee, lieu, date)) {
            throw new RuntimeException("Cette affectation existe déjà");
        }

        Affecter affecter = new Affecter();
        affecter.setEmployee(employee);
        affecter.setLieu(lieu);
        affecter.setDateAffectation(date);

        return affecterRepository.save(affecter);
    }

    // ✅ AJOUT : UPDATE (c'était la méthode manquante -> erreur 405)
    public Affecter updateAffectation(Long id, String codeemp, String codelieu, LocalDate date) {
        Affecter affecter = getAffectationById(id);

        Employee employee = employeeService.getEmployeeByCode(codeemp);
        Lieu lieu = lieuService.getLieuByCode(codelieu);

        // Vérifier qu'aucune AUTRE affectation n'a déjà (employé, lieu, date)
        affecterRepository.findByEmployeeAndLieuAndDateAffectation(employee, lieu, date)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Cette affectation existe déjà");
                });

        affecter.setEmployee(employee);
        affecter.setLieu(lieu);
        affecter.setDateAffectation(date);

        return affecterRepository.save(affecter);
    }

    // READ (toutes)
    public List<Affecter> getAllAffectations() {
        return affecterRepository.findAll();
    }

    // READ (par ID)
    public Affecter getAffectationById(Long id) {
        return affecterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec l'id: " + id));
    }

    // READ (par employé)
    public List<Affecter> getAffectationsByEmployee(String codeemp) {
        Employee employee = employeeService.getEmployeeByCode(codeemp);
        return affecterRepository.findByEmployee(employee);
    }

    // READ (par lieu)
    public List<Affecter> getAffectationsByLieu(String codelieu) {
        Lieu lieu = lieuService.getLieuByCode(codelieu);
        return affecterRepository.findByLieu(lieu);
    }

    // DELETE
    public void deleteAffectation(Long id) {
        if (!affecterRepository.existsById(id)) {
            throw new RuntimeException("Affectation non trouvée avec l'id: " + id);
        }
        affecterRepository.deleteById(id);
    }

    // Statistiques
    // Statistiques
    public Map<String, Long> getAffectationsCountByLieu() {
        List<Object[]> results = affecterRepository.countAffectationsByLieu();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()   // ✅ accepte Integer ET Long
                ));
    }
}