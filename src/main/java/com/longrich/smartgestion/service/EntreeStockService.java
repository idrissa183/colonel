package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.EntreeStockDTO;
import com.longrich.smartgestion.dto.LigneEntreeStockDTO;
import com.longrich.smartgestion.entity.*;
import com.longrich.smartgestion.enums.StatutEntreeStock;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeMouvement;
import com.longrich.smartgestion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntreeStockService {

    private final EntreeStockRepository entreeStockRepository;
    private final LigneEntreeStockRepository ligneEntreeStockRepository;
    private final StockRepository stockRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;

    @Transactional
    public EntreeStock creerEntreeStock(EntreeStockDTO dto) {
        log.info("Création d'une entrée de stock pour le fournisseur: {}", dto.getFournisseurId());

        EntreeStock entreeStock = EntreeStock.builder()
                .dateEntree(dto.getDateEntree() != null ? dto.getDateEntree() : LocalDateTime.now())
                .dateCommande(dto.getDateCommande())
                .dateLivraison(dto.getDateLivraison())
                .numeroFactureFournisseur(dto.getNumeroFactureFournisseur())
                .numeroBonLivraison(dto.getNumeroBonLivraison())
                .statut(StatutEntreeStock.EN_ATTENTE)
                .observation(dto.getObservation())
                .build();

        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + dto.getFournisseurId()));
            entreeStock.setFournisseur(fournisseur);
        }

        EntreeStock savedEntreeStock = entreeStockRepository.save(entreeStock);

        if (dto.getLignesEntree() != null && !dto.getLignesEntree().isEmpty()) {
            for (LigneEntreeStockDTO ligneDto : dto.getLignesEntree()) {
                ajouterLigneEntree(savedEntreeStock.getId(), ligneDto);
            }
        }

        return savedEntreeStock;
    }

    @Transactional
    public LigneEntreeStock ajouterLigneEntree(Long entreeStockId, LigneEntreeStockDTO dto) {
        EntreeStock entreeStock = entreeStockRepository.findById(entreeStockId)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée: " + entreeStockId));

        if (entreeStock.isValidee()) {
            throw new RuntimeException("Impossible de modifier une entrée de stock validée");
        }

        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + dto.getProduitId()));

        LigneEntreeStock ligne = LigneEntreeStock.builder()
                .entreeStock(entreeStock)
                .produit(produit)
                .quantite(dto.getQuantite())
                .quantiteRecue(dto.getQuantiteRecue() != null ? dto.getQuantiteRecue() : 0)
                .prixUnitaire(dto.getPrixUnitaire())
                .datePeremption(dto.getDatePeremption())
                .numeroLot(dto.getNumeroLot())
                .observation(dto.getObservation())
                .emplacementMagasin(dto.getEmplacementMagasin() != null ? dto.getEmplacementMagasin() : "MAGASIN")
                .build();

        LigneEntreeStock savedLigne = ligneEntreeStockRepository.save(ligne);
        
        // Recalculer le montant total
        recalculerMontantTotal(entreeStock);
        
        return savedLigne;
    }

    @Transactional
    public void validerReception(Long entreeStockId, List<LigneEntreeStockDTO> lignesRecues) {
        log.info("Validation de la réception pour l'entrée: {}", entreeStockId);

        EntreeStock entreeStock = entreeStockRepository.findById(entreeStockId)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée: " + entreeStockId));

        if (entreeStock.isValidee()) {
            throw new RuntimeException("Cette entrée de stock est déjà validée");
        }

        // Mettre à jour les quantités reçues
        for (LigneEntreeStockDTO ligneDto : lignesRecues) {
            LigneEntreeStock ligne = ligneEntreeStockRepository.findById(ligneDto.getId())
                    .orElseThrow(() -> new RuntimeException("Ligne non trouvée: " + ligneDto.getId()));
            
            ligne.setQuantiteRecue(ligneDto.getQuantiteRecue());
            ligne.setDatePeremption(ligneDto.getDatePeremption());
            ligne.setNumeroLot(ligneDto.getNumeroLot());
            ligne.setObservation(ligneDto.getObservation());
            
            ligneEntreeStockRepository.save(ligne);

            // Créer/Mettre à jour le stock en magasin
            if (ligneDto.getQuantiteRecue() > 0) {
                mettreAJourStockMagasin(ligne);
                
                // Enregistrer le mouvement de stock
                enregistrerMouvementStock(ligne);
            }
        }

        // Déterminer le nouveau statut
        boolean toutRecu = entreeStock.getLignesEntree().stream()
                .allMatch(ligne -> ligne.getQuantiteRecue().equals(ligne.getQuantite()));
        
        boolean partiellementRecu = entreeStock.getLignesEntree().stream()
                .anyMatch(ligne -> ligne.getQuantiteRecue() > 0);

        if (toutRecu) {
            entreeStock.setStatut(StatutEntreeStock.VALIDEE);
        } else if (partiellementRecu) {
            entreeStock.setStatut(StatutEntreeStock.PARTIELLEMENT_RECUE);
        }

        entreeStockRepository.save(entreeStock);
        log.info("Entrée de stock {} validée avec le statut: {}", entreeStockId, entreeStock.getStatut());
    }

    private void mettreAJourStockMagasin(LigneEntreeStock ligne) {
        // Rechercher un stock existant pour ce produit en magasin
        Optional<Stock> stockExistant = stockRepository.findByProduitAndTypeStock(
                ligne.getProduit(), TypeEmplacement.MAGASIN);

        Stock stock;
        if (stockExistant.isPresent()) {
            stock = stockExistant.get();
            stock.setQuantite(stock.getQuantite() + ligne.getQuantiteRecue());
        } else {
            stock = Stock.builder()
                    .produit(ligne.getProduit())
                    .quantite(ligne.getQuantiteRecue())
                    .quantiteReservee(0)
                    .typeStock(TypeEmplacement.MAGASIN)
                    .emplacement(ligne.getEmplacementMagasin())
                    .dateEntree(ligne.getEntreeStock().getDateEntree().toLocalDate())
                    .build();
        }

        stockRepository.save(stock);
    }

    private void enregistrerMouvementStock(LigneEntreeStock ligne) {
        MouvementStock mouvement = MouvementStock.builder()
                .produit(ligne.getProduit())
                .typeMouvement(TypeMouvement.ENTREE)
                .quantite(ligne.getQuantiteRecue())
                .origine("FOURNISSEUR")
                .destination("MAGASIN")
                .referenceDocument(ligne.getEntreeStock().getNumeroEntree())
                .observation("Réception entrée stock - " + ligne.getEntreeStock().getFournisseur().getNomComplet())
                .dateMouvement(LocalDateTime.now())
                .build();

        mouvementStockRepository.save(mouvement);
    }

    private void recalculerMontantTotal(EntreeStock entreeStock) {
        BigDecimal montantTotal = entreeStock.getLignesEntree().stream()
                .map(ligne -> ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        entreeStock.setMontantTotal(montantTotal);
        entreeStockRepository.save(entreeStock);
    }

    @Transactional(readOnly = true)
    public List<EntreeStock> findAll() {
        return entreeStockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<EntreeStock> findById(Long id) {
        return entreeStockRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<EntreeStock> findByStatut(StatutEntreeStock statut) {
        return entreeStockRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<EntreeStock> findByFournisseur(Long fournisseurId) {
        Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + fournisseurId));
        return entreeStockRepository.findByFournisseur(fournisseur);
    }

    @Transactional
    public void supprimerEntreeStock(Long id) {
        EntreeStock entreeStock = entreeStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée: " + id));

        if (entreeStock.isValidee()) {
            throw new RuntimeException("Impossible de supprimer une entrée de stock validée");
        }

        entreeStockRepository.delete(entreeStock);
        log.info("Entrée de stock {} supprimée", id);
    }
}