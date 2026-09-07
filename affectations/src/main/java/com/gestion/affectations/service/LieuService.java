package com.gestion.affectations.service;

import com.gestion.affectations.entity.Lieu;
import com.gestion.affectations.repository.LieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LieuService {

    @Autowired
    private LieuRepository lieuRepository;

    // CREATE - Génère le code formaté automatiquement
    public Lieu createLieu(Lieu lieu) {
        Lieu saved = lieuRepository.save(lieu);
        String formattedCode = String.format("LIEU-%03d", saved.getId());
        saved.setCodeFormate(formattedCode);
        return lieuRepository.save(saved);
    }

    // READ (tous)
    public List<Lieu> getAllLieus() {
        return lieuRepository.findAll();
    }

    // READ (par code formaté)
    public Lieu getLieuByCode(String codeFormate) {
        return lieuRepository.findByCodeFormate(codeFormate)
                .orElseThrow(() -> new RuntimeException("Lieu non trouvé avec le code: " + codeFormate));
    }

    // UPDATE (par code formaté)
    public Lieu updateLieu(String codeFormate, Lieu lieuDetails) {
        Lieu lieu = getLieuByCode(codeFormate);
        lieu.setDesignation(lieuDetails.getDesignation());
        lieu.setProvince(lieuDetails.getProvince());
        return lieuRepository.save(lieu);
    }

    // DELETE (par code formaté)
    public void deleteLieu(String codeFormate) {
        Lieu lieu = getLieuByCode(codeFormate);
        lieuRepository.deleteById(lieu.getId());
    }
}