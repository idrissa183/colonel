package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.CommandeFournisseurDTO;
import com.longrich.smartgestion.dto.LigneCommandeFournisseurDTO;
import com.longrich.smartgestion.entity.CommandeFournisseur;
import com.longrich.smartgestion.entity.Fournisseur;
import com.longrich.smartgestion.entity.LigneCommandeFournisseur;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.enums.StatutCommande;
import com.longrich.smartgestion.repository.CommandeFournisseurRepository;
import com.longrich.smartgestion.repository.FournisseurRepository;
import com.longrich.smartgestion.repository.LigneCommandeFournisseurRepository;
import com.longrich.smartgestion.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommandeFournisseurService {

    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;

    @Transactional(readOnly = true)
    public List<CommandeFournisseur> getAllCommandes() {
        return commandeFournisseurRepository.findAllActiveOrderByDateDesc();
    }

    @Transactional(readOnly = true)
    public Page<CommandeFournisseur> getAllCommandes(Pageable pageable) {
        return commandeFournisseurRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<CommandeFournisseur> getCommandeById(Long id) {
        return commandeFournisseurRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<CommandeFournisseur> getCommandeByNumero(String numeroCommande) {
        return commandeFournisseurRepository.findByNumeroCommande(numeroCommande);
    }

    @Transactional(readOnly = true)
    public Optional<CommandeFournisseur> getCommandeWithLignesByNumero(String numeroCommande) {
        return commandeFournisseurRepository.findByNumeroCommandeWithLignes(numeroCommande);
    }

    @Transactional(readOnly = true)
    public List<CommandeFournisseur> getCommandesByFournisseur(Long fournisseurId) {
        return fournisseurRepository.findById(fournisseurId)
                .map(commandeFournisseurRepository::findByFournisseur)
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<CommandeFournisseur> getCommandesByStatut(StatutCommande statut) {
        return commandeFournisseurRepository.findActiveByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<CommandeFournisseur> getCommandesByPeriod(LocalDateTime debut, LocalDateTime fin) {
        return commandeFournisseurRepository.findByDateCommandeBetweenOrderByDateCommandeDesc(debut, fin);
    }

    @Transactional(readOnly = true)
    public List<CommandeFournisseur> searchCommandes(String searchTerm) {
        return commandeFournisseurRepository.searchCommandes(searchTerm);
    }

    @Transactional(readOnly = true)
    public List<CommandeFournisseur> getCommandesEnRetard() {
        return commandeFournisseurRepository.findCommandesEnRetard(LocalDateTime.now());
    }

    public CommandeFournisseur createCommande(CommandeFournisseurDTO commandeDTO) {
        log.info("Création d'une nouvelle commande fournisseur pour le fournisseur ID: {}", commandeDTO.getFournisseurId());

        Fournisseur fournisseur = fournisseurRepository.findById(commandeDTO.getFournisseurId())
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur non trouvé"));

        CommandeFournisseur commande = CommandeFournisseur.builder()
                .fournisseur(fournisseur)
                .dateCommande(commandeDTO.getDateCommande())
                .dateLivraisonPrevue(commandeDTO.getDateLivraisonPrevue())
                .statut(StatutCommande.EN_COURS) // Statut automatique: EN_COURS après création
                .observations(commandeDTO.getObservations())
                .montantTotal(BigDecimal.ZERO)
                .build();

        CommandeFournisseur savedCommande = commandeFournisseurRepository.save(commande);

        // Ajouter les lignes de commande si présentes
        if (commandeDTO.getLignes() != null && !commandeDTO.getLignes().isEmpty()) {
            for (LigneCommandeFournisseurDTO ligneDTO : commandeDTO.getLignes()) {
                addLigneToCommande(savedCommande.getId(), ligneDTO);
            }
            // Recalculer le total après ajout des lignes
            savedCommande = recalculateMontantTotal(savedCommande.getId());
        }

        log.info("Commande fournisseur créée avec succès: {}", savedCommande.getNumeroCommande());
        return savedCommande;
    }

    public CommandeFournisseur updateCommande(Long id, CommandeFournisseurDTO commandeDTO) {
        log.info("Mise à jour de la commande fournisseur ID: {}", id);

        CommandeFournisseur commande = commandeFournisseurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        if (commandeDTO.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(commandeDTO.getFournisseurId())
                    .orElseThrow(() -> new IllegalArgumentException("Fournisseur non trouvé"));
            commande.setFournisseur(fournisseur);
        }

        if (commandeDTO.getDateCommande() != null) {
            commande.setDateCommande(commandeDTO.getDateCommande());
        }

        if (commandeDTO.getDateLivraisonPrevue() != null) {
            commande.setDateLivraisonPrevue(commandeDTO.getDateLivraisonPrevue());
        }

        if (commandeDTO.getStatut() != null) {
            commande.setStatut(commandeDTO.getStatut());
        }

        if (commandeDTO.getObservations() != null) {
            commande.setObservations(commandeDTO.getObservations());
        }

        CommandeFournisseur updatedCommande = commandeFournisseurRepository.save(commande);
        log.info("Commande fournisseur mise à jour: {}", updatedCommande.getNumeroCommande());
        return updatedCommande;
    }

    public CommandeFournisseur updateStatutCommande(Long id, StatutCommande nouveauStatut) {
        log.info("Mise à jour du statut de la commande ID {} vers {}", id, nouveauStatut);

        CommandeFournisseur commande = commandeFournisseurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        commande.setStatut(nouveauStatut);

        if (nouveauStatut == StatutCommande.LIVREE && commande.getDateLivraisonReelle() == null) {
            commande.setDateLivraisonReelle(LocalDateTime.now());
        }

        return commandeFournisseurRepository.save(commande);
    }

    public void deleteCommande(Long id) {
        log.info("Suppression de la commande fournisseur ID: {}", id);

        CommandeFournisseur commande = commandeFournisseurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        // Soft delete
        commande.setActive(false);
        commandeFournisseurRepository.save(commande);
    }

    // Gestion des lignes de commande

    @Transactional(readOnly = true)
    public List<LigneCommandeFournisseur> getLignesCommande(Long commandeId) {
        return ligneCommandeFournisseurRepository.findActiveByCommandeId(commandeId);
    }

    public LigneCommandeFournisseur addLigneToCommande(Long commandeId, LigneCommandeFournisseurDTO ligneDTO) {
        log.info("Ajout d'une ligne à la commande ID: {}", commandeId);

        CommandeFournisseur commande = commandeFournisseurRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        LigneCommandeFournisseur ligne = LigneCommandeFournisseur.builder()
                .commandeFournisseur(commande)
                .produit(produit)
                .quantiteCommandee(ligneDTO.getQuantiteCommandee())
                .prixUnitaire(ligneDTO.getPrixUnitaire())
                .build();

        LigneCommandeFournisseur savedLigne = ligneCommandeFournisseurRepository.save(ligne);

        // Recalculer le montant total de la commande
        recalculateMontantTotal(commandeId);

        return savedLigne;
    }

    public LigneCommandeFournisseur updateLigneCommande(Long ligneId, LigneCommandeFournisseurDTO ligneDTO) {
        log.info("Mise à jour de la ligne de commande ID: {}", ligneId);

        LigneCommandeFournisseur ligne = ligneCommandeFournisseurRepository.findById(ligneId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de commande non trouvée"));

        if (ligneDTO.getQuantiteCommandee() != null) {
            ligne.setQuantiteCommandee(ligneDTO.getQuantiteCommandee());
        }

        if (ligneDTO.getPrixUnitaire() != null) {
            ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
        }

        LigneCommandeFournisseur updatedLigne = ligneCommandeFournisseurRepository.save(ligne);

        // Recalculer le montant total de la commande
        recalculateMontantTotal(ligne.getCommandeFournisseur().getId());

        return updatedLigne;
    }

    public void deleteLigneCommande(Long ligneId) {
        log.info("Suppression de la ligne de commande ID: {}", ligneId);

        LigneCommandeFournisseur ligne = ligneCommandeFournisseurRepository.findById(ligneId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne de commande non trouvée"));

        Long commandeId = ligne.getCommandeFournisseur().getId();

        // Soft delete
        ligne.setActive(false);
        ligneCommandeFournisseurRepository.save(ligne);

        // Recalculer le montant total de la commande
        recalculateMontantTotal(commandeId);
    }

    private CommandeFournisseur recalculateMontantTotal(Long commandeId) {
        CommandeFournisseur commande = commandeFournisseurRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        BigDecimal montantTotal = ligneCommandeFournisseurRepository.sumSousTotalByCommande(commande);
        commande.setMontantTotal(montantTotal != null ? montantTotal : BigDecimal.ZERO);

        return commandeFournisseurRepository.save(commande);
    }

    // Statistiques et rapports

    @Transactional(readOnly = true)
    public long countCommandesByStatut(StatutCommande statut) {
        return commandeFournisseurRepository.countByStatut(statut);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalMontantByStatuts(List<StatutCommande> statuts) {
        BigDecimal total = commandeFournisseurRepository.sumMontantTotalByStatutIn(statuts);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public String generateNextNumeroCommande() {
        Integer lastNumber = commandeFournisseurRepository.findLastCommandeNumber();
        int nextNumber = (lastNumber != null ? lastNumber : 0) + 1;
        return String.format("CMDF-%06d", nextNumber);
    }

    // Méthodes pour gérer l'évolution automatique des statuts

    public CommandeFournisseur confirmerCommande(Long commandeId) {
        log.info("Confirmation de la commande ID: {}", commandeId);
        
        CommandeFournisseur commande = commandeFournisseurRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        if (commande.getStatut() != StatutCommande.EN_COURS) {
            throw new IllegalStateException("Seules les commandes EN_COURS peuvent être confirmées");
        }

        commande.setStatut(StatutCommande.CONFIRMEE);
        return commandeFournisseurRepository.save(commande);
    }

    public CommandeFournisseur annulerCommande(Long commandeId, String motifAnnulation) {
        log.info("Annulation de la commande ID: {} - Motif: {}", commandeId, motifAnnulation);
        
        CommandeFournisseur commande = commandeFournisseurRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new IllegalStateException("Une commande livrée ne peut pas être annulée");
        }

        commande.setStatut(StatutCommande.ANNULEE);
        
        // Ajouter le motif d'annulation aux observations
        String observations = commande.getObservations() != null ? commande.getObservations() : "";
        observations += "\n[ANNULÉE] " + motifAnnulation;
        commande.setObservations(observations);
        
        return commandeFournisseurRepository.save(commande);
    }

    public CommandeFournisseur enregistrerLivraison(Long commandeId, Map<Long, Integer> quantitesLivrees) {
        log.info("Enregistrement de livraison pour la commande ID: {}", commandeId);
        
        CommandeFournisseur commande = commandeFournisseurRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new IllegalStateException("Une commande annulée ne peut pas recevoir de livraison");
        }

        // Mettre à jour les quantités livrées pour chaque ligne
        List<LigneCommandeFournisseur> lignes = getLignesCommande(commandeId);
        for (LigneCommandeFournisseur ligne : lignes) {
            if (quantitesLivrees.containsKey(ligne.getId())) {
                Integer nouvelleQuantiteLivree = quantitesLivrees.get(ligne.getId());
                
                // Validation : ne pas dépasser la quantité commandée
                if (nouvelleQuantiteLivree > ligne.getQuantiteCommandee()) {
                    throw new IllegalArgumentException("La quantité livrée ne peut pas dépasser la quantité commandée pour " + ligne.getProduit().getLibelle());
                }
                
                ligne.setQuantiteLivree(nouvelleQuantiteLivree);
                ligneCommandeFournisseurRepository.save(ligne);
            }
        }

        // Recalculer automatiquement le statut de la commande
        StatutCommande nouveauStatut = commande.calculerStatutSelonLivraisons();
        commande.setStatut(nouveauStatut);
        
        // Si entièrement livrée, mettre la date de livraison réelle
        if (nouveauStatut == StatutCommande.LIVREE) {
            commande.setDateLivraisonReelle(LocalDateTime.now());
        }

        // Ajouter dans les observations
        String observations = commande.getObservations() != null ? commande.getObservations() : "";
        observations += "\n[LIVRAISON] " + LocalDateTime.now().toLocalDate() + 
                       " - Statut: " + nouveauStatut.getLibelle() + 
                       " (" + String.format("%.1f", commande.getPourcentageGlobalLivraison()) + "%)";
        commande.setObservations(observations);
        
        return commandeFournisseurRepository.save(commande);
    }

    public CommandeFournisseur livrerTotalement(Long commandeId) {
        log.info("Livraison totale automatique de la commande ID: {}", commandeId);

        // Livrer toutes les quantités automatiquement
        List<LigneCommandeFournisseur> lignes = getLignesCommande(commandeId);
        Map<Long, Integer> quantitesTotales = new java.util.HashMap<>();
        
        for (LigneCommandeFournisseur ligne : lignes) {
            quantitesTotales.put(ligne.getId(), ligne.getQuantiteCommandee());
        }
        
        return enregistrerLivraison(commandeId, quantitesTotales);
    }

    @Transactional(readOnly = true)
    public boolean peutEtreConfirmee(Long commandeId) {
        return commandeFournisseurRepository.findById(commandeId)
                .map(c -> c.getStatut() == StatutCommande.EN_COURS)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean peutEtreAnnulee(Long commandeId) {
        return commandeFournisseurRepository.findById(commandeId)
                .map(c -> c.getStatut() != StatutCommande.LIVREE)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean peutEtreLivree(Long commandeId) {
        return commandeFournisseurRepository.findById(commandeId)
                .map(c -> c.getStatut() == StatutCommande.CONFIRMEE || 
                         c.getStatut() == StatutCommande.EN_COURS ||
                         c.getStatut() == StatutCommande.PARTIELLEMENT_LIVREE)
                .orElse(false);
    }

    // Compatibility method for simple partial delivery with text details
    public CommandeFournisseur livrerPartiellement(Long commandeId, String details) {
        log.info("Livraison partielle simple pour la commande ID: {} - Détails: {}", commandeId, details);
        
        CommandeFournisseur commande = commandeFournisseurRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        // For now, just update status and add details to observations
        if (commande.getStatut() == StatutCommande.EN_COURS || commande.getStatut() == StatutCommande.CONFIRMEE) {
            commande.setStatut(StatutCommande.PARTIELLEMENT_LIVREE);
        }
        
        // Add details to observations
        String observations = commande.getObservations() != null ? commande.getObservations() : "";
        observations += "\n[LIVRAISON PARTIELLE] " + LocalDateTime.now().toLocalDate() + " - " + details;
        commande.setObservations(observations);
        
        return commandeFournisseurRepository.save(commande);
    }
}