package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.VentePromotionnelleDTO;
import com.longrich.smartgestion.dto.ProduitPromotionnelDTO;
import com.longrich.smartgestion.entity.VentePromotionnelle;
import com.longrich.smartgestion.entity.ProduitPromotionnel;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.repository.VentePromotionnelleRepository;
import com.longrich.smartgestion.repository.ProduitRepository;
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
    private final ProduitRepository produitRepository;
    
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

    public VentePromotionnelle createPromotion(VentePromotionnelleDTO dto, java.util.List<ProduitPromotionnelDTO> lignes) {
        VentePromotionnelle vp = VentePromotionnelle.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .active(Boolean.TRUE.equals(dto.getActive()))
                .build();

        java.util.Set<ProduitPromotionnel> set = new java.util.HashSet<>();
        for (ProduitPromotionnelDTO l : lignes) {
            Produit produit = produitRepository.findById(l.getProduitId())
                    .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé: " + l.getProduitId()));
            Produit bonus = produitRepository.findById(l.getProduitBonusId())
                    .orElseThrow(() -> new IllegalArgumentException("Produit bonus non trouvé: " + l.getProduitBonusId()));
            ProduitPromotionnel pp = ProduitPromotionnel.builder()
                    .ventePromotionnelle(vp)
                    .produit(produit)
                    .produitBonus(bonus)
                    .quantiteMinimum(l.getQuantiteMinimum())
                    .quantiteBonus(l.getQuantiteBonus())
                    .description(l.getDescription())
                    .build();
            set.add(pp);
        }
        vp.setProduitsPromotionnels(set);
        return ventePromotionnelleRepository.save(vp);
    }
}
