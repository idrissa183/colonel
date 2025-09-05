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

import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
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
    
    // Nouveaux champs pour l'amélioration
    private JTextField txtFournisseurReadOnly;
    private JSpinner spinnerDateEntree;
    private JTextField txtFichierReference;

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

        // Ligne 1: Commande Fournisseur (obligatoire)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel labelCommande = new JLabel("Commande Fournisseur *:");
        labelCommande.setFont(labelCommande.getFont().deriveFont(java.awt.Font.BOLD));
        formPanel.add(labelCommande, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        comboCommande = new JComboBox<>();
        comboCommande.addActionListener(e -> onCommandeSelectionnee());
        formPanel.add(comboCommande, gbc);

        // Ligne 2: Fournisseur (en lecture seule, dérivé de la commande)
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Fournisseur:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtFournisseur = new JTextField();
        txtFournisseur.setEditable(false);
        txtFournisseur.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtFournisseur, gbc);
        
        // Référence pour mise à jour automatique du fournisseur
        this.txtFournisseurReadOnly = txtFournisseur;

        // N° Facture non affiché

        // Ligne 3: Date d'entrée (obligatoire)
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel labelDate = new JLabel("Date d'entrée *:");
        labelDate.setFont(labelDate.getFont().deriveFont(java.awt.Font.BOLD));
        formPanel.add(labelDate, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        this.spinnerDateEntree = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDateEntree, "dd/MM/yyyy");
        spinnerDateEntree.setEditor(dateEditor);
        formPanel.add(spinnerDateEntree, gbc);

        // Ligne 4: N° Bon de livraison
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("N° Bon Livraison:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtNumeroBonLivraison, gbc);

        // Ligne 5: Fichier de référence
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Fichier de référence:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel filePanel = new JPanel(new BorderLayout());
        this.txtFichierReference = new JTextField();
        txtFichierReference.setEditable(false);
        JButton btnParcourir = new JButton("Parcourir...");
        btnParcourir.addActionListener(e -> choisirFichier(txtFichierReference));
        filePanel.add(txtFichierReference, BorderLayout.CENTER);
        filePanel.add(btnParcourir, BorderLayout.EAST);
        formPanel.add(filePanel, gbc);

        // Ligne 6: Observation
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
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
        panel.setBorder(BorderFactory.createTitledBorder("Produits de la Commande"));

        // Panneau d'information
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><i>Sélectionnez une commande pour voir les produits disponibles.</i></html>");
        infoPanel.add(infoLabel);
        panel.add(infoPanel, BorderLayout.NORTH);

        // Table des lignes avec colonnes adaptées
        String[] colonnesLignes = {"Produit", "Qté Commandée", "Qté Déjà Livrée", "Qté Restante", "Qté Reçue", "Prix Unit.", "Montant", "Actions", "ProduitId"};
        modelLignes = new DefaultTableModel(colonnesLignes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Seules les colonnes Qté Reçue et Actions sont éditables
                return column == 4 || column == 7;
            }
        };
        tableLignes = new JTable(modelLignes);
        
        // Masquer la colonne ProduitId
        if (tableLignes.getColumnModel().getColumnCount() > 8) {
            tableLignes.getColumnModel().getColumn(8).setMinWidth(0);
            tableLignes.getColumnModel().getColumn(8).setMaxWidth(0);
            tableLignes.getColumnModel().getColumn(8).setWidth(0);
        }

        // Configuration des tailles de colonnes
        tableLignes.getColumnModel().getColumn(0).setPreferredWidth(200); // Produit
        tableLignes.getColumnModel().getColumn(1).setPreferredWidth(80);  // Qté Commandée
        tableLignes.getColumnModel().getColumn(2).setPreferredWidth(80);  // Qté Déjà Livrée
        tableLignes.getColumnModel().getColumn(3).setPreferredWidth(80);  // Qté Restante
        tableLignes.getColumnModel().getColumn(4).setPreferredWidth(80);  // Qté Reçue
        tableLignes.getColumnModel().getColumn(5).setPreferredWidth(80);  // Prix Unit.
        tableLignes.getColumnModel().getColumn(6).setPreferredWidth(100); // Montant
        tableLignes.getColumnModel().getColumn(7).setPreferredWidth(80);  // Actions

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
        chargerCommandesDisponibles();

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
            // Validation des champs obligatoires
            if (comboCommande.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "La commande fournisseur est obligatoire.", 
                    "Champ obligatoire", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Vérifier qu'au moins une quantité reçue > 0
            boolean auMoinsUneQuantite = false;
            for (int i = 0; i < modelLignes.getRowCount(); i++) {
                Object qteRecue = modelLignes.getValueAt(i, 4);
                if (qteRecue != null && Integer.parseInt(qteRecue.toString()) > 0) {
                    auMoinsUneQuantite = true;
                    break;
                }
            }
            
            if (!auMoinsUneQuantite) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir au moins une quantité reçue.", 
                    "Quantités manquantes", JOptionPane.WARNING_MESSAGE);
                return;
            }

            EntreeStockDTO dto = new EntreeStockDTO();
            dto.setCommandeFournisseurId(((com.longrich.smartgestion.entity.CommandeFournisseur) comboCommande.getSelectedItem()).getId());
            dto.setNumeroBonLivraison(txtNumeroBonLivraison.getText());
            dto.setObservation(txtObservation.getText());
            // Récupérer la date depuis le spinner
            Date dateEntree = (Date) spinnerDateEntree.getValue();
            dto.setDateEntree(dateEntree.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            if (!txtFichierReference.getText().trim().isEmpty()) {
                dto.setFichierReference(txtFichierReference.getText().trim());
            }

            // Collecter les lignes avec quantité reçue > 0
            List<LigneEntreeStockDTO> lignes = new ArrayList<>();
            for (int i = 0; i < modelLignes.getRowCount(); i++) {
                Object qteRecueObj = modelLignes.getValueAt(i, 4);
                int qteRecue = qteRecueObj != null ? Integer.parseInt(qteRecueObj.toString()) : 0;
                
                if (qteRecue > 0) {
                    LigneEntreeStockDTO ligne = new LigneEntreeStockDTO();
                    Object produitIdVal = modelLignes.getValueAt(i, 8);
                    if (produitIdVal != null) {
                        ligne.setProduitId(Long.valueOf(produitIdVal.toString()));
                    }
                    Object prix = modelLignes.getValueAt(i, 5);
                    ligne.setQuantite(qteRecue);
                    ligne.setQuantiteRecue(qteRecue);
                    ligne.setPrixUnitaire(new BigDecimal(prix.toString()));
                    lignes.add(ligne);
                }
            }
            dto.setLignesEntree(lignes);

            // Utiliser la méthode créer et valider directement
            entreeStockService.creerEtValiderDepuisCommande(dto);
            
            JOptionPane.showMessageDialog(this, "Entrée de stock enregistrée et validée avec succès!");
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
        comboCommande.setSelectedIndex(-1);
        if (txtFournisseurReadOnly != null) {
            txtFournisseurReadOnly.setText("");
        }
        spinnerDateEntree.setValue(new Date());
        txtNumeroBonLivraison.setText("");
        if (txtFichierReference != null) {
            txtFichierReference.setText("");
        }
        txtObservation.setText("");
        modelLignes.setRowCount(0);
    }

    private void chargerCommandesDisponibles() {
        comboCommande.removeAllItems();
        try {
            List<com.longrich.smartgestion.entity.CommandeFournisseur> enCours = 
                commandeFournisseurService.getCommandesByStatut(com.longrich.smartgestion.enums.StatutCommande.EN_COURS);
            List<com.longrich.smartgestion.entity.CommandeFournisseur> partiel = 
                commandeFournisseurService.getCommandesByStatut(com.longrich.smartgestion.enums.StatutCommande.PARTIELLEMENT_LIVREE);
            
            for (var c : enCours) comboCommande.addItem(c);
            for (var c : partiel) comboCommande.addItem(c);
        } catch (Exception e) {
            log.error("Erreur lors du chargement des commandes", e);
        }
    }

    private void onCommandeSelectionnee() {
        com.longrich.smartgestion.entity.CommandeFournisseur commande = 
            (com.longrich.smartgestion.entity.CommandeFournisseur) comboCommande.getSelectedItem();
        
        if (commande != null) {
            // Mettre à jour le fournisseur en lecture seule
            if (txtFournisseurReadOnly != null) {
                txtFournisseurReadOnly.setText(commande.getFournisseur().getNomComplet());
            }
            chargerProduitsCommande(commande);
        } else {
            if (txtFournisseurReadOnly != null) {
                txtFournisseurReadOnly.setText("");
            }
            modelLignes.setRowCount(0);
        }
    }

    private void chargerProduitsCommande(com.longrich.smartgestion.entity.CommandeFournisseur commande) {
        modelLignes.setRowCount(0);
        
        // Charger les lignes de commande avec les quantités
        try {
            List<com.longrich.smartgestion.entity.LigneCommandeFournisseur> lignes = 
                commande.getLignes();
            
            for (com.longrich.smartgestion.entity.LigneCommandeFournisseur ligne : lignes) {
                int qteCommandee = ligne.getQuantiteCommandee();
                int qteLivree = ligne.getQuantiteLivree() != null ? ligne.getQuantiteLivree() : 0;
                int qteRestante = qteCommandee - qteLivree;
                
                // Afficher seulement les produits partiellement ou non livrés
                if (qteRestante > 0) {
                    Object[] row = {
                        ligne.getProduit().getLibelle(),           // Produit
                        qteCommandee,                              // Qté Commandée
                        qteLivree,                                 // Qté Déjà Livrée
                        qteRestante,                               // Qté Restante
                        0,                                         // Qté Reçue (saisir)
                        ligne.getPrixUnitaire(),                  // Prix Unit.
                        BigDecimal.ZERO,                          // Montant (calculé)
                        "Saisir",                                  // Actions
                        ligne.getProduit().getId()                // ProduitId (masqué)
                    };
                    modelLignes.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des produits: " + e.getMessage());
        }
    }

    private void choisirFichier(JTextField txtFichier) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner un fichier de référence");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Fichiers de référence (PDF, JPG, PNG)", "pdf", "jpg", "jpeg", "png"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFichier.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
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
