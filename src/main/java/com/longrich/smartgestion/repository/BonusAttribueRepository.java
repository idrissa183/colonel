package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.BonusAttribue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BonusAttribueRepository extends JpaRepository<BonusAttribue, Long> {
    
    List<BonusAttribue> findByClientId(Long clientId);
    
    List<BonusAttribue> findByDistribueFalse();
    
    @Query("SELECT ba FROM BonusAttribue ba WHERE ba.produitPromotionnel.produitBonus.id = :produitId AND ba.dateAttribution BETWEEN :startDate AND :endDate")
    List<BonusAttribue> findBonusSortiesByProduitAndPeriod(@Param("produitId") Long produitId, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(ba.quantiteBonus) FROM BonusAttribue ba WHERE ba.produitPromotionnel.produitBonus.id = :produitId AND ba.dateAttribution BETWEEN :startDate AND :endDate")
    Integer getTotalBonusQuantityByProduitAndPeriod(@Param("produitId") Long produitId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ba FROM BonusAttribue ba WHERE DATE(ba.dateAttribution) = :date")
    List<BonusAttribue> findByDateAttribution(@Param("date") LocalDate date);
}