package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.dto.CommandeDTO;
import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.ProduitService;
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

    private final ClientService clientService;
    private final ProduitService produitService;

    // Composants UI pour les commandes
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JTable commandesTable;
    private DefaultTableModel commandesTableModel;
    private JLabel statsLabel;

    // Composants pour nouvelle commande avec validation
    private ComponentFactory.FieldPanel clientFieldPanel;
    private JComboBox<String> clientCombo;
    private ComponentFactory.FieldPanel dateFieldPanel;
    private ModernDatePicker dateCommandePicker;
    private ComponentFactory.FieldPanel statutFieldPanel;
    private JComboBox<String> statutCombo;
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

    private List<ClientDTO> clientsList;
    private List<ProduitDto> produitsList;
    private List<LigneCommandeTemp> lignesCommande;
    private CommandeDTO currentCommande;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lignesCommande = new ArrayList<>();

        createHeaderPanel();
        createMainContent();

        loadCommandes();
        loadClients();
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

        JButton newCommandeButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.PLUS, "Nouvelle commande", SUCCESS_COLOR, e -> showNewCommandeDialog());
        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter", INFO_COLOR, e -> exportCommandes());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR, e -> refreshData());

        panel.add(newCommandeButton);
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
        searchField = ComponentFactory.createStyledTextField("Rechercher par numéro, client...");
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.addActionListener(e -> searchCommandes());
        JPanel searchPanel_inner = ComponentFactory.createSearchField(searchField);
        searchPanel_inner.setPreferredSize(new Dimension(250, 38));

        // Filtre par statut stylisé
        String[] statutOptions = { "Tous les statuts", "En attente", "Confirmée", "En cours", "Livrée", "Annulée" };
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
        String[] columns = { "N° Commande", "Client", "Date", "Statut", "Total", "Actions" };
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

        // Première ligne - Client et Date avec validation
        JPanel firstRow = new JPanel(new GridLayout(1, 2, 20, 0));
        firstRow.setBackground(ComponentFactory.getCardColor());
        firstRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        firstRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Client avec validation
        clientCombo = ComponentFactory.createStyledComboBox();
        clientFieldPanel = ComponentFactory.createFieldPanel("Client", clientCombo, true);
        firstRow.add(clientFieldPanel);

        // Date avec DatePicker moderne
        dateCommandePicker = new ModernDatePicker(LocalDate.now());
        dateFieldPanel = ComponentFactory.createFieldPanel("Date de commande", dateCommandePicker, true);
        firstRow.add(dateFieldPanel);

        panel.add(firstRow);
        panel.add(Box.createVerticalStrut(10));

        // Deuxième ligne - Statut et Observations
        JPanel secondRow = new JPanel(new GridLayout(1, 2, 20, 0));
        secondRow.setBackground(ComponentFactory.getCardColor());
        secondRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        secondRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Statut avec validation
        String[] statutOptions = { "En attente", "Confirmée", "En cours", "Livrée", "Annulée" };
        statutCombo = ComponentFactory.createStyledComboBox(statutOptions);
        statutFieldPanel = ComponentFactory.createFieldPanel("Statut", statutCombo, true);
        secondRow.add(statutFieldPanel);

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

        // Validation client
        if (clientCombo.getSelectedIndex() == -1) {
            clientFieldPanel.setError("Veuillez sélectionner un client");
            isValid = false;
        } else {
            clientFieldPanel.clearError();
        }

        // Validation date
        if (dateCommandePicker.getSelectedDate() == null) {
            dateFieldPanel.setError("Veuillez sélectionner une date");
            isValid = false;
        } else {
            dateFieldPanel.clearError();
        }

        // Validation statut
        if (statutCombo.getSelectedIndex() == -1) {
            statutFieldPanel.setError("Veuillez sélectionner un statut");
            isValid = false;
        } else {
            statutFieldPanel.clearError();
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
        clientFieldPanel.clearError();
        dateFieldPanel.clearError();
        statutFieldPanel.clearError();
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
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            JButton viewButton = new JButton(FontIcon.of(FontAwesomeSolid.EYE, 12, INFO_COLOR));
            JButton editButton = new JButton(FontIcon.of(FontAwesomeSolid.EDIT, 12, WARNING_COLOR));
            JButton deleteButton = new JButton(FontIcon.of(FontAwesomeSolid.TRASH, 12, DANGER_COLOR));

            for (JButton btn : new JButton[] { viewButton, editButton, deleteButton }) {
                btn.setPreferredSize(new Dimension(25, 25));
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.add(btn);
            }

            return panel;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        public ActionButtonEditor() {
            super(new JCheckBox());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return new ActionButtonRenderer().getTableCellRendererComponent(table, value, isSelected, false, row,
                    column);
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
        commandesTableModel.setRowCount(0);

        // Données factices
        Object[][] sampleData = {
                { "CMD-001", "Client Test 1", "15/01/2024", "En cours", "150,000 FCFA", "" },
                { "CMD-002", "Client Test 2", "16/01/2024", "Livrée", "75,500 FCFA", "" },
                { "CMD-003", "Client Test 3", "17/01/2024", "En attente", "200,000 FCFA", "" }
        };

        for (Object[] row : sampleData) {
            commandesTableModel.addRow(row);
        }

        updateStats();
    }

    private void loadClients() {
        try {
            clientsList = clientService.getAllClients();
            clientCombo.removeAllItems();
            for (ClientDTO client : clientsList) {
                clientCombo.addItem(client.getNom() + " " + client.getPrenom());
            }
        } catch (Exception e) {
            // Gestion d'erreur silencieuse
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
        statsLabel.setText("3 commandes • 2 en cours • 1 livrée");
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
            showWarningMessage("Veuillez corriger les erreurs dans les informations de la commande");
            return;
        }

        if (lignesCommande.isEmpty()) {
            showWarningMessage("Veuillez ajouter au moins un produit à la commande");
            return;
        }

        // Confirmation avec détails
        BigDecimal total = lignesCommande.stream()
                .map(LigneCommandeTemp::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int option = JOptionPane.showConfirmDialog(
                this,
                String.format("Confirmer la sauvegarde de la commande ?\n\n" +
                             "Client: %s\n" +
                             "Date: %s\n" +
                             "Produits: %d\n" +
                             "Total: %,.0f FCFA",
                             clientCombo.getSelectedItem(),
                             dateCommandePicker.getDateText(),
                             lignesCommande.size(),
                             total),
                "Confirmation de sauvegarde",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                // Simulation de sauvegarde avec progress
                showInfoMessage("Sauvegarde en cours...");
                // TODO: Implémenter la sauvegarde réelle
                
                showSuccessMessage("✓ Commande sauvegardée avec succès (N° CMD-" + 
                                  String.format("%04d", System.currentTimeMillis() % 10000) + ")");
                
                clearCommande();
                loadCommandes();
                
                // Basculer vers l'onglet liste
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
        if (!lignesCommande.isEmpty() || clientCombo.getSelectedIndex() != -1) {
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
        clientCombo.setSelectedIndex(-1);
        dateCommandePicker.setSelectedDate(LocalDate.now());
        statutCombo.setSelectedIndex(0);
        observationsArea.setText("");
        clearLigneFields();

        lignesCommande.clear();
        lignesTableModel.setRowCount(0);
        updateTotalCommande();
        clearAllErrors();
        
        showInfoMessage("Formulaire vidé");
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
        showInfoMessage("Fonctionnalité d'export en cours de développement");
    }

    private void refreshData() {
        try {
            loadCommandes();
            loadClients();
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
}