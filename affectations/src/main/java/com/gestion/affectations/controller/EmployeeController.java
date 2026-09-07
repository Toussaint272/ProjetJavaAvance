package com.gestion.affectations.controller;

import com.gestion.affectations.entity.Employee;
import com.gestion.affectations.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // CREATE - Le code est généré automatiquement
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        try {
            Employee created = employeeService.createEmployee(employee);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // READ (tous)
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    // READ (par code formaté - EMP-001)
    @GetMapping("/{codeFormate}")
    public ResponseEntity<Employee> getEmployeeByCode(@PathVariable String codeFormate) {
        try {
            Employee employee = employeeService.getEmployeeByCode(codeFormate);
            return new ResponseEntity<>(employee, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // READ (recherche)
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam String keyword) {
        List<Employee> employees = employeeService.searchEmployees(keyword);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    // UPDATE (par code formaté)
    @PutMapping("/{codeFormate}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String codeFormate,
                                                   @Valid @RequestBody Employee employee) {
        try {
            Employee updated = employeeService.updateEmployee(codeFormate, employee);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // DELETE (par code formaté)
    @DeleteMapping("/{codeFormate}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable String codeFormate) {
        try {
            employeeService.deleteEmployee(codeFormate);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}