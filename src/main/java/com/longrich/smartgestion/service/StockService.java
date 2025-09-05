package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.*;
import com.longrich.smartgestion.entity.*;
import com.longrich.smartgestion.enums.MotifSortie;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.enums.TypeMouvement;
import com.longrich.smartgestion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockService {

    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ApprovisionnementRepository approvisionnementRepository;
    private final AlerteStockRepository alerteStockRepository;
    private final AjustementInventaireRepository ajustementInventaireRepository;
    private final UserRepository userRepository;

    // === GESTION DES ENTRÉES DE STOCK ===

    @Transactional
    public ApprovisionnementDTO creerEntreeStock(ApprovisionnementDTO dto) {
        log.info("Création d'une entrée de stock pour le produit ID: {}", dto.getProduitId());

        Produit produit = produitRepository.findById(dto.getProduitId())
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Créer l'approvisionnement
        Approvisionnement approvisionnement = Approvisionnement.builder()
            .produit(produit)
            .quantite(dto.getQuantite())
            .prixUnitaire(dto.getPrixUnitaire())
            .dateApprovisionnement(dto.getDateApprovisionnement() != null ? 
                dto.getDateApprovisionnement() : LocalDate.now())
            .numeroCommande(dto.getNumeroCommande())
            .numeroFacture(dto.getNumeroFacture())
            .commentaire(dto.getCommentaire())
            .fichierReference(dto.getFichierReference())
            .statut(Approvisionnement.StatutApprovisionnement.RECU_COMPLET)
            .build();

        approvisionnement = approvisionnementRepository.save(approvisionnement);

        // Mettre à jour le stock
        mettreAJourStockApresEntree(produit, dto.getQuantite());

        // Créer le mouvement de stock
        creerMouvementStock(produit, TypeMouvement.ENTREE, dto.getQuantite(), 
            "Approvisionnement - " + dto.getCommentaire());

        // Vérifier et résoudre les alertes si nécessaire
        verifierEtResoudreAlertes(produit);

        return convertirApprovisionnementToDTO(approvisionnement);
    }

    @Transactional
    public ApprovisionnementDTO creerEntreeStockAvecOptions(ApprovisionnementDTO dto, String typeInventaire, boolean historiser) {
        log.info("Création d'une entrée de stock avec options - type: {}, historiser: {}", typeInventaire, historiser);
        
        Produit produit = produitRepository.findById(dto.getProduitId())
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Créer l'approvisionnement avec fichier de référence
        Approvisionnement approvisionnement = Approvisionnement.builder()
            .produit(produit)
            .quantite(dto.getQuantite())
            .prixUnitaire(dto.getPrixUnitaire())
            .dateApprovisionnement(dto.getDateApprovisionnement() != null ? 
                dto.getDateApprovisionnement() : LocalDate.now())
            .numeroCommande(dto.getNumeroCommande())
            .numeroFacture(dto.getNumeroFacture())
            .commentaire(dto.getCommentaire())
            .fichierReference(dto.getFichierReference())
            .statut(resolveStatutFromDto(dto))
            .build();

        approvisionnement = approvisionnementRepository.save(approvisionnement);

        // Mettre à jour le stock selon le type d'inventaire
        if (historiser) {
            mettreAJourStockAvecHistorisation(produit, dto.getQuantite(), typeInventaire, dto.getFichierReference());
        } else {
            mettreAJourStockCumulGlobal(produit, dto.getQuantite(), typeInventaire);
        }

        // Créer le mouvement de stock
        creerMouvementStock(produit, TypeMouvement.ENTREE, dto.getQuantite(), 
            "Approvisionnement " + typeInventaire + " - " + dto.getCommentaire());

        return convertirApprovisionnementToDTO(approvisionnement);
    }

    private Approvisionnement.StatutApprovisionnement resolveStatutFromDto(ApprovisionnementDTO dto) {
        if (dto.getStatut() == null) return Approvisionnement.StatutApprovisionnement.RECU_COMPLET;
        try {
            return Approvisionnement.StatutApprovisionnement.valueOf(dto.getStatut());
        } catch (Exception e) {
            return Approvisionnement.StatutApprovisionnement.RECU_COMPLET;
        }
    }

    @Transactional(readOnly = true)
    public List<String> rechercherProduitsAutoComplete(String query) {
        return produitRepository.findByLibelleContainingIgnoreCase(query)
            .stream()
            .limit(10)
            .map(Produit::getLibelle)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Stock> filtrerStocks(Long familleProduitId, String magasin, LocalDate dateDebut, LocalDate dateFin) {
        return stockRepository.findStocksWithFilters(familleProduitId, magasin, dateDebut, dateFin);
    }

    // === GESTION DES SORTIES DE STOCK ===

    @Transactional
    public void creerSortieStock(Long produitId, Integer quantite, MotifSortie motif, String commentaire) {
        log.info("Création d'une sortie de stock pour le produit ID: {}, quantité: {}", produitId, quantite);

        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Vérifier le stock disponible
        Stock stock = stockRepository.findByProduit(produit)
            .orElseThrow(() -> new RuntimeException("Stock non trouvé pour ce produit"));

        if (stock.getQuantiteDisponible() < quantite) {
            throw new RuntimeException("Stock insuffisant. Disponible: " + 
                stock.getQuantiteDisponible() + ", Demandé: " + quantite);
        }

        // Mettre à jour le stock
        stock.setQuantite(stock.getQuantite() - quantite);
        stockRepository.save(stock);

        // Créer le mouvement de stock
        creerMouvementStock(produit, TypeMouvement.SORTIE, quantite, 
            motif.getDisplayName() + " - " + (commentaire != null ? commentaire : ""));

        // Vérifier les alertes
        verifierEtCreerAlertes(produit, stock.getQuantite());
    }

    // === GESTION DE L'INVENTAIRE ===

    @Transactional
    public AjustementInventaireDTO creerAjustementInventaire(AjustementInventaireDTO dto) {
        log.info("Création d'un ajustement d'inventaire pour le produit ID: {}", dto.getProduitId());

        Produit produit = produitRepository.findById(dto.getProduitId())
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Récupérer le stock actuel
        Stock stock = stockRepository.findByProduit(produit)
            .orElseThrow(() -> new RuntimeException("Stock non trouvé pour ce produit"));

        // Créer l'ajustement
        AjustementInventaire ajustement = AjustementInventaire.builder()
            .produit(produit)
            .quantiteTheorique(stock.getQuantite())
            .quantitePhysique(dto.getQuantitePhysique())
            .motifAjustement(AjustementInventaire.MotifAjustement.valueOf(dto.getMotifAjustement()))
            .commentaire(dto.getCommentaire())
            .dateAjustement(dto.getDateAjustement() != null ? 
                dto.getDateAjustement() : LocalDate.now())
            .build();

        ajustement = ajustementInventaireRepository.save(ajustement);

        // Mettre à jour le stock si l'ajustement est validé automatiquement
        if (Math.abs(ajustement.getEcart()) <= 5) { // Seuil de validation automatique
            stock.setQuantite(dto.getQuantitePhysique());
            stockRepository.save(stock);

            ajustement.setValide(true);
            ajustementInventaireRepository.save(ajustement);

            // Créer le mouvement de stock correspondant
            if (ajustement.getEcart() != 0) {
                TypeMouvement typeMouvement = ajustement.getEcart() > 0 ? 
                    TypeMouvement.ENTREE : TypeMouvement.SORTIE;
                creerMouvementStock(produit, typeMouvement, Math.abs(ajustement.getEcart()),
                    "Ajustement d'inventaire - " + dto.getMotifAjustement());
            }
        }

        return convertirAjustementToDTO(ajustement);
    }

    // === CONSULTATION ET STATISTIQUES ===

    @Transactional(readOnly = true)
    public Page<Stock> getVueEnsembleStock(Pageable pageable) {
        return stockRepository.findAllWithProduit(pageable);
    }

    @Transactional(readOnly = true)
    public StatistiquesStockDTO getStatistiquesStock() {
        log.debug("Calcul des statistiques de stock");

        return StatistiquesStockDTO.builder()
            .totalProduits(stockRepository.count())
            .totalProduitsEnStock(stockRepository.countProduitsEnStock())
            .totalProduitsRupture(stockRepository.countProduitsRupture())
            .totalProduitsStockFaible(stockRepository.countProduitsStockFaible())
            .valeurTotaleStock(stockRepository.calculateTotalStockValue())
            .mouvementsAujourdhui(mouvementStockRepository.countByDateMouvementToday())
            .entreesAujourdhui(mouvementStockRepository.countByTypeMouvementAndDateToday(TypeMouvement.ENTREE))
            .sortiesAujourdhui(mouvementStockRepository.countByTypeMouvementAndDateToday(TypeMouvement.SORTIE))
            .totalAlertes(alerteStockRepository.countByEstActive(true))
            .alertesCritiques((long) alerteStockRepository.findByNiveauAlerteAndEstActiveOrderByDateCreationDesc(
                AlerteStock.NiveauAlerte.CRITIQUE, true).size())
            .alertesAttention((long) alerteStockRepository.findByNiveauAlerteAndEstActiveOrderByDateCreationDesc(
                AlerteStock.NiveauAlerte.ATTENTION, true).size())
            .approvisionnementsPendants((long) approvisionnementRepository
                .findPendingApprovisionnements(Arrays.asList(
                    Approvisionnement.StatutApprovisionnement.EN_ATTENTE,
                    Approvisionnement.StatutApprovisionnement.COMMANDE)).size())
            .ajustementsEnAttente((long) ajustementInventaireRepository.findPendingValidation().size())
            .build();
    }

    @Transactional(readOnly = true)
    public List<AlerteStockDTO> getAlertesActives() {
        return alerteStockRepository.findByEstActiveOrderByDateCreationDesc(true)
            .stream()
            .map(this::convertirAlerteToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovisionnementDTO> getHistoriqueApprovisionnements(Long produitId) {
        return approvisionnementRepository.findHistoriqueByProduitId(produitId)
            .stream()
            .map(this::convertirApprovisionnementToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovisionnementDTO> filtrerApprovisionnements(Long familleProduitId, LocalDate dateDebut, LocalDate dateFin) {
        return approvisionnementRepository.findByFamilleAndPeriode(familleProduitId, dateDebut, dateFin)
            .stream()
            .map(this::convertirApprovisionnementToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AjustementInventaireDTO> getHistoriqueAjustements(Long produitId) {
        return ajustementInventaireRepository.findHistoriqueByProduitId(produitId)
            .stream()
            .map(this::convertirAjustementToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Stock> getEntreesParEmplacement(String typeStock, LocalDate dateDebut, LocalDate dateFin) {
        return stockRepository.findHistorisedEntries(typeStock, dateDebut, dateFin);
    }

    // === MÉTHODES PRIVÉES ===

    private void mettreAJourStockApresEntree(Produit produit, Integer quantite) {
        Stock stock = stockRepository.findByProduit(produit)
            .orElseGet(() -> {
                Stock newStock = new Stock();
                newStock.setProduit(produit);
                newStock.setQuantite(0);
                newStock.setQuantiteReservee(0);
                return newStock;
            });

        stock.setQuantite(stock.getQuantite() + quantite);
        stockRepository.save(stock);
    }

    private void mettreAJourStockAvecHistorisation(Produit produit, Integer quantite, String typeInventaire, String fichierReference) {
        // Créer une nouvelle entrée de stock pour historisation
        Stock nouveauStock = Stock.builder()
            .produit(produit)
            .quantite(quantite)
            .quantiteReservee(0)
            .typeStock(TypeEmplacement.valueOf(typeInventaire))
            .dateEntree(LocalDate.now())
            .fichierReference(fichierReference)
            .build();
        
        stockRepository.save(nouveauStock);
    }

    private void mettreAJourStockCumulGlobal(Produit produit, Integer quantite, String typeInventaire) {
        Stock stock = stockRepository.findByProduitAndTypeStock(produit, typeInventaire)
            .orElseGet(() -> {
                Stock newStock = new Stock();
                newStock.setProduit(produit);
                newStock.setQuantite(0);
                newStock.setQuantiteReservee(0);
                newStock.setTypeStock(TypeEmplacement.valueOf(typeInventaire));
                newStock.setDateEntree(LocalDate.now());
                return newStock;
            });

        stock.setQuantite(stock.getQuantite() + quantite);
        stockRepository.save(stock);
    }

    private void creerMouvementStock(Produit produit, TypeMouvement type, Integer quantite, String observation) {
        MouvementStock mouvement = MouvementStock.builder()
            .produit(produit)
            .typeMouvement(type)
            .quantite(quantite)
            .observation(observation)
            .dateMouvement(LocalDateTime.now())
            .build();

        mouvementStockRepository.save(mouvement);
    }

    private void verifierEtCreerAlertes(Produit produit, Integer quantiteActuelle) {
        // Vérifier rupture de stock
        if (quantiteActuelle <= 0) {
            creerAlerteStock(produit, AlerteStock.TypeAlerte.RUPTURE_STOCK, 
                AlerteStock.NiveauAlerte.CRITIQUE, "Stock épuisé", 0, quantiteActuelle);
        }
        // Vérifier stock faible
        else if (produit.getStockMinimum() != null && quantiteActuelle <= produit.getStockMinimum()) {
            creerAlerteStock(produit, AlerteStock.TypeAlerte.STOCK_FAIBLE, 
                AlerteStock.NiveauAlerte.ATTENTION, 
                "Stock faible (" + quantiteActuelle + "/" + produit.getStockMinimum() + ")",
                produit.getStockMinimum(), quantiteActuelle);
        }
    }

    private void verifierEtResoudreAlertes(Produit produit) {
        // Résoudre les alertes si le stock est redevenu normal
        Stock stock = stockRepository.findByProduit(produit).orElse(null);
        if (stock != null && produit.getStockMinimum() != null && 
            stock.getQuantite() > produit.getStockMinimum()) {
            
            List<AlerteStock> alertesActives = alerteStockRepository
                .findByProduitAndEstActiveOrderByDateCreationDesc(produit, true);
            
            for (AlerteStock alerte : alertesActives) {
                alerte.resoudre(null); // TODO: Récupérer l'utilisateur actuel
                alerteStockRepository.save(alerte);
            }
        }
    }

    private void creerAlerteStock(Produit produit, AlerteStock.TypeAlerte type, 
                                 AlerteStock.NiveauAlerte niveau, String message,
                                 Integer seuil, Integer quantiteActuelle) {
        
        // Vérifier si une alerte similaire existe déjà
        Optional<AlerteStock> alerteExistante = alerteStockRepository
            .findByProduitAndTypeAlerteAndEstActive(produit, type, true);
        
        if (alerteExistante.isEmpty()) {
            AlerteStock alerte = AlerteStock.builder()
                .produit(produit)
                .typeAlerte(type)
                .niveauAlerte(niveau)
                .message(message)
                .seuilDeclenche(seuil)
                .quantiteActuelle(quantiteActuelle)
                .estActive(true)
                .dateCreation(LocalDateTime.now())
                .build();
            
            alerteStockRepository.save(alerte);
        }
    }

    // === MÉTHODES DE CONVERSION DTO ===

    private ApprovisionnementDTO convertirApprovisionnementToDTO(Approvisionnement approvisionnement) {
        return ApprovisionnementDTO.builder()
            .id(approvisionnement.getId())
            .produitId(approvisionnement.getProduit().getId())
            .produitLibelle(approvisionnement.getProduit().getLibelle())
            .quantite(approvisionnement.getQuantite())
            .prixUnitaire(approvisionnement.getPrixUnitaire())
            .prixTotal(approvisionnement.getPrixTotal())
            .dateApprovisionnement(approvisionnement.getDateApprovisionnement())
            .dateReception(approvisionnement.getDateReception())
            .numeroCommande(approvisionnement.getNumeroCommande())
            .numeroFacture(approvisionnement.getNumeroFacture())
            .commentaire(approvisionnement.getCommentaire())
            .statut(approvisionnement.getStatut().name())
            .statutDisplayName(approvisionnement.getStatut().getDisplayName())
            .build();
    }

    private AlerteStockDTO convertirAlerteToDTO(AlerteStock alerte) {
        return AlerteStockDTO.builder()
            .id(alerte.getId())
            .produitId(alerte.getProduit().getId())
            .produitLibelle(alerte.getProduit().getLibelle())
            .typeAlerte(alerte.getTypeAlerte().name())
            .typeAlerteDisplayName(alerte.getTypeAlerte().getDisplayName())
            .niveauAlerte(alerte.getNiveauAlerte().name())
            .niveauAlerteDisplayName(alerte.getNiveauAlerte().getDisplayName())
            .niveauAlerteIcone(alerte.getNiveauAlerte().getIcone())
            .message(alerte.getMessage())
            .seuilDeclenche(alerte.getSeuilDeclenche())
            .quantiteActuelle(alerte.getQuantiteActuelle())
            .estActive(alerte.getEstActive())
            .dateCreation(alerte.getDateCreation())
            .dateResolution(alerte.getDateResolution())
            .build();
    }

    private AjustementInventaireDTO convertirAjustementToDTO(AjustementInventaire ajustement) {
        return AjustementInventaireDTO.builder()
            .id(ajustement.getId())
            .produitId(ajustement.getProduit().getId())
            .produitLibelle(ajustement.getProduit().getLibelle())
            .quantiteTheorique(ajustement.getQuantiteTheorique())
            .quantitePhysique(ajustement.getQuantitePhysique())
            .ecart(ajustement.getEcart())
            .motifAjustement(ajustement.getMotifAjustement().name())
            .motifAjustementDisplayName(ajustement.getMotifAjustement().getDisplayName())
            .commentaire(ajustement.getCommentaire())
            .dateAjustement(ajustement.getDateAjustement())
            .valide(ajustement.getValide())
            .build();
    }

    /**
     * Méthodes utilitaires pour les nouveaux panneaux UI
     */
    @Transactional(readOnly = true)
    public List<Stock> getStocksByTypeStock(TypeEmplacement typeEmplacement) {
        return stockRepository.findByTypeStock(typeEmplacement);
    }

    @Transactional(readOnly = true)
    public Stock getStockByProduitAndType(Produit produit, TypeEmplacement typeEmplacement) {
        return stockRepository.findByProduitAndTypeStock(produit, typeEmplacement).orElse(null);
    }

    @Transactional(readOnly = true) 
    public Stock findByProduitAndTypeStock(Produit produit, TypeEmplacement typeEmplacement) {
        return stockRepository.findByProduitAndTypeStock(produit, typeEmplacement).orElse(null);
    }
}
