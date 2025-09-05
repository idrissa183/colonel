package com.longrich.smartgestion.service;

import com.longrich.smartgestion.entity.*;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeMouvement;
import com.longrich.smartgestion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VenteService {
    
    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitPromotionnelRepository produitPromotionnelRepository;
    private final BonusAttribueRepository bonusAttribueRepository;
    private final ClientRepository clientRepository;
    
    private static final TypeEmplacement TYPE_SURFACE_VENTE = TypeEmplacement.SURFACE_VENTE;
    
    // === VENTES AVEC GESTION DES PROMOTIONS ===
    
    @Transactional
    public void effectuerVente(Long clientId, Long produitId, Integer quantite) {
        log.info("Effectuer vente - Client: {}, Produit: {}, Quantité: {}", clientId, produitId, quantite);
        
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        // Vérifier stock surface de vente uniquement
        Stock stockSurfaceVente = stockRepository.findByProduitAndTypeStock(produit, TYPE_SURFACE_VENTE)
            .orElseThrow(() -> new RuntimeException("Produit non disponible en surface de vente"));
        
        if (stockSurfaceVente.getQuantiteDisponible() < quantite) {
            throw new RuntimeException("Stock insuffisant en surface de vente. Disponible: " + 
                stockSurfaceVente.getQuantiteDisponible() + ", Demandé: " + quantite);
        }
        
        // Diminuer le stock surface de vente
        stockSurfaceVente.setQuantite(stockSurfaceVente.getQuantite() - quantite);
        stockRepository.save(stockSurfaceVente);
        
        // Créer mouvement de sortie
        creerMouvementVente(produit, quantite);
        
        // Vérifier et attribuer des bonus promotionnels
        verifierEtAttribuerBonus(client, produit, quantite);
        
        log.info("Vente effectuée avec succès");
    }
    
    @Transactional
    public void effectuerVenteMultiple(Long clientId, List<LigneVente> lignesVente) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        for (LigneVente ligne : lignesVente) {
            effectuerVente(clientId, ligne.getProduitId(), ligne.getQuantite());
        }
        
        log.info("Vente multiple effectuée - {} articles pour le client {}", lignesVente.size(), client.getNomComplet());
    }
    
    // === GESTION DES BONUS PROMOTIONNELS ===
    
    @Transactional
    public void verifierEtAttribuerBonus(Client client, Produit produit, Integer quantiteAchetee) {
        List<ProduitPromotionnel> promotionsActives = produitPromotionnelRepository
            .findActivePromotionsForProduct(produit.getId());
        
        for (ProduitPromotionnel promotion : promotionsActives) {
            if (quantiteAchetee >= promotion.getQuantiteMinimum()) {
                // Calculer le nombre de bonus à attribuer
                Integer nombreBonus = quantiteAchetee / promotion.getQuantiteMinimum();
                Integer quantiteBonusTotal = nombreBonus * promotion.getQuantiteBonus();
                
                // Créer l'entrée de bonus attribué
                BonusAttribue bonus = BonusAttribue.builder()
                    .produitPromotionnel(promotion)
                    .client(client)
                    .quantiteBonus(quantiteBonusTotal)
                    .dateAttribution(LocalDateTime.now())
                    .distribue(false)
                    .observation("Bonus automatique - Achat de " + quantiteAchetee + " " + produit.getLibelle())
                    .build();
                
                bonusAttribueRepository.save(bonus);
                
                // Créer mouvement de sortie pour les bonus (impact sur le stock)
                creerMouvementBonusSortie(promotion.getProduitBonus(), quantiteBonusTotal);
                
                log.info("Bonus attribué - Client: {}, Produit bonus: {}, Quantité: {}", 
                    client.getNomComplet(), promotion.getProduitBonus().getLibelle(), quantiteBonusTotal);
            }
        }
    }
    
    @Transactional
    public void distribuerBonus(Long bonusId) {
        BonusAttribue bonus = bonusAttribueRepository.findById(bonusId)
            .orElseThrow(() -> new RuntimeException("Bonus non trouvé"));
        
        if (bonus.getDistribue()) {
            throw new RuntimeException("Ce bonus a déjà été distribué");
        }
        
        // Vérifier stock surface de vente pour le produit bonus
        Stock stockBonus = stockRepository.findByProduitAndTypeStock(
            bonus.getProduitPromotionnel().getProduitBonus(), TYPE_SURFACE_VENTE)
            .orElseThrow(() -> new RuntimeException("Produit bonus non disponible en surface de vente"));
        
        if (stockBonus.getQuantiteDisponible() < bonus.getQuantiteBonus()) {
            throw new RuntimeException("Stock insuffisant pour distribuer le bonus");
        }
        
        // Diminuer le stock
        stockBonus.setQuantite(stockBonus.getQuantite() - bonus.getQuantiteBonus());
        stockRepository.save(stockBonus);
        
        // Marquer comme distribué
        bonus.setDistribue(true);
        bonus.setDateDistribution(LocalDateTime.now());
        bonusAttribueRepository.save(bonus);
        
        log.info("Bonus distribué - ID: {}, Produit: {}, Quantité: {}", 
            bonusId, bonus.getProduitPromotionnel().getProduitBonus().getLibelle(), bonus.getQuantiteBonus());
    }
    
    // === CONSULTATION PROMOTIONS ACTIVES ===
    
    @Transactional(readOnly = true)
    public List<ProduitPromotionnel> getPromotionsActives() {
        return produitPromotionnelRepository.findAllActivePromotionalProducts();
    }
    
    @Transactional(readOnly = true)
    public List<BonusAttribue> getBonusNonDistribues() {
        return bonusAttribueRepository.findByDistribueFalse();
    }
    
    @Transactional(readOnly = true)
    public boolean verifierDisponibiliteSurfaceVente(Long produitId, Integer quantite) {
        return stockRepository.findByProduitId(produitId)
            .filter(stock -> stock.getTypeStock() == TYPE_SURFACE_VENTE)
            .map(stock -> stock.getQuantiteDisponible() >= quantite)
            .orElse(false);
    }
    
    // === MÉTHODES PRIVÉES ===
    
    private void creerMouvementVente(Produit produit, Integer quantite) {
        MouvementStock mouvement = MouvementStock.builder()
            .produit(produit)
            .typeMouvement(TypeMouvement.SORTIE)
            .quantite(quantite)
            .origine(TYPE_SURFACE_VENTE.getDisplayName())
            .observation("Vente - Surface de vente")
            .dateMouvement(LocalDateTime.now())
            .build();
        
        mouvementStockRepository.save(mouvement);
    }
    
    private void creerMouvementBonusSortie(Produit produitBonus, Integer quantite) {
        MouvementStock mouvement = MouvementStock.builder()
            .produit(produitBonus)
            .typeMouvement(TypeMouvement.SORTIE)
            .quantite(quantite)
            .origine(TYPE_SURFACE_VENTE.getDisplayName())
            .observation("Sortie bonus - Vente promotionnelle")
            .dateMouvement(LocalDateTime.now())
            .build();
        
        mouvementStockRepository.save(mouvement);
    }
    
    // === CLASSE INTERNE POUR LIGNE DE VENTE ===
    
    public static class LigneVente {
        private Long produitId;
        private Integer quantite;
        
        public LigneVente(Long produitId, Integer quantite) {
            this.produitId = produitId;
            this.quantite = quantite;
        }
        
        public Long getProduitId() { return produitId; }
        public Integer getQuantite() { return quantite; }
        public void setProduitId(Long produitId) { this.produitId = produitId; }
        public void setQuantite(Integer quantite) { this.quantite = quantite; }
    }
}
