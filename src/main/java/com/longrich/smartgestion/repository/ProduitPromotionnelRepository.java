package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.ProduitPromotionnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitPromotionnelRepository extends JpaRepository<ProduitPromotionnel, Long> {
    
    List<ProduitPromotionnel> findByVentePromotionnelleId(Long ventePromotionnelleId);
    
    @Query("SELECT pp FROM ProduitPromotionnel pp JOIN pp.ventePromotionnelle vp WHERE pp.produit.id = :produitId AND vp.active = true AND CURRENT_DATE BETWEEN vp.dateDebut AND vp.dateFin")
    List<ProduitPromotionnel> findActivePromotionsForProduct(@Param("produitId") Long produitId);
    
    @Query("SELECT pp FROM ProduitPromotionnel pp JOIN pp.ventePromotionnelle vp WHERE vp.active = true AND CURRENT_DATE BETWEEN vp.dateDebut AND vp.dateFin")
    List<ProduitPromotionnel> findAllActivePromotionalProducts();
}