package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.*;
import com.longrich.smartgestion.enums.MotifSortie;
import com.longrich.smartgestion.service.StockService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.service.FournisseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Profile("!headless")
@Slf4j
public class ModernStockPanel extends JPanel {

    // Couleurs modernes cohérentes
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(99, 102, 241);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);

    private final StockService stockService;
    private final ProduitService produitService;
    private final FournisseurService fournisseurService;

    // Composants principaux
    private JTabbedPane tabbedPane;
    private JPanel statsPanel;
    
    // Données en cache
    private List<ProduitDto> produitsCache;
    private List<FournisseurDTO> fournisseursCache;
    
    // Onglet Vue d'ensemble
    private JTable vueEnsembleTable;
    private DefaultTableModel vueEnsembleTableModel;
    private JTextField vueEnsembleSearchField;
    private JComboBox<String> vueEnsembleFilterCombo;
    
    // Onglet Entrées
    private JComboBox<String> entreeProduitCombo;
    private JComboBox<String> entreeFournisseurCombo;
    private JTextField entreeQuantiteField;
    private JTextField entreePrixUnitaireField;
    private JTextField entreeDateField;
    private JTextArea entreeCommentaireArea;
    private JTable entreeHistoriqueTable;
    private DefaultTableModel entreeHistoriqueTableModel;
    
    // Onglet Sorties
    private JComboBox<String> sortieProduitCombo;
    private JLabel sortieStockDisponibleLabel;
    private JTextField sortieQuantiteField;
    private JComboBox<String> sortieMotifCombo;
    private JTextField sortieDateField;
    private JTextArea sortieCommentaireArea;
    private JTable sortieHistoriqueTable;
    private DefaultTableModel sortieHistoriqueTableModel;
    
    // Onglet Inventaire
    private JTable inventaireTable;
    private DefaultTableModel inventaireTableModel;

    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Chargement des données de base
        chargerDonneesDeBase();

        // Création des composants
        creerPanneauStatistiques();
        creerOnglets();

        // Chargement initial des données
        rafraichirToutesLesDonnees();
    }

    private void chargerDonneesDeBase() {
        try {
            produitsCache = produitService.getActiveProduits();
            fournisseursCache = fournisseurService.getAllFournisseurs();
        } catch (Exception e) {
            log.error("Erreur lors du chargement des données de base", e);
            produitsCache = List.of();
            fournisseursCache = List.of();
        }
    }

    private void creerPanneauStatistiques() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setPreferredSize(new Dimension(0, 120));
        
        add(statsPanel, BorderLayout.NORTH);
        
        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        try {
            StatistiquesStockDTO stats = stockService.getStatistiquesStock();
            statsPanel.removeAll();
            
            // Carte Total Produits
            statsPanel.add(creerCarteStatistique("📦", "Produits en Stock", 
                stats.getTotalProduitsEnStock().toString(), PRIMARY_COLOR));
            
            // Carte Valeur Totale
            statsPanel.add(creerCarteStatistique("💰", "Valeur Totale", 
                String.format("%.0f FCFA", stats.getValeurTotaleStock()), SUCCESS_COLOR));
            
            // Carte Alertes
            statsPanel.add(creerCarteStatistique("⚠️", "Alertes Stock", 
                stats.getTotalAlertes().toString(), WARNING_COLOR));
            
            // Carte Mouvements du jour
            statsPanel.add(creerCarteStatistique("📈", "Mouvements Aujourd'hui", 
                stats.getMouvementsAujourdhui().toString(), INFO_COLOR));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des statistiques", e);
        }
    }

    private JPanel creerCarteStatistique(String icone, String titre, String valeur, Color couleur) {
        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(CARD_COLOR);
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(15, 15, 15, 15)));

        // Icône
        JLabel iconeLabel = new JLabel(icone);
        iconeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Titre
        JLabel titreLabel = new JLabel(titre);
        titreLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titreLabel.setForeground(TEXT_SECONDARY);
        titreLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Valeur
        JLabel valeurLabel = new JLabel(valeur);
        valeurLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valeurLabel.setForeground(couleur);
        valeurLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        carte.add(iconeLabel);
        carte.add(Box.createVerticalStrut(5));
        carte.add(titreLabel);
        carte.add(Box.createVerticalStrut(5));
        carte.add(valeurLabel);

        return carte;
    }

    private void creerOnglets() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(CARD_COLOR);
        
        // Onglet Vue d'ensemble
        tabbedPane.addTab("🏠 Vue d'Ensemble", creerOngletVueEnsemble());
        
        // Onglet Entrées de Stock
        tabbedPane.addTab("📥 Entrées de Stock", creerOngletEntrees());
        
        // Onglet Sorties de Stock
        tabbedPane.addTab("📤 Sorties de Stock", creerOngletSorties());
        
        // Onglet Inventaire
        tabbedPane.addTab("📋 Inventaire", creerOngletInventaire());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel creerOngletVueEnsemble() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panneau de recherche
        JPanel recherchePanel = creerPanneauRechercheVueEnsemble();
        panel.add(recherchePanel, BorderLayout.NORTH);

        // Tableau
        creerTableauVueEnsemble();
        JScrollPane scrollPane = new JScrollPane(vueEnsembleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel creerPanneauRechercheVueEnsemble() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)));

        JLabel titreLabel = new JLabel("📊 Inventaire des Stocks");
        titreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titreLabel.setForeground(TEXT_PRIMARY);

        // Champ de recherche
        vueEnsembleSearchField = creerChampTexte("Rechercher un produit...");
        vueEnsembleSearchField.setPreferredSize(new Dimension(200, 35));

        // Bouton recherche
        JButton rechercheButton = creerBouton("🔍", PRIMARY_COLOR, e -> filtrerVueEnsemble());
        rechercheButton.setPreferredSize(new Dimension(40, 35));

        // Filtre par statut
        vueEnsembleFilterCombo = new JComboBox<>(new String[]{
            "Tous les statuts", "Stock normal", "Stock faible", "Rupture de stock"
        });
        stylerComboBox(vueEnsembleFilterCombo);
        vueEnsembleFilterCombo.setPreferredSize(new Dimension(150, 35));
        vueEnsembleFilterCombo.addActionListener(e -> filtrerVueEnsemble());

        panel.add(titreLabel);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(new JLabel("Recherche:"));
        panel.add(vueEnsembleSearchField);
        panel.add(rechercheButton);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Statut:"));
        panel.add(vueEnsembleFilterCombo);

        return panel;
    }

    private void creerTableauVueEnsemble() {
        String[] colonnes = {"Produit", "Famille", "Quantité", "Seuil d'Alerte", "Statut", "Valeur (FCFA)"};
        vueEnsembleTableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vueEnsembleTable = new JTable(vueEnsembleTableModel);
        stylerTableau(vueEnsembleTable);
    }

    private JPanel creerOngletEntrees() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panneau gauche - Formulaire
        JPanel formulairePanel = creerFormulaireEntrees();
        
        // Panneau droit - Historique
        JPanel historiquePanel = creerHistoriqueEntrees();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulairePanel, historiquePanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel creerFormulaireEntrees() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titre = new JLabel("📥 Entrée de Stock");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titre.setForeground(TEXT_PRIMARY);
        titre.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        // Formulaire
        entreeProduitCombo = creerComboBoxProduits();
        entreeFournisseurCombo = creerComboBoxFournisseurs();
        entreeQuantiteField = creerChampTexte("Quantité");
        entreePrixUnitaireField = creerChampTexte("Prix unitaire");
        entreeDateField = creerChampTexte("Date (yyyy-mm-dd)");
        entreeDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        entreeCommentaireArea = creerZoneTexte("Commentaire");

        // Boutons
        JPanel boutonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        boutonPanel.setBackground(CARD_COLOR);
        boutonPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        boutonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        
        JButton enregistrerButton = creerBouton("💾 Enregistrer", SUCCESS_COLOR, 
            e -> enregistrerEntreeStock());
        JButton viderButton = creerBouton("🗑️ Vider", TEXT_SECONDARY, 
            e -> viderFormulaireEntrees());

        boutonPanel.add(enregistrerButton);
        boutonPanel.add(viderButton);

        panel.add(titre);
        panel.add(Box.createVerticalStrut(20));
        panel.add(creerChampAvecLabel("Produit:", entreeProduitCombo));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Fournisseur:", entreeFournisseurCombo));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Quantité:", entreeQuantiteField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Prix unitaire:", entreePrixUnitaireField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Date:", entreeDateField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Commentaire:", entreeCommentaireArea));
        panel.add(Box.createVerticalStrut(20));
        panel.add(boutonPanel);

        return panel;
    }

    private JPanel creerHistoriqueEntrees() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titre = new JLabel("📈 Historique des Entrées");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titre.setForeground(TEXT_PRIMARY);

        // Tableau
        String[] colonnes = {"Date", "Produit", "Fournisseur", "Quantité", "Prix Total"};
        entreeHistoriqueTableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        entreeHistoriqueTable = new JTable(entreeHistoriqueTableModel);
        stylerTableau(entreeHistoriqueTable);

        JScrollPane scrollPane = new JScrollPane(entreeHistoriqueTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(titre, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel creerOngletSorties() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panneau gauche - Formulaire
        JPanel formulairePanel = creerFormulaireSorties();
        
        // Panneau droit - Historique
        JPanel historiquePanel = creerHistoriqueSorties();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulairePanel, historiquePanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel creerFormulaireSorties() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titre = new JLabel("📤 Sortie de Stock");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titre.setForeground(TEXT_PRIMARY);
        titre.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        // Formulaire
        sortieProduitCombo = creerComboBoxProduits();
        sortieProduitCombo.addActionListener(e -> mettreAJourStockDisponible());
        
        sortieStockDisponibleLabel = new JLabel("Sélectionnez d'abord un produit");
        sortieStockDisponibleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        sortieStockDisponibleLabel.setForeground(TEXT_SECONDARY);
        
        sortieQuantiteField = creerChampTexte("Quantité à sortir");
        
        // Motifs de sortie
        String[] motifs = new String[MotifSortie.values().length];
        for (int i = 0; i < MotifSortie.values().length; i++) {
            motifs[i] = MotifSortie.values()[i].getDisplayName();
        }
        sortieMotifCombo = new JComboBox<>(motifs);
        stylerComboBox(sortieMotifCombo);
        
        sortieDateField = creerChampTexte("Date (yyyy-mm-dd)");
        sortieDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        sortieCommentaireArea = creerZoneTexte("Commentaire");

        // Boutons
        JPanel boutonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        boutonPanel.setBackground(CARD_COLOR);
        boutonPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        boutonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        
        JButton enregistrerButton = creerBouton("📤 Sortie Stock", DANGER_COLOR, 
            e -> enregistrerSortieStock());
        JButton viderButton = creerBouton("🗑️ Vider", TEXT_SECONDARY, 
            e -> viderFormulaireSorties());

        boutonPanel.add(enregistrerButton);
        boutonPanel.add(viderButton);

        panel.add(titre);
        panel.add(Box.createVerticalStrut(20));
        panel.add(creerChampAvecLabel("Produit:", sortieProduitCombo));
        panel.add(Box.createVerticalStrut(5));
        panel.add(creerChampAvecLabel("Stock disponible:", sortieStockDisponibleLabel));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Quantité:", sortieQuantiteField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Motif:", sortieMotifCombo));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Date:", sortieDateField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(creerChampAvecLabel("Commentaire:", sortieCommentaireArea));
        panel.add(Box.createVerticalStrut(20));
        panel.add(boutonPanel);

        return panel;
    }

    private JPanel creerHistoriqueSorties() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titre = new JLabel("📉 Historique des Sorties");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titre.setForeground(TEXT_PRIMARY);

        // Tableau
        String[] colonnes = {"Date", "Produit", "Quantité", "Motif", "Commentaire"};
        sortieHistoriqueTableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sortieHistoriqueTable = new JTable(sortieHistoriqueTableModel);
        stylerTableau(sortieHistoriqueTable);

        JScrollPane scrollPane = new JScrollPane(sortieHistoriqueTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(titre, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel creerOngletInventaire() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panneau haut - Contrôles
        JPanel controlesPanel = creerControlesInventaire();
        panel.add(controlesPanel, BorderLayout.NORTH);

        // Panneau centre - Seulement le tableau
        JPanel tableauPanel = creerTableauInventaire();
        panel.add(tableauPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel creerControlesInventaire() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 20, 15, 20)));

        JLabel titre = new JLabel("📋 Gestion d'Inventaire");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titre.setForeground(TEXT_PRIMARY);

        JPanel boutonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutonPanel.setBackground(CARD_COLOR);

        JButton exportButton = creerBouton("📊 Export", SUCCESS_COLOR, e -> exporterInventaire());
        JButton actualiserButton = creerBouton("🔄 Actualiser", PRIMARY_COLOR, 
            e -> rafraichirInventaire());

        boutonPanel.add(exportButton);
        boutonPanel.add(actualiserButton);

        panel.add(titre, BorderLayout.WEST);
        panel.add(boutonPanel, BorderLayout.EAST);

        return panel;
    }


    private JPanel creerTableauInventaire() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titre = new JLabel("📊 Inventaire Détaillé");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titre.setForeground(TEXT_PRIMARY);

        // Tableau
        String[] colonnes = {"Produit", "Stock Actuel", "Stock Théorique", "Écart", "Valeur Stock", "Dernière MAJ"};
        inventaireTableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventaireTable = new JTable(inventaireTableModel);
        stylerTableau(inventaireTable);

        JScrollPane scrollPane = new JScrollPane(inventaireTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(titre, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // === MÉTHODES UTILITAIRES UI ===

    private JTextField creerChampTexte(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        if (placeholder != null) {
            field.setToolTipText(placeholder);
        }

        return field;
    }

    private JTextArea creerZoneTexte(String placeholder) {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(Color.WHITE);
        area.setForeground(TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        if (placeholder != null) {
            area.setToolTipText(placeholder);
        }

        // Note: Le JScrollPane sera ajouté au moment de l'insertion via creerChampAvecLabel
        return area;
    }

    private JComboBox<String> creerComboBoxProduits() {
        JComboBox<String> comboBox = new JComboBox<>();
        for (ProduitDto produit : produitsCache) {
            comboBox.addItem(produit.getLibelle() + " (ID: " + produit.getId() + ")");
        }
        stylerComboBox(comboBox);
        return comboBox;
    }

    private JComboBox<String> creerComboBoxFournisseurs() {
        JComboBox<String> comboBox = new JComboBox<>();
        for (FournisseurDTO fournisseur : fournisseursCache) {
            comboBox.addItem(fournisseur.getNom() + " " + fournisseur.getPrenom());
        }
        stylerComboBox(comboBox);
        return comboBox;
    }

    private void stylerComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        comboBox.setPreferredSize(new Dimension(0, 36));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JButton creerBouton(String texte, Color couleur, ActionListener action) {
        JButton bouton = new JButton(texte);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        bouton.setFocusPainted(false);
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.addActionListener(action);
        return bouton;
    }

    private JPanel creerChampAvecLabel(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        // Enveloppe du composant pour gérer l'erreur et l'espacement
        JComponent toAdd = component;
        if (component instanceof JTextArea) {
            // Envelopper les zones de texte dans un JScrollPane pour un rendu cohérent
            JScrollPane sp = new JScrollPane(component);
            sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 0, 0)));
            sp.setPreferredSize(new Dimension(0, 80));
            sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            toAdd = sp;
        }

        toAdd.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(toAdd);
        panel.add(Box.createVerticalStrut(10));

        // Contraindre la hauteur max pour une uniformité visuelle
        int maxH = (toAdd instanceof JScrollPane) ? 120 : 70;
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxH));

        return panel;
    }

    private void stylerTableau(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(59, 130, 246, 20));
        table.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);

        // Renderer avec alternance de couleurs
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }

                setBorder(new EmptyBorder(8, 12, 8, 12));
                return comp;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    // === MÉTHODES D'ACTION ===

    private void filtrerVueEnsemble() {
        // TODO: Implémenter le filtrage
        afficherMessage("Filtrage en cours de développement", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void enregistrerEntreeStock() {
        try {
            // Validation des champs
            if (entreeProduitCombo.getSelectedIndex() == -1) {
                afficherMessage("Veuillez sélectionner un produit", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String quantiteText = entreeQuantiteField.getText().trim();
            if (quantiteText.isEmpty()) {
                afficherMessage("Veuillez saisir une quantité", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantite = Integer.parseInt(quantiteText);
            if (quantite <= 0) {
                afficherMessage("La quantité doit être positive", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Récupération des données
            ProduitDto produitSelectionne = produitsCache.get(entreeProduitCombo.getSelectedIndex());

            ApprovisionnementDTO dto = ApprovisionnementDTO.builder()
                .produitId(produitSelectionne.getId())
                .quantite(quantite)
                .commentaire(entreeCommentaireArea.getText().trim())
                .dateApprovisionnement(LocalDate.parse(entreeDateField.getText().trim()))
                .build();

            // Si prix unitaire renseigné
            String prixText = entreePrixUnitaireField.getText().trim();
            if (!prixText.isEmpty()) {
                dto.setPrixUnitaire(new java.math.BigDecimal(prixText));
            }

            // Enregistrement
            stockService.creerEntreeStock(dto);
            
            afficherMessage("Entrée de stock enregistrée avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
            viderFormulaireEntrees();
            rafraichirToutesLesDonnees();

        } catch (NumberFormatException e) {
            afficherMessage("Veuillez saisir des valeurs numériques valides", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'entrée", e);
            afficherMessage("Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viderFormulaireEntrees() {
        entreeProduitCombo.setSelectedIndex(-1);
        entreeFournisseurCombo.setSelectedIndex(-1);
        entreeQuantiteField.setText("");
        entreePrixUnitaireField.setText("");
        entreeDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        entreeCommentaireArea.setText("");
    }

    private void enregistrerSortieStock() {
        try {
            // Validation des champs
            if (sortieProduitCombo.getSelectedIndex() == -1) {
                afficherMessage("Veuillez sélectionner un produit", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String quantiteText = sortieQuantiteField.getText().trim();
            if (quantiteText.isEmpty()) {
                afficherMessage("Veuillez saisir une quantité", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantite = Integer.parseInt(quantiteText);
            if (quantite <= 0) {
                afficherMessage("La quantité doit être positive", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Récupération des données
            ProduitDto produitSelectionne = produitsCache.get(sortieProduitCombo.getSelectedIndex());
            MotifSortie motif = MotifSortie.values()[sortieMotifCombo.getSelectedIndex()];

            // Enregistrement
            stockService.creerSortieStock(produitSelectionne.getId(), quantite, motif,
                sortieCommentaireArea.getText().trim());

            afficherMessage("Sortie de stock enregistrée avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
            viderFormulaireSorties();
            rafraichirToutesLesDonnees();

        } catch (NumberFormatException e) {
            afficherMessage("Veuillez saisir des valeurs numériques valides", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de la sortie", e);
            afficherMessage("Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viderFormulaireSorties() {
        sortieProduitCombo.setSelectedIndex(-1);
        sortieStockDisponibleLabel.setText("Sélectionnez d'abord un produit");
        sortieQuantiteField.setText("");
        sortieMotifCombo.setSelectedIndex(0);
        sortieDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        sortieCommentaireArea.setText("");
    }

    private void mettreAJourStockDisponible() {
        if (sortieProduitCombo.getSelectedIndex() >= 0) {
            ProduitDto produit = produitsCache.get(sortieProduitCombo.getSelectedIndex());
            Integer stock = produit.getQuantiteStock();
            sortieStockDisponibleLabel.setText(stock != null ? stock + " unités" : "0 unités");
        }
    }


    private void exporterInventaire() {
        afficherMessage("Export d'inventaire en cours de développement", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rafraichirInventaire() {
        // TODO: Implémenter le rafraîchissement de l'inventaire
        afficherMessage("Inventaire actualisé", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rafraichirToutesLesDonnees() {
        mettreAJourStatistiques();
        // TODO: Rafraîchir tous les tableaux
    }

    private void afficherMessage(String message, String titre, int type) {
        JOptionPane.showMessageDialog(this, message, titre, type);
    }
}
