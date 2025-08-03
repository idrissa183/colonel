package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.FamilleProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilleProduitRepository extends JpaRepository<FamilleProduit, Long> {
    
    Optional<FamilleProduit> findByLibelle(String libelle);
    Optional<FamilleProduit> findByCodeFamille(String codeFamille);
    
    boolean existsByLibelle(String libelle);
    boolean existsByCodeFamille(String codeFamille);
    
    List<FamilleProduit> findByActiveTrue();
}