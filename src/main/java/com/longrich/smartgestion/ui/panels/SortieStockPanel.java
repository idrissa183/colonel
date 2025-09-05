package com.longrich.smartgestion.ui.panels;

import com.longrich.smartgestion.dto.SortieStockDTO;
import com.longrich.smartgestion.dto.LigneSortieStockDTO;
import com.longrich.smartgestion.entity.SortieStock;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.entity.Stock;
import com.longrich.smartgestion.enums.TypeSortieStock;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.service.SortieStockService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.service.StockService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class SortieStockPanel extends JPanel {

    private final SortieStockService sortieStockService;
    private final ProduitService produitService;
    private final StockService stockService;

    // Composants UI
    private JTable tableSorties;
    private DefaultTableModel tableModel;
    private JComboBox<TypeSortieStock> comboTypeSortie;
    private JComboBox<String> comboEmplacementOrigine;
    private JSpinner spinnerDateSortie;
    private JTextArea txtObservation;
    private JTextField txtReference;

    // Table des lignes de sortie
    private JTable tableLignes;
    private DefaultTableModel modelLignes;
    private JComboBox<Produit> comboProduit;
    private JSpinner spinnerQuantite;
    private JLabel lblStockDisponible;

    private SortieStock sortieStockCourante;

    public SortieStockPanel(SortieStockService sortieStockService,
                           ProduitService produitService,
                           StockService stockService) {
        this.sortieStockService = sortieStockService;
        this.produitService = produitService;
        this.stockService = stockService;

        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {
        // Table principale des sorties
        String[] colonnes = {"N° Sortie", "Date", "Type", "Emplacement", "Montant", "Actions"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seule la colonne Actions est éditable
            }
        };
        tableSorties = new JTable(tableModel);

        // Formulaire de saisie
        comboTypeSortie = new JComboBox<>(TypeSortieStock.values());
        comboTypeSortie.addActionListener(e -> onTypeSortieChanged());
        
        comboEmplacementOrigine = new JComboBox<>();
        comboEmplacementOrigine.addItem("MAGASIN");
        comboEmplacementOrigine.addItem("SURFACE_VENTE");
        comboEmplacementOrigine.addActionListener(e -> onEmplacementChanged());
        
        spinnerDateSortie = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDateSortie, "dd/MM/yyyy HH:mm");
        spinnerDateSortie.setEditor(dateEditor);
        spinnerDateSortie.setValue(new Date());
        
        txtObservation = new JTextArea(3, 20);
        txtObservation.setLineWrap(true);
        txtReference = new JTextField(20);

        // Table des lignes de sortie
        String[] colonnesLignes = {"Produit", "Stock Disponible", "Quantité", "Prix Unit.", "Montant", "Actions", "ProduitId"};
        modelLignes = new DefaultTableModel(colonnesLignes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 5; // Quantité et Actions éditables
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
        comboProduit.addActionListener(e -> onProduitChanged());
        spinnerQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        lblStockDisponible = new JLabel("Stock: 0");
        lblStockDisponible.setForeground(Color.BLUE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Onglet Liste des sorties
        JPanel panelListe = createListPanel();
        tabbedPane.addTab("Liste des Sorties", panelListe);

        // Onglet Nouvelle sortie
        JPanel panelForm = createFormPanel();
        tabbedPane.addTab("Nouvelle Sortie", panelForm);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de filtres
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<TypeSortieStock> filterType = new JComboBox<>();
        filterType.addItem(null); // Option "Tous"
        for (TypeSortieStock type : TypeSortieStock.values()) {
            filterType.addItem(type);
        }
        
        JButton btnFilter = new JButton("Filtrer");
        btnFilter.addActionListener(e -> filtrerSorties((TypeSortieStock) filterType.getSelectedItem()));

        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(filterType);
        filterPanel.add(btnFilter);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Table avec scroll
        JScrollPane scrollTable = new JScrollPane(tableSorties);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Boutons d'actions
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnNouvelle = new JButton("Nouvelle Sortie");
        JButton btnVoir = new JButton("Voir Détails");
        JButton btnSupprimer = new JButton("Supprimer");

        btnNouvelle.addActionListener(e -> nouvelleSortie());
        btnVoir.addActionListener(e -> voirDetails());
        btnSupprimer.addActionListener(e -> supprimerSortie());

        buttonPanel.add(btnNouvelle);
        buttonPanel.add(btnVoir);
        buttonPanel.add(btnSupprimer);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulaire principal
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Type de sortie (obligatoire)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel labelType = new JLabel("Type de Sortie *:");
        labelType.setFont(labelType.getFont().deriveFont(Font.BOLD));
        formPanel.add(labelType, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(comboTypeSortie, gbc);

        // Emplacement d'origine (obligatoire)
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel labelEmplacement = new JLabel("Emplacement d'origine *:");
        labelEmplacement.setFont(labelEmplacement.getFont().deriveFont(Font.BOLD));
        formPanel.add(labelEmplacement, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(comboEmplacementOrigine, gbc);

        // Date de sortie
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date de sortie:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(spinnerDateSortie, gbc);

        // Référence document
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Référence:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtReference, gbc);

        // Observation
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Observation:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(txtObservation), gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Section lignes de sortie
        JPanel lignesPanel = createLignesPanel();
        panel.add(lignesPanel, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSave = new JButton("Enregistrer Sortie");
        JButton btnCancel = new JButton("Annuler");

        btnSave.addActionListener(e -> sauvegarderSortie());
        btnCancel.addActionListener(e -> annulerSaisie());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Produits à sortir"));

        // Formulaire d'ajout de ligne
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(new JLabel("Produit:"), gbc);
        gbc.gridx = 1;
        addPanel.add(comboProduit, gbc);

        gbc.gridx = 2;
        addPanel.add(lblStockDisponible, gbc);

        gbc.gridx = 3;
        addPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 4;
        addPanel.add(spinnerQuantite, gbc);

        JButton btnAdd = new JButton("Ajouter Produit");
        btnAdd.addActionListener(e -> ajouterLigneProduit());
        gbc.gridx = 5;
        addPanel.add(btnAdd, gbc);

        panel.add(addPanel, BorderLayout.NORTH);

        // Table des lignes
        JScrollPane scrollLignes = new JScrollPane(tableLignes);
        panel.add(scrollLignes, BorderLayout.CENTER);

        return panel;
    }

    private void loadData() {
        // Charger les produits
        List<Produit> produits = produitService.findAll();
        comboProduit.removeAllItems();
        for (Produit produit : produits) {
            comboProduit.addItem(produit);
        }

        // Charger les sorties de stock
        chargerSorties();
        
        // Initialiser les contrôles
        onTypeSortieChanged();
        onEmplacementChanged();
    }

    private void chargerSorties() {
        tableModel.setRowCount(0);
        List<SortieStock> sorties = sortieStockService.findAll();
        
        for (SortieStock sortie : sorties) {
            Object[] row = {
                sortie.getNumeroSortie(),
                sortie.getDateSortie().toLocalDate(),
                sortie.getTypeSortie(),
                sortie.getEmplacementOrigine(),
                sortie.getMontantTotal() != null ? sortie.getMontantTotal() : BigDecimal.ZERO,
                "Voir"
            };
            tableModel.addRow(row);
        }
    }

    private void onTypeSortieChanged() {
        TypeSortieStock type = (TypeSortieStock) comboTypeSortie.getSelectedItem();
        
        if (type == TypeSortieStock.VENTE) {
            // Les ventes ne peuvent se faire que depuis la surface de vente
            comboEmplacementOrigine.removeAllItems();
            comboEmplacementOrigine.addItem("SURFACE_VENTE");
            comboEmplacementOrigine.setEnabled(false);
        } else {
            // Autres types : tous les emplacements
            comboEmplacementOrigine.removeAllItems();
            comboEmplacementOrigine.addItem("MAGASIN");
            comboEmplacementOrigine.addItem("SURFACE_VENTE");
            comboEmplacementOrigine.setEnabled(true);
        }
        
        onEmplacementChanged();
    }

    private void onEmplacementChanged() {
        // Recharger les produits selon l'emplacement sélectionné
        String emplacement = (String) comboEmplacementOrigine.getSelectedItem();
        if (emplacement != null) {
            chargerProduitsParEmplacement(emplacement);
        }
        
        modelLignes.setRowCount(0); // Vider la table des lignes
    }

    private void chargerProduitsParEmplacement(String emplacement) {
        comboProduit.removeAllItems();
        
        TypeEmplacement typeEmplacement = "MAGASIN".equals(emplacement) ? 
            TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
        
        try {
            List<Stock> stocks = stockService.getStocksByTypeStock(typeEmplacement);
            
            for (Stock stock : stocks) {
                if (stock.getQuantite() > stock.getQuantiteReservee()) {
                    comboProduit.addItem(stock.getProduit());
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors du chargement des produits", e);
        }
        
        onProduitChanged();
    }

    private void onProduitChanged() {
        Produit produit = (Produit) comboProduit.getSelectedItem();
        String emplacement = (String) comboEmplacementOrigine.getSelectedItem();
        
        if (produit != null && emplacement != null) {
            try {
                TypeEmplacement typeEmplacement = "MAGASIN".equals(emplacement) ? 
                    TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
                
                Stock stock = stockService.getStockByProduitAndType(produit, typeEmplacement);
                if (stock != null) {
                    int disponible = stock.getQuantite() - stock.getQuantiteReservee();
                    lblStockDisponible.setText("Stock: " + disponible);
                    
                    // Mettre à jour les limites du spinner
                    SpinnerNumberModel model = (SpinnerNumberModel) spinnerQuantite.getModel();
                    model.setMaximum(Math.max(1, disponible));
                } else {
                    lblStockDisponible.setText("Stock: 0");
                    SpinnerNumberModel model = (SpinnerNumberModel) spinnerQuantite.getModel();
                    model.setMaximum(0);
                }
            } catch (Exception e) {
                lblStockDisponible.setText("Erreur");
                log.error("Erreur lors de la récupération du stock", e);
            }
        } else {
            lblStockDisponible.setText("Stock: 0");
        }
    }

    private void ajouterLigneProduit() {
        Produit produit = (Produit) comboProduit.getSelectedItem();
        Integer quantite = (Integer) spinnerQuantite.getValue();
        
        if (produit != null && quantite > 0) {
            // Vérifier si le produit n'est pas déjà dans la liste
            for (int i = 0; i < modelLignes.getRowCount(); i++) {
                Long produitId = Long.valueOf(modelLignes.getValueAt(i, 6).toString());
                if (produitId.equals(produit.getId())) {
                    JOptionPane.showMessageDialog(this, 
                        "Ce produit est déjà dans la liste. Modifiez la quantité directement dans le tableau.",
                        "Produit déjà ajouté", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            BigDecimal prixUnitaire = produit.getPrixRevente() != null ? 
                produit.getPrixRevente() : BigDecimal.ZERO;
            BigDecimal montant = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
            
            Object[] row = {
                produit.getLibelle(),
                lblStockDisponible.getText(),
                quantite,
                prixUnitaire,
                montant,
                "Supprimer",
                produit.getId()
            };
            modelLignes.addRow(row);

            // Réinitialiser les champs
            comboProduit.setSelectedIndex(0);
            spinnerQuantite.setValue(1);
            onProduitChanged();
        }
    }

    private void sauvegarderSortie() {
        try {
            // Validation des champs obligatoires
            TypeSortieStock typeSortie = (TypeSortieStock) comboTypeSortie.getSelectedItem();
            String emplacementOrigine = (String) comboEmplacementOrigine.getSelectedItem();
            
            if (typeSortie == null) {
                JOptionPane.showMessageDialog(this, "Le type de sortie est obligatoire.", 
                    "Champ obligatoire", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (emplacementOrigine == null) {
                JOptionPane.showMessageDialog(this, "L'emplacement d'origine est obligatoire.", 
                    "Champ obligatoire", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (modelLignes.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Veuillez ajouter au moins un produit.", 
                    "Aucun produit", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SortieStockDTO dto = new SortieStockDTO();
            dto.setTypeSortie(typeSortie);
            dto.setEmplacementOrigine(emplacementOrigine);
            Date dateSortie = (Date) spinnerDateSortie.getValue();
            dto.setDateSortie(dateSortie.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            dto.setObservation(txtObservation.getText());
            dto.setReferenceDocument(txtReference.getText());

            // Collecter les lignes
            List<LigneSortieStockDTO> lignes = new ArrayList<>();
            for (int i = 0; i < modelLignes.getRowCount(); i++) {
                LigneSortieStockDTO ligne = new LigneSortieStockDTO();
                Object produitIdVal = modelLignes.getValueAt(i, 6);
                Object quantiteVal = modelLignes.getValueAt(i, 2);
                Object prixVal = modelLignes.getValueAt(i, 3);
                
                if (produitIdVal != null) {
                    ligne.setProduitId(Long.valueOf(produitIdVal.toString()));
                }
                ligne.setQuantite(Integer.valueOf(quantiteVal.toString()));
                ligne.setPrixUnitaire(new BigDecimal(prixVal.toString()));
                lignes.add(ligne);
            }
            dto.setLignesSortie(lignes);

            sortieStockService.creerSortieStock(dto);
            
            JOptionPane.showMessageDialog(this, "Sortie de stock enregistrée avec succès!");
            chargerSorties();
            annulerSaisie();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la sauvegarde: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void annulerSaisie() {
        comboTypeSortie.setSelectedIndex(0);
        comboEmplacementOrigine.setSelectedIndex(0);
        spinnerDateSortie.setValue(new Date());
        txtReference.setText("");
        txtObservation.setText("");
        modelLignes.setRowCount(0);
        
        onTypeSortieChanged(); // Réinitialiser les contraintes
    }

    private void nouvelleSortie() {
        sortieStockCourante = null;
        annulerSaisie();
    }

    private void voirDetails() {
        int selectedRow = tableSorties.getSelectedRow();
        if (selectedRow >= 0) {
            JOptionPane.showMessageDialog(this, "Fonctionnalité de détails en cours de développement.");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une sortie à consulter.");
        }
    }

    private void supprimerSortie() {
        int selectedRow = tableSorties.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer cette sortie?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Fonctionnalité de suppression en cours de développement.");
                chargerSorties();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une sortie à supprimer.");
        }
    }

    private void filtrerSorties(TypeSortieStock typeSortie) {
        if (typeSortie == null) {
            chargerSorties();
        } else {
            tableModel.setRowCount(0);
            List<SortieStock> sorties = sortieStockService.findByTypeSortie(typeSortie);
            
            for (SortieStock sortie : sorties) {
                Object[] row = {
                    sortie.getNumeroSortie(),
                    sortie.getDateSortie().toLocalDate(),
                    sortie.getTypeSortie(),
                    sortie.getEmplacementOrigine(),
                    sortie.getMontantTotal() != null ? sortie.getMontantTotal() : BigDecimal.ZERO,
                    "Voir"
                };
                tableModel.addRow(row);
            }
        }
    }
}