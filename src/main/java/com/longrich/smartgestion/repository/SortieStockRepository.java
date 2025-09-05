package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.SortieStock;
import com.longrich.smartgestion.enums.TypeSortieStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SortieStockRepository extends JpaRepository<SortieStock, Long> {
    
    List<SortieStock> findByTypeSortie(TypeSortieStock typeSortie);
    
    @Query("SELECT s FROM SortieStock s WHERE s.dateSortie BETWEEN :debut AND :fin ORDER BY s.dateSortie DESC")
    List<SortieStock> findByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT s FROM SortieStock s WHERE s.emplacementOrigine = :emplacement ORDER BY s.dateSortie DESC")
    List<SortieStock> findByEmplacementOrigine(@Param("emplacement") String emplacement);
    
    @Query("SELECT s FROM SortieStock s JOIN s.lignesSortie l WHERE l.produit.id = :produitId ORDER BY s.dateSortie DESC")
    List<SortieStock> findByProduit(@Param("produitId") Long produitId);
    
    @Query("SELECT s FROM SortieStock s WHERE s.facture.id = :factureId")
    List<SortieStock> findByFacture(@Param("factureId") Long factureId);
}