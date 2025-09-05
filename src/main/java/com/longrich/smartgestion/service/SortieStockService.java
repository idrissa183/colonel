package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.SortieStockDTO;
import com.longrich.smartgestion.dto.LigneSortieStockDTO;
import com.longrich.smartgestion.entity.*;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeMouvement;
import com.longrich.smartgestion.enums.TypeSortieStock;
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
public class SortieStockService {

    private final SortieStockRepository sortieStockRepository;
    private final LigneSortieStockRepository ligneSortieStockRepository;
    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final FactureRepository factureRepository;

    /**
     * Crée une sortie de stock de type VENTE (surface de vente uniquement)
     */
    @Transactional
    public SortieStock creerSortieVente(SortieStockDTO dto) {
        if (dto.getTypeSortie() != TypeSortieStock.VENTE) {
            throw new RuntimeException("Cette méthode est réservée aux sorties de type VENTE");
        }

        // Les ventes ne concernent que la surface de vente
        dto.setEmplacementOrigine("SURFACE_VENTE");
        
        return creerSortieStock(dto);
    }

    /**
     * Crée une sortie de stock générique avec validation des quantités disponibles
     */
    @Transactional
    public SortieStock creerSortieStock(SortieStockDTO dto) {
        log.info("Création d'une sortie de stock de type: {}", dto.getTypeSortie());

        // Validation selon le type de sortie
        validerReglesMetier(dto);

        SortieStock sortieStock = SortieStock.builder()
                .dateSortie(dto.getDateSortie() != null ? dto.getDateSortie() : LocalDateTime.now())
                .typeSortie(dto.getTypeSortie())
                .emplacementOrigine(dto.getEmplacementOrigine())
                .emplacementDestination(dto.getEmplacementDestination())
                .observation(dto.getObservation())
                .referenceDocument(dto.getReferenceDocument())
                .build();

        // Associer la facture si c'est une vente
        if (dto.getTypeSortie() == TypeSortieStock.VENTE && dto.getFactureId() != null) {
            Facture facture = factureRepository.findById(dto.getFactureId())
                    .orElseThrow(() -> new RuntimeException("Facture non trouvée: " + dto.getFactureId()));
            sortieStock.setFacture(facture);
        }

        SortieStock savedSortieStock = sortieStockRepository.save(sortieStock);

        if (dto.getLignesSortie() != null && !dto.getLignesSortie().isEmpty()) {
            for (LigneSortieStockDTO ligneDto : dto.getLignesSortie()) {
                ajouterLigneSortie(savedSortieStock.getId(), ligneDto);
            }
        }

        // Recalculer le montant total
        recalculerMontantTotal(savedSortieStock);

        return savedSortieStock;
    }

    /**
     * Crée un transfert de stock entre emplacements
     */
    @Transactional
    public SortieStock creerTransfert(String emplacementOrigine, String emplacementDestination, 
                                     List<LigneSortieStockDTO> lignes, String observation) {
        
        // Validation des emplacements
        if (!isEmplacementValide(emplacementOrigine) || !isEmplacementValide(emplacementDestination)) {
            throw new RuntimeException("Emplacements invalides. Utilisez MAGASIN ou SURFACE_VENTE");
        }

        if (emplacementOrigine.equals(emplacementDestination)) {
            throw new RuntimeException("L'emplacement d'origine et de destination ne peuvent pas être identiques");
        }

        SortieStockDTO dto = new SortieStockDTO();
        dto.setTypeSortie(TypeSortieStock.TRANSFERT);
        dto.setEmplacementOrigine(emplacementOrigine);
        dto.setEmplacementDestination(emplacementDestination);
        dto.setObservation(observation);
        dto.setLignesSortie(lignes);

        return creerSortieStock(dto);
    }

