package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.User;
import com.longrich.smartgestion.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByActiveTrue();
    
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchActiveUsers(@Param("search") String search);
}