package com.longrich.smartgestion.ui.panels;

import com.longrich.smartgestion.dto.TransfertStockDTO;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.entity.Stock;
import com.longrich.smartgestion.entity.SortieStock;
import com.longrich.smartgestion.enums.TypeEmplacement;
import com.longrich.smartgestion.service.TransfertStockService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.service.StockService;
import com.longrich.smartgestion.service.SortieStockService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TransfertStockPanel extends JPanel {

    private final TransfertStockService transfertStockService;
    private final ProduitService produitService;
    private final StockService stockService;
    private final SortieStockService sortieStockService;

    // Composants UI
    private JTable tableTransferts;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboDirection;
    private JTextArea txtObservation;
    private JButton btnRecommandations;

    // Table des lignes de transfert
    private JTable tableLignes;
    private DefaultTableModel modelLignes;
    private JComboBox<Produit> comboProduit;
    private JSpinner spinnerQuantite;
    private JLabel lblStockOrigine;
    private JLabel lblStockDestination;

    public TransfertStockPanel(TransfertStockService transfertStockService,
                              ProduitService produitService,
                              StockService stockService,
                              SortieStockService sortieStockService) {
        this.transfertStockService = transfertStockService;
        this.produitService = produitService;
        this.stockService = stockService;
        this.sortieStockService = sortieStockService;

        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {
        // Table principale des transferts
        String[] colonnes = {"N° Transfert", "Date", "Direction", "Nb Produits", "Observation", "Actions"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seule la colonne Actions est éditable
            }
        };
        tableTransferts = new JTable(tableModel);

        // Formulaire de saisie
        comboDirection = new JComboBox<>();
        comboDirection.addItem("Magasin → Surface de Vente");
        comboDirection.addItem("Surface de Vente → Magasin");
        comboDirection.addActionListener(e -> onDirectionChanged());
        
        txtObservation = new JTextArea(3, 20);
        txtObservation.setLineWrap(true);
        
        btnRecommandations = new JButton("Recommandations Auto");
        btnRecommandations.addActionListener(e -> chargerRecommandations());

        // Table des lignes de transfert
        String[] colonnesLignes = {"Produit", "Stock Origine", "Stock Destination", "Quantité", "Observation", "Actions", "ProduitId"};
        modelLignes = new DefaultTableModel(colonnesLignes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4 || column == 5; // Quantité, Observation et Actions éditables
            }
        };
        tableLignes = new JTable(modelLignes);
        
        // Masquer la colonne ProduitId
        if (tableLignes.getColumnModel().getColumnCount() > 6) {
            tableLignes.getColumnModel().getColumn(6).setMinWidth(0);
            tableLignes.getColumnModel().getColumn(6).setMaxWidth(0);
            tableLignes.getColumnModel().getColumn(6).setWidth(0);
        }

        // Configuration des tailles de colonnes
        tableLignes.getColumnModel().getColumn(0).setPreferredWidth(200); // Produit
        tableLignes.getColumnModel().getColumn(1).setPreferredWidth(100); // Stock Origine
        tableLignes.getColumnModel().getColumn(2).setPreferredWidth(120); // Stock Destination
        tableLignes.getColumnModel().getColumn(3).setPreferredWidth(80);  // Quantité
        tableLignes.getColumnModel().getColumn(4).setPreferredWidth(150); // Observation
        tableLignes.getColumnModel().getColumn(5).setPreferredWidth(80);  // Actions

        // Composants pour ajouter des lignes
        comboProduit = new JComboBox<>();
        comboProduit.addActionListener(e -> onProduitChanged());
        spinnerQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        lblStockOrigine = new JLabel("Origine: 0");
        lblStockOrigine.setForeground(Color.BLUE);
        lblStockDestination = new JLabel("Destination: 0");
        lblStockDestination.setForeground(Color.GREEN);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Onglet Liste des transferts
        JPanel panelListe = createListPanel();
        tabbedPane.addTab("Historique des Transferts", panelListe);

        // Onglet Nouveau transfert
        JPanel panelForm = createFormPanel();
        tabbedPane.addTab("Nouveau Transfert", panelForm);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de filtres
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><i>Historique des transferts entre magasin et surface de vente</i></html>");
        filterPanel.add(infoLabel);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Table avec scroll
        JScrollPane scrollTable = new JScrollPane(tableTransferts);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Boutons d'actions
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnNouveau = new JButton("Nouveau Transfert");
        JButton btnVoir = new JButton("Voir Détails");
        JButton btnRapport = new JButton("Rapport Mensuel");

        btnNouveau.addActionListener(e -> nouveauTransfert());
        btnVoir.addActionListener(e -> voirDetails());
        btnRapport.addActionListener(e -> genererRapport());

        buttonPanel.add(btnNouveau);
        buttonPanel.add(btnVoir);
        buttonPanel.add(btnRapport);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulaire principal
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Direction du transfert (obligatoire)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel labelDirection = new JLabel("Direction *:");
        labelDirection.setFont(labelDirection.getFont().deriveFont(Font.BOLD));
        formPanel.add(labelDirection, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 2;
        formPanel.add(comboDirection, gbc);

        // Bouton recommandations
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(btnRecommandations, gbc);

        // Observation
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Observation:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(txtObservation), gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Section lignes de transfert
        JPanel lignesPanel = createLignesPanel();
        panel.add(lignesPanel, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnExecuter = new JButton("Exécuter Transfert");
        JButton btnCancel = new JButton("Annuler");

        btnExecuter.addActionListener(e -> executerTransfert());
        btnCancel.addActionListener(e -> annulerSaisie());

        // Style du bouton principal
        btnExecuter.setFont(btnExecuter.getFont().deriveFont(Font.BOLD));
        btnExecuter.setBackground(new Color(46, 125, 50));
        btnExecuter.setForeground(Color.WHITE);

        buttonPanel.add(btnExecuter);
        buttonPanel.add(btnCancel);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Produits à transférer"));

        // Formulaire d'ajout de ligne
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(new JLabel("Produit:"), gbc);
        gbc.gridx = 1;
        addPanel.add(comboProduit, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addPanel.add(lblStockOrigine, gbc);
        gbc.gridx = 1;
        addPanel.add(lblStockDestination, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        addPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 3;
        addPanel.add(spinnerQuantite, gbc);

        JButton btnAdd = new JButton("Ajouter");
        btnAdd.addActionListener(e -> ajouterLigneTransfert());
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        addPanel.add(btnAdd, gbc);

        panel.add(addPanel, BorderLayout.NORTH);

        // Table des lignes
        JScrollPane scrollLignes = new JScrollPane(tableLignes);
        panel.add(scrollLignes, BorderLayout.CENTER);

        // Panneau de résumé
        JPanel resumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel lblResume = new JLabel("Total produits: 0 | Valeur estimée: 0 €");
        lblResume.setFont(lblResume.getFont().deriveFont(Font.BOLD));
        resumePanel.add(lblResume);
        panel.add(resumePanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadData() {
        // Charger les produits initiaux
        onDirectionChanged();
        
        // Charger l'historique des transferts
        chargerTransferts();
    }

    private void chargerTransferts() {
        tableModel.setRowCount(0);
        
        try {
            List<SortieStock> transferts = sortieStockService.findByTypeSortie(
                com.longrich.smartgestion.enums.TypeSortieStock.TRANSFERT);
            
            for (SortieStock transfert : transferts) {
                String direction = transfert.getEmplacementOrigine() + " → " + 
                                 (transfert.getEmplacementDestination() != null ? 
                                  transfert.getEmplacementDestination() : "?");
                
                Object[] row = {
                    transfert.getNumeroSortie(),
                    transfert.getDateSortie().toLocalDate(),
                    direction,
                    transfert.getTotalQuantite(),
                    transfert.getObservation() != null ? 
                        (transfert.getObservation().length() > 30 ? 
                         transfert.getObservation().substring(0, 30) + "..." : 
                         transfert.getObservation()) : "",
                    "Voir"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            log.error("Erreur lors du chargement des transferts", e);
        }
    }

    private void onDirectionChanged() {
        String direction = (String) comboDirection.getSelectedItem();
        boolean versSurfaceVente = direction != null && direction.contains("Surface de Vente");
        
        // Charger les produits disponibles dans l'emplacement source
        chargerProduitsSource(versSurfaceVente);
        
        modelLignes.setRowCount(0); // Vider la table des lignes
        onProduitChanged(); // Mettre à jour les stocks affichés
    }

    private void chargerProduitsSource(boolean versSurfaceVente) {
        comboProduit.removeAllItems();
        
        TypeEmplacement sourceType = versSurfaceVente ? 
            TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
        
        try {
            List<Stock> stocks = transfertStockService.getStocksDisponiblesPourTransfert(
                versSurfaceVente ? "MAGASIN" : "SURFACE_VENTE");
            
            for (Stock stock : stocks) {
                comboProduit.addItem(stock.getProduit());
            }
        } catch (Exception e) {
            log.error("Erreur lors du chargement des produits source", e);
        }
    }

    private void onProduitChanged() {
        Produit produit = (Produit) comboProduit.getSelectedItem();
        String direction = (String) comboDirection.getSelectedItem();
        
        if (produit != null && direction != null) {
            boolean versSurfaceVente = direction.contains("Surface de Vente");
            
            try {
                // Stock origine
                TypeEmplacement sourceType = versSurfaceVente ? 
                    TypeEmplacement.MAGASIN : TypeEmplacement.SURFACE_VENTE;
                Stock stockOrigine = stockService.findByProduitAndTypeStock(produit, sourceType);
                
                int stockOrigineQte = 0;
                if (stockOrigine != null) {
                    stockOrigineQte = stockOrigine.getQuantite() - stockOrigine.getQuantiteReservee();
                }
                lblStockOrigine.setText("Origine: " + stockOrigineQte);
                
                // Stock destination
                TypeEmplacement destType = versSurfaceVente ? 
                    TypeEmplacement.SURFACE_VENTE : TypeEmplacement.MAGASIN;
                Stock stockDest = stockService.findByProduitAndTypeStock(produit, destType);
                
                int stockDestQte = stockDest != null ? stockDest.getQuantite() : 0;
                lblStockDestination.setText("Destination: " + stockDestQte);
                
                // Mettre à jour les limites du spinner
                SpinnerNumberModel model = (SpinnerNumberModel) spinnerQuantite.getModel();
                model.setMaximum(Math.max(1, stockOrigineQte));
                
            } catch (Exception e) {
                lblStockOrigine.setText("Origine: Erreur");
                lblStockDestination.setText("Destination: Erreur");
                log.error("Erreur lors de la récupération des stocks", e);
            }
        } else {
            lblStockOrigine.setText("Origine: 0");
            lblStockDestination.setText("Destination: 0");
        }
    }

    private void ajouterLigneTransfert() {
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
            
            Object[] row = {
                produit.getLibelle(),
                lblStockOrigine.getText(),
                lblStockDestination.getText(),
                quantite,
                "", // Observation vide par défaut
                "Supprimer",
                produit.getId()
            };
            modelLignes.addRow(row);

            // Réinitialiser les champs
            comboProduit.setSelectedIndex(0);
            spinnerQuantite.setValue(1);
            onProduitChanged();
            
            // Mettre à jour le résumé
            mettreAJourResume();
        }
    }

    private void chargerRecommandations() {
        try {
            List<TransfertStockDTO.LigneTransfert> recommandations = 
                transfertStockService.calculerTransfertRecommande();
            
            if (recommandations.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Aucune recommandation de transfert pour le moment.\n" +
                    "Les stocks semblent équilibrés.", 
                    "Recommandations", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Forcer direction magasin → surface de vente pour les recommandations
            comboDirection.setSelectedItem("Magasin → Surface de Vente");
            onDirectionChanged();
            
            // Vider la table actuelle
            modelLignes.setRowCount(0);
            
            // Ajouter les recommandations
            for (TransfertStockDTO.LigneTransfert recommandation : recommandations) {
                Produit produit = produitService.findById(recommandation.getProduitId());
                if (produit != null) {
                    // Calculer les stocks actuels
                    Stock stockMagasin = stockService.findByProduitAndTypeStock(produit, TypeEmplacement.MAGASIN);
                    Stock stockSurface = stockService.findByProduitAndTypeStock(produit, TypeEmplacement.SURFACE_VENTE);
                    
                    String stockOrigine = "Origine: " + (stockMagasin != null ? 
                        (stockMagasin.getQuantite() - stockMagasin.getQuantiteReservee()) : 0);
                    String stockDest = "Destination: " + (stockSurface != null ? 
                        stockSurface.getQuantite() : 0);
                    
                    Object[] row = {
                        produit.getLibelle(),
                        stockOrigine,
                        stockDest,
                        recommandation.getQuantite(),
                        recommandation.getObservation(),
                        "Supprimer",
                        produit.getId()
                    };
                    modelLignes.addRow(row);
                }
            }
            
            mettreAJourResume();
            
            JOptionPane.showMessageDialog(this, 
                recommandations.size() + " recommandations chargées.\n" +
                "Vous pouvez modifier les quantités avant d'exécuter le transfert.", 
                "Recommandations chargées", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des recommandations: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executerTransfert() {
        try {
            if (modelLignes.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Veuillez ajouter au moins un produit à transférer.", 
                    "Aucun produit", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String direction = (String) comboDirection.getSelectedItem();
            boolean versSurfaceVente = direction != null && direction.contains("Surface de Vente");
            
            String emplacementOrigine = versSurfaceVente ? "MAGASIN" : "SURFACE_VENTE";
            String emplacementDestination = versSurfaceVente ? "SURFACE_VENTE" : "MAGASIN";

            // Préparer le DTO
            TransfertStockDTO dto = new TransfertStockDTO();
            dto.setObservation(txtObservation.getText());
            
            List<TransfertStockDTO.LigneTransfert> lignes = new ArrayList<>();
            for (int i = 0; i < modelLignes.getRowCount(); i++) {
                TransfertStockDTO.LigneTransfert ligne = new TransfertStockDTO.LigneTransfert();
                
                Object produitIdVal = modelLignes.getValueAt(i, 6);
                Object quantiteVal = modelLignes.getValueAt(i, 3);
                Object observationVal = modelLignes.getValueAt(i, 4);
                
                ligne.setProduitId(Long.valueOf(produitIdVal.toString()));
                ligne.setQuantite(Integer.valueOf(quantiteVal.toString()));
                ligne.setObservation(observationVal != null ? observationVal.toString() : "");
                
                lignes.add(ligne);
            }
            dto.setLignesTransfert(lignes);

            // Exécuter le transfert
            SortieStock transfert = transfertStockService.effectuerTransfert(
                emplacementOrigine, emplacementDestination, dto);
            
            JOptionPane.showMessageDialog(this, 
                "Transfert exécuté avec succès!\n" +
                "Numéro de transfert: " + transfert.getNumeroSortie(),
                "Transfert réussi", 
                JOptionPane.INFORMATION_MESSAGE);
            
            chargerTransferts();
            annulerSaisie();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'exécution du transfert: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mettreAJourResume() {
        int totalProduits = modelLignes.getRowCount();
        int totalQuantite = 0;
        
        for (int i = 0; i < modelLignes.getRowCount(); i++) {
            Object qte = modelLignes.getValueAt(i, 3);
            totalQuantite += Integer.parseInt(qte.toString());
        }
        
        final int finalTotalProduits = totalProduits;
        final int finalTotalQuantite = totalQuantite;
        
        // Trouver le label de résumé et le mettre à jour
        // Cette méthode sera appelée après la création de l'interface
        SwingUtilities.invokeLater(() -> {
            Component[] components = getRootPane().getComponents();
            updateResumeLabel(components, finalTotalProduits, finalTotalQuantite);
        });
    }

    private void updateResumeLabel(Component[] components, int totalProduits, int totalQuantite) {
        for (Component comp : components) {
            if (comp instanceof JLabel && ((JLabel) comp).getText().startsWith("Total produits:")) {
                ((JLabel) comp).setText("Total produits: " + totalProduits + " | Quantité totale: " + totalQuantite);
                return;
            }
            if (comp instanceof Container) {
                updateResumeLabel(((Container) comp).getComponents(), totalProduits, totalQuantite);
            }
        }
    }

    private void annulerSaisie() {
        comboDirection.setSelectedIndex(0);
        txtObservation.setText("");
        modelLignes.setRowCount(0);
        onDirectionChanged();
    }

    private void nouveauTransfert() {
        annulerSaisie();
    }

    private void voirDetails() {
        int selectedRow = tableTransferts.getSelectedRow();
        if (selectedRow >= 0) {
            JOptionPane.showMessageDialog(this, "Fonctionnalité de détails en cours de développement.");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un transfert à consulter.");
        }
    }

    private void genererRapport() {
        JOptionPane.showMessageDialog(this, 
            "Rapport mensuel des transferts en cours de développement.\n" +
            "Cette fonctionnalité permettra de visualiser :\n" +
            "- Volume des transferts par direction\n" +
            "- Produits les plus transférés\n" +
            "- Évolution des stocks par emplacement", 
            "Rapport en développement", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}