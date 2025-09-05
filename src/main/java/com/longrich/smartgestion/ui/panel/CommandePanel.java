package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.FournisseurDTO;
import com.longrich.smartgestion.dto.CommandeFournisseurDTO;
import com.longrich.smartgestion.dto.LigneCommandeFournisseurDTO;
import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.entity.CommandeFournisseur;
import com.longrich.smartgestion.enums.StatutCommande;
import com.longrich.smartgestion.service.FournisseurService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.service.CommandeFournisseurService;
import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;
import com.longrich.smartgestion.ui.components.ModernDatePicker;

import lombok.RequiredArgsConstructor;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

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

        JLabel titleLabel = new JLabel("Gestion des Commandes Fournisseur");
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
        tabbedPane.addTab("📋 Liste des Commandes Fournisseur", listePanel);

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
        JLabel searchTitle = ComponentFactory.createSectionTitle("Liste des Commandes Fournisseur");

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
        JLabel titleLabel = ComponentFactory.createSectionTitle("Informations de la Commande Fournisseur");
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Première ligne - Fournisseur et Date de commande
        JPanel firstRow = new JPanel(new GridLayout(1, 2, 20, 0));
        firstRow.setBackground(ComponentFactory.getCardColor());
        firstRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        firstRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

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
        secondRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

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

            // Récupérer le statut de la commande pour afficher les bons boutons
            String statut = (String) table.getValueAt(row, 3); // Colonne statut
            
            JButton viewButton = new JButton(FontIcon.of(FontAwesomeSolid.EYE, 10, INFO_COLOR));
            viewButton.setToolTipText("Voir détails");
            
            // Boutons selon le statut
            if ("En Cours".equals(statut)) {
                JButton confirmButton = new JButton(FontIcon.of(FontAwesomeSolid.CHECK, 10, SUCCESS_COLOR));
                confirmButton.setToolTipText("Confirmer");
                JButton cancelButton = new JButton(FontIcon.of(FontAwesomeSolid.TIMES, 10, DANGER_COLOR));
                cancelButton.setToolTipText("Annuler");
                
                for (JButton btn : new JButton[] { viewButton, confirmButton, cancelButton }) {
                    styleActionButton(btn);
                    panel.add(btn);
                }
            } else if ("Confirmée".equals(statut)) {
                JButton partialButton = new JButton(FontIcon.of(FontAwesomeSolid.SHIPPING_FAST, 10, WARNING_COLOR));
                partialButton.setToolTipText("Livraison partielle");
                JButton deliverButton = new JButton(FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 10, SUCCESS_COLOR));
                deliverButton.setToolTipText("Livrer totalement");
                JButton cancelButton = new JButton(FontIcon.of(FontAwesomeSolid.TIMES, 10, DANGER_COLOR));
                cancelButton.setToolTipText("Annuler");
                
                for (JButton btn : new JButton[] { viewButton, partialButton, deliverButton, cancelButton }) {
                    styleActionButton(btn);
                    panel.add(btn);
                }
            } else if ("Partiellement Livrée".equals(statut)) {
                JButton deliverButton = new JButton(FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 10, SUCCESS_COLOR));
                deliverButton.setToolTipText("Livrer totalement");
                
                for (JButton btn : new JButton[] { viewButton, deliverButton }) {
                    styleActionButton(btn);
                    panel.add(btn);
                }
            } else {
                // Statut Livrée ou Annulée - seulement voir
                styleActionButton(viewButton);
                panel.add(viewButton);
            }

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

            String statut = (String) table.getValueAt(row, 3);
            String numeroCommande = (String) table.getValueAt(row, 0);
            
            JButton viewButton = createActionButton(FontAwesomeSolid.EYE, "Voir", INFO_COLOR, 
                e -> voirDetailsCommande(numeroCommande));
            
            if ("En Cours".equals(statut)) {
                JButton confirmButton = createActionButton(FontAwesomeSolid.CHECK, "Confirmer", SUCCESS_COLOR,
                    e -> confirmerCommande(numeroCommande));
                JButton cancelButton = createActionButton(FontAwesomeSolid.TIMES, "Annuler", DANGER_COLOR,
                    e -> annulerCommande(numeroCommande));
                
                panel.add(viewButton);
                panel.add(confirmButton);
                panel.add(cancelButton);
                
            } else if ("Confirmée".equals(statut)) {
                JButton partialButton = createActionButton(FontAwesomeSolid.SHIPPING_FAST, "Livraison partielle", WARNING_COLOR,
                    e -> livrerPartiellement(numeroCommande));
                JButton deliverButton = createActionButton(FontAwesomeSolid.CHECK_CIRCLE, "Livrer", SUCCESS_COLOR,
                    e -> livrerTotalement(numeroCommande));
                JButton cancelButton = createActionButton(FontAwesomeSolid.TIMES, "Annuler", DANGER_COLOR,
                    e -> annulerCommande(numeroCommande));
                
                panel.add(viewButton);
                panel.add(partialButton);
                panel.add(deliverButton);
                panel.add(cancelButton);
                
            } else if ("Partiellement Livrée".equals(statut)) {
                JButton deliverButton = createActionButton(FontAwesomeSolid.CHECK_CIRCLE, "Livrer", SUCCESS_COLOR,
                    e -> livrerTotalement(numeroCommande));
                
                panel.add(viewButton);
                panel.add(deliverButton);
                
            } else {
                panel.add(viewButton);
            }

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
        // Validation complète avec gestion d'erreurs moderne
        if (!validateCommandeFields()) {
            return;
        }

        if (lignesCommande.isEmpty()) {
            return;
        }

        // Confirmation avec détails
        BigDecimal total = lignesCommande.stream()
                .map(LigneCommandeTemp::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String dateLivraisonText = dateLivraisonPrevuePicker.getSelectedDate() != null ? 
            dateLivraisonPrevuePicker.getDateText() : "Non définie";
            
        int option = JOptionPane.showConfirmDialog(
                this,
                String.format("Confirmer la création de la commande fournisseur ?\n\n" +
                        "Fournisseur: %s\n" +
                        "Date de commande: %s\n" +
                        "Date de livraison prévue: %s\n" +
                        "Produits: %d\n" +
                        "Total: %,.0f FCFA\n\n" +
                        "Statut initial: EN_COURS",
                        fournisseurCombo.getSelectedItem(),
                        dateCommandePicker.getDateText(),
                        dateLivraisonText,
                        lignesCommande.size(),
                        total),
                "Confirmation de création",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                showInfoMessage("Sauvegarde en cours...");
                
                // Créer la commande fournisseur
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
                        .quantite(ligne.getQuantite())
                        .prixUnitaire(ligne.getPrixUnitaire())
                        .build());
                }
                commandeDTO.setLignes(lignesDTO);
                
                CommandeFournisseur savedCommande = commandeFournisseurService.createCommande(commandeDTO);
                
                showSuccessMessage("✓ Commande fournisseur sauvegardée: " + savedCommande.getNumeroCommande());
                
                clearCommande();
                loadCommandes();
                switchToListTab();
                
            } catch (Exception e) {
                showErrorMessage("Erreur lors de la sauvegarde: " + e.getMessage());
            }
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

        // Réinitialisation complète avec gestion d'erreurs
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

    // Méthodes pour gérer les actions sur les commandes fournisseurs

    private void voirDetailsCommande(String numeroCommande) {
        try {
            CommandeFournisseur commande = commandeFournisseurService.getCommandeByNumero(numeroCommande)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
            
            StringBuilder details = new StringBuilder();
            details.append("DÉTAILS DE LA COMMANDE\n\n");
            details.append("Numéro: ").append(commande.getNumeroCommande()).append("\n");
            details.append("Fournisseur: ").append(commande.getFournisseur().getNomComplet()).append("\n");
            details.append("Date de commande: ").append(commande.getDateCommande().toLocalDate()).append("\n");
            details.append("Statut: ").append(commande.getStatut().getLibelle()).append("\n");
            details.append("Montant total: ").append(String.format("%,.0f FCFA", commande.getMontantTotal())).append("\n");
            
            if (commande.getDateLivraisonPrevue() != null) {
                details.append("Date de livraison prévue: ").append(commande.getDateLivraisonPrevue().toLocalDate()).append("\n");
            }
            if (commande.getDateLivraisonReelle() != null) {
                details.append("Date de livraison réelle: ").append(commande.getDateLivraisonReelle().toLocalDate()).append("\n");
            }
            
            if (commande.getObservations() != null && !commande.getObservations().trim().isEmpty()) {
                details.append("\nObservations:\n").append(commande.getObservations());
            }
            
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Détails de la commande " + numeroCommande, 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'affichage des détails: " + e.getMessage());
        }
    }

    private void confirmerCommande(String numeroCommande) {
        int option = JOptionPane.showConfirmDialog(this, 
            "Confirmer la commande " + numeroCommande + " ?\n\n" +
            "Cette action changera le statut de 'En Cours' vers 'Confirmée'.",
            "Confirmation de commande", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
            
        if (option == JOptionPane.YES_OPTION) {
            try {
                CommandeFournisseur commande = commandeFournisseurService.getCommandeByNumero(numeroCommande)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
                    
                commandeFournisseurService.confirmerCommande(commande.getId());
                showSuccessMessage("✓ Commande " + numeroCommande + " confirmée");
                loadCommandes();
            } catch (Exception e) {
                showErrorMessage("Erreur lors de la confirmation: " + e.getMessage());
            }
        }
    }

    private void annulerCommande(String numeroCommande) {
        String motif = JOptionPane.showInputDialog(this, 
            "Motif d'annulation de la commande " + numeroCommande + " :",
            "Annulation de commande",
            JOptionPane.QUESTION_MESSAGE);
            
        if (motif != null && !motif.trim().isEmpty()) {
            try {
                CommandeFournisseur commande = commandeFournisseurService.getCommandeByNumero(numeroCommande)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
                    
                commandeFournisseurService.annulerCommande(commande.getId(), motif);
                showSuccessMessage("✓ Commande " + numeroCommande + " annulée");
                loadCommandes();
            } catch (Exception e) {
                showErrorMessage("Erreur lors de l'annulation: " + e.getMessage());
            }
        }
    }

    private void livrerPartiellement(String numeroCommande) {
        String details = JOptionPane.showInputDialog(this,
            "Détails de la livraison partielle pour " + numeroCommande + " :\n" +
            "(Ex: 50% des produits livrés, reste prévu pour...)",
            "Livraison partielle",
            JOptionPane.QUESTION_MESSAGE);
            
        if (details != null && !details.trim().isEmpty()) {
            try {
                CommandeFournisseur commande = commandeFournisseurService.getCommandeByNumero(numeroCommande)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
                    
                commandeFournisseurService.livrerPartiellement(commande.getId(), details);
                showSuccessMessage("✓ Livraison partielle enregistrée pour " + numeroCommande);
                loadCommandes();
            } catch (Exception e) {
                showErrorMessage("Erreur lors de l'enregistrement de la livraison: " + e.getMessage());
            }
        }
    }

    private void livrerTotalement(String numeroCommande) {
        int option = JOptionPane.showConfirmDialog(this,
            "Confirmer la livraison totale de la commande " + numeroCommande + " ?\n\n" +
            "Cette action marquera la commande comme entièrement livrée.",
            "Livraison totale",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (option == JOptionPane.YES_OPTION) {
            try {
                CommandeFournisseur commande = commandeFournisseurService.getCommandeByNumero(numeroCommande)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
                    
                commandeFournisseurService.livrerTotalement(commande.getId());
                showSuccessMessage("✓ Commande " + numeroCommande + " entièrement livrée");
                loadCommandes();
            } catch (Exception e) {
                showErrorMessage("Erreur lors de la livraison: " + e.getMessage());
            }
        }
    }
}
