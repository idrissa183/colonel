package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.SalleVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalleVenteRepository extends JpaRepository<SalleVente, Long> {

    Optional<SalleVente> findByCodeSalle(String codeSalle);

    boolean existsByCodeSalle(String codeSalle);

    List<SalleVente> findByActiveTrue();

    List<SalleVente> findByResponsableId(Long responsableId);
}