package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.MouvementStock;
import com.longrich.smartgestion.enums.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    List<MouvementStock> findByProduitId(Long produitId);

    List<MouvementStock> findByTypeMouvement(TypeMouvement typeMouvement);

    List<MouvementStock> findByUtilisateurId(Long utilisateurId);

    @Query("SELECT m FROM MouvementStock m WHERE m.dateMouvement BETWEEN :debut AND :fin")
    List<MouvementStock> findByDateMouvementBetween(@Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT m FROM MouvementStock m WHERE m.produit.id = :produitId AND m.dateMouvement BETWEEN :debut AND :fin")
    List<MouvementStock> findByProduitAndDateBetween(@Param("produitId") Long produitId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    // Comptage des mouvements du jour
    @Query("SELECT COUNT(m) FROM MouvementStock m WHERE DATE(m.dateMouvement) = CURRENT_DATE")
    Long countByDateMouvementToday();

    // Comptage par type et date du jour
    @Query("SELECT COUNT(m) FROM MouvementStock m WHERE m.typeMouvement = :type AND DATE(m.dateMouvement) = CURRENT_DATE")
    Long countByTypeMouvementAndDateToday(@Param("type") TypeMouvement type);
}