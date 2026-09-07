package com.gestion.affectations.controller;

import com.gestion.affectations.entity.Affecter;
import com.gestion.affectations.service.AffecterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/affectations")
@CrossOrigin(origins = "*")
public class AffecterController {

    @Autowired
    private AffecterService affecterService;

    @PostMapping
    public ResponseEntity<Affecter> createAffectation(
            @RequestParam String codeemp,
            @RequestParam String codelieu,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Affecter created = affecterService.createAffectation(codeemp, codelieu, date);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // ✅ AJOUT : UPDATE (résout l'erreur 405 Method Not Allowed)
    @PutMapping("/{id}")
    public ResponseEntity<Affecter> updateAffectation(
            @PathVariable Long id,
            @RequestParam String codeemp,
            @RequestParam String codelieu,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Affecter updated = affecterService.updateAffectation(id, codeemp, codelieu, date);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Affecter>> getAllAffectations() {
        List<Affecter> affectations = affecterService.getAllAffectations();
        return new ResponseEntity<>(affectations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Affecter> getAffectationById(@PathVariable Long id) {
        try {
            Affecter affecter = affecterService.getAffectationById(id);
            return new ResponseEntity<>(affecter, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/byemployee/{codeemp}")
    public ResponseEntity<List<Affecter>> getAffectationsByEmployee(@PathVariable String codeemp) {
        List<Affecter> affectations = affecterService.getAffectationsByEmployee(codeemp);
        return new ResponseEntity<>(affectations, HttpStatus.OK);
    }

    @GetMapping("/bylieu/{codelieu}")
    public ResponseEntity<List<Affecter>> getAffectationsByLieu(@PathVariable String codelieu) {
        List<Affecter> affectations = affecterService.getAffectationsByLieu(codelieu);
        return new ResponseEntity<>(affectations, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAffectation(@PathVariable Long id) {
        try {
            affecterService.deleteAffectation(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getAffectationsStatistics() {
        Map<String, Long> stats = affecterService.getAffectationsCountByLieu();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}