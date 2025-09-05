package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.LigneSortieStockDTO;
import com.longrich.smartgestion.dto.SortieStockDTO;
import com.longrich.smartgestion.dto.TransfertStockDTO;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.entity.SortieStock;
import com.longrich.smartgestion.entity.Stock;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeSortieStock;
import com.longrich.smartgestion.repository.ProduitRepository;
import com.longrich.smartgestion.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransfertStockService {

    private final SortieStockService sortieStockService;
    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;

    /**
     * Effectue un transfert de magasin vers surface de vente
     */
    @Transactional
    public SortieStock transfererVersVente(TransfertStockDTO dto) {
        return effectuerTransfert("MAGASIN", "SURFACE_VENTE", dto);
    }

    /**
     * Effectue un transfert de surface de vente vers magasin
     */
    @Transactional
    public SortieStock transfererVersMagasin(TransfertStockDTO dto) {
        return effectuerTransfert("SURFACE_VENTE", "MAGASIN", dto);
    }

    /**
     * Effectue un transfert générique entre emplacements
     */
    @Transactional
    public SortieStock effectuerTransfert(String emplacementOrigine, String emplacementDestination, 
                                         TransfertStockDTO dto) {
        log.info("Transfert de {} vers {} pour {} produits", 
            emplacementOrigine, emplacementDestination, dto.getLignesTransfert().size());

        // Validation des emplacements
        validerEmplacements(emplacementOrigine, emplacementDestination);

        // Validation et préparation des lignes
        List<LigneSortieStockDTO> lignes = new ArrayList<>();
        for (TransfertStockDTO.LigneTransfert ligneDto : dto.getLignesTransfert()) {
            validerLigneTransfert(ligneDto, emplacementOrigine);
            
            LigneSortieStockDTO ligne = LigneSortieStockDTO.builder()
                    .produitId(ligneDto.getProduitId())
                    .quantite(ligneDto.getQuantite())
                    .prixUnitaire(obtenirPrixProduit(ligneDto.getProduitId()))
                    .observation(ligneDto.getObservation())
                    .build();
            
            lignes.add(ligne);
        }

        // Créer la sortie de type transfert
        return sortieStockService.creerTransfert(emplacementOrigine, emplacementDestination, 
                                               lignes, dto.getObservation());
    }

    /**
     * Obtient les stocks disponibles pour transfert par emplacement
     */
    @Transactional(readOnly = true)
    public List<Stock> getStocksDisponiblesPourTransfert(String emplacement) {
        TypeEmplacement type = "MAGASIN".equals(emplacement) ? 
            TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
        
        return stockRepository.findByTypeStockAndQuantiteGreaterThan(type, 0);
    }

    /**
     * Calcule les quantités recommandées pour un transfert automatique vers la surface de vente
     */
    @Transactional(readOnly = true)
    public List<TransfertStockDTO.LigneTransfert> calculerTransfertRecommande() {
        List<TransfertStockDTO.LigneTransfert> recommandations = new ArrayList<>();
        
        // Logique pour recommander des transferts basés sur :
        // - Stock faible en surface de vente
        // - Stock disponible en magasin
        // - Historique des ventes
        
        List<Stock> stocksSurfaceVente = stockRepository.findByTypeStock(TypeEmplacement.SURFACE_VENTE);
        
        for (Stock stockVente : stocksSurfaceVente) {
            // Si le stock en surface de vente est faible (< 10 unités par exemple)
            if (stockVente.getQuantite() < 10) {
                Optional<Stock> stockMagasinOpt = stockRepository
                        .findByProduitAndTypeStock(stockVente.getProduit(), TypeEmplacement.MAGASIN);
                
                if (stockMagasinOpt.isPresent()) {
                    Stock stockMagasin = stockMagasinOpt.get();
                    if (stockMagasin.getQuantite() > 5) {
                        // Recommander un transfert de 20 unités ou la moitié du stock magasin
                        int quantiteRecommandee = Math.min(20, stockMagasin.getQuantite() / 2);
                        
                        TransfertStockDTO.LigneTransfert ligne = new TransfertStockDTO.LigneTransfert();
                        ligne.setProduitId(stockVente.getProduit().getId());
                        ligne.setQuantite(quantiteRecommandee);
                        ligne.setObservation("Réapprovisionnement automatique - stock faible en surface");
                        
                        recommandations.add(ligne);
                    }
                }
            }
        }
        
        return recommandations;
    }

    private void validerEmplacements(String origine, String destination) {
        if (!isEmplacementValide(origine) || !isEmplacementValide(destination)) {
            throw new RuntimeException("Emplacements invalides. Utilisez MAGASIN ou SURFACE_VENTE");
        }
        
        if (origine.equals(destination)) {
            throw new RuntimeException("L'emplacement d'origine et de destination ne peuvent pas être identiques");
        }
    }

    private void validerLigneTransfert(TransfertStockDTO.LigneTransfert ligne, String emplacementOrigine) {
        if (ligne.getProduitId() == null) {
            throw new RuntimeException("Le produit est obligatoire pour une ligne de transfert");
        }
        
        if (ligne.getQuantite() == null || ligne.getQuantite() <= 0) {
            throw new RuntimeException("La quantité doit être positive");
        }

        // Vérifier la disponibilité du stock
        Produit produit = produitRepository.findById(ligne.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + ligne.getProduitId()));
        
        TypeEmplacement type = "MAGASIN".equals(emplacementOrigine) ? 
            TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
        
        Optional<Stock> stockOpt = stockRepository.findByProduitAndTypeStock(produit, type);
        
        if (stockOpt.isEmpty()) {
            throw new RuntimeException("Aucun stock disponible pour le produit: " + produit.getLibelle() + 
                " dans l'emplacement: " + emplacementOrigine);
        }
        
        Stock stock = stockOpt.get();
        int disponible = stock.getQuantite() - stock.getQuantiteReservee();
        
        if (disponible < ligne.getQuantite()) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + produit.getLibelle() + 
                ". Disponible: " + disponible + ", Demandé: " + ligne.getQuantite());
        }
    }

    private BigDecimal obtenirPrixProduit(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + produitId));
        
        // Utiliser le prix d'achat comme référence pour les transferts internes
        return produit.getPrixAchat() != null ? produit.getPrixAchat() : BigDecimal.ZERO;
    }

    private boolean isEmplacementValide(String emplacement) {
        return "MAGASIN".equals(emplacement) || "SURFACE_VENTE".equals(emplacement);
    }
}