package com.longrich.smartgestion.service;

import com.longrich.smartgestion.entity.BonusAttribue;
import com.longrich.smartgestion.entity.ProduitPromotionnel;
import com.longrich.smartgestion.repository.BonusAttribueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BonusAttribueService {
    
    private final BonusAttribueRepository bonusAttribueRepository;
    
    public List<BonusAttribue> findByClientId(Long clientId) {
        return bonusAttribueRepository.findByClientId(clientId);
    }
    
    public List<BonusAttribue> findNonDistribues() {
        return bonusAttribueRepository.findByDistribueFalse();
    }
    
    public List<BonusAttribue> getBonusSortiesByProduitAndPeriod(Long produitId, LocalDateTime startDate, LocalDateTime endDate) {
        return bonusAttribueRepository.findBonusSortiesByProduitAndPeriod(produitId, startDate, endDate);
    }
    
    public Integer getTotalBonusQuantityByProduitAndPeriod(Long produitId, LocalDateTime startDate, LocalDateTime endDate) {
        Integer total = bonusAttribueRepository.getTotalBonusQuantityByProduitAndPeriod(produitId, startDate, endDate);
        return total != null ? total : 0;
    }
    
    public BonusAttribue save(BonusAttribue bonusAttribue) {
        return bonusAttribueRepository.save(bonusAttribue);
    }
    
    public void marquerCommeDistribue(Long id, String observation) {
        Optional<BonusAttribue> optionalBonus = bonusAttribueRepository.findById(id);
        if (optionalBonus.isPresent()) {
            BonusAttribue bonus = optionalBonus.get();
            bonus.setDistribue(true);
            bonus.setDateDistribution(LocalDateTime.now());
            if (observation != null && !observation.trim().isEmpty()) {
                bonus.setObservation(observation);
            }
            bonusAttribueRepository.save(bonus);
        }
    }
    
    public List<BonusAttribue> findByDate(LocalDate date) {
        return bonusAttribueRepository.findByDateAttribution(date);
    }
    
    public Optional<BonusAttribue> findById(Long id) {
        return bonusAttribueRepository.findById(id);
    }
    
    public List<BonusAttribue> findAll() {
        return bonusAttribueRepository.findAll();
    }
    
    public void deleteById(Long id) {
        bonusAttribueRepository.deleteById(id);
    }
}