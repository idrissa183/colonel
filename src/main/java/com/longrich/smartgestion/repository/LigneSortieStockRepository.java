package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.LigneSortieStock;
import com.longrich.smartgestion.entity.SortieStock;
import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LigneSortieStockRepository extends JpaRepository<LigneSortieStock, Long> {
    
    List<LigneSortieStock> findBySortieStock(SortieStock sortieStock);
    
    List<LigneSortieStock> findByProduit(Produit produit);
    
    @Query("SELECT l FROM LigneSortieStock l WHERE l.sortieStock.dateSortie BETWEEN :debut AND :fin")
    List<LigneSortieStock> findByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT l FROM LigneSortieStock l WHERE l.emplacementOrigine = :emplacement")
    List<LigneSortieStock> findByEmplacementOrigine(@Param("emplacement") String emplacement);
    
    @Query("SELECT SUM(l.quantite) FROM LigneSortieStock l WHERE l.produit.id = :produitId AND l.emplacementOrigine = :emplacement")
    Integer countQuantiteSortieByProduitAndEmplacement(@Param("produitId") Long produitId, @Param("emplacement") String emplacement);
}