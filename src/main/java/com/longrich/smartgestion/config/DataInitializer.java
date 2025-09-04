package com.longrich.smartgestion.config;

import com.longrich.smartgestion.entity.*;
import com.longrich.smartgestion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final VentePromotionnelleRepository ventePromotionnelleRepository;
    private final ProduitPromotionnelRepository produitPromotionnelRepository;
    private final ProduitRepository produitRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializePromotionalData();
    }

    private void initializePromotionalData() {
        try {
            // Créer une promotion hebdomadaire si elle n'existe pas déjà
            Optional<VentePromotionnelle> existingPromo = ventePromotionnelleRepository
                .findActivePromotionsAtDate(LocalDate.now())
                .stream()
                .findFirst();

            if (existingPromo.isEmpty()) {
                log.info("Initialisation des données promotionnelles...");
                
                // Créer une promotion de la semaine
                VentePromotionnelle promoSemaine = VentePromotionnelle.builder()
                    .nom("Promotion de la Semaine")
                    .description("Promotion hebdomadaire Longrich")
                    .dateDebut(LocalDate.now().with(java.time.DayOfWeek.MONDAY))
                    .dateFin(LocalDate.now().with(java.time.DayOfWeek.SUNDAY))
                    .active(true)
                    .build();

                promoSemaine = ventePromotionnelleRepository.save(promoSemaine);
                
                // Ajouter des produits promotionnels si des produits existent
                var produits = produitRepository.findByActiveTrue();
                if (produits.size() >= 2) {
                    Produit produitPrincipal = produits.get(0);
                    Produit produitBonus = produits.get(1);
                    
                    ProduitPromotionnel produitPromo = ProduitPromotionnel.builder()
                        .ventePromotionnelle(promoSemaine)
                        .produit(produitPrincipal)
                        .produitBonus(produitBonus)
                        .quantiteMinimum(5)
                        .quantiteBonus(1)
                        .description("Achetez 5 " + produitPrincipal.getLibelle() + 
                                   ", recevez 1 " + produitBonus.getLibelle() + " gratuit")
                        .build();
                    
                    produitPromotionnelRepository.save(produitPromo);
                    log.info("Promotion créée: {} -> {}", produitPrincipal.getLibelle(), produitBonus.getLibelle());
                }
            }
        } catch (Exception e) {
            log.warn("Erreur lors de l'initialisation des données promotionnelles: {}", e.getMessage());
        }
    }
}