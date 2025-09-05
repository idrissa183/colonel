package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.EntreeStock;
import com.longrich.smartgestion.entity.LigneEntreeStock;
import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneEntreeStockRepository extends JpaRepository<LigneEntreeStock, Long> {

    List<LigneEntreeStock> findByEntreeStock(EntreeStock entreeStock);

    List<LigneEntreeStock> findByProduit(Produit produit);

    @Query("SELECT l FROM LigneEntreeStock l WHERE l.entreeStock.id = :entreeStockId")
    List<LigneEntreeStock> findByEntreeStockId(@Param("entreeStockId") Long entreeStockId);

    @Query("SELECT l FROM LigneEntreeStock l WHERE l.produit.id = :produitId ORDER BY l.entreeStock.dateEntree DESC")
    List<LigneEntreeStock> findByProduitId(@Param("produitId") Long produitId);

    @Query("SELECT l FROM LigneEntreeStock l WHERE l.quantite > l.quantiteRecue")
    List<LigneEntreeStock> findLignesNonCompletes();

    @Query("SELECT l FROM LigneEntreeStock l WHERE l.entreeStock.id = :entreeStockId AND l.quantite > l.quantiteRecue")
    List<LigneEntreeStock> findLignesNonCompletesByEntreeStock(@Param("entreeStockId") Long entreeStockId);

    @Query("SELECT SUM(l.quantiteRecue) FROM LigneEntreeStock l WHERE l.produit.id = :produitId")
    Integer getTotalQuantiteRecueByProduit(@Param("produitId") Long produitId);

    @Query("SELECT l FROM LigneEntreeStock l WHERE l.numeroLot = :numeroLot")
    List<LigneEntreeStock> findByNumeroLot(@Param("numeroLot") String numeroLot);
}