    @Transactional
    public LigneSortieStock ajouterLigneSortie(Long sortieStockId, LigneSortieStockDTO dto) {
        SortieStock sortieStock = sortieStockRepository.findById(sortieStockId)
                .orElseThrow(() -> new RuntimeException("Sortie de stock non trouvée: " + sortieStockId));

        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + dto.getProduitId()));

        // Validation de la disponibilité du stock
        validerDisponibiliteStock(produit, dto.getQuantite(), sortieStock.getEmplacementOrigine());

        LigneSortieStock ligne = LigneSortieStock.builder()
                .sortieStock(sortieStock)
                .produit(produit)
                .quantite(dto.getQuantite())
                .prixUnitaire(dto.getPrixUnitaire())
                .datePeremption(dto.getDatePeremption())
                .numeroLot(dto.getNumeroLot())
                .observation(dto.getObservation())
                .emplacementOrigine(sortieStock.getEmplacementOrigine())
                .emplacementDestination(sortieStock.getEmplacementDestination())
                .build();

        LigneSortieStock savedLigne = ligneSortieStockRepository.save(ligne);

        // Mettre à jour le stock
        mettreAJourStock(savedLigne);

        // Enregistrer le mouvement de stock
        enregistrerMouvementStock(savedLigne);

        return savedLigne;
    }

    private void validerReglesMetier(SortieStockDTO dto) {
        switch (dto.getTypeSortie()) {
            case VENTE:
                if (!"SURFACE_VENTE".equals(dto.getEmplacementOrigine())) {
                    throw new RuntimeException("Les ventes ne peuvent se faire que depuis la SURFACE_VENTE");
                }
                break;
            case TRANSFERT:
                if (dto.getEmplacementDestination() == null) {
                    throw new RuntimeException("L'emplacement de destination est obligatoire pour les transferts");
                }
                if (dto.getEmplacementOrigine().equals(dto.getEmplacementDestination())) {
                    throw new RuntimeException("Les emplacements d'origine et de destination doivent être différents");
                }
                break;
            case PERTE:
            case PEREMPTION:
                // Pas de règles spécifiques
                break;
        }
    }

    private void validerDisponibiliteStock(Produit produit, Integer quantiteRequise, String emplacement) {
        TypeEmplacement typeEmplacement = "MAGASIN".equals(emplacement) ? 
            TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
        
        Optional<Stock> stockOpt = stockRepository.findByProduitAndTypeStock(produit, typeEmplacement);
        
        if (stockOpt.isEmpty()) {
            throw new RuntimeException("Aucun stock disponible pour le produit: " + produit.getLibelle() + 
                " dans l'emplacement: " + emplacement);
        }

        Stock stock = stockOpt.get();
        int quantiteDisponible = stock.getQuantite() - stock.getQuantiteReservee();
        
        if (quantiteDisponible < quantiteRequise) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + produit.getLibelle() + 
                ". Disponible: " + quantiteDisponible + ", Requis: " + quantiteRequise);
        }
    }

    private void mettreAJourStock(LigneSortieStock ligne) {
        TypeEmplacement typeEmplacement = "MAGASIN".equals(ligne.getEmplacementOrigine()) ? 
            TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
        
        Stock stock = stockRepository.findByProduitAndTypeStock(ligne.getProduit(), typeEmplacement)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour le produit: " + 
                    ligne.getProduit().getLibelle()));

        // Diminuer le stock d'origine
        stock.setQuantite(stock.getQuantite() - ligne.getQuantite());
        stockRepository.save(stock);

        // Si c'est un transfert, augmenter le stock de destination
        if (ligne.getSortieStock().getTypeSortie() == TypeSortieStock.TRANSFERT) {
            TypeEmplacement typeDestination = "MAGASIN".equals(ligne.getEmplacementDestination()) ? 
                TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
            
            Optional<Stock> stockDestOpt = stockRepository.findByProduitAndTypeStock(
                ligne.getProduit(), typeDestination);
            
            Stock stockDest;
            if (stockDestOpt.isPresent()) {
                stockDest = stockDestOpt.get();
                stockDest.setQuantite(stockDest.getQuantite() + ligne.getQuantite());
            } else {
                stockDest = Stock.builder()
                        .produit(ligne.getProduit())
                        .quantite(ligne.getQuantite())
                        .quantiteReservee(0)
                        .typeStock(typeDestination)
                        .emplacement(ligne.getEmplacementDestination())
                        .dateEntree(LocalDateTime.now().toLocalDate())
                        .build();
            }
            stockRepository.save(stockDest);
        }
    }

    private void enregistrerMouvementStock(LigneSortieStock ligne) {
        TypeMouvement typeMouvement = switch (ligne.getSortieStock().getTypeSortie()) {
            case VENTE -> TypeMouvement.SORTIE;
            case PERTE -> TypeMouvement.SORTIE;
            case PEREMPTION -> TypeMouvement.SORTIE;
            case TRANSFERT -> TypeMouvement.TRANSFERT;
        };

        MouvementStock mouvement = MouvementStock.builder()
                .produit(ligne.getProduit())
                .typeMouvement(typeMouvement)
                .quantite(ligne.getQuantite())
                .origine(ligne.getEmplacementOrigine())
                .destination(ligne.getEmplacementDestination())
                .referenceDocument(ligne.getSortieStock().getNumeroSortie())
                .observation(ligne.getSortieStock().getObservation())
                .dateMouvement(LocalDateTime.now())
                .build();

        mouvementStockRepository.save(mouvement);
    }

    private void recalculerMontantTotal(SortieStock sortieStock) {
        BigDecimal montantTotal = sortieStock.getLignesSortie().stream()
                .map(LigneSortieStock::getMontantLigne)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        sortieStock.setMontantTotal(montantTotal);
        sortieStockRepository.save(sortieStock);
    }

    private boolean isEmplacementValide(String emplacement) {
        return "MAGASIN".equals(emplacement) || "SURFACE_VENTE".equals(emplacement);
    }

    @Transactional(readOnly = true)
    public List<SortieStock> findAll() {
        return sortieStockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SortieStock> findByTypeSortie(TypeSortieStock typeSortie) {
        return sortieStockRepository.findByTypeSortie(typeSortie);
    }

    @Transactional(readOnly = true)
    public Optional<SortieStock> findById(Long id) {
        return sortieStockRepository.findById(id);
    }

    @Transactional
    public void supprimerSortieStock(Long id) {
        SortieStock sortieStock = sortieStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie de stock non trouvée: " + id));

        // Ne pas permettre la suppression si c'est une vente déjà facturée
        if (sortieStock.getTypeSortie() == TypeSortieStock.VENTE && sortieStock.getFacture() != null) {
            throw new RuntimeException("Impossible de supprimer une sortie de stock liée à une facture");
        }

        sortieStockRepository.delete(sortieStock);
        log.info("Sortie de stock {} supprimée", id);
    }
}