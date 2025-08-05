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


    public FamilleProduitDTO saveFamille(FamilleProduitDTO familleDTO) {
        if (familleDTO.getId() == null &&
                familleProduitRepository.existsByLibelleFamille(familleDTO.getLibelleFamille())) {
            throw new IllegalArgumentException("Une famille avec ce libellé existe déjà");
        }

        FamilleProduit famille = convertToEntity(familleDTO);
        FamilleProduit savedFamille = familleProduitRepository.save(famille);
        log.info("Famille produit sauvegardée: {}", savedFamille.getLibelleFamille());
        return convertToDTO(savedFamille);
    }

    public FamilleProduitDTO updateFamille(Long id, FamilleProduitDTO familleDTO) {
        FamilleProduit existingFamille = familleProduitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Famille produit non trouvée"));

        if (!existingFamille.getLibelleFamille().equals(familleDTO.getLibelleFamille()) &&
                familleProduitRepository.existsByLibelleFamille(familleDTO.getLibelleFamille())) {
            throw new IllegalArgumentException("Une famille avec ce libellé existe déjà");
        }

        updateFamilleFromDTO(existingFamille, familleDTO);
        FamilleProduit updatedFamille = familleProduitRepository.save(existingFamille);
        log.info("Famille produit mise à jour: {}", updatedFamille.getLibelleFamille());
        return convertToDTO(updatedFamille);
    }

    public void deleteFamille(Long id) {
        FamilleProduit famille = familleProduitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Famille produit non trouvée"));

        famille.setActive(false);
        familleProduitRepository.save(famille);
        log.info("Famille produit désactivée: {}", famille.getLibelleFamille());
    }

    private FamilleProduitDTO convertToDTO(FamilleProduit famille) {
        return FamilleProduitDTO.builder()
                .id(famille.getId())
                .libelleFamille(famille.getLibelleFamille())
                .description(famille.getDescription())
                .active(famille.getActive())
                .nombreProduits(famille.getProduits() != null ? famille.getProduits().size() : 0)
                .build();
    }

    private FamilleProduit convertToEntity(FamilleProduitDTO dto) {
        FamilleProduit famille = new FamilleProduit();
        if (dto.getId() != null) {
            famille.setId(dto.getId());
        }
        famille.setLibelleFamille(dto.getLibelleFamille());
        famille.setDescription(dto.getDescription());
        famille.setActive(Boolean.TRUE.equals(dto.getActive()));
        return famille;
    }

    private void updateFamilleFromDTO(FamilleProduit famille, FamilleProduitDTO dto) {
        famille.setLibelleFamille(dto.getLibelleFamille());
        famille.setDescription(dto.getDescription());
        famille.setActive(Boolean.TRUE.equals(dto.getActive()));
    }
}