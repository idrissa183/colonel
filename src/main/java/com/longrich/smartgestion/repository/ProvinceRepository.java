package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    
    Optional<Province> findByNom(String nom);
    
    List<Province> findByRegion(String region);
    
    @Query("SELECT p FROM Province p ORDER BY p.nom ASC")
    List<Province> findAllOrderByNom();
    
    @Query("SELECT DISTINCT p.region FROM Province p ORDER BY p.region ASC")
    List<String> findDistinctRegions();
}