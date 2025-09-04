package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.VentePromotionnelleDTO;
import com.longrich.smartgestion.entity.VentePromotionnelle;
import com.longrich.smartgestion.repository.VentePromotionnelleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VentePromotionnelleService {
    
    private final VentePromotionnelleRepository ventePromotionnelleRepository;
    
    public List<VentePromotionnelle> getAllActivePromotions() {
        return ventePromotionnelleRepository.findByActiveTrue();
    }
    
    public List<VentePromotionnelle> getActivePromotionsAtDate(LocalDate date) {
        return ventePromotionnelleRepository.findActivePromotionsAtDate(date);
    }
    
    public List<VentePromotionnelle> getCurrentActivePromotions() {
        return getActivePromotionsAtDate(LocalDate.now());
    }
    
    public Optional<VentePromotionnelle> findById(Long id) {
        return ventePromotionnelleRepository.findById(id);
    }
    
    public VentePromotionnelle save(VentePromotionnelle ventePromotionnelle) {
        return ventePromotionnelleRepository.save(ventePromotionnelle);
    }
    
    public void deleteById(Long id) {
        ventePromotionnelleRepository.deleteById(id);
    }
    
    public List<VentePromotionnelle> findAll() {
        return ventePromotionnelleRepository.findAll();
    }
    
    public List<VentePromotionnelle> findByPeriod(LocalDate startDate, LocalDate endDate) {
        return ventePromotionnelleRepository.findByDateDebutBetween(startDate, endDate);
    }
}