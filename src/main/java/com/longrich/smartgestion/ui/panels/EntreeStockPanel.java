package com.longrich.smartgestion.ui.panels;

import com.longrich.smartgestion.dto.EntreeStockDTO;
import com.longrich.smartgestion.dto.LigneEntreeStockDTO;
import com.longrich.smartgestion.entity.EntreeStock;
import com.longrich.smartgestion.entity.Fournisseur;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.enums.StatutEntreeStock;
import com.longrich.smartgestion.service.EntreeStockService;
import com.longrich.smartgestion.service.CommandeFournisseurService;
import com.longrich.smartgestion.service.FournisseurService;
import com.longrich.smartgestion.service.ProduitService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EntreeStockPanel extends JPanel {

    private final EntreeStockService entreeStockService;
    private final FournisseurService fournisseurService;
    private final ProduitService produitService;
    private final CommandeFournisseurService commandeFournisseurService;

    // Composants UI
    private JTable tableEntrees;
    private DefaultTableModel tableModel;
    private JComboBox<Fournisseur> comboFournisseur;
    private JTextField txtNumeroFacture;
    private JTextField txtNumeroBonLivraison;
    private JTextArea txtObservation;
    private JComboBox<StatutEntreeStock> comboStatut;
    private JComboBox<com.longrich.smartgestion.entity.CommandeFournisseur> comboCommande;

    // Table des lignes d'entrée
    private JTable tableLignes;
    private DefaultTableModel modelLignes;
    private JComboBox<Produit> comboProduit;
    private JSpinner spinnerQuantite;
    private JSpinner spinnerPrixUnitaire;

    private EntreeStock entreeStockCourante;

    public EntreeStockPanel(EntreeStockService entreeStockService,
                           FournisseurService fournisseurService,
                           ProduitService produitService,
                           CommandeFournisseurService commandeFournisseurService) {
        this.entreeStockService = entreeStockService;
        this.fournisseurService = fournisseurService;
        this.produitService = produitService;
        this.commandeFournisseurService = commandeFournisseurService;

        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {
        // Table principale des entrées
        String[] colonnes = {"N° Entrée", "Date", "Fournisseur", "Statut", "Montant", "Actions"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seule la colonne Actions est éditable
            }
        };
        tableEntrees = new JTable(tableModel);

        // Formulaire de saisie
        comboFournisseur = new JComboBox<>();
        txtNumeroFacture = new JTextField(20);
        txtNumeroFacture.setVisible(false);
        txtNumeroBonLivraison = new JTextField(20);
        txtObservation = new JTextArea(3, 20);
        txtObservation.setLineWrap(true);
        comboStatut = new JComboBox<>();
        comboStatut.setVisible(false);

        // Table des lignes d'entrée
        String[] colonnesLignes = {"Produit", "Quantité", "Qté Reçue", "Prix Unit.", "Montant", "Actions", "ProduitId"};
        modelLignes = new DefaultTableModel(colonnesLignes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seule la colonne Actions est éditable
            }
        };
        tableLignes = new JTable(modelLignes);
        // Masquer la colonne ProduitId
        if (tableLignes.getColumnModel().getColumnCount() > 6) {
            tableLignes.getColumnModel().getColumn(6).setMinWidth(0);
            tableLignes.getColumnModel().getColumn(6).setMaxWidth(0);
            tableLignes.getColumnModel().getColumn(6).setWidth(0);
        }

        // Composants pour ajouter des lignes
        comboProduit = new JComboBox<>();
        spinnerQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        spinnerPrixUnitaire = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.01));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel principal avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();

        // Onglet Liste des entrées
        JPanel panelListe = createListPanel();
        tabbedPane.addTab("Liste des Entrées", panelListe);

        // Onglet Nouvelle entrée / Modification
        JPanel panelForm = createFormPanel();
        tabbedPane.addTab("Nouvelle Entrée", panelForm);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de filtres
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<StatutEntreeStock> filterStatut = new JComboBox<>();
        filterStatut.addItem(null); // Option "Tous"
        for (StatutEntreeStock statut : StatutEntreeStock.values()) {
            filterStatut.addItem(statut);
        }
        
        JButton btnFilter = new JButton("Filtrer");
        btnFilter.addActionListener(e -> filtrerEntrees((StatutEntreeStock) filterStatut.getSelectedItem()));

        filterPanel.add(new JLabel("Statut:"));
        filterPanel.add(filterStatut);
        filterPanel.add(btnFilter);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Table avec scroll
        JScrollPane scrollTable = new JScrollPane(tableEntrees);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Boutons d'actions
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnNouvelle = new JButton("Nouvelle Entrée");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnValider = new JButton("Valider Réception");

        btnNouvelle.addActionListener(e -> nouvelleEntree());
        btnModifier.addActionListener(e -> modifierEntree());
        btnSupprimer.addActionListener(e -> supprimerEntree());
        btnValider.addActionListener(e -> validerReception());

        buttonPanel.add(btnNouvelle);
        buttonPanel.add(btnModifier);
        buttonPanel.add(btnSupprimer);
        buttonPanel.add(btnValider);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulaire principal
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ligne 1: Fournisseur
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Fournisseur:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(comboFournisseur, gbc);

        // Ligne 2: Commande Fournisseur
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Commande Fournisseur:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        comboCommande = new JComboBox<>();
        formPanel.add(comboCommande, gbc);

        // N° Facture non affiché

        // Ligne 4: N° Bon de livraison
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("N° Bon Livraison:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtNumeroBonLivraison, gbc);

        // Statut réception non affiché

        // Ligne 6: Observation
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Observation:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(txtObservation), gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Section lignes d'entrée
        JPanel lignesPanel = createLignesPanel();
        panel.add(lignesPanel, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSave = new JButton("Enregistrer");
        JButton btnCancel = new JButton("Annuler");

        btnSave.addActionListener(e -> sauvegarderEntree());
        btnCancel.addActionListener(e -> annulerSaisie());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lignes d'Entrée"));

        // Formulaire d'ajout de ligne
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(new JLabel("Produit:"), gbc);
        gbc.gridx = 1;
        addPanel.add(comboProduit, gbc);

        gbc.gridx = 2;
        addPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 3;
        addPanel.add(spinnerQuantite, gbc);

        gbc.gridx = 4;
        addPanel.add(new JLabel("Prix Unit.:"), gbc);
        gbc.gridx = 5;
        addPanel.add(spinnerPrixUnitaire, gbc);

        JButton btnAdd = new JButton("Ajouter");
        btnAdd.addActionListener(e -> ajouterLigne());
        gbc.gridx = 6;
        addPanel.add(btnAdd, gbc);

        panel.add(addPanel, BorderLayout.NORTH);

        // Table des lignes
        JScrollPane scrollLignes = new JScrollPane(tableLignes);
        panel.add(scrollLignes, BorderLayout.CENTER);

        return panel;
    }

    private void loadData() {
        // Charger les fournisseurs
        List<Fournisseur> fournisseurs = fournisseurService.findAll();
        comboFournisseur.removeAllItems();
        for (Fournisseur fournisseur : fournisseurs) {
            comboFournisseur.addItem(fournisseur);
        }

        // Charger les produits
        List<Produit> produits = produitService.findAll();
        comboProduit.removeAllItems();
        for (Produit produit : produits) {
            comboProduit.addItem(produit);
        }

        // Charger les commandes fournisseur actives (EN_COURS, PARTIELLEMENT_LIVREE)
        comboCommande.removeAllItems();
        try {
            List<com.longrich.smartgestion.entity.CommandeFournisseur> enCours = commandeFournisseurService.getCommandesByStatut(com.longrich.smartgestion.enums.StatutCommande.EN_COURS);
            List<com.longrich.smartgestion.entity.CommandeFournisseur> partiel = commandeFournisseurService.getCommandesByStatut(com.longrich.smartgestion.enums.StatutCommande.PARTIELLEMENT_LIVREE);
            for (var c : enCours) comboCommande.addItem(c);
            for (var c : partiel) comboCommande.addItem(c);
        } catch (Exception e) {
            // ignore pour l'UI
        }

        // Charger les entrées de stock
        chargerEntrees();
    }

    private void chargerEntrees() {
        tableModel.setRowCount(0);
        List<EntreeStock> entrees = entreeStockService.findAll();
        
        for (EntreeStock entree : entrees) {
            Object[] row = {
                entree.getNumeroEntree(),
                entree.getDateEntree().toLocalDate(),
                entree.getFournisseur() != null ? entree.getFournisseur().getNomComplet() : "",
                entree.getStatut(),
                entree.getMontantTotal() != null ? entree.getMontantTotal() : BigDecimal.ZERO,
                "Voir"
            };
            tableModel.addRow(row);
        }
    }

    private void nouvelleEntree() {
        entreeStockCourante = null;
        viderFormulaire();
    }

    private void modifierEntree() {
        int selectedRow = tableEntrees.getSelectedRow();
        if (selectedRow >= 0) {
            // Récupérer l'entrée sélectionnée et remplir le formulaire
            String numeroEntree = (String) tableModel.getValueAt(selectedRow, 0);
            // Logique pour charger l'entrée dans le formulaire
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une entrée à modifier.");
        }
    }

    private void supprimerEntree() {
        int selectedRow = tableEntrees.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer cette entrée?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Logique de suppression
                chargerEntrees(); // Recharger la liste
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une entrée à supprimer.");
        }
    }

    private void validerReception() {
        int selectedRow = tableEntrees.getSelectedRow();
        if (selectedRow >= 0) {
            // Ouvrir une fenêtre de validation de réception
            JOptionPane.showMessageDialog(this, "Fonctionnalité de validation en cours de développement.");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une entrée à valider.");
        }
    }

    private void ajouterLigne() {
        Produit produit = (Produit) comboProduit.getSelectedItem();
        Integer quantite = (Integer) spinnerQuantite.getValue();
        BigDecimal prixUnitaire = new BigDecimal(spinnerPrixUnitaire.getValue().toString());

        if (produit != null && quantite > 0) {
            Object[] row = {
                produit.getLibelle(),
                quantite,
                0, // Quantité reçue
                prixUnitaire,
                prixUnitaire.multiply(BigDecimal.valueOf(quantite)),
                "Supprimer",
                produit.getId()
            };
            modelLignes.addRow(row);

            // Réinitialiser les champs
            comboProduit.setSelectedIndex(0);
            spinnerQuantite.setValue(1);
            spinnerPrixUnitaire.setValue(0.0);
        }
    }

    private void sauvegarderEntree() {
        try {
            EntreeStockDTO dto = new EntreeStockDTO();
            dto.setFournisseurId(((Fournisseur) comboFournisseur.getSelectedItem()).getId());
            if (comboCommande.getSelectedItem() != null) {
                dto.setCommandeFournisseurId(((com.longrich.smartgestion.entity.CommandeFournisseur) comboCommande.getSelectedItem()).getId());
            }
            // Pas de numéro facture/statut dans ce flux
            dto.setNumeroBonLivraison(txtNumeroBonLivraison.getText());
            dto.setObservation(txtObservation.getText());
            dto.setDateEntree(LocalDateTime.now());

            // Collecter les lignes
            List<LigneEntreeStockDTO> lignes = new ArrayList<>();
            for (int i = 0; i < modelLignes.getRowCount(); i++) {
                LigneEntreeStockDTO ligne = new LigneEntreeStockDTO();
                Object produitIdVal = modelLignes.getValueAt(i, 6);
                if (produitIdVal != null) {
                    ligne.setProduitId(Long.valueOf(produitIdVal.toString()));
                }
                Object qte = modelLignes.getValueAt(i, 1);
                Object prix = modelLignes.getValueAt(i, 3);
                ligne.setQuantite(Integer.valueOf(qte.toString()));
                ligne.setQuantiteRecue(0);
                ligne.setPrixUnitaire(new BigDecimal(prix.toString()));
                lignes.add(ligne);
            }
            dto.setLignesEntree(lignes);

            entreeStockService.creerEntreeStock(dto);
            
            JOptionPane.showMessageDialog(this, "Entrée de stock créée avec succès!");
            chargerEntrees();
            viderFormulaire();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la sauvegarde: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void annulerSaisie() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        comboFournisseur.setSelectedIndex(0);
        txtNumeroFacture.setText("");
        txtNumeroBonLivraison.setText("");
        txtObservation.setText("");
        comboStatut.setSelectedItem(StatutEntreeStock.EN_ATTENTE);
        modelLignes.setRowCount(0);
    }

    private void filtrerEntrees(StatutEntreeStock statut) {
        if (statut == null) {
            chargerEntrees();
        } else {
            tableModel.setRowCount(0);
            List<EntreeStock> entrees = entreeStockService.findByStatut(statut);
            
            for (EntreeStock entree : entrees) {
                Object[] row = {
                    entree.getNumeroEntree(),
                    entree.getDateEntree().toLocalDate(),
                    entree.getFournisseur() != null ? entree.getFournisseur().getNomComplet() : "",
                    entree.getStatut(),
                    entree.getMontantTotal() != null ? entree.getMontantTotal() : BigDecimal.ZERO,
                    "Voir"
                };
                tableModel.addRow(row);
            }
        }
    }
}
