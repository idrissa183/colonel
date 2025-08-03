package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.CommandeDTO;
import com.longrich.smartgestion.entity.Commande;
import com.longrich.smartgestion.entity.Client;
import com.longrich.smartgestion.entity.LigneCommande;
import com.longrich.smartgestion.entity.SuiviPV;
import com.longrich.smartgestion.enums.StatutCommande;
import com.longrich.smartgestion.enums.TypeVente;
import com.longrich.smartgestion.repository.CommandeRepository;
import com.longrich.smartgestion.repository.ClientRepository;
import com.longrich.smartgestion.repository.SuiviPVRepository;
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
@Transactional
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final SuiviPVRepository suiviPVRepository;

    public List<CommandeDTO> getAllCommandes() {
        return commandeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<CommandeDTO> getCommandesNonLivrees() {
        return commandeRepository.findByStatutIn(List.of(StatutCommande.EN_COURS, StatutCommande.EN_ATTENTE))
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<CommandeDTO> getCommandesByClient(Long clientId) {
        return commandeRepository.findByClientId(clientId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<CommandeDTO> getCommandeById(Long id) {
        return commandeRepository.findById(id)
                .map(this::convertToDTO);
    }

    public CommandeDTO livrerCommande(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new IllegalArgumentException("La commande est déjà livrée");
        }

        // Marquer toutes les lignes comme livrées
        commande.getLignes().forEach(ligne -> 
            ligne.setQuantiteLivree(ligne.getQuantite())
        );

        commande.setStatut(StatutCommande.LIVREE);
        commande.setDateLivraisonEffective(LocalDateTime.now());

        // Créer les entrées de suivi PV si le client est partenaire
        Client client = commande.getClient();
        if (isEligiblePourPV(client)) {
            creerSuiviPV(commande);
            // Mettre à jour le total PV du client
            client.setTotalPv(client.getTotalPv() + commande.getTotalPv().intValue());
            clientRepository.save(client);
        }

        Commande savedCommande = commandeRepository.save(commande);
        log.info("Commande livrée: {}", savedCommande.getNumeroCommande());
        return convertToDTO(savedCommande);
    }

    public CommandeDTO livrerPartiellement(Long commandeId, Long ligneId, Integer quantiteLivree) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        LigneCommande ligne = commande.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ligne de commande non trouvée"));

        if (quantiteLivree > ligne.getQuantiteRestante()) {
            throw new IllegalArgumentException("Quantité livrée supérieure à la quantité restante");
        }

        ligne.setQuantiteLivree(ligne.getQuantiteLivree() + quantiteLivree);

        // Vérifier si la commande est complètement livrée
        boolean commandeComplete = commande.getLignes().stream()
                .allMatch(LigneCommande::isLivreCompletement);

        if (commandeComplete) {
            commande.setStatut(StatutCommande.LIVREE);
            commande.setDateLivraisonEffective(LocalDateTime.now());
        } else {
            commande.setStatut(StatutCommande.PARTIELLEMENT_LIVREE);
        }

        Commande savedCommande = commandeRepository.save(commande);
        log.info("Livraison partielle pour commande: {} - Ligne: {} - Quantité: {}", 
                 savedCommande.getNumeroCommande(), ligneId, quantiteLivree);
        return convertToDTO(savedCommande);
    }

    private boolean isEligiblePourPV(Client client) {
        return client.getTypeClient().name().contains("PARTENAIRE");
    }

    private void creerSuiviPV(Commande commande) {
        commande.getLignes().forEach(ligne -> {
            SuiviPV suiviPV = SuiviPV.builder()
                    .client(commande.getClient())
                    .produit(ligne.getProduit())
                    .quantite(ligne.getQuantiteLivree())
                    .pvGagne(ligne.getProduit().getPv().multiply(BigDecimal.valueOf(ligne.getQuantiteLivree())))
                    .montantVente(ligne.getSousTotal())
                    .typeVente(TypeVente.AVEC_PV)
                    .numeroFacture(commande.getNumeroCommande())
                    .build();
            suiviPVRepository.save(suiviPV);
        });
    }

    private CommandeDTO convertToDTO(Commande commande) {
        return CommandeDTO.builder()
                .id(commande.getId())
                .numeroCommande(commande.getNumeroCommande())
                .clientId(commande.getClient().getId())
                .clientNom(commande.getClient().getNomComplet())
                .userId(commande.getUser().getId())
                .userNom(commande.getUser().getNom())
                .dateCommande(commande.getDateCommande())
                .dateLivraisonPrevue(commande.getDateLivraisonPrevue())
                .dateLivraisonEffective(commande.getDateLivraisonEffective())
                .statut(commande.getStatut())
                .montantTotal(commande.getMontantTotal())
                .totalPv(commande.getTotalPv())
                .tva(commande.getTva())
                .montantHT(commande.getMontantHT())
                .observations(commande.getObservations())
                .commandeNonLivree(commande.isCommandeNonLivree())
                .totalQuantiteCommandee(commande.getTotalQuantiteCommandee())
                .totalQuantiteLivree(commande.getTotalQuantiteLivree())
                .totalQuantiteRestante(commande.getTotalQuantiteRestante())
                .build();
    }
}