package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.EntreeStock;
import com.longrich.smartgestion.entity.Fournisseur;
import com.longrich.smartgestion.enums.StatutEntreeStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntreeStockRepository extends JpaRepository<EntreeStock, Long> {

    Optional<EntreeStock> findByNumeroEntree(String numeroEntree);

    List<EntreeStock> findByStatut(StatutEntreeStock statut);

    List<EntreeStock> findByFournisseur(Fournisseur fournisseur);

    Page<EntreeStock> findByStatutOrderByDateEntreeDesc(StatutEntreeStock statut, Pageable pageable);

    @Query("SELECT e FROM EntreeStock e WHERE e.dateEntree BETWEEN :startDate AND :endDate ORDER BY e.dateEntree DESC")
    List<EntreeStock> findByDateEntreBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM EntreeStock e WHERE e.fournisseur.id = :fournisseurId AND e.statut = :statut")
    List<EntreeStock> findByFournisseurAndStatut(@Param("fournisseurId") Long fournisseurId, 
                                                @Param("statut") StatutEntreeStock statut);

    @Query("SELECT e FROM EntreeStock e JOIN e.lignesEntree l WHERE l.produit.id = :produitId ORDER BY e.dateEntree DESC")
    List<EntreeStock> findByProduit(@Param("produitId") Long produitId);

    @Query("SELECT e FROM EntreeStock e WHERE e.statut IN :statuts ORDER BY e.dateEntree DESC")
    List<EntreeStock> findByStatutIn(@Param("statuts") List<StatutEntreeStock> statuts);

    @Query("SELECT COUNT(e) FROM EntreeStock e WHERE e.statut = :statut")
    Long countByStatut(@Param("statut") StatutEntreeStock statut);

    @Query("SELECT e FROM EntreeStock e WHERE e.numeroFactureFournisseur = :numeroFacture")
    List<EntreeStock> findByNumeroFactureFournisseur(@Param("numeroFacture") String numeroFacture);

    @Query("SELECT e FROM EntreeStock e WHERE e.numeroBonLivraison = :numeroBonLivraison")
    List<EntreeStock> findByNumeroBonLivraison(@Param("numeroBonLivraison") String numeroBonLivraison);

    List<EntreeStock> findTop10ByOrderByDateEntreeDesc();
}