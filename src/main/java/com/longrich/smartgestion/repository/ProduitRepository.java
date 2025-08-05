package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
// import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

       // Méthodes de recherche par code barre supprimées - utilisation de l'ID

       List<Produit> findByActiveTrue();

       List<Produit> findByFamilleProduitId(Long familleProduitId);

       @Query("SELECT p FROM Produit p WHERE p.active = true AND " +
                     "LOWER(p.libelle) LIKE LOWER(CONCAT('%', :search, '%'))")
       List<Produit> searchActiveProduits(@Param("search") String search);

       @Query("SELECT p FROM Produit p JOIN p.stocks s WHERE " +
                     "s.quantite - s.quantiteReservee <= p.stockMinimum AND p.active = true")
       List<Produit> findProduitsStockFaible();

       @Query("SELECT p FROM Produit p JOIN p.stocks s WHERE " +
                     "s.quantite - s.quantiteReservee > 0 AND p.active = true")
       List<Produit> findProduitsEnStock();
}