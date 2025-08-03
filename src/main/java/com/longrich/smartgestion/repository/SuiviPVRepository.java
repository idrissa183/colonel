package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.SuiviPV;
import com.longrich.smartgestion.enums.TypeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuiviPVRepository extends JpaRepository<SuiviPV, Long> {

    List<SuiviPV> findByClientId(Long clientId);

    List<SuiviPV> findByProduitId(Long produitId);

    List<SuiviPV> findByTypeVente(TypeVente typeVente);

    @Query("SELECT SUM(s.pvGagne) FROM SuiviPV s WHERE s.client.id = :clientId")
    BigDecimal getTotalPVByClient(@Param("clientId") Long clientId);

    @Query("SELECT SUM(s.pvGagne) FROM SuiviPV s WHERE s.client.id = :clientId AND s.dateVente BETWEEN :debut AND :fin")
    BigDecimal getTotalPVByClientAndPeriod(@Param("clientId") Long clientId,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin);

    @Query("SELECT s FROM SuiviPV s WHERE s.dateVente BETWEEN :debut AND :fin")
    List<SuiviPV> findByDateVenteBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT s FROM SuiviPV s WHERE s.client.id = :clientId AND s.dateVente BETWEEN :debut AND :fin")
    List<SuiviPV> findByClientAndDateBetween(@Param("clientId") Long clientId,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin);
}