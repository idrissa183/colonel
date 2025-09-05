package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.FamilleProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilleProduitRepository extends JpaRepository<FamilleProduit, Long> {

    Optional<FamilleProduit> findByLibelleFamille(String libelleFamille);

    boolean existsByLibelleFamille(String libelleFamille);
    boolean existsByLibelleFamilleAndIdNot(String libelleFamille, Long id);

    List<FamilleProduit> findByActiveTrue();
}