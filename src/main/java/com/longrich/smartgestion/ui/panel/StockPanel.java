package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.ui.components.ButtonFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class StockPanel extends JPanel {

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

    private final ProduitService produitService;

    // Composants UI principaux
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JComboBox<String> categoryFilterCombo;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;

    // Composants formulaire mouvement
    private JTextField quantiteField;
    private JTextField motifField;
    private JComboBox<String> mouvementTypeCombo;
    private JComboBox<String> produitCombo;
    private List<ProduitDto> produitsList;

    // Composants historique
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextField historySearchField;
    private JComboBox<String> historyFilterCombo;

    // Validation des erreurs
    private final Map<JComponent, JLabel> errorLabels = new HashMap<>();

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createMainContent();

        loadStock();
        loadProduits();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Stocks");
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

        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportStock());
        JButton alertsButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.EXCLAMATION_TRIANGLE, "Alertes", WARNING_COLOR, e -> showStockAlerts());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR, e -> refreshData());

        panel.add(alertsButton);
        panel.add(exportButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.65);
        splitPane.setOneTouchExpandable(true);

        // Panneau de gauche - Liste des stocks
        JPanel stockContainer = createStockContainer();
        splitPane.setLeftComponent(stockContainer);

        // Panneau de droite - Mouvements de stock
        JPanel mouvementContainer = createMouvementContainer();
        splitPane.setRightComponent(mouvementContainer);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createStockContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        // Panneau de recherche et filtres
        JPanel searchContainer = createSearchPanel();
        container.add(searchContainer, BorderLayout.NORTH);

        // Table des stocks
        JPanel tablePanel = createStockTable();
        container.add(tablePanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(CARD_COLOR);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        // Titre
        JLabel searchTitle = new JLabel("Inventaire des Stocks");
        searchTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchTitle.setForeground(TEXT_PRIMARY);
        searchPanel.add(searchTitle, BorderLayout.NORTH);

        // Panneau de filtres amélioré
        JPanel filtersContainer = new JPanel();
        filtersContainer.setLayout(new BoxLayout(filtersContainer, BoxLayout.Y_AXIS));
        filtersContainer.setBackground(CARD_COLOR);
        filtersContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Première ligne de filtres
        JPanel firstRowFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        firstRowFilters.setBackground(CARD_COLOR);
        firstRowFilters.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Champ de recherche avec bouton
        JPanel searchGroup = createFieldGroup("Recherche par nom/code");
        searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 38));
        searchField.setMinimumSize(new Dimension(150, 38));
        searchField.addActionListener(e -> searchStock());

        JButton searchButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SEARCH, "", PRIMARY_COLOR, e -> searchStock());
        searchButton.setPreferredSize(new Dimension(40, 38));

        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.setBackground(CARD_COLOR);
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);

        searchGroup.add(searchInputPanel);
        firstRowFilters.add(searchGroup);

        // Filtre par statut
        JPanel statusGroup = createFieldGroup("Statut du stock");
        filterCombo = new JComboBox<>(new String[] {
                "Tous les statuts", "Stock normal", "Stock faible", "Rupture de stock"
        });
        styleComboBox(filterCombo);
        filterCombo.setPreferredSize(new Dimension(160, 38));
        filterCombo.setMinimumSize(new Dimension(120, 38));
        filterCombo.addActionListener(e -> filterStock());
        statusGroup.add(filterCombo);
        firstRowFilters.add(statusGroup);

        // Filtre par catégorie
        JPanel categoryGroup = createFieldGroup("Catégorie");
        categoryFilterCombo = new JComboBox<>(new String[] {
                "Toutes catégories", "Nutrition", "Soins", "Cosmétiques", "Hygiène"
        });
        styleComboBox(categoryFilterCombo);
        categoryFilterCombo.setPreferredSize(new Dimension(130, 38));
        categoryFilterCombo.setMinimumSize(new Dimension(100, 38));
        categoryFilterCombo.addActionListener(e -> filterStock());
        categoryGroup.add(categoryFilterCombo);
        firstRowFilters.add(categoryGroup);

        filtersContainer.add(firstRowFilters);

        // Boutons d'action rapide
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionButtons.setBackground(CARD_COLOR);
        actionButtons.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JButton clearFiltersButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.TIMES, "Effacer filtres", SECONDARY_COLOR, e -> clearFilters());
        JButton lowStockButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.EXCLAMATION_TRIANGLE, "Stocks faibles", WARNING_COLOR, e -> showLowStock());
        JButton outOfStockButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.TIMES_CIRCLE, "Ruptures", DANGER_COLOR, e -> showOutOfStock());

        actionButtons.add(clearFiltersButton);
        actionButtons.add(lowStockButton);
        actionButtons.add(outOfStockButton);

        filtersContainer.add(actionButtons);
        searchPanel.add(filtersContainer, BorderLayout.CENTER);

        return searchPanel;
    }

    private JPanel createFieldGroup(String labelText) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(CARD_COLOR);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        group.add(label);
        return group;
    }

    private JPanel createStockTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Modèle de table
        String[] columns = { "Produit", "Code", "Quantité", "Stock Min", "Statut", "Dernière MAJ" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stockTable = new JTable(tableModel);
        styleTable(stockTable);

        // Listener pour double-click
        stockTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showStockDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createMouvementContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        // Formulaire de mouvement
        JPanel formPanel = createMouvementForm();
        container.add(formPanel, BorderLayout.NORTH);

        // Historique des mouvements (placeholder)
        JPanel historyPanel = createHistoryPanel();
        container.add(historyPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createMouvementForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        // Titre
        JLabel formTitle = new JLabel("Mouvement de Stock");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Section sélection produit
        formPanel.add(createSectionTitle("Sélection du Produit"));
        produitCombo = new JComboBox<>();
        styleComboBox(produitCombo);
        formPanel.add(createFieldPanelWithValidation("Produit :", produitCombo));
        formPanel.add(Box.createVerticalStrut(15));

        // Section détails du mouvement
        formPanel.add(createSectionTitle("Détails du Mouvement"));

        // Type de mouvement
        mouvementTypeCombo = new JComboBox<>(new String[] {
                "Entrée de stock", "Sortie de stock", "Ajustement d'inventaire", "Transfert", "Retour"
        });
        styleComboBox(mouvementTypeCombo);
        formPanel.add(createFieldPanelWithValidation("Type de mouvement :", mouvementTypeCombo));

        // Quantité
        quantiteField = createStyledTextField();
        quantiteField.setToolTipText("Saisissez la quantité (nombre positif)");
        formPanel.add(createFieldPanelWithValidation("Quantité :", quantiteField));

        // Motif
        motifField = createStyledTextField();
        motifField.setToolTipText("Indiquez le motif du mouvement (optionnel)");
        formPanel.add(createFieldPanelWithValidation("Motif :", motifField));

        formPanel.add(Box.createVerticalStrut(20));

        // Boutons
        JPanel buttonPanel = createMouvementButtonPanel();
        buttonPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(buttonPanel);

        return formPanel;
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(PRIMARY_COLOR);
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    private JPanel createMouvementButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton saveButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SAVE, "Enregistrer", SUCCESS_COLOR, e -> saveMouvement());
        JButton clearButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.ERASER, "Vider", SECONDARY_COLOR, e -> clearMouvementFields());

        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(clearButton);

        return buttonPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // En-tête avec titre et filtres
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        JLabel titleLabel = new JLabel("Historique des Mouvements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Filtres de l'historique
        JPanel historyFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        historyFilters.setBackground(CARD_COLOR);

        historySearchField = createStyledTextField();
        historySearchField.setPreferredSize(new Dimension(150, 32));
        historySearchField.setToolTipText("Rechercher dans l'historique");
        historySearchField.addActionListener(e -> filterHistory());

        historyFilterCombo = new JComboBox<>(new String[] {
                "Tous", "Entrées", "Sorties", "Ajustements", "Aujourd'hui", "Cette semaine"
        });
        styleComboBox(historyFilterCombo);
        historyFilterCombo.setPreferredSize(new Dimension(120, 32));
        historyFilterCombo.addActionListener(e -> filterHistory());

        historyFilters.add(new JLabel("Recherche:"));
        historyFilters.add(historySearchField);
        historyFilters.add(historyFilterCombo);

        headerPanel.add(historyFilters, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table d'historique améliorée
        createHistoryTable();
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void createHistoryTable() {
        String[] columns = {
                "Date/Heure", "Type", "Produit", "Quantité", "Motif", "Utilisateur"
        };

        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(35);
        historyTable.setShowVerticalLines(false);
        historyTable.setGridColor(new Color(243, 244, 246));
        historyTable.setSelectionBackground(new Color(239, 246, 255));
        historyTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = historyTable.getTableHeader();
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 40));

        // Renderer personnalisé
        DefaultTableCellRenderer historyRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }

                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // Coloration selon le type de mouvement (colonne 1)
                if (column == 1 && value != null) {
                    String type = value.toString();
                    if (type.contains("Entrée")) {
                        setForeground(SUCCESS_COLOR);
                    } else if (type.contains("Sortie")) {
                        setForeground(DANGER_COLOR);
                    } else if (type.contains("Ajustement")) {
                        setForeground(WARNING_COLOR);
                    } else {
                        setForeground(INFO_COLOR);
                    }
                } else {
                    setForeground(TEXT_PRIMARY);
                }

                return c;
            }
        };

        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(historyRenderer);
        }

        // Ajout de données d'exemple
        loadHistoryData();
    }

    // Méthodes utilitaires pour le styling
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 38));

        // Effet focus
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });

        return field;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        comboBox.setPreferredSize(new Dimension(0, 38));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Effet focus
        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        field.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createFieldPanelWithValidation(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);

        JLabel errorLabel = new JLabel();
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(DANGER_COLOR);
        errorLabel.setVisible(false);

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(CARD_COLOR);
        fieldWrapper.add(field, BorderLayout.CENTER);
        fieldWrapper.add(errorLabel, BorderLayout.SOUTH);

        panel.add(label, BorderLayout.NORTH);
        panel.add(fieldWrapper, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);

        errorLabels.put(field, errorLabel);

        return panel;
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

        // Renderer spécial pour les statuts
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }

                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));

                // Coloration selon le statut (colonne 4)
                if (column == 4 && value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "Normal":
                            setForeground(SUCCESS_COLOR);
                            break;
                        case "Stock faible":
                            setForeground(WARNING_COLOR);
                            break;
                        case "Rupture":
                            setForeground(DANGER_COLOR);
                            break;
                        default:
                            setForeground(TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TEXT_PRIMARY);
                }

                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
        }
    }

    // Méthodes d'action
    private void loadStock() {
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            tableModel.setRowCount(0);

            for (ProduitDto produit : produits) {
                String status = getStockStatus(produit);
                String lastUpdate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                Object[] row = {
                        produit.getLibelle(),
                        produit.getCodeBarre(),
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "0",
                        status,
                        lastUpdate
                };
                tableModel.addRow(row);
            }
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des stocks: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getStockStatus(ProduitDto produit) {
        if (produit.getQuantiteStock() == null || produit.getQuantiteStock() == 0) {
            return "Rupture";
        }
        if (produit.getStockMinimum() != null && produit.getQuantiteStock() <= produit.getStockMinimum()) {
            return "Stock faible";
        }
        return "Normal";
    }

    private void loadProduits() {
        try {
            produitsList = produitService.getActiveProduits();
            produitCombo.removeAllItems();
            for (ProduitDto produit : produitsList) {
                produitCombo.addItem(produit.getLibelle() + " (" + produit.getCodeBarre() + ")");
            }
        } catch (Exception e) {
            // Gestion d'erreur silencieuse
        }
    }

    private void updateStats() {
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            int totalProduits = produits.size();
            long ruptures = produits.stream().filter(p -> p.getQuantiteStock() == null || p.getQuantiteStock() == 0)
                    .count();
            long alertes = produits.stream()
                    .filter(p -> p.getQuantiteStock() != null && p.getStockMinimum() != null)
                    .filter(p -> p.getQuantiteStock() <= p.getStockMinimum())
                    .count();

            statsLabel.setText(String.format("%d produits • %d ruptures • %d alertes",
                    totalProduits, ruptures, alertes));
        } catch (Exception e) {
            statsLabel.setText("Statistiques non disponibles");
        }
    }

    private void searchStock() {
        // Implémentation de la recherche
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            loadStock();
            return;
        }

        // Filtrer les lignes existantes
        tableModel.setRowCount(0);
        try {
            List<ProduitDto> produits = produitService.searchProduits(searchText);
            for (ProduitDto produit : produits) {
                String status = getStockStatus(produit);
                String lastUpdate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                Object[] row = {
                        produit.getLibelle(),
                        produit.getCodeBarre(),
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "0",
                        status,
                        lastUpdate
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterStock() {
        try {
            List<ProduitDto> allProduits = produitService.getActiveProduits();
            String statusFilter = (String) filterCombo.getSelectedItem();
            String categoryFilter = (String) categoryFilterCombo.getSelectedItem();
            String searchText = searchField.getText().toLowerCase().trim();

            tableModel.setRowCount(0);

            for (ProduitDto produit : allProduits) {
                // Filtre par recherche
                if (!searchText.isEmpty()) {
                    boolean matchesSearch = produit.getLibelle().toLowerCase().contains(searchText) ||
                            (produit.getCodeBarre() != null &&
                                    produit.getCodeBarre().toLowerCase().contains(searchText));
                    if (!matchesSearch)
                        continue;
                }

                // Filtre par statut
                String status = getStockStatus(produit);
                if (!"Tous les statuts".equals(statusFilter)) {
                    String filterStatus = statusFilter.replace("Stock ", "").replace(" de stock", "");
                    if (!status.toLowerCase().contains(filterStatus.toLowerCase())) {
                        continue;
                    }
                }

                // TODO: Implémenter le filtre par catégorie quand les catégories seront
                // disponibles

                String lastUpdate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                Object[] row = {
                        produit.getLibelle(),
                        produit.getCodeBarre(),
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "0",
                        status,
                        lastUpdate
                };
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            showErrorMessage("Erreur lors du filtrage: " + e.getMessage());
        }
    }

    private void saveMouvement() {
        if (!validateMouvementFields()) {
            return;
        }

        try {
            // Récupération des valeurs
            int produitIndex = produitCombo.getSelectedIndex();
            String mouvementType = (String) mouvementTypeCombo.getSelectedItem();
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            String motif = motifField.getText().trim();

            // Ajout à l'historique
            String dateTime = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String produitName = produitsList.get(produitIndex).getLibelle();

            Object[] historyRow = {
                    dateTime,
                    mouvementType,
                    produitName,
                    (mouvementType.contains("Sortie") ? "-" : "+") + quantite,
                    motif.isEmpty() ? "-" : motif,
                    "Admin" // TODO: Récupérer l'utilisateur connecté
            };
            historyTableModel.insertRow(0, historyRow);

            showSuccessMessage("✓ Mouvement de stock enregistré avec succès");
            clearMouvementFields();
            loadStock();

        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void clearMouvementFields() {
        produitCombo.setSelectedIndex(-1);
        mouvementTypeCombo.setSelectedIndex(0);
        quantiteField.setText("");
        motifField.setText("");
        clearErrors();

    }

    private void showStockDetails() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow >= 0) {
            String produitName = (String) tableModel.getValueAt(selectedRow, 0);
            String quantite = (String) tableModel.getValueAt(selectedRow, 2);
            String status = (String) tableModel.getValueAt(selectedRow, 4);

            JOptionPane.showMessageDialog(this,
                    String.format("Détails du stock:\n\nProduit: %s\nQuantité: %s\nStatut: %s",
                            produitName, quantite, status),
                    "Détails", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showStockAlerts() {
        StringBuilder alerts = new StringBuilder("Alertes de stock:\n\n");
        boolean hasAlerts = false;

        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            for (ProduitDto produit : produits) {
                if ("Rupture".equals(getStockStatus(produit)) || "Stock faible".equals(getStockStatus(produit))) {
                    alerts.append("⚠️ ").append(produit.getLibelle()).append(" - ").append(getStockStatus(produit))
                            .append("\n");
                    hasAlerts = true;
                }
            }

            if (!hasAlerts) {
                alerts.append("✅ Aucune alerte de stock");
            }

        } catch (Exception e) {
            alerts.append("❌ Erreur lors de la vérification des stocks");
        }

        JOptionPane.showMessageDialog(this, alerts.toString(), "Alertes de Stock", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportStock() {
        JOptionPane.showMessageDialog(this, "Fonctionnalité d'export en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadStock();
        loadProduits();
        loadHistoryData();
        showSuccessMessage("Données actualisées");
    }

    // Nouvelles méthodes pour les fonctionnaliés améliorées
    private void clearFilters() {
        searchField.setText("");
        filterCombo.setSelectedIndex(0);
        categoryFilterCombo.setSelectedIndex(0);
        loadStock();
    }

    private void showLowStock() {
        filterCombo.setSelectedItem("Stock faible");
        filterStock();
    }

    private void showOutOfStock() {
        filterCombo.setSelectedItem("Rupture de stock");
        filterStock();
    }

    private void filterHistory() {
        // TODO: Implémenter le filtrage de l'historique
        String searchText = historySearchField.getText().toLowerCase().trim();
        String filter = (String) historyFilterCombo.getSelectedItem();

        // Pour l'instant, on recharge les données
        loadHistoryData();
    }

    private void loadHistoryData() {
        historyTableModel.setRowCount(0);

        // Données d'exemple améliorées
        Object[][] sampleData = {
                { "04/08/2025 14:30", "Entrée de stock", "Longrich Nutriv", "+50", "Approvisionnement", "Admin" },
                { "04/08/2025 11:15", "Sortie de stock", "Longrich White Tea", "-25", "Vente client", "Vendeur1" },
                { "04/08/2025 09:45", "Ajustement d'inventaire", "Longrich Pi Cup", "+10", "Correction inventaire",
                        "Manager" },
                { "03/08/2025 16:20", "Entrée de stock", "Longrich Bambou", "+100", "Livraison fournisseur", "Admin" },
                { "03/08/2025 13:10", "Sortie de stock", "Longrich Superbklenz", "-15", "Commande en ligne",
                        "Vendeur2" },
                { "02/08/2025 10:30", "Transfert", "Longrich Cordyceps", "-30", "Vers magasin B", "Manager" }
        };

        for (Object[] row : sampleData) {
            historyTableModel.addRow(row);
        }
    }

    // Méthodes de validation
    private boolean validateMouvementFields() {
        clearErrors();
        boolean valid = true;

        if (produitCombo.getSelectedIndex() == -1) {
            setFieldError(produitCombo, "Veuillez sélectionner un produit");
            valid = false;
        }

        if (quantiteField.getText().trim().isEmpty()) {
            setFieldError(quantiteField, "La quantité est requise");
            valid = false;
        } else {
            try {
                int quantite = Integer.parseInt(quantiteField.getText().trim());
                if (quantite <= 0) {
                    setFieldError(quantiteField, "La quantité doit être positive");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                setFieldError(quantiteField, "Veuillez saisir un nombre valide");
                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        errorLabels.forEach((field, label) -> {
            label.setVisible(false);
            label.setText("");
            if (field instanceof JComboBox) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            } else {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    private void setFieldError(JComponent field, String message) {
        JLabel label = errorLabels.get(field);
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
        }
        if (field instanceof JComboBox) {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        }
    }

    // Méthodes utilitaires pour les messages
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}