package com.longrich.smartgestion.service;

import com.longrich.smartgestion.entity.Stock;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.entity.MouvementStock;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventaireService {
    
    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    
    private static final String TYPE_SURFACE_VENTE = "SALLE_VENTE";
    private static final String TYPE_MAGASIN = "MAGASIN";
    
    // === GESTION INVENTAIRE SURFACE DE VENTE ===
    
    @Transactional(readOnly = true)
    public List<Stock> getInventaireSurfaceVente() {
        return stockRepository.findByTypeStock(TYPE_SURFACE_VENTE);
    }
    
    @Transactional(readOnly = true)
    public List<Stock> getInventaireMagasin() {
        return stockRepository.findByTypeStock(TYPE_MAGASIN);
    }
    
    @Transactional
    public void effectuerInventaireHebdomadaire() {
        LocalDate today = LocalDate.now();
        
        if (today.getDayOfWeek().getValue() == 1) { // Lundi
            log.info("Effectuer inventaire hebdomadaire de la surface de vente");
            List<Stock> stocksSurfaceVente = getInventaireSurfaceVente();
            
            stocksSurfaceVente.forEach(stock -> {
                log.info("Inventaire - Produit: {}, Quantité: {}", 
                    stock.getProduit().getLibelle(), stock.getQuantite());
            });
        }
    }
    
    // === TRANSFERTS ENTRE INVENTAIRES ===
    
    @Transactional
    public void transfererVersSurfaceVente(Long produitId, Integer quantite) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        // Vérifier stock magasin
        Stock stockMagasin = stockRepository.findByProduitAndTypeStock(produit, TYPE_MAGASIN)
            .orElseThrow(() -> new RuntimeException("Stock magasin non trouvé"));
        
        if (stockMagasin.getQuantiteDisponible() < quantite) {
            throw new RuntimeException("Stock insuffisant en magasin");
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
        
        log.info("Transfert effectué - Produit: {}, Quantité: {} vers surface de vente", 
            produit.getLibelle(), quantite);
    }
    
    @Transactional
    public void transfererVersMagasin(Long produitId, Integer quantite) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        // Vérifier stock surface de vente
        Stock stockSurfaceVente = stockRepository.findByProduitAndTypeStock(produit, TYPE_SURFACE_VENTE)
            .orElseThrow(() -> new RuntimeException("Stock surface de vente non trouvé"));
        
        if (stockSurfaceVente.getQuantiteDisponible() < quantite) {
            throw new RuntimeException("Stock insuffisant en surface de vente");
        }
        
        // Diminuer stock surface de vente
        stockSurfaceVente.setQuantite(stockSurfaceVente.getQuantite() - quantite);
        stockRepository.save(stockSurfaceVente);
        
        // Augmenter stock magasin
        Stock stockMagasin = stockRepository.findByProduitAndTypeStock(produit, TYPE_MAGASIN)
            .orElseGet(() -> {
                Stock newStock = Stock.builder()
                    .produit(produit)
                    .quantite(0)
                    .quantiteReservee(0)
                    .typeStock(TYPE_MAGASIN)
                    .dateEntree(LocalDate.now())
                    .build();
                return stockRepository.save(newStock);
            });
        
        stockMagasin.setQuantite(stockMagasin.getQuantite() + quantite);
        stockRepository.save(stockMagasin);
        
        // Créer mouvements de stock
        creerMouvementTransfert(produit, quantite, TYPE_SURFACE_VENTE, TYPE_MAGASIN);
        
        log.info("Transfert effectué - Produit: {}, Quantité: {} vers magasin", 
            produit.getLibelle(), quantite);
    }
    
    // === CONSULTATION STOCKS PAR TYPE ===
    
    @Transactional(readOnly = true)
    public Optional<Stock> getStockSurfaceVente(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        return stockRepository.findByProduitAndTypeStock(produit, TYPE_SURFACE_VENTE);
    }
    
    @Transactional(readOnly = true)
    public Optional<Stock> getStockMagasin(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        return stockRepository.findByProduitAndTypeStock(produit, TYPE_MAGASIN);
    }
    
    @Transactional(readOnly = true)
    public boolean verifierDisponibiliteSurfaceVente(Long produitId, Integer quantiteRequise) {
        return getStockSurfaceVente(produitId)
            .map(stock -> stock.getQuantiteDisponible() >= quantiteRequise)
            .orElse(false);
    }
    
    // === MÉTHODES PRIVÉES ===
    
    private void creerMouvementTransfert(Produit produit, Integer quantite, String origine, String destination) {
        // Mouvement de sortie
        MouvementStock mouvementSortie = MouvementStock.builder()
            .produit(produit)
            .typeMouvement(TypeMouvement.SORTIE)
            .quantite(quantite)
            .origine(origine)
            .destination(destination)
            .observation("Transfert de " + origine + " vers " + destination)
            .dateMouvement(LocalDateTime.now())
            .build();
        
        mouvementStockRepository.save(mouvementSortie);
        
        // Mouvement d'entrée
        MouvementStock mouvementEntree = MouvementStock.builder()
            .produit(produit)
            .typeMouvement(TypeMouvement.ENTREE)
            .quantite(quantite)
            .origine(origine)
            .destination(destination)
            .observation("Réception transfert de " + origine + " vers " + destination)
            .dateMouvement(LocalDateTime.now())
            .build();
        
        mouvementStockRepository.save(mouvementEntree);
    }
}
