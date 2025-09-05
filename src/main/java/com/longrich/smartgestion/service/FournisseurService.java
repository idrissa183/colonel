package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.FournisseurDTO;
import com.longrich.smartgestion.entity.Fournisseur;
import com.longrich.smartgestion.enums.TypeStockiste;
import com.longrich.smartgestion.mapper.FournisseurMapper;
import com.longrich.smartgestion.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final FournisseurMapper fournisseurMapper;

    public List<FournisseurDTO> getAllFournisseurs() {
        return fournisseurMapper.toDTOList(fournisseurRepository.findAll());
    }

    public List<Fournisseur> findAll() {
        return fournisseurRepository.findAll();
    }

    public List<FournisseurDTO> getActiveFournisseurs() {
        return fournisseurMapper.toDTOList(fournisseurRepository.findByActiveTrue());
    }

    public Optional<FournisseurDTO> getFournisseurById(Long id) {
        return fournisseurRepository.findById(id)
                .map(fournisseurMapper::toDTO);
    }

    public Optional<FournisseurDTO> getFournisseurByCodeStockiste(String codeStockiste) {
        return fournisseurRepository.findByCodeStockiste(codeStockiste)
                .map(fournisseurMapper::toDTO);
    }

    public List<FournisseurDTO> getFournisseursByType(TypeStockiste typeStockiste) {
        return fournisseurMapper.toDTOList(fournisseurRepository.findByTypeStockiste(typeStockiste));
    }

    public FournisseurDTO saveFournisseur(FournisseurDTO fournisseurDTO) {
        // Validation: Code stockiste unique
        if (fournisseurDTO.getId() == null && fournisseurRepository.existsByCodeStockiste(fournisseurDTO.getCodeStockiste())) {
            throw new IllegalArgumentException("Un fournisseur avec ce code stockiste existe déjà");
        }

        // Validation: Prénom obligatoire pour personne physique
        if (fournisseurDTO.getTypeStockiste() == TypeStockiste.PERSONNE_PHYSIQUE &&
            (fournisseurDTO.getPrenom() == null || fournisseurDTO.getPrenom().trim().isEmpty())) {
            throw new IllegalArgumentException("Le prénom est obligatoire pour une personne physique");
        }

        Fournisseur fournisseur = fournisseurMapper.toEntity(fournisseurDTO);
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        log.info("Fournisseur sauvegardé: {}", savedFournisseur.getCodeStockiste());
        return fournisseurMapper.toDTO(savedFournisseur);
    }

    public FournisseurDTO updateFournisseur(Long id, FournisseurDTO fournisseurDTO) {
        Fournisseur existingFournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur non trouvé"));

        // Vérifier si le code stockiste est modifié et s'il existe déjà
        if (!existingFournisseur.getCodeStockiste().equals(fournisseurDTO.getCodeStockiste()) &&
                fournisseurRepository.existsByCodeStockiste(fournisseurDTO.getCodeStockiste())) {
            throw new IllegalArgumentException("Un fournisseur avec ce code stockiste existe déjà");
        }

        // Validation: Prénom obligatoire pour personne physique
        if (fournisseurDTO.getTypeStockiste() == TypeStockiste.PERSONNE_PHYSIQUE &&
            (fournisseurDTO.getPrenom() == null || fournisseurDTO.getPrenom().trim().isEmpty())) {
            throw new IllegalArgumentException("Le prénom est obligatoire pour une personne physique");
        }

        fournisseurMapper.updateEntity(fournisseurDTO, existingFournisseur);
        Fournisseur updatedFournisseur = fournisseurRepository.save(existingFournisseur);
        log.info("Fournisseur mis à jour: {}", updatedFournisseur.getCodeStockiste());
        return fournisseurMapper.toDTO(updatedFournisseur);
    }

    public void deleteFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur non trouvé"));

        // Soft delete
        fournisseur.setActive(false);
        fournisseurRepository.save(fournisseur);
        log.info("Fournisseur désactivé: {}", fournisseur.getCodeStockiste());
    }

    public boolean existsByCodeStockiste(String codeStockiste) {
        return fournisseurRepository.existsByCodeStockiste(codeStockiste);
    }

    public List<FournisseurDTO> searchFournisseurs(String search) {
        return fournisseurMapper.toDTOList(fournisseurRepository.searchActiveFournisseurs(search));
    }
}