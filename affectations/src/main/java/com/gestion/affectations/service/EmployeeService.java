package com.gestion.affectations.service;

import com.gestion.affectations.entity.Employee;
import com.gestion.affectations.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // CREATE - Génère le code formaté automatiquement
    public Employee createEmployee(Employee employee) {
        // Sauvegarder d'abord pour obtenir l'ID
        Employee saved = employeeRepository.save(employee);

        // Générer le code formaté avec l'ID
        String formattedCode = String.format("EMP-%03d", saved.getId());
        saved.setCodeFormate(formattedCode);

        // Mettre à jour avec le code formaté
        return employeeRepository.save(saved);
    }

    // READ (tous)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // READ (par ID - utilisé en interne)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
    }

    // READ (par code formaté) - PUBLIC
    public Employee getEmployeeByCode(String codeFormate) {
        return employeeRepository.findByCodeFormate(codeFormate)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec le code: " + codeFormate));
    }

    // READ (recherche)
    public List<Employee> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return employeeRepository.findAll();
        }
        return employeeRepository.searchByCodeOrNom(keyword.trim());
    }

    // UPDATE (par code formaté)
    public Employee updateEmployee(String codeFormate, Employee employeeDetails) {
        Employee employee = getEmployeeByCode(codeFormate);
        employee.setNom(employeeDetails.getNom());
        employee.setPrenom(employeeDetails.getPrenom());
        employee.setPoste(employeeDetails.getPoste());
        return employeeRepository.save(employee);
    }

    // DELETE (par code formaté)
    public void deleteEmployee(String codeFormate) {
        Employee employee = getEmployeeByCode(codeFormate);
        employeeRepository.deleteById(employee.getId());
    }
}