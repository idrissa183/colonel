package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.dto.CommandeFournisseurDTO;
import com.longrich.smartgestion.dto.FournisseurDTO;
import com.longrich.smartgestion.dto.LigneCommandeFournisseurDTO;
import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.entity.CommandeFournisseur;
import com.longrich.smartgestion.entity.LigneCommandeFournisseur;
import com.longrich.smartgestion.enums.StatutCommande;
import com.longrich.smartgestion.service.CommandeFournisseurService;
import com.longrich.smartgestion.service.FournisseurService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;
import com.longrich.smartgestion.ui.components.ModernDatePicker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class CommandePanel extends JPanel {

    // Couleurs modernes cohérentes
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);

    private final FournisseurService fournisseurService;
    private final ProduitService produitService;
    private final CommandeFournisseurService commandeFournisseurService;

    // Composants UI pour les commandes
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JTable commandesTable;
    private DefaultTableModel commandesTableModel;
    private JLabel statsLabel;

    // Composants pour nouvelle commande avec validation
    private ComponentFactory.FieldPanel fournisseurFieldPanel;
    private JComboBox<String> fournisseurCombo;
    private ComponentFactory.FieldPanel dateFieldPanel;
    private ModernDatePicker dateCommandePicker;
    private ComponentFactory.FieldPanel dateLivraisonFieldPanel;
    private ModernDatePicker dateLivraisonPrevuePicker;
    private ComponentFactory.FieldPanel observationsFieldPanel;
    private JTextArea observationsArea;

    // Table des lignes de commande avec validation
    private JTable lignesTable;
    private DefaultTableModel lignesTableModel;
    private ComponentFactory.FieldPanel produitFieldPanel;
    private JComboBox<String> produitCombo;
    private ComponentFactory.FieldPanel quantiteFieldPanel;
    private JTextField quantiteField;
    private ComponentFactory.FieldPanel prixFieldPanel;
    private JTextField prixUnitaireField;
    private JLabel totalCommandeLabel;

    private List<FournisseurDTO> fournisseursList;
    private List<ProduitDto> produitsList;
    private List<LigneCommandeTemp> lignesCommande;
    private CommandeFournisseurDTO currentCommande;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lignesCommande = new ArrayList<>();

        createHeaderPanel();
        createMainContent();

        loadCommandes();
        loadFournisseurs();
        loadProduits();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Commandes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(TEXT_SECONDARY);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 0, 0));
        titlePanel.add(statsLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Boutons d'action rapide
        JPanel quickActionsPanel = createQuickActionsPanel();
        headerPanel.add(quickActionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(BACKGROUND_COLOR);

        // JButton newCommandeButton = ButtonFactory.createActionButton(
        // FontAwesomeSolid.PLUS, "Nouvelle commande", SUCCESS_COLOR, e ->
        // showNewCommandeDialog());
        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter", INFO_COLOR, e -> exportCommandes());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR, e -> refreshData());

        // panel.add(newCommandeButton);
        panel.add(exportButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(BACKGROUND_COLOR);

        // Onglet liste des commandes
        JPanel listePanel = createListeCommandesPanel();
        tabbedPane.addTab("📋 Liste des Commandes", listePanel);

        // Onglet nouvelle commande
        JPanel nouvellePanel = createNouvelleCommandePanel();
        tabbedPane.addTab("➕ Nouvelle Commande", nouvellePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createListeCommandesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Panneau de recherche et filtres
        JPanel searchContainer = createSearchPanel();
        panel.add(searchContainer, BorderLayout.NORTH);

        // Table des commandes
        JPanel tablePanel = createCommandesTable();
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = ComponentFactory.createCardPanel();
        searchPanel.setLayout(new BorderLayout());

        // Titre
        JLabel searchTitle = ComponentFactory.createSectionTitle("Liste des Commandes");

        // Panneau de filtres moderne
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        filtersPanel.setBackground(ComponentFactory.getCardColor());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;

        // Champ de recherche avec icône
        searchField = ComponentFactory.createStyledTextField("Rechercher par numéro, fournisseur...");
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.addActionListener(e -> searchCommandes());
        JPanel searchPanel_inner = ComponentFactory.createSearchField(searchField);
        searchPanel_inner.setPreferredSize(new Dimension(250, 38));

        // Filtre par statut stylisé
        String[] statutOptions = { "Tous les statuts", "En Attente", "Confirmée", "En Cours", "Livrée", "Annulée" };
        statusFilterCombo = ComponentFactory.createStyledComboBox(statutOptions);
        statusFilterCombo.setPreferredSize(new Dimension(150, 38));
        statusFilterCombo.addActionListener(e -> filterCommandes());

        // Bouton de recherche
        JButton searchButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SEARCH, "Rechercher", ComponentFactory.getPrimaryColor(), e -> searchCommandes());
        searchButton.setPreferredSize(new Dimension(120, 38));

        // Layout des filtres
        gbc.gridx = 0;
        filtersPanel.add(ComponentFactory.createLabel("Recherche:"), gbc);
        gbc.gridx = 1;
        filtersPanel.add(searchPanel_inner, gbc);
        gbc.gridx = 2;
        filtersPanel.add(ComponentFactory.createLabel("Statut:"), gbc);
        gbc.gridx = 3;
        filtersPanel.add(statusFilterCombo, gbc);
        gbc.gridx = 4;
        filtersPanel.add(searchButton, gbc);

        // Layout principal
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ComponentFactory.getCardColor());
        topPanel.add(searchTitle, BorderLayout.WEST);
        topPanel.add(filtersPanel, BorderLayout.EAST);

        searchPanel.add(topPanel, BorderLayout.NORTH);
        return searchPanel;
    }

    private JPanel createCommandesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Modèle de table
        String[] columns = { "N° Commande", "Fournisseur", "Date", "Statut", "Total", "Actions" };
        commandesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seule la colonne Actions est éditable
            }
        };

        commandesTable = new JTable(commandesTableModel);
        styleTable(commandesTable);

        // Renderer spécial pour la colonne Actions
        commandesTable.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        commandesTable.getColumn("Actions").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(commandesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createNouvelleCommandePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Panneau supérieur - Informations générales
        JPanel headerPanel = createCommandeHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panneau central - Lignes de commande
        JPanel centerPanel = createLignesCommandePanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panneau inférieur - Actions
        JPanel footerPanel = createCommandeFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createCommandeHeaderPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Titre
        JLabel titleLabel = ComponentFactory.createSectionTitle("Informations de la Commande");
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Première ligne - Fournisseur et Date de commande
        JPanel firstRow = new JPanel(new GridLayout(1, 2, 20, 0));
        firstRow.setBackground(ComponentFactory.getCardColor());
        firstRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        firstRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        firstRow.setPreferredSize(new Dimension(Integer.MAX_VALUE, 90));

        // Fournisseur avec validation
        fournisseurCombo = ComponentFactory.createStyledComboBox();
        fournisseurFieldPanel = ComponentFactory.createFieldPanel("Fournisseur", fournisseurCombo, true);
        firstRow.add(fournisseurFieldPanel);

        // Date de commande avec DatePicker moderne
        dateCommandePicker = new ModernDatePicker(LocalDate.now());
        dateFieldPanel = ComponentFactory.createFieldPanel("Date de commande", dateCommandePicker, true);
        firstRow.add(dateFieldPanel);

        panel.add(firstRow);
        panel.add(Box.createVerticalStrut(10));

        // Deuxième ligne - Date de livraison prévue et Observations
        JPanel secondRow = new JPanel(new GridLayout(1, 2, 20, 0));
        secondRow.setBackground(ComponentFactory.getCardColor());
        secondRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        secondRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        secondRow.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));

        // Date de livraison prévue
        dateLivraisonPrevuePicker = new ModernDatePicker(LocalDate.now().plusDays(7));
        dateLivraisonFieldPanel = ComponentFactory.createFieldPanel("Date de livraison prévue", dateLivraisonPrevuePicker, false);
        secondRow.add(dateLivraisonFieldPanel);

        // Observations
        observationsArea = ComponentFactory.createStyledTextArea(3);
        JScrollPane obsScrollPane = ComponentFactory.createStyledScrollPane(observationsArea);
        observationsFieldPanel = ComponentFactory.createFieldPanel("Observations", obsScrollPane);
        secondRow.add(observationsFieldPanel);

        panel.add(secondRow);

        return panel;
    }

    private JPanel createLignesCommandePanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.3);

        // Panneau supérieur - Ajout de ligne
        JPanel addLignePanel = createAddLignePanel();
        splitPane.setTopComponent(addLignePanel);

        // Panneau inférieur - Liste des lignes
        JPanel lignesListPanel = createLignesListPanel();
        splitPane.setBottomComponent(lignesListPanel);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        container.add(splitPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createAddLignePanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Titre avec style moderne
        JLabel titleLabel = ComponentFactory.createSectionTitle("Ajouter un Produit à la Commande");
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Ligne de saisie parfaitement alignée
        JPanel inputRow = new JPanel(new GridBagLayout());
        inputRow.setBackground(ComponentFactory.getCardColor());
        inputRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.weighty = 1.0;

        // Produit avec validation
        produitCombo = ComponentFactory.createStyledComboBox();
        produitCombo.addActionListener(e -> updatePrixUnitaire());
        produitFieldPanel = ComponentFactory.createFieldPanel("Produit", produitCombo, true);
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        inputRow.add(produitFieldPanel, gbc);

        // Quantité avec validation
        quantiteField = ComponentFactory.createStyledTextField("Ex: 5");
        quantiteFieldPanel = ComponentFactory.createFieldPanel("Quantité", quantiteField, true);
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        inputRow.add(quantiteFieldPanel, gbc);

        // Prix unitaire avec validation
        prixUnitaireField = ComponentFactory.createStyledTextField("Prix auto-rempli");
        prixFieldPanel = ComponentFactory.createFieldPanel("Prix unitaire (FCFA)", prixUnitaireField, true);
        gbc.gridx = 2;
        gbc.weightx = 0.25;
        inputRow.add(prixFieldPanel, gbc);

        // Bouton d'ajout parfaitement centré
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        buttonPanel.setBackground(ComponentFactory.getCardColor());
        JButton addButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.PLUS, "Ajouter", ComponentFactory.getSuccessColor(), e -> addLigneCommande());
        addButton.setPreferredSize(new Dimension(100, 38));
        buttonPanel.add(addButton);

        gbc.gridx = 3;
        gbc.weightx = 0.15;
        gbc.insets = new Insets(0, 0, 0, 0);
        inputRow.add(buttonPanel, gbc);

        panel.add(inputRow);

        return panel;
    }

    private JPanel createLignesListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Header avec total
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Lignes de Commande");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        totalCommandeLabel = new JLabel("Total: 0 FCFA");
        totalCommandeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalCommandeLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(totalCommandeLabel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table des lignes
        String[] columns = { "Produit", "Quantité", "Prix Unit.", "Total", "Action" };
        lignesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Seule la colonne Action est éditable
            }
        };

        lignesTable = new JTable(lignesTableModel);
        styleTable(lignesTable);

        // Renderer pour la colonne Action
        lignesTable.getColumn("Action").setCellRenderer(new DeleteButtonRenderer());
        lignesTable.getColumn("Action").setCellEditor(new DeleteButtonEditor());

        JScrollPane scrollPane = new JScrollPane(lignesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCommandeFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createModernButton("Enregistrer", FontAwesomeSolid.SAVE, SUCCESS_COLOR,
                e -> saveCommande());
        JButton clearButton = createModernButton("Vider", FontAwesomeSolid.ERASER, SECONDARY_COLOR,
                e -> clearCommande());

        panel.add(clearButton);
        panel.add(saveButton);

        return panel;
    }

    // Validation des champs avec affichage d'erreurs
    private boolean validateCommandeFields() {
        boolean isValid = true;

        // Validation fournisseur
        if (fournisseurCombo.getSelectedIndex() == -1) {
            fournisseurFieldPanel.setError("Veuillez sélectionner un fournisseur");
            isValid = false;
        } else {
            fournisseurFieldPanel.clearError();
        }

        // Validation date de commande
        if (dateCommandePicker.getSelectedDate() == null) {
            dateFieldPanel.setError("Veuillez sélectionner une date de commande");
            isValid = false;
        } else if (dateCommandePicker.getSelectedDate().isAfter(LocalDate.now())) {
            dateFieldPanel.setError("La date de commande ne peut pas être dans le futur");
            isValid = false;
        } else {
            dateFieldPanel.clearError();
        }

        // Validation date de livraison prévue (optionnelle mais doit être après la date de commande si renseignée)
        if (dateLivraisonPrevuePicker.getSelectedDate() != null && dateCommandePicker.getSelectedDate() != null) {
            if (dateLivraisonPrevuePicker.getSelectedDate().isBefore(dateCommandePicker.getSelectedDate())) {
                dateLivraisonFieldPanel.setError("La date de livraison doit être postérieure à la date de commande");
                isValid = false;
            } else {
                dateLivraisonFieldPanel.clearError();
            }
        }

        return isValid;
    }

    private boolean validateLigneFields() {
        boolean isValid = true;

        // Validation produit
        if (produitCombo.getSelectedIndex() == -1) {
            produitFieldPanel.setError("Sélectionnez un produit");
            isValid = false;
        } else {
            produitFieldPanel.clearError();
        }

        // Validation quantité
        String quantiteText = quantiteField.getText().trim();
        if (quantiteText.isEmpty()) {
            quantiteFieldPanel.setError("Saisissez la quantité");
            isValid = false;
        } else {
            try {
                int quantite = Integer.parseInt(quantiteText);
                if (quantite <= 0) {
                    quantiteFieldPanel.setError("La quantité doit être positive");
                    isValid = false;
                } else {
                    quantiteFieldPanel.clearError();
                }
            } catch (NumberFormatException e) {
                quantiteFieldPanel.setError("Quantité invalide");
                isValid = false;
            }
        }

        // Validation prix
        String prixText = prixUnitaireField.getText().trim();
        if (prixText.isEmpty()) {
            prixFieldPanel.setError("Le prix est requis");
            isValid = false;
        } else {
            try {
                BigDecimal prix = new BigDecimal(prixText);
                if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                    prixFieldPanel.setError("Le prix doit être positif");
                    isValid = false;
                } else {
                    prixFieldPanel.clearError();
                }
            } catch (NumberFormatException e) {
                prixFieldPanel.setError("Prix invalide");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearAllErrors() {
        fournisseurFieldPanel.clearError();
        dateFieldPanel.clearError();
        dateLivraisonFieldPanel.clearError();
        produitFieldPanel.clearError();
        quantiteFieldPanel.clearError();
        prixFieldPanel.clearError();
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(37, 99, 235, 20));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);

        // Default renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }

                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setForeground(TEXT_PRIMARY);

                return this;
            }
        };

        // Appliquer le renderer à toutes les colonnes sauf Actions
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private JButton createIconButton(FontAwesomeSolid icon, String tooltip, Color color) {
        JButton button = new JButton();
        button.setIcon(FontIcon.of(icon, 16, color));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(40, 40));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createModernButton(String text, FontAwesomeSolid icon, Color backgroundColor,
            ActionListener action) {
        JButton button = new JButton(text);
        button.setIcon(FontIcon.of(icon, 14, Color.WHITE));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        return button;
    }

    // Classes pour les renderers des boutons dans les tables
    private class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            // Seulement le bouton "Voir"
            JButton viewButton = new JButton(FontIcon.of(FontAwesomeSolid.EYE, 10, INFO_COLOR));
            viewButton.setToolTipText("Voir détails");
            styleActionButton(viewButton);
            panel.add(viewButton);

            return panel;
        }
        
        private void styleActionButton(JButton btn) {
            btn.setPreferredSize(new Dimension(22, 22));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        
        public ActionButtonEditor() {
            super(new JCheckBox());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String numeroCommande = (String) table.getValueAt(row, 0);
            
            // Seulement le bouton "Voir"
            JButton viewButton = createActionButton(FontAwesomeSolid.EYE, "Voir", INFO_COLOR, 
                e -> voirDetailsCommande(numeroCommande));
            
            panel.add(viewButton);

            return panel;
        }
        
        private JButton createActionButton(FontAwesomeSolid icon, String tooltip, Color color, ActionListener action) {
            JButton btn = new JButton(FontIcon.of(icon, 10, color));
            btn.setToolTipText(tooltip);
            btn.setPreferredSize(new Dimension(22, 22));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(action);
            return btn;
        }
    }

    private class DeleteButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = new JButton(FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));
            button.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }
    }

    private class DeleteButtonEditor extends DefaultCellEditor {
        public DeleteButtonEditor() {
            super(new JCheckBox());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            JButton button = new JButton(FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));
            button.addActionListener(e -> removeLigneCommande(row));
            return button;
        }
    }

    // Classe temporaire pour les lignes de commande
    private static class LigneCommandeTemp {
        private String produitNom;
        private int quantite;
        private BigDecimal prixUnitaire;
        private BigDecimal total;

        public LigneCommandeTemp(String produitNom, int quantite, BigDecimal prixUnitaire) {
            this.produitNom = produitNom;
            this.quantite = quantite;
            this.prixUnitaire = prixUnitaire;
            this.total = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }

        // Getters
        public String getProduitNom() {
            return produitNom;
        }

        public int getQuantite() {
            return quantite;
        }

        public BigDecimal getPrixUnitaire() {
            return prixUnitaire;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    // Méthodes d'action
    private void loadCommandes() {
        try {
            commandesTableModel.setRowCount(0);
            List<CommandeFournisseur> commandes = commandeFournisseurService.getAllCommandes();
            
            for (CommandeFournisseur commande : commandes) {
                Object[] row = {
                    commande.getNumeroCommande(),
                    commande.getFournisseur().getNomComplet(),
                    commande.getDateCommande().toLocalDate().toString(),
                    commande.getStatut().getLibelle(),
                    String.format("%,.0f FCFA", commande.getMontantTotal()),
                    ""
                };
                commandesTableModel.addRow(row);
            }
            updateStats();
        } catch (Exception e) {
            showErrorMessage("Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    private void loadFournisseurs() {
        try {
            fournisseursList = fournisseurService.getActiveFournisseurs();
            fournisseurCombo.removeAllItems();
            for (FournisseurDTO fournisseur : fournisseursList) {
                fournisseurCombo.addItem(fournisseur.getNomComplet());
            }
        } catch (Exception e) {
            showErrorMessage("Erreur lors du chargement des fournisseurs: " + e.getMessage());
        }
    }
    private void loadProduits() {
        try {
            produitsList = produitService.getActiveProduits();
            produitCombo.removeAllItems();
            for (ProduitDto produit : produitsList) {
                produitCombo.addItem(produit.getLibelle());
            }
        } catch (Exception e) {
            // Gestion d'erreur silencieuse
        }
    }

    private void updateStats() {
        try {
            long totalCommandes = commandeFournisseurService.getAllCommandes().size();
            long enCours = commandeFournisseurService.countCommandesByStatut(StatutCommande.EN_COURS);
            long livrees = commandeFournisseurService.countCommandesByStatut(StatutCommande.LIVREE);
            
            statsLabel.setText(String.format("%d commandes • %d en cours • %d livrées", 
                totalCommandes, enCours, livrees));
        } catch (Exception e) {
            statsLabel.setText("Erreur lors du calcul des statistiques");
        }
    }

    private void updatePrixUnitaire() {
        int selectedIndex = produitCombo.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < produitsList.size()) {
            ProduitDto produit = produitsList.get(selectedIndex);
            if (produit.getPrixRevente() != null) {
                prixUnitaireField.setText(produit.getPrixRevente().toString());
            }
        }
    }

    private void addLigneCommande() {
        // Validation avec affichage d'erreurs contextuelles
        if (!validateLigneFields()) {
            return;
        }

        try {
            String produitNom = (String) produitCombo.getSelectedItem();
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            BigDecimal prix = new BigDecimal(prixUnitaireField.getText().trim());

            LigneCommandeTemp ligne = new LigneCommandeTemp(produitNom, quantite, prix);
            lignesCommande.add(ligne);

            // Ajouter à la table avec formatage moderne
            Object[] row = {
                    ligne.getProduitNom(),
                    String.format("%d", ligne.getQuantite()),
                    String.format("%,.0f FCFA", ligne.getPrixUnitaire()),
                    String.format("%,.0f FCFA", ligne.getTotal()),
                    ""
            };
            lignesTableModel.addRow(row);

            // Calculer le total avec animation
            updateTotalCommande();

            // Vider les champs avec réinitialisation propre
            clearLigneFields();

            // Animation de succès subtile
            showSuccessMessage("✓ Produit ajouté à la commande");

        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'ajout du produit: " + e.getMessage());
        }
    }

    private void clearLigneFields() {
        produitCombo.setSelectedIndex(-1);
        quantiteField.setText("");
        prixUnitaireField.setText("");
        clearAllErrors();
    }

    private void removeLigneCommande(int index) {
        if (index >= 0 && index < lignesCommande.size()) {
            lignesCommande.remove(index);
            lignesTableModel.removeRow(index);
            updateTotalCommande();
        }
    }

    private void updateTotalCommande() {
        BigDecimal total = lignesCommande.stream()
                .map(LigneCommandeTemp::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalCommandeLabel.setText("Total: " + total + " FCFA");
    }

    private void saveCommande() {
        // Validation complète
        if (!validateCommandeFields()) {
            return;
        }

        if (lignesCommande.isEmpty()) {
            showErrorMessage("Veuillez ajouter au moins un produit à la commande");
            return;
        }

        try {
            // Créer la commande fournisseur directement
            FournisseurDTO selectedFournisseur = fournisseursList.get(fournisseurCombo.getSelectedIndex());
            
            CommandeFournisseurDTO commandeDTO = CommandeFournisseurDTO.builder()
                .fournisseurId(selectedFournisseur.getId())
                .dateCommande(dateCommandePicker.getSelectedDate().atStartOfDay())
                .dateLivraisonPrevue(dateLivraisonPrevuePicker.getSelectedDate() != null ? 
                    dateLivraisonPrevuePicker.getSelectedDate().atStartOfDay() : null)
                .observations(observationsArea.getText())
                .build();
            
            // Créer les lignes de commande
            List<LigneCommandeFournisseurDTO> lignesDTO = new ArrayList<>();
            for (LigneCommandeTemp ligne : lignesCommande) {
                ProduitDto produit = produitsList.stream()
                    .filter(p -> p.getLibelle().equals(ligne.getProduitNom()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Produit non trouvé: " + ligne.getProduitNom()));
                
                lignesDTO.add(LigneCommandeFournisseurDTO.builder()
                    .produitId(produit.getId())
                    .quantiteCommandee(ligne.getQuantite())
                    .prixUnitaire(ligne.getPrixUnitaire())
                    .build());
            }
            commandeDTO.setLignes(lignesDTO);
            
            // Sauvegarder
            CommandeFournisseur savedCommande = commandeFournisseurService.createCommande(commandeDTO);
            
            // Dialogue de succès comme pour les clients
            showSuccessDialog(
                "Commande " + savedCommande.getNumeroCommande() + " créée avec succès !",
                ""
            );
            
            // Nettoyage automatique des champs
            clearCommandeWithoutConfirmation();
            loadCommandes();
            switchToListTab();
            
        } catch (Exception e) {
            String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            showErrorDialog(
                "Erreur lors de la création", 
                "Impossible de créer la commande : " + errorMessage
            );
        }
    }

    private void switchToListTab() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedIndex(0);
        }
    }

    private void clearCommande() {
        // Confirmation avant vidage si des données existent
        if (!lignesCommande.isEmpty() || fournisseurCombo.getSelectedIndex() != -1) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Êtes-vous sûr de vouloir vider tous les champs ?\n" +
                            "Toutes les données non sauvegardées seront perdues.",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }

        clearCommandeWithoutConfirmation();
    }

    private void clearCommandeWithoutConfirmation() {
        // Réinitialisation complète sans confirmation
        fournisseurCombo.setSelectedIndex(-1);
        dateCommandePicker.setSelectedDate(LocalDate.now());
        dateLivraisonPrevuePicker.setSelectedDate(LocalDate.now().plusDays(7));
        observationsArea.setText("");
        clearLigneFields();

        lignesCommande.clear();
        lignesTableModel.setRowCount(0);
        updateTotalCommande();
        clearAllErrors();
    }

    private void searchCommandes() {
        // Implémentation de la recherche
        JOptionPane.showMessageDialog(this, "Fonctionnalité de recherche en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void filterCommandes() {
        // Implémentation du filtrage
        loadCommandes(); // Pour l'instant, on recharge tout
    }

    private void showNewCommandeDialog() {
        // Basculer vers l'onglet nouvelle commande
        Container parent = getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedIndex(1);
        }
    }

    private void exportCommandes() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter les commandes");
        fileChooser.setSelectedFile(new File("commandes.csv"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Écriture de l'en-tête
            writer.println("\"N° Commande\",\"Fournisseur\",\"Date\",\"Statut\",\"Total\"");

            int rowCount = commandesTableModel.getRowCount();
            int columnCount = commandesTableModel.getColumnCount() - 1; // Ignorer la colonne Actions
            for (int i = 0; i < rowCount; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < columnCount; j++) {
                    Object value = commandesTableModel.getValueAt(i, j);
                    String text = value != null ? value.toString() : "";
                    text = text.replace("\"", "\"\"");
                    line.append('"').append(text).append('"');
                    if (j < columnCount - 1) {
                        line.append(',');
                    }
                }
                writer.println(line);
            }

            showSuccessMessage("Commandes exportées vers " + file.getAbsolutePath());
        } catch (IOException e) {
            showErrorMessage("Erreur lors de l'export: " + e.getMessage());
        }
    }

    private void refreshData() {
        try {
            loadCommandes();
            loadFournisseurs();
            loadProduits();
            showSuccessMessage("Données actualisées");
        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'actualisation: " + e.getMessage());
        }
    }

    // Méthodes utilitaires pour les messages avec style moderne
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Avertissement", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccessDialog(String title, String message) {
        JDialog successDialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), 
            title, true);
        successDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        successDialog.setSize(400, message != null && !message.trim().isEmpty() ? 180 : 150);
        successDialog.setLocationRelativeTo(this);
        successDialog.setResizable(false);

        JPanel dialogPanel = new JPanel(new BorderLayout(15, 15));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialogPanel.setBackground(Color.WHITE);

        // Icône de succès
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBackground(Color.WHITE);
        JLabel iconLabel = new JLabel(FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 48, SUCCESS_COLOR));
        iconPanel.add(iconLabel);
        dialogPanel.add(iconPanel, BorderLayout.WEST);

        // Message
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        messagePanel.add(titleLabel, BorderLayout.NORTH);

        // N'afficher le message descriptif que s'il n'est pas vide
        if (message != null && !message.trim().isEmpty()) {
            JTextArea messageArea = new JTextArea(message);
            messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            messageArea.setForeground(TEXT_SECONDARY);
            messageArea.setEditable(false);
            messageArea.setOpaque(false);
            messageArea.setWrapStyleWord(true);
            messageArea.setLineWrap(true);
            messagePanel.add(messageArea, BorderLayout.CENTER);
        }

        dialogPanel.add(messagePanel, BorderLayout.CENTER);

        // Bouton OK
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton okButton = createModernButton("OK", FontAwesomeSolid.CHECK, SUCCESS_COLOR, 
            e -> successDialog.dispose());
        okButton.setPreferredSize(new Dimension(80, 35));
        buttonPanel.add(okButton);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        successDialog.add(dialogPanel);
        successDialog.setVisible(true);
    }

    private void showErrorDialog(String title, String message) {
        JDialog errorDialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), 
            title, true);
        errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        errorDialog.setSize(400, 180);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setResizable(false);

        JPanel dialogPanel = new JPanel(new BorderLayout(15, 15));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialogPanel.setBackground(Color.WHITE);

        // Icône d'erreur
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBackground(Color.WHITE);
        JLabel iconLabel = new JLabel(FontIcon.of(FontAwesomeSolid.EXCLAMATION_TRIANGLE, 48, DANGER_COLOR));
        iconPanel.add(iconLabel);
        dialogPanel.add(iconPanel, BorderLayout.WEST);

        // Message
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        messagePanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageArea.setForeground(TEXT_SECONDARY);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messagePanel.add(messageArea, BorderLayout.CENTER);

        dialogPanel.add(messagePanel, BorderLayout.CENTER);

        // Bouton OK
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton okButton = createModernButton("OK", FontAwesomeSolid.TIMES, DANGER_COLOR, 
            e -> errorDialog.dispose());
        okButton.setPreferredSize(new Dimension(80, 35));
        buttonPanel.add(okButton);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        errorDialog.add(dialogPanel);
        errorDialog.setVisible(true);
    }

    private void showSuccessToast(String message) {
        // Toast notification discrète en vert
        JLabel successLabel = new JLabel(
            "<html><div style='text-align: center; color: white; font-weight: bold;'>"
            + "<span style='font-size: 16px;'>✓</span><br>"
            + message
            + "</div></html>");
        successLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JWindow toast = new JWindow();
        toast.setBackground(new Color(0, 0, 0, 0));
        JPanel toastPanel = new JPanel(new BorderLayout());
        toastPanel.setBackground(new Color(34, 197, 94, 240));
        toastPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(22, 163, 74), 2),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)));
        toastPanel.add(successLabel);
        toast.add(toastPanel);
        toast.setSize(350, 90);
        toast.setLocationRelativeTo(this);
        
        // Animation d'apparition
        toast.setOpacity(0.0f);
        toast.setVisible(true);
        
        Timer fadeIn = new Timer(20, null);
        fadeIn.addActionListener(e -> {
            float opacity = toast.getOpacity() + 0.05f;
            if (opacity >= 1.0f) {
                opacity = 1.0f;
                fadeIn.stop();
                
                // Auto-fermeture après 2.5 secondes
                Timer autoClose = new Timer(2500, evt -> {
                    Timer fadeOut = new Timer(20, null);
                    fadeOut.addActionListener(fade -> {
                        float op = toast.getOpacity() - 0.05f;
                        if (op <= 0.0f) {
                            toast.dispose();
                            fadeOut.stop();
                        } else {
                            toast.setOpacity(op);
                        }
                    });
                    fadeOut.start();
                });
                autoClose.setRepeats(false);
                autoClose.start();
            }
            toast.setOpacity(opacity);
        });
        fadeIn.start();
    }

    // Méthodes pour gérer les actions sur les commandes fournisseurs

    private void voirDetailsCommande(String numeroCommande) {
        try {
            CommandeFournisseur commande = commandeFournisseurService.getCommandeWithLignesByNumero(numeroCommande)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
            
            // Créer le dialogue personnalisé
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Détails de la commande " + numeroCommande, true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            
            // Panel principal avec informations générales
            JPanel headerPanel = createCommandeHeaderPanelForDialog(commande);
            
            // Panel avec tableau des produits
            JPanel productsPanel = createCommandeProductsPanel(commande);
            
            // Panel des boutons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(CARD_COLOR);
            
            JButton closeButton = createStyledButton("Fermer", SECONDARY_COLOR);
            closeButton.addActionListener(e -> dialog.dispose());
            
            JButton printButton = createStyledButton("Imprimer", PRIMARY_COLOR);
            printButton.addActionListener(e -> imprimerCommande(commande));
            
            buttonPanel.add(printButton);
            buttonPanel.add(closeButton);
            
            // Assemblage du dialogue
            dialog.add(headerPanel, BorderLayout.NORTH);
            dialog.add(productsPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
                
        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'affichage des détails: " + e.getMessage());
        }
    }

    private JPanel createCommandeHeaderPanelForDialog(CommandeFournisseur commande) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Titre
        JLabel titleLabel = new JLabel("DÉTAILS DE LA COMMANDE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        // Panel d'informations en grille
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Ligne 1 : Numéro et Fournisseur
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(createInfoLabel("Numéro:", commande.getNumeroCommande()), gbc);
        gbc.gridx = 1;
        infoPanel.add(createInfoLabel("Fournisseur:", commande.getFournisseur().getNomComplet()), gbc);
        
        // Ligne 2 : Date commande et Statut
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(createInfoLabel("Date de commande:", commande.getDateCommande().toLocalDate().toString()), gbc);
        gbc.gridx = 1;
        JPanel statutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statutPanel.setBackground(CARD_COLOR);
        JLabel statutLabel = new JLabel(commande.getStatut().getLibelle());
        statutLabel.setOpaque(true);
        statutLabel.setBackground(getStatutColor(commande.getStatut()));
        statutLabel.setForeground(Color.WHITE);
        statutLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statutPanel.add(statutLabel);
        infoPanel.add(createInfoLabel("Statut:", ""), gbc);
        gbc.gridx = 2;
        infoPanel.add(statutPanel, gbc);
        
        // Ligne 3 : Dates de livraison si disponibles
        if (commande.getDateLivraisonPrevue() != null) {
            gbc.gridx = 0; gbc.gridy = 2;
            infoPanel.add(createInfoLabel("Livraison prévue:", commande.getDateLivraisonPrevue().toLocalDate().toString()), gbc);
        }
        if (commande.getDateLivraisonReelle() != null) {
            gbc.gridx = 1; gbc.gridy = 2;
            infoPanel.add(createInfoLabel("Livraison réelle:", commande.getDateLivraisonReelle().toLocalDate().toString()), gbc);
        }
        
        // Montant total
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JLabel montantLabel = new JLabel("Montant total: " + String.format("%,.0f FCFA", commande.getMontantTotal()));
        montantLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        montantLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(montantLabel, gbc);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createCommandeProductsPanel(CommandeFournisseur commande) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 20, 20)
        ));
        
        // Titre du tableau
        JLabel tableTitle = new JLabel("PRODUITS COMMANDÉS");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Créer le tableau des produits
        String[] columnNames = {"Produit", "Quantité", "Prix Unitaire", "Montant", "Statut Livraison"};
        DefaultTableModel productsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable productsTable = new JTable(productsModel);
        styleProductsTable(productsTable);
        
        // Remplir le tableau avec les lignes de commande
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            for (LigneCommandeFournisseur ligne : commande.getLignes()) {
                BigDecimal montantLigne = ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantiteCommandee()));
                String statutLivraison = getStatutLivraison(ligne);
                
                Object[] row = {
                    ligne.getProduit().getLibelle(),
                    ligne.getQuantiteCommandee(),
                    String.format("%,.0f FCFA", ligne.getPrixUnitaire()),
                    String.format("%,.0f FCFA", montantLigne),
                    statutLivraison
                };
                productsModel.addRow(row);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(productsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setPreferredSize(new Dimension(750, 200));
        
        panel.add(tableTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel des totaux
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            JPanel totalsPanel = createTotalsPanel(commande);
            panel.add(totalsPanel, BorderLayout.SOUTH);
        }
        
        return panel;
    }

    private JPanel createTotalsPanel(CommandeFournisseur commande) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        int totalProduits = commande.getLignes().size();
        int totalQuantites = commande.getLignes().stream()
            .mapToInt(LigneCommandeFournisseur::getQuantiteCommandee)
            .sum();
        
        JLabel statsLabel = new JLabel(String.format(
            "Total: %d produits • %d unités • %,.0f FCFA", 
            totalProduits, totalQuantites, commande.getMontantTotal()));
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsLabel.setForeground(TEXT_SECONDARY);
        
        panel.add(statsLabel);
        return panel;
    }

    private void styleProductsTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(59, 130, 246, 50));
        table.setSelectionForeground(TEXT_PRIMARY);
        
        // Style de l'en-tête
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(BACKGROUND_COLOR);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        header.setReorderingAllowed(false);
        
        // Configuration des largeurs de colonnes
        table.getColumnModel().getColumn(0).setPreferredWidth(250); // Produit
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Quantité
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Prix Unitaire
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Montant
        table.getColumnModel().getColumn(4).setPreferredWidth(130); // Statut Livraison
        
        // Centrer les colonnes numériques
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
    }

    private JPanel createInfoLabel(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(CARD_COLOR);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComponent.setForeground(TEXT_SECONDARY);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueComponent.setForeground(TEXT_PRIMARY);
        
        panel.add(labelComponent);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(valueComponent);
        
        return panel;
    }

    private String getStatutLivraison(LigneCommandeFournisseur ligne) {
        int quantiteLivree = ligne.getQuantiteLivree() != null ? ligne.getQuantiteLivree() : 0;
        int quantiteCommandee = ligne.getQuantiteCommandee();
        
        if (quantiteLivree == 0) {
            return "Non livrée";
        } else if (quantiteLivree >= quantiteCommandee) {
            return "Livrée";
        } else {
            return "Partielle (" + quantiteLivree + "/" + quantiteCommandee + ")";
        }
    }

    private Color getStatutColor(StatutCommande statut) {
        return switch (statut) {
            case EN_ATTENTE -> WARNING_COLOR;
            case EN_COURS -> INFO_COLOR;
            case CONFIRMEE -> PRIMARY_COLOR;
            case PARTIELLEMENT_LIVREE -> new Color(255, 193, 7);
            case LIVREE -> SUCCESS_COLOR;
            case ANNULEE -> DANGER_COLOR;
            default -> SECONDARY_COLOR;
        };
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private void imprimerCommande(CommandeFournisseur commande) {
        JOptionPane.showMessageDialog(this, 
            "Fonctionnalité d'impression en cours de développement.\n" +
            "Cette fonctionnalité permettra d'imprimer ou d'exporter la commande en PDF.", 
            "Impression", 
            JOptionPane.INFORMATION_MESSAGE);
    }

}
