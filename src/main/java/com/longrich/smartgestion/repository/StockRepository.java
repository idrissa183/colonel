package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Stock;
import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // Recherche par produit
    Optional<Stock> findByProduit(Produit produit);
    
    // Recherche par produit et type de stock
    Optional<Stock> findByProduitAndTypeStock(Produit produit, String typeStock);

    // Recherche par produit ID
    @Query("SELECT s FROM Stock s WHERE s.produit.id = :produitId")
    Optional<Stock> findByProduitId(@Param("produitId") Long produitId);

    // Stocks avec produits
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p ORDER BY p.libelle")
    List<Stock> findAllWithProduit();

    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p ORDER BY p.libelle")
    Page<Stock> findAllWithProduit(Pageable pageable);

    // Stocks en rupture
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE s.quantite <= 0")
    List<Stock> findProduitsEnRupture();

    // Stocks faibles
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE s.quantite > 0 AND s.quantite <= p.stockMinimum")
    List<Stock> findProduitsStockFaible();

    // Produits avec stock normal
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE s.quantite > p.stockMinimum")
    List<Stock> findProduitsStockNormal();

    // Recherche par type de stock
    List<Stock> findByTypeStock(String typeStock);

    // Recherche par emplacement
    List<Stock> findByEmplacementContainingIgnoreCase(String emplacement);

    // Statistiques
    @Query("SELECT COUNT(s) FROM Stock s")
    Long countAllStocks();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite > 0")
    Long countProduitsEnStock();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite <= 0")
    Long countProduitsRupture();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite > 0 AND s.quantite <= s.produit.stockMinimum")
    Long countProduitsStockFaible();

    // Valeur totale du stock
    @Query("SELECT COALESCE(SUM(s.quantite * p.prixRevente), 0) FROM Stock s JOIN s.produit p WHERE s.quantite > 0")
    BigDecimal calculateTotalStockValue();

    @Query("SELECT COALESCE(AVG(s.quantite * p.prixRevente), 0) FROM Stock s JOIN s.produit p WHERE s.quantite > 0")
    BigDecimal calculateAverageStockValue();

    // Recherche avec filtres
    @Query("SELECT s FROM Stock s JOIN s.produit p WHERE " +
           "(:recherche IS NULL OR LOWER(p.libelle) LIKE LOWER(CONCAT('%', :recherche, '%'))) AND " +
           "(:typeStock IS NULL OR s.typeStock = :typeStock) AND " +
           "(:seulementEnStock IS NULL OR (:seulementEnStock = true AND s.quantite > 0)) " +
           "ORDER BY p.libelle")
    Page<Stock> findWithFilters(@Param("recherche") String recherche,
                                @Param("typeStock") String typeStock,
                                @Param("seulementEnStock") Boolean seulementEnStock,
                                Pageable pageable);

    // Top produits par valeur de stock
    @Query("SELECT s.produit, s.quantite, (s.quantite * p.prixRevente) as valeur " +
           "FROM Stock s JOIN s.produit p WHERE s.quantite > 0 " +
           "ORDER BY valeur DESC")
    List<Object[]> findTopProduitsByStockValue();

    // Produits nécessitant un réapprovisionnement
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE " +
           "(s.quantite <= 0) OR (s.quantite <= p.stockMinimum AND p.stockMinimum > 0) " +
           "ORDER BY s.quantite ASC")
    List<Stock> findProduitsNecessitantReapprovisionnement();

    // Stocks par salle de vente
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE s.salleVente.id = :salleVenteId")
    List<Stock> findBySalleVenteId(@Param("salleVenteId") Long salleVenteId);

    // Quantité totale en stock
    @Query("SELECT COALESCE(SUM(s.quantite), 0) FROM Stock s WHERE s.quantite > 0")
    Long getTotalQuantiteEnStock();

    // Quantité réservée totale
    @Query("SELECT COALESCE(SUM(s.quantiteReservee), 0) FROM Stock s WHERE s.quantiteReservee > 0")
    Long getTotalQuantiteReservee();

    // Vérification de disponibilité
    @Query("SELECT CASE WHEN s.quantite - s.quantiteReservee >= :quantiteRequise THEN true ELSE false END " +
           "FROM Stock s WHERE s.produit.id = :produitId")
    Boolean isQuantiteDisponible(@Param("produitId") Long produitId, @Param("quantiteRequise") Integer quantiteRequise);
    
    // Filtrage avec famille de produit, magasin et dates
    @Query("SELECT s FROM Stock s JOIN s.produit p LEFT JOIN p.familleProduit f WHERE " +
           "(:familleProduitId IS NULL OR f.id = :familleProduitId) AND " +
           "(:magasin IS NULL OR s.typeStock = :magasin) AND " +
           "(:dateDebut IS NULL OR s.dateEntree >= :dateDebut) AND " +
           "(:dateFin IS NULL OR s.dateEntree <= :dateFin)")
    List<Stock> findStocksWithFilters(@Param("familleProduitId") Long familleProduitId,
                                     @Param("magasin") String magasin,
                                     @Param("dateDebut") java.time.LocalDate dateDebut,
                                     @Param("dateFin") java.time.LocalDate dateFin);

    // Entrées historisées par typeStock et période (rapport par emplacement)
    @Query("SELECT s FROM Stock s JOIN s.produit p WHERE " +
           "(:typeStock IS NULL OR s.typeStock = :typeStock) AND " +
           "(:dateDebut IS NULL OR s.dateEntree >= :dateDebut) AND " +
           "(:dateFin IS NULL OR s.dateEntree <= :dateFin) " +
           "ORDER BY s.dateEntree DESC")
    List<Stock> findHistorisedEntries(@Param("typeStock") String typeStock,
                                      @Param("dateDebut") java.time.LocalDate dateDebut,
                                      @Param("dateFin") java.time.LocalDate dateFin);
}
