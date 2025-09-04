package com.longrich.smartgestion.service;

import com.longrich.smartgestion.entity.ProduitPromotionnel;
import com.longrich.smartgestion.repository.ProduitPromotionnelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitPromotionnelService {
    
    private final ProduitPromotionnelRepository produitPromotionnelRepository;
    
    public List<ProduitPromotionnel> findByVentePromotionnelleId(Long ventePromotionnelleId) {
        return produitPromotionnelRepository.findByVentePromotionnelleId(ventePromotionnelleId);
    }
    
    public List<ProduitPromotionnel> findActivePromotionsForProduct(Long produitId) {
        return produitPromotionnelRepository.findActivePromotionsForProduct(produitId);
    }
    
    public List<ProduitPromotionnel> findAllActivePromotionalProducts() {
        return produitPromotionnelRepository.findAllActivePromotionalProducts();
    }
    
    public ProduitPromotionnel save(ProduitPromotionnel produitPromotionnel) {
        return produitPromotionnelRepository.save(produitPromotionnel);
    }
    
    public Optional<ProduitPromotionnel> findById(Long id) {
        return produitPromotionnelRepository.findById(id);
    }
    
    public void deleteById(Long id) {
        produitPromotionnelRepository.deleteById(id);
    }
    
    public List<ProduitPromotionnel> findAll() {
        return produitPromotionnelRepository.findAll();
    }
}