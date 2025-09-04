package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.VentePromotionnelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentePromotionnelleRepository extends JpaRepository<VentePromotionnelle, Long> {
    
    List<VentePromotionnelle> findByActiveTrue();
    
    @Query("SELECT vp FROM VentePromotionnelle vp WHERE vp.active = true AND vp.dateDebut <= :date AND vp.dateFin >= :date")
    List<VentePromotionnelle> findActivePromotionsAtDate(@Param("date") LocalDate date);
    
    @Query("SELECT vp FROM VentePromotionnelle vp WHERE vp.dateDebut BETWEEN :startDate AND :endDate")
    List<VentePromotionnelle> findByDateDebutBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}