package com.gestion.affectations.controller;

import com.gestion.affectations.entity.Lieu;
import com.gestion.affectations.service.LieuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lieux")
@CrossOrigin(origins = "*")
public class LieuController {

    @Autowired
    private LieuService lieuService;

    @PostMapping
    public ResponseEntity<Lieu> createLieu(@Valid @RequestBody Lieu lieu) {
        try {
            Lieu created = lieuService.createLieu(lieu);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Lieu>> getAllLieus() {
        List<Lieu> lieus = lieuService.getAllLieus();
        return new ResponseEntity<>(lieus, HttpStatus.OK);
    }

    @GetMapping("/{codeFormate}")
    public ResponseEntity<Lieu> getLieuByCode(@PathVariable String codeFormate) {
        try {
            Lieu lieu = lieuService.getLieuByCode(codeFormate);
            return new ResponseEntity<>(lieu, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{codeFormate}")
    public ResponseEntity<Lieu> updateLieu(@PathVariable String codeFormate,
                                           @Valid @RequestBody Lieu lieu) {
        try {
            Lieu updated = lieuService.updateLieu(codeFormate, lieu);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{codeFormate}")
    public ResponseEntity<HttpStatus> deleteLieu(@PathVariable String codeFormate) {
        try {
            lieuService.deleteLieu(codeFormate);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}