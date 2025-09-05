package com.longrich.smartgestion.service;

import com.longrich.smartgestion.entity.Stock;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.entity.MouvementStock;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeMouvement;
import com.longrich.smartgestion.repository.StockRepository;
import com.longrich.smartgestion.repository.ProduitRepository;
import com.longrich.smartgestion.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransfertInventaireService {
    
    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    
    private static final TypeEmplacement TYPE_SURFACE_VENTE = TypeEmplacement.SURFACE_VENTE;
    private static final TypeEmplacement TYPE_MAGASIN = TypeEmplacement.MAGASIN;
    
    /**
     * Effectue un transfert automatique vers la surface de vente si nécessaire
     */
    @Transactional
    public void verifierEtEffectuerTransfertAutomatique() {
        List<Stock> stocksSurfaceVente = stockRepository.findByTypeStock(TYPE_SURFACE_VENTE);
        
        for (Stock stockSurface : stocksSurfaceVente) {
            // Si le stock en surface de vente est faible, transférer depuis le magasin
            if (stockSurface.getQuantiteDisponible() <= 5) {
                transfererDepuisMagasin(stockSurface.getProduit().getId(), 10);
            }
        }
    }
    
    @Transactional
    public void transfererDepuisMagasin(Long produitId, Integer quantite) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        Stock stockMagasin = stockRepository.findByProduitAndTypeStock(produit, TYPE_MAGASIN)
            .orElse(null);
            
        if (stockMagasin == null || stockMagasin.getQuantiteDisponible() < quantite) {
            log.warn("Stock insuffisant en magasin pour le produit: {}", produit.getLibelle());
            return;
        }
        
        // Diminuer stock magasin
        stockMagasin.setQuantite(stockMagasin.getQuantite() - quantite);
        stockRepository.save(stockMagasin);
        
        // Augmenter stock surface de vente
        Stock stockSurfaceVente = stockRepository.findByProduitAndTypeStock(produit, TYPE_SURFACE_VENTE)
            .orElseGet(() -> {
                Stock newStock = Stock.builder()
                    .produit(produit)
                    .quantite(0)
                    .quantiteReservee(0)
                    .typeStock(TYPE_SURFACE_VENTE)
                    .dateEntree(LocalDate.now())
                    .build();
                return stockRepository.save(newStock);
            });
        
        stockSurfaceVente.setQuantite(stockSurfaceVente.getQuantite() + quantite);
        stockRepository.save(stockSurfaceVente);
        
        // Créer mouvements de stock
        creerMouvementTransfert(produit, quantite, TYPE_MAGASIN, TYPE_SURFACE_VENTE);
        
        log.info("Transfert automatique effectué - Produit: {}, Quantité: {} vers surface de vente", 
            produit.getLibelle(), quantite);
    }
    
    @Transactional(readOnly = true)
    public List<Stock> getStocksNecessitantTransfert() {
        return stockRepository.findByTypeStock(TYPE_SURFACE_VENTE)
            .stream()
            .filter(stock -> stock.getQuantiteDisponible() <= 5)
            .toList();
    }
    
    private void creerMouvementTransfert(Produit produit, Integer quantite, TypeEmplacement origine, TypeEmplacement destination) {
        // Mouvement de sortie
        MouvementStock mouvementSortie = MouvementStock.builder()
            .produit(produit)
            .typeMouvement(TypeMouvement.SORTIE)
            .quantite(quantite)
            .origine(origine.getDisplayName())
            .destination(destination.getDisplayName())
            .observation("Transfert automatique de " + origine.getDisplayName() + " vers " + destination.getDisplayName())
            .dateMouvement(LocalDateTime.now())
            .build();
        
        mouvementStockRepository.save(mouvementSortie);
        
        // Mouvement d'entrée
        MouvementStock mouvementEntree = MouvementStock.builder()
            .produit(produit)
            .typeMouvement(TypeMouvement.ENTREE)
            .quantite(quantite)
            .origine(origine.getDisplayName())
            .destination(destination.getDisplayName())
            .observation("Réception transfert de " + origine.getDisplayName() + " vers " + destination.getDisplayName())
            .dateMouvement(LocalDateTime.now())
            .build();
        
        mouvementStockRepository.save(mouvementEntree);
    }
}