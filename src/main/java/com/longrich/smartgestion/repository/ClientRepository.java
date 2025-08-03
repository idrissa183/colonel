package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Client;
import com.longrich.smartgestion.enums.TypeClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Client> findByActiveTrue();
    
    List<Client> findByTypeClient(TypeClient typeClient);
    
    List<Client> findByProvince(String province);
    
    @Query("SELECT c FROM Client c WHERE c.active = true AND " +
           "(LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Client> searchActiveClients(@Param("search") String search);
    
    @Query("SELECT DISTINCT c.province FROM Client c WHERE c.province IS NOT NULL ORDER BY c.province")
    List<String> findAllProvinces();
}