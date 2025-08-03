package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.entity.FamilleProduit;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.repository.FamilleProduitRepository;
import com.longrich.smartgestion.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final FamilleProduitRepository familleProduitRepository;

    public List<ProduitDto> getAllProduits() {
        return produitRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProduitDto> getActiveProduits() {
        return produitRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProduitDto> getProduitById(Long id) {
        return produitRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<ProduitDto> getProduitByCodeBarre(String codeBarre) {
        return produitRepository.findByCodeBarre(codeBarre)
                .map(this::convertToDto);
    }

    public List<ProduitDto> searchProduits(String search) {
        return produitRepository.searchActiveProduits(search).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProduitDto> getProduitsEnStock() {
        return produitRepository.findProduitsEnStock().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProduitDto> getProduitsStockFaible() {
        return produitRepository.findProduitsStockFaible().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProduitDto saveProduit(ProduitDto produitDto) {
        if (produitDto.getId() == null && produitRepository.existsByCodeBarre(produitDto.getCodeBarre())) {
            throw new IllegalArgumentException("Un produit avec ce code barre existe déjà");
        }

        Produit produit = convertToEntity(produitDto);
        Produit savedProduit = produitRepository.save(produit);
        log.info("Produit sauvegardé: {}", savedProduit.getCodeBarre());
        return convertToDto(savedProduit);
    }

    public ProduitDto updateProduit(Long id, ProduitDto produitDto) {
        Produit existingProduit = produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        // Vérifier si le code barre est modifié et s'il existe déjà
        if (!existingProduit.getCodeBarre().equals(produitDto.getCodeBarre()) && 
            produitRepository.existsByCodeBarre(produitDto.getCodeBarre())) {
            throw new IllegalArgumentException("Un produit avec ce code barre existe déjà");
        }

        updateProduitFromDto(existingProduit, produitDto);
        Produit updatedProduit = produitRepository.save(existingProduit);
        log.info("Produit mis à jour: {}", updatedProduit.getCodeBarre());
        return convertToDto(updatedProduit);
    }

    public void deleteProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));
        
        // Soft delete
        produit.setActive(false);
        produitRepository.save(produit);
        log.info("Produit désactivé: {}", produit.getCodeBarre());
    }

    private ProduitDto convertToDto(Produit produit) {
        ProduitDto dto = ProduitDto.builder()
                .id(produit.getId())
                .codeBarre(produit.getCodeBarre())
                .libelle(produit.getLibelle())
                .description(produit.getDescription())
                .datePeremption(produit.getDatePeremption())
                .prixAchat(produit.getPrixAchat())
                .prixRevente(produit.getPrixRevente())
                .pv(produit.getPv())
                .active(produit.getActive())
                .stockMinimum(produit.getStockMinimum())
                .marge(produit.getMarge())
                .pourcentageMarge(produit.getPourcentageMarge())
                .build();

        if (produit.getFamille() != null) {
            dto.setFamilleId(produit.getFamille().getId());
            dto.setFamilleName(produit.getFamille().getLibelle());
        }

        // Calculer la quantité en stock
        if (produit.getStocks() != null && !produit.getStocks().isEmpty()) {
            int quantiteStock = produit.getStocks().stream()
                    .mapToInt(stock -> stock.getQuantiteDisponible())
                    .sum();
            dto.setQuantiteStock(quantiteStock);
            dto.setStockFaible(quantiteStock <= (produit.getStockMinimum() != null ? produit.getStockMinimum() : 0));
        }

        return dto;
    }

    private Produit convertToEntity(ProduitDto dto) {
        Produit produit = Produit.builder()
                .id(dto.getId())
                .codeBarre(dto.getCodeBarre())
                .libelle(dto.getLibelle())
                .description(dto.getDescription())
                .datePeremption(dto.getDatePeremption())
                .prixAchat(dto.getPrixAchat())
                .prixRevente(dto.getPrixRevente())
                .pv(dto.getPv())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .stockMinimum(dto.getStockMinimum())
                .build();

        if (dto.getFamilleId() != null) {
            FamilleProduit famille = familleProduitRepository.findById(dto.getFamilleId())
                    .orElseThrow(() -> new IllegalArgumentException("Famille de produit non trouvée"));
            produit.setFamille(famille);
        }

        return produit;
    }

    private void updateProduitFromDto(Produit produit, ProduitDto dto) {
        produit.setCodeBarre(dto.getCodeBarre());
        produit.setLibelle(dto.getLibelle());
        produit.setDescription(dto.getDescription());
        produit.setDatePeremption(dto.getDatePeremption());
        produit.setPrixAchat(dto.getPrixAchat());
        produit.setPrixRevente(dto.getPrixRevente());
        produit.setPv(dto.getPv());
        produit.setStockMinimum(dto.getStockMinimum());
        
        if (dto.getActive() != null) {
            produit.setActive(dto.getActive());
        }

        if (dto.getFamilleId() != null) {
            FamilleProduit famille = familleProduitRepository.findById(dto.getFamilleId())
                    .orElseThrow(() -> new IllegalArgumentException("Famille de produit non trouvée"));
            produit.setFamille(famille);
        }
    }
}