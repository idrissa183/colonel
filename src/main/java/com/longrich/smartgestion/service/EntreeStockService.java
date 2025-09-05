package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.EntreeStockDTO;
import com.longrich.smartgestion.dto.LigneEntreeStockDTO;
import com.longrich.smartgestion.entity.*;
import com.longrich.smartgestion.enums.StatutEntreeStock;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeMouvement;
import com.longrich.smartgestion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntreeStockService {

    private final EntreeStockRepository entreeStockRepository;
    private final LigneEntreeStockRepository ligneEntreeStockRepository;
    private final StockRepository stockRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;

    @Transactional
    public EntreeStock creerEntreeStock(EntreeStockDTO dto) {
        log.info("Création d'une entrée de stock pour le fournisseur: {}", dto.getFournisseurId());

        EntreeStock entreeStock = EntreeStock.builder()
                .dateEntree(dto.getDateEntree() != null ? dto.getDateEntree() : LocalDateTime.now())
                .dateCommande(dto.getDateCommande())
                .dateLivraison(dto.getDateLivraison())
                .numeroFactureFournisseur(dto.getNumeroFactureFournisseur())
                .numeroBonLivraison(dto.getNumeroBonLivraison())
                .statut(StatutEntreeStock.EN_ATTENTE)
                .observation(dto.getObservation())
                .fichierReference(dto.getFichierReference())
                .build();

        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + dto.getFournisseurId()));
            entreeStock.setFournisseur(fournisseur);
        }

        if (dto.getCommandeFournisseurId() != null) {
            CommandeFournisseur commande = commandeFournisseurRepository.findById(dto.getCommandeFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Commande fournisseur non trouvée: " + dto.getCommandeFournisseurId()));
            entreeStock.setCommandeFournisseur(commande);
        }

        EntreeStock savedEntreeStock = entreeStockRepository.save(entreeStock);

        if (dto.getLignesEntree() != null && !dto.getLignesEntree().isEmpty()) {
            for (LigneEntreeStockDTO ligneDto : dto.getLignesEntree()) {
                ajouterLigneEntree(savedEntreeStock.getId(), ligneDto);
            }
        }

        return savedEntreeStock;
    }

    /**
     * Crée une entrée de stock à partir d'une commande fournisseur et valide immédiatement la réception.
     * Contraintes:
     * - commandeFournisseurId obligatoire
     * - Chaque quantité reçue <= quantité restante (commandée - déjà livrée)
     * - Emplacement: MAGASIN (fixe)
     * - Date d'entrée obligatoire (si absente, now)
     */
    @Transactional
    public EntreeStock creerEtValiderDepuisCommande(EntreeStockDTO dto) {
        if (dto.getCommandeFournisseurId() == null) {
            throw new RuntimeException("La commande fournisseur est obligatoire pour enregistrer une entrée");
        }

        CommandeFournisseur commande = commandeFournisseurRepository.findById(dto.getCommandeFournisseurId())
                .orElseThrow(() -> new RuntimeException("Commande fournisseur non trouvée: " + dto.getCommandeFournisseurId()));

        if (dto.getLignesEntree() == null || dto.getLignesEntree().isEmpty()) {
            throw new RuntimeException("Au moins une ligne de réception est requise");
        }

        // Préparer l'entrée liée à la commande (+ fournisseur dérivé)
        EntreeStock entree = EntreeStock.builder()
                .dateEntree(dto.getDateEntree() != null ? dto.getDateEntree() : LocalDateTime.now())
                .dateCommande(commande.getDateCommande().toLocalDate())
                .fournisseur(commande.getFournisseur())
                .commandeFournisseur(commande)
                .numeroFactureFournisseur(dto.getNumeroFactureFournisseur())
                .numeroBonLivraison(dto.getNumeroBonLivraison())
                .statut(StatutEntreeStock.EN_ATTENTE)
                .observation(dto.getObservation())
                .fichierReference(dto.getFichierReference())
                .build();

        EntreeStock saved = entreeStockRepository.save(entree);

        // Indexer les lignes de commande par produit
        List<LigneCommandeFournisseur> lignesCmd = ligneCommandeFournisseurRepository.findByCommandeFournisseur(commande);
        java.util.Map<Long, LigneCommandeFournisseur> lcfByProduit = new java.util.HashMap<>();
        for (LigneCommandeFournisseur l : lignesCmd) {
            lcfByProduit.put(l.getProduit().getId(), l);
        }

        // Créer lignes d'entrée avec quantiteRecue initiale à 0 (on validera ensuite)
        for (LigneEntreeStockDTO lDto : dto.getLignesEntree()) {
            if (lDto.getProduitId() == null) {
                throw new RuntimeException("Produit manquant pour une ligne");
            }
            Produit produit = produitRepository.findById(lDto.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + lDto.getProduitId()));

            LigneCommandeFournisseur lcf = lcfByProduit.get(produit.getId());
            if (lcf == null) {
                throw new RuntimeException("Le produit " + produit.getLibelle() + " n'appartient pas à la commande sélectionnée");
            }

            int dejaLivre = lcf.getQuantiteLivree() != null ? lcf.getQuantiteLivree() : 0;
            int restante = lcf.getQuantiteCommandee() - dejaLivre;
            int qRecue = lDto.getQuantiteRecue() != null ? lDto.getQuantiteRecue() : 0;
            if (qRecue <= 0) {
                throw new RuntimeException("La quantité reçue doit être positive pour " + produit.getLibelle());
            }
            if (qRecue > restante) {
                throw new RuntimeException("La quantité reçue (" + qRecue + ") dépasse la quantité restante (" + restante + ") pour " + produit.getLibelle());
            }

            LigneEntreeStock ligne = LigneEntreeStock.builder()
                    .entreeStock(saved)
                    .produit(produit)
                    .quantite(qRecue)
                    .quantiteRecue(0) // sera mis à jour lors de la validation
                    .prixUnitaire(lcf.getPrixUnitaire())
                    .emplacementMagasin("MAGASIN")
                    .ligneCommandeFournisseur(lcf)
                    .build();
            ligneEntreeStockRepository.save(ligne);
        }

        // Préparer la validation avec les quantités reçues
        List<LigneEntreeStock> savedLignes = ligneEntreeStockRepository.findByEntreeStock(saved);
        List<LigneEntreeStockDTO> toValidate = new java.util.ArrayList<>();
        for (LigneEntreeStock l : savedLignes) {
            // Retrouver la cible
            LigneEntreeStockDTO source = dto.getLignesEntree().stream()
                    .filter(x -> x.getProduitId() != null && x.getProduitId().equals(l.getProduit().getId()))
                    .findFirst()
                    .orElse(null);
            if (source != null) {
                LigneEntreeStockDTO v = new LigneEntreeStockDTO();
                v.setId(l.getId());
                v.setQuantiteRecue(source.getQuantiteRecue());
                v.setDatePeremption(source.getDatePeremption());
                v.setNumeroLot(source.getNumeroLot());
                v.setObservation(source.getObservation());
                toValidate.add(v);
            }
        }

        // Valider la réception (met à jour stock MAGASIN + mouvements + statut commande)
        validerReception(saved.getId(), toValidate);

        // Recharger et retourner
        return entreeStockRepository.findById(saved.getId()).orElse(saved);
    }

    @Transactional
    public LigneEntreeStock ajouterLigneEntree(Long entreeStockId, LigneEntreeStockDTO dto) {
        EntreeStock entreeStock = entreeStockRepository.findById(entreeStockId)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée: " + entreeStockId));

        if (entreeStock.isValidee()) {
            throw new RuntimeException("Impossible de modifier une entrée de stock validée");
        }

        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + dto.getProduitId()));

        LigneEntreeStock ligne = LigneEntreeStock.builder()
                .entreeStock(entreeStock)
                .produit(produit)
                .quantite(dto.getQuantite())
                .quantiteRecue(dto.getQuantiteRecue() != null ? dto.getQuantiteRecue() : 0)
                .prixUnitaire(dto.getPrixUnitaire())
                .datePeremption(dto.getDatePeremption())
                .numeroLot(dto.getNumeroLot())
                .observation(dto.getObservation())
                .emplacementMagasin(dto.getEmplacementMagasin() != null ? dto.getEmplacementMagasin() : "MAGASIN")
                .build();

        // Lier la ligne d'entrée à une ligne de commande si fournie / possible
        if (dto.getLigneCommandeFournisseurId() != null) {
            LigneCommandeFournisseur lcf = ligneCommandeFournisseurRepository.findById(dto.getLigneCommandeFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Ligne de commande fournisseur non trouvée: " + dto.getLigneCommandeFournisseurId()));
            if (!lcf.getProduit().getId().equals(produit.getId())) {
                throw new RuntimeException("Le produit de la ligne d'entrée ne correspond pas à la ligne de commande fournisseur");
            }
            ligne.setLigneCommandeFournisseur(lcf);
        } else if (entreeStock.getCommandeFournisseur() != null) {
            // Si la commande est liée à l'entrée, essayer d'associer automatiquement par produit
            List<LigneCommandeFournisseur> lignesCmd = ligneCommandeFournisseurRepository
                    .findByCommandeFournisseurAndProduit(entreeStock.getCommandeFournisseur(), produit);
            if (!lignesCmd.isEmpty()) {
                ligne.setLigneCommandeFournisseur(lignesCmd.get(0));
            }
        }

        LigneEntreeStock savedLigne = ligneEntreeStockRepository.save(ligne);
        
        // Recalculer le montant total
        recalculerMontantTotal(entreeStock);
        
        return savedLigne;
    }

    @Transactional
    public void validerReception(Long entreeStockId, List<LigneEntreeStockDTO> lignesRecues) {
        log.info("Validation de la réception pour l'entrée: {}", entreeStockId);

        EntreeStock entreeStock = entreeStockRepository.findById(entreeStockId)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée: " + entreeStockId));

        if (entreeStock.isValidee()) {
            throw new RuntimeException("Cette entrée de stock est déjà validée");
        }

        // Mettre à jour les quantités reçues
        for (LigneEntreeStockDTO ligneDto : lignesRecues) {
            LigneEntreeStock ligne = ligneEntreeStockRepository.findById(ligneDto.getId())
                    .orElseThrow(() -> new RuntimeException("Ligne non trouvée: " + ligneDto.getId()));
            // Calculer le delta reçu pour impact sur commande
            int ancienneRecue = ligne.getQuantiteRecue() != null ? ligne.getQuantiteRecue() : 0;
            int nouvelleRecue = ligneDto.getQuantiteRecue() != null ? ligneDto.getQuantiteRecue() : 0;
            int deltaRecue = nouvelleRecue - ancienneRecue;

            if (nouvelleRecue < 0) {
                throw new RuntimeException("La quantité reçue ne peut pas être négative");
            }

            ligne.setQuantiteRecue(nouvelleRecue);
            ligne.setDatePeremption(ligneDto.getDatePeremption());
            ligne.setNumeroLot(ligneDto.getNumeroLot());
            ligne.setObservation(ligneDto.getObservation());
            
            ligneEntreeStockRepository.save(ligne);

            // Créer/Mettre à jour le stock en magasin
            if (nouvelleRecue > 0) {
                mettreAJourStockMagasin(ligne);
                
                // Enregistrer le mouvement de stock
                enregistrerMouvementStock(ligne);
            }

            // Mettre à jour la commande fournisseur liée (si présente)
            if (deltaRecue != 0 && entreeStock.getCommandeFournisseur() != null) {
                LigneCommandeFournisseur lcf = ligne.getLigneCommandeFournisseur();
                if (lcf == null) {
                    // tenter association automatique si non liée
                    List<LigneCommandeFournisseur> candidates = ligneCommandeFournisseurRepository
                            .findByCommandeFournisseurAndProduit(entreeStock.getCommandeFournisseur(), ligne.getProduit());
                    if (!candidates.isEmpty()) {
                        lcf = candidates.get(0);
                        ligne.setLigneCommandeFournisseur(lcf);
                        ligneEntreeStockRepository.save(ligne);
                    }
                }
                if (lcf != null) {
                    int actuel = lcf.getQuantiteLivree() != null ? lcf.getQuantiteLivree() : 0;
                    int cible = Math.max(0, Math.min(lcf.getQuantiteCommandee(), actuel + deltaRecue));
                    lcf.setQuantiteLivree(cible);
                    ligneCommandeFournisseurRepository.save(lcf);
                }
            }
        }

        // Déterminer le nouveau statut
        boolean toutRecu = entreeStock.getLignesEntree().stream()
                .allMatch(ligne -> ligne.getQuantiteRecue().equals(ligne.getQuantite()));
        
        boolean partiellementRecu = entreeStock.getLignesEntree().stream()
                .anyMatch(ligne -> ligne.getQuantiteRecue() > 0);

        if (toutRecu) {
            entreeStock.setStatut(StatutEntreeStock.VALIDEE);
        } else if (partiellementRecu) {
            entreeStock.setStatut(StatutEntreeStock.PARTIELLEMENT_RECUE);
        }

        entreeStockRepository.save(entreeStock);
        log.info("Entrée de stock {} validée avec le statut: {}", entreeStockId, entreeStock.getStatut());

        // Mettre à jour le statut de la commande fournisseur si liée
        if (entreeStock.getCommandeFournisseur() != null) {
            CommandeFournisseur commande = commandeFournisseurRepository
                    .findById(entreeStock.getCommandeFournisseur().getId())
                    .orElseThrow(() -> new RuntimeException("Commande fournisseur non trouvée"));

            commande.setStatut(commande.calculerStatutSelonLivraisons());
            if (commande.getStatut() == com.longrich.smartgestion.enums.StatutCommande.LIVREE
                    && commande.getDateLivraisonReelle() == null) {
                commande.setDateLivraisonReelle(LocalDateTime.now());
            }
            commandeFournisseurRepository.save(commande);
        }
    }

    private void mettreAJourStockMagasin(LigneEntreeStock ligne) {
        // Rechercher un stock existant pour ce produit en magasin
        Optional<Stock> stockExistant = stockRepository.findByProduitAndTypeStock(
                ligne.getProduit(), TypeEmplacement.MAGASIN);

        Stock stock;
        if (stockExistant.isPresent()) {
            stock = stockExistant.get();
            stock.setQuantite(stock.getQuantite() + ligne.getQuantiteRecue());
        } else {
            stock = Stock.builder()
                    .produit(ligne.getProduit())
                    .quantite(ligne.getQuantiteRecue())
                    .quantiteReservee(0)
                    .typeStock(TypeEmplacement.MAGASIN)
                    .emplacement(ligne.getEmplacementMagasin())
                    .dateEntree(ligne.getEntreeStock().getDateEntree().toLocalDate())
                    .build();
        }

        stockRepository.save(stock);
    }

    private void enregistrerMouvementStock(LigneEntreeStock ligne) {
        MouvementStock mouvement = MouvementStock.builder()
                .produit(ligne.getProduit())
                .typeMouvement(TypeMouvement.ENTREE)
                .quantite(ligne.getQuantiteRecue())
                .origine("FOURNISSEUR")
                .destination("MAGASIN")
                .referenceDocument(ligne.getEntreeStock().getNumeroEntree())
                .observation("Réception entrée stock - " + ligne.getEntreeStock().getFournisseur().getNomComplet())
                .dateMouvement(LocalDateTime.now())
                .build();

        mouvementStockRepository.save(mouvement);
    }

    private void recalculerMontantTotal(EntreeStock entreeStock) {
        BigDecimal montantTotal = entreeStock.getLignesEntree().stream()
                .map(ligne -> ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        entreeStock.setMontantTotal(montantTotal);
        entreeStockRepository.save(entreeStock);
    }

    @Transactional(readOnly = true)
    public List<EntreeStock> findAll() {
        return entreeStockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<EntreeStock> findById(Long id) {
        return entreeStockRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<EntreeStock> findByStatut(StatutEntreeStock statut) {
        return entreeStockRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<EntreeStock> findByFournisseur(Long fournisseurId) {
        Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + fournisseurId));
        return entreeStockRepository.findByFournisseur(fournisseur);
    }

    @Transactional
    public void supprimerEntreeStock(Long id) {
        EntreeStock entreeStock = entreeStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée: " + id));

        if (entreeStock.isValidee()) {
            throw new RuntimeException("Impossible de supprimer une entrée de stock validée");
        }

        entreeStockRepository.delete(entreeStock);
        log.info("Entrée de stock {} supprimée", id);
    }
}
