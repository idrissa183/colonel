package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Fournisseur;
import com.longrich.smartgestion.enums.TypeStockiste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    Optional<Fournisseur> findByCodeStockiste(String codeStockiste);

    boolean existsByCodeStockiste(String codeStockiste);

    List<Fournisseur> findByActiveTrue();

    List<Fournisseur> findByTypeStockiste(TypeStockiste typeStockiste);

    @Query("SELECT f FROM Fournisseur f WHERE f.active = true AND " +
            "(LOWER(f.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.codeStockiste) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Fournisseur> searchActiveFournisseurs(@Param("search") String search);
}