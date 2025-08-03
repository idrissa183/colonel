package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.FamilleProduitDTO;
import com.longrich.smartgestion.entity.FamilleProduit;
import com.longrich.smartgestion.repository.FamilleProduitRepository;
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
public class FamilleProduitService {

    private final FamilleProduitRepository familleProduitRepository;

    public List<FamilleProduitDTO> getAllFamilles() {
        return familleProduitRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<FamilleProduitDTO> getFamilleById(Long id) {
        return familleProduitRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<FamilleProduitDTO> getFamilleByCode(String codeFamille) {
        return familleProduitRepository.findByCodeFamille(codeFamille)
                .map(this::convertToDTO);
    }

    public FamilleProduitDTO saveFamille(FamilleProduitDTO familleDTO) {
        if (familleDTO.getId() == null && 
            familleProduitRepository.existsByCodeFamille(familleDTO.getCodeFamille())) {
            throw new IllegalArgumentException("Une famille avec ce code existe déjà");
        }

        FamilleProduit famille = convertToEntity(familleDTO);
        FamilleProduit savedFamille = familleProduitRepository.save(famille);
        log.info("Famille produit sauvegardée: {}", savedFamille.getCodeFamille());
        return convertToDTO(savedFamille);
    }

    public FamilleProduitDTO updateFamille(Long id, FamilleProduitDTO familleDTO) {
        FamilleProduit existingFamille = familleProduitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Famille produit non trouvée"));

        if (!existingFamille.getCodeFamille().equals(familleDTO.getCodeFamille()) && 
            familleProduitRepository.existsByCodeFamille(familleDTO.getCodeFamille())) {
            throw new IllegalArgumentException("Une famille avec ce code existe déjà");
        }

        updateFamilleFromDTO(existingFamille, familleDTO);
        FamilleProduit updatedFamille = familleProduitRepository.save(existingFamille);
        log.info("Famille produit mise à jour: {}", updatedFamille.getCodeFamille());
        return convertToDTO(updatedFamille);
    }

    public void deleteFamille(Long id) {
        FamilleProduit famille = familleProduitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Famille produit non trouvée"));
        
        famille.setActive(false);
        familleProduitRepository.save(famille);
        log.info("Famille produit désactivée: {}", famille.getCodeFamille());
    }

    private FamilleProduitDTO convertToDTO(FamilleProduit famille) {
        return FamilleProduitDTO.builder()
                .id(famille.getId())
                .codeFamille(famille.getCodeFamille())
                .libelleFamille(famille.getLibelleFamille())
                .description(famille.getDescription())
                .active(famille.getActive())
                .nombreProduits(famille.getProduits() != null ? famille.getProduits().size() : 0)
                .build();
    }

    private FamilleProduit convertToEntity(FamilleProduitDTO dto) {
        return FamilleProduit.builder()
                .id(dto.getId())
                .codeFamille(dto.getCodeFamille())
                .libelleFamille(dto.getLibelleFamille())
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    private void updateFamilleFromDTO(FamilleProduit famille, FamilleProduitDTO dto) {
        famille.setCodeFamille(dto.getCodeFamille());
        famille.setLibelleFamille(dto.getLibelleFamille());
        famille.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            famille.setActive(dto.getActive());
        }
    }
}