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

    public List<Produit> findAll() {
        return produitRepository.findAll();
    }

    public List<ProduitDto> getActiveProduits() {
        return produitRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // public Optional<ProduitDto> getProduitById(Long id) {
    // return produitRepository.findById(id)
    // .map(this::convertToDto);
    // }

    // Méthode de recherche par code barre supprimée - utilisation de l'ID

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
        // Vérification d'unicité du libellé
        if (produitRepository.existsByLibelle(produitDto.getLibelle())) {
            throw new IllegalArgumentException("Un produit avec ce libellé existe déjà");
        }

        Produit produit = convertToEntity(produitDto);
        Produit savedProduit = produitRepository.save(produit);
        log.info("Produit sauvegardé: {}", savedProduit.getId());
        return convertToDto(savedProduit);
    }

    public ProduitDto updateProduit(Long id, ProduitDto produitDto) {
        Produit existingProduit = produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        // Vérification d'unicité du libellé (exclure le produit actuel)
        if (produitRepository.existsByLibelleAndIdNot(produitDto.getLibelle(), id)) {
            throw new IllegalArgumentException("Un produit avec ce libellé existe déjà");
        }

        updateProduitFromDto(existingProduit, produitDto);
        Produit updatedProduit = produitRepository.save(existingProduit);
        log.info("Produit mis à jour: {}", updatedProduit.getId());
        return convertToDto(updatedProduit);
    }

    public void deleteProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        // Soft delete
        produit.setActive(false);
        produitRepository.save(produit);
        log.info("Produit désactivé: {}", produit.getId());
    }

    private ProduitDto convertToDto(Produit produit) {
        ProduitDto dto = ProduitDto.builder()
                .id(produit.getId())
                // Code barre supprimé
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

        if (produit.getFamilleProduit() != null) {
            dto.setFamilleId(produit.getFamilleProduit().getId());
            dto.setFamilleName(produit.getFamilleProduit().getLibelleFamille());
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
        Produit produit = new Produit();
        if (dto.getId() != null) {
            produit.setId(dto.getId());
        }
        // Code barre supprimé
        produit.setLibelle(dto.getLibelle());
        produit.setDescription(dto.getDescription());
        produit.setDatePeremption(dto.getDatePeremption());
        produit.setPrixAchat(dto.getPrixAchat());
        produit.setPrixRevente(dto.getPrixRevente());
        produit.setPv(dto.getPv());
        produit.setActive(Boolean.TRUE.equals(dto.getActive()));
        produit.setStockMinimum(dto.getStockMinimum());

        if (dto.getFamilleId() != null) {
            FamilleProduit famille = familleProduitRepository.findById(dto.getFamilleId())
                    .orElseThrow(() -> new IllegalArgumentException("Famille de produit non trouvée"));
            produit.setFamilleProduit(famille);
        }

        return produit;
    }

    private void updateProduitFromDto(Produit produit, ProduitDto dto) {
        // Code barre supprimé
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
            produit.setFamilleProduit(famille);
        }
    }

    public Optional<ProduitDto> getProduitById(Long id) {
        log.debug("Recherche du produit avec ID: {}", id);

        if (id == null) {
            log.warn("Tentative de recherche avec ID null");
            return Optional.empty();
        }

        try {
            Optional<Produit> produitOpt = produitRepository.findById(id);
            if (produitOpt.isPresent()) {
                Produit produit = produitOpt.get();
                log.debug("Produit trouvé: {} avec ID réel: {}", produit.getLibelle(), produit.getId());
                return Optional.of(convertToDto(produit));
            } else {
                log.warn("Aucun produit trouvé avec l'ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du produit avec ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    // Méthode utilitaire pour vérifier les données
    public void verifierProduitsAvecIdNull() {
        try {
            List<Produit> produits = produitRepository.findAll();
            long produitsAvecIdNull = produits.stream()
                    .filter(p -> p.getId() == null)
                    .count();

            if (produitsAvecIdNull > 0) {
                log.error("ATTENTION: {} produits ont un ID null dans la base de données!", produitsAvecIdNull);

                // Lister les produits problématiques
                produits.stream()
                        .filter(p -> p.getId() == null)
                        .forEach(p -> log.error("Produit avec ID null: {}", p.getLibelle()));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des IDs: {}", e.getMessage());
        }
    }

    /**
     * Recherche un produit par son ID - méthode utilitaire pour les UI
     */
    @Transactional(readOnly = true)
    public Produit findById(Long id) {
        if (id == null) {
            return null;
        }
        return produitRepository.findById(id).orElse(null);
    }
}