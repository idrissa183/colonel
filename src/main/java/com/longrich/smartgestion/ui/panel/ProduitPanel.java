package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.ui.components.ButtonFactory;

import lombok.RequiredArgsConstructor;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;

import javax.swing.*;
// import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
// import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
// import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Profile("!headless")
public class ProduitPanel extends JPanel {

    // Couleurs modernes
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);

    private final ProduitService produitService;

    // Composants UI
    private JTextField codeBarreField;
    private JTextField libelleField;
    private JTextArea descriptionArea;
    private JTextField datePeremptionField;
    private JTextField prixAchatField;
    private JTextField prixReventeField;
    private JTextField pvField;
    private JComboBox<String> familleCombo;
    private JTextField stockMinimumField;
    private JCheckBox activeCheckBox;

    private JTable produitTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statsLabel;

    private ProduitDto currentProduit;

    private final Map<JComponent, JLabel> errorLabels = new HashMap<>();

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createMainContent();

        loadProduits();
        updateStats();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Produits");
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
                FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportProduits());
        JButton importButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_IMPORT, "Importer", PRIMARY_COLOR, e -> importProduits());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR, e -> refreshData());

        panel.add(exportButton);
        panel.add(importButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.35);

        // Panneau de gauche - Formulaire
        JPanel formContainer = createFormContainer();
        splitPane.setLeftComponent(formContainer);

        // Panneau de droite - Table et recherche
        JPanel tableContainer = createTableContainer();
        splitPane.setRightComponent(tableContainer);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createFormContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        JPanel formPanel = createModernFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(createButtonPanel(), BorderLayout.SOUTH);

        return container;
    }

    private JPanel createModernFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        // Titre du formulaire
        JLabel formTitle = new JLabel("Informations Produit");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Section Identification
        formPanel.add(createSectionTitle("Identification"));
        codeBarreField = createStyledTextField();
        libelleField = createStyledTextField();

        formPanel.add(createFieldPanel("Code barre:", codeBarreField));
        formPanel.add(createFieldPanel("Libellé:", libelleField));

        // Description avec TextArea stylée
        descriptionArea = createStyledTextArea();
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(0, 80));
        descScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        formPanel.add(createFieldPanel("Description:", descScrollPane));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Famille
        formPanel.add(createSectionTitle("Catégorie"));
        familleCombo = new JComboBox<>();
        styleComboBox(familleCombo);
        loadFamilles();
        formPanel.add(createFieldPanel("Famille:", familleCombo));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Prix et PV
        formPanel.add(createSectionTitle("Prix et Points de Vente"));
        prixAchatField = createStyledTextField();
        prixReventeField = createStyledTextField();
        pvField = createStyledTextField();

        formPanel.add(createFieldPanel("Prix d'achat (FCFA):", prixAchatField));
        formPanel.add(createFieldPanel("Prix de revente (FCFA):", prixReventeField));
        formPanel.add(createFieldPanel("Nombre de PV:", pvField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Stock et Dates
        formPanel.add(createSectionTitle("Stock et Dates"));
        stockMinimumField = createStyledTextField();
        datePeremptionField = createStyledTextField();
        datePeremptionField.setToolTipText("Format: YYYY-MM-DD (ex: 2024-12-31)");

        formPanel.add(createFieldPanel("Stock minimum:", stockMinimumField));
        formPanel.add(createFieldPanel("Date péremption:", datePeremptionField));

        // Checkbox
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        checkboxPanel.setBackground(CARD_COLOR);
        checkboxPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        activeCheckBox = createStyledCheckBox("Produit actif");
        activeCheckBox.setSelected(true);
        checkboxPanel.add(activeCheckBox);

        formPanel.add(checkboxPanel);
        formPanel.add(Box.createVerticalGlue());

        return formPanel;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(PRIMARY_COLOR);
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Focus effects
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });

        return field;
    }

    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea(3, 0);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(Color.WHITE);
        area.setForeground(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return area;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        comboBox.setPreferredSize(new Dimension(0, 36));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        checkBox.setBackground(CARD_COLOR);
        checkBox.setForeground(TEXT_PRIMARY);
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));

        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, field instanceof JScrollPane ? 120 : 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
        panel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

        errorLabels.put(field, errorLabel);

        return panel;
    }

    private JPanel createTableContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        // Panneau de recherche
        JPanel searchPanel = createSearchPanel();
        container.add(searchPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = createTablePanel();
        container.add(tablePanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel searchLabel = new JLabel("Rechercher un produit");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchLabel.setForeground(TEXT_PRIMARY);
        panel.add(searchLabel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setBackground(CARD_COLOR);
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        searchField = createStyledTextField();
        searchField.addActionListener(e -> searchProduits());

        JButton searchButton = ButtonFactory.createActionButton(FontAwesomeSolid.SEARCH, "", PRIMARY_COLOR,
                e -> searchProduits());
        searchButton.setPreferredSize(new Dimension(50, 38));

        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);

        panel.add(searchInputPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // En-tête de table
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(CARD_COLOR);
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel tableTitle = new JLabel("Liste des Produits");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);

        // Modèle de table
        String[] columns = { "Code", "Libellé", "Prix Achat", "Prix Revente", "PV", "Stock", "Statut" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        produitTable = new JTable(tableModel);
        styleTable(produitTable);

        // Listener pour la sélection
        produitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedProduit();
            }
        });

        // Click handler pour double-click
        produitTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedProduit();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(produitTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
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
        header.setResizingAllowed(true);

        // Cell renderer with alternating colors
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

                // Status column special formatting
                if (column == 6 && value != null) {
                    boolean isActive = Boolean.parseBoolean(value.toString());
                    setText(isActive ? "Actif" : "Inactif");
                    setForeground(isActive ? SUCCESS_COLOR : TEXT_SECONDARY);
                } else {
                    setForeground(TEXT_PRIMARY);
                }

                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Column widths
        if (table.getColumnCount() >= 7) {
            table.getColumnModel().getColumn(0).setPreferredWidth(100); // Code
            table.getColumnModel().getColumn(1).setPreferredWidth(200); // Libellé
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Prix Achat
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // Prix Revente
            table.getColumnModel().getColumn(4).setPreferredWidth(80); // PV
            table.getColumnModel().getColumn(5).setPreferredWidth(80); // Stock
            table.getColumnModel().getColumn(6).setPreferredWidth(80); // Statut
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = ButtonFactory.createActionButton(FontAwesomeSolid.SAVE, "Sauvegarder", SUCCESS_COLOR,
                e -> saveProduit());
        JButton updateButton = ButtonFactory.createActionButton(FontAwesomeSolid.EDIT, "Modifier", WARNING_COLOR,
                e -> updateProduit());
        JButton deleteButton = ButtonFactory.createActionButton(FontAwesomeSolid.TRASH, "Supprimer", DANGER_COLOR,
                e -> deleteProduit());
        JButton clearButton = ButtonFactory.createActionButton(FontAwesomeSolid.ERASER, "Vider", SECONDARY_COLOR,
                e -> clearFields());

        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        return buttonPanel;
    }

    private void updateStats() {
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            int totalProduits = produits.size();
            long produitsActifs = produits.stream().mapToLong(p -> p.getActive() ? 1 : 0).sum();

            statsLabel.setText(String.format("%d produits • %d actifs", totalProduits, produitsActifs));
        } catch (Exception e) {
            statsLabel.setText("Statistiques non disponibles");
        }
    }

    private void exportProduits() {
        JOptionPane.showMessageDialog(this, "Fonctionnalité d'export en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void importProduits() {
        JOptionPane.showMessageDialog(this, "Fonctionnalité d'import en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadProduits();
        updateStats();
        clearFields();
    }

    private void loadFamilles() {
        // Charger les familles de produits
        familleCombo.removeAllItems();
        familleCombo.addItem("Beauté");
        familleCombo.addItem("Soins");
        familleCombo.addItem("Autre");
    }

    private void loadProduits() {
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            tableModel.setRowCount(0);

            for (ProduitDto produit : produits) {
                Object[] row = {
                        produit.getCodeBarre(),
                        produit.getLibelle(),
                        produit.getPrixAchat() != null ? produit.getPrixAchat() + " FCFA" : "-",
                        produit.getPrixRevente() != null ? produit.getPrixRevente() + " FCFA" : "-",
                        produit.getPv() != null ? produit.getPv().toString() : "-",
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getActive() != null ? produit.getActive().toString() : "true"
                };
                tableModel.addRow(row);
            }
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des produits: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProduits() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadProduits();
            return;
        }

        try {
            List<ProduitDto> produits = produitService.searchProduits(searchText);
            tableModel.setRowCount(0);

            for (ProduitDto produit : produits) {
                Object[] row = {
                        produit.getCodeBarre(),
                        produit.getLibelle(),
                        produit.getPrixAchat() != null ? produit.getPrixAchat() + " FCFA" : "-",
                        produit.getPrixRevente() != null ? produit.getPrixRevente() + " FCFA" : "-",
                        produit.getPv() != null ? produit.getPv().toString() : "-",
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getActive() != null ? produit.getActive().toString() : "true"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedProduit() {
        int selectedRow = produitTable.getSelectedRow();
        if (selectedRow >= 0) {
            String codeBarre = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                List<ProduitDto> produits = produitService.searchProduits(codeBarre);
                if (!produits.isEmpty()) {
                    ProduitDto produit = produits.stream()
                            .filter(p -> codeBarre.equals(p.getCodeBarre()))
                            .findFirst()
                            .orElse(produits.get(0));
                    currentProduit = produit;
                    populateFields(produit);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur lors du chargement du produit: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateFields(ProduitDto produit) {
        codeBarreField.setText(produit.getCodeBarre());
        libelleField.setText(produit.getLibelle());
        descriptionArea.setText(produit.getDescription());
        datePeremptionField.setText(produit.getDatePeremption() != null ? produit.getDatePeremption().toString() : "");
        prixAchatField.setText(produit.getPrixAchat() != null ? produit.getPrixAchat().toString() : "");
        prixReventeField.setText(produit.getPrixRevente() != null ? produit.getPrixRevente().toString() : "");
        pvField.setText(produit.getPv() != null ? produit.getPv().toString() : "");
        familleCombo.setSelectedItem(produit.getFamilleName());
        stockMinimumField.setText(produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "");
        activeCheckBox.setSelected(produit.getActive());
    }

    private void clearErrors() {
        errorLabels.forEach((field, label) -> {
            label.setVisible(false);
            label.setText("");
            if (field instanceof JComboBox) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            } else if (field instanceof JScrollPane) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
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
                    BorderFactory.createLineBorder(DANGER_COLOR, 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        } else if (field instanceof JScrollPane) {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }
    }

    private void saveProduit() {
        if (!validateFields()) {
            return;
        }

        try {
            ProduitDto produit = createProduitFromFields();
            produitService.saveProduit(produit);

            // Animation de succès
            JOptionPane.showMessageDialog(this,
                    "✓ Produit sauvegardé avec succès",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadProduits();
        } catch (IllegalArgumentException e) {
            setFieldError(codeBarreField, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Erreur lors de la sauvegarde: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduit() {
        if (currentProduit == null) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Veuillez sélectionner un produit à modifier",
                    "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            ProduitDto produit = createProduitFromFields();
            produitService.updateProduit(currentProduit.getId(), produit);

            JOptionPane.showMessageDialog(this,
                    "✓ Produit mis à jour avec succès",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadProduits();
        } catch (IllegalArgumentException e) {
            setFieldError(codeBarreField, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Erreur lors de la mise à jour: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduit() {
        if (currentProduit == null) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Veuillez sélectionner un produit à supprimer",
                    "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Dialog de confirmation moderne
        int option = JOptionPane.showConfirmDialog(
                this,
                String.format("🗑️ Êtes-vous sûr de vouloir supprimer ce produit ?\n\n" +
                        "Produit: %s\n" +
                        "Code: %s\n\n" +
                        "Cette action est irréversible.",
                        currentProduit.getLibelle(),
                        currentProduit.getCodeBarre()),
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                produitService.deleteProduit(currentProduit.getId());

                JOptionPane.showMessageDialog(this,
                        "✓ Produit supprimé avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);

                clearFields();
                loadProduits();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Erreur lors de la suppression: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ProduitDto createProduitFromFields() {
        ProduitDto.ProduitDtoBuilder builder = ProduitDto.builder()
                .codeBarre(codeBarreField.getText().trim())
                .libelle(libelleField.getText().trim())
                .description(descriptionArea.getText().trim())
                .active(activeCheckBox.isSelected());

        // Date de péremption
        String dateStr = datePeremptionField.getText().trim();
        if (!dateStr.isEmpty()) {
            try {
                builder.datePeremption(LocalDate.parse(dateStr));
            } catch (Exception e) {
                throw new IllegalArgumentException("Format de date invalide. Utilisez YYYY-MM-DD");
            }
        }

        // Prix d'achat
        String prixAchatStr = prixAchatField.getText().trim();
        if (!prixAchatStr.isEmpty()) {
            try {
                builder.prixAchat(new BigDecimal(prixAchatStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Prix d'achat invalide");
            }
        }

        // Prix de revente
        String prixReventeStr = prixReventeField.getText().trim();
        if (!prixReventeStr.isEmpty()) {
            try {
                builder.prixRevente(new BigDecimal(prixReventeStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Prix de revente invalide");
            }
        }

        // PV
        String pvStr = pvField.getText().trim();
        if (!pvStr.isEmpty()) {
            try {
                builder.pv(new BigDecimal(pvStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Nombre de PV invalide");
            }
        }

        // Stock minimum
        String stockMinStr = stockMinimumField.getText().trim();
        if (!stockMinStr.isEmpty()) {
            try {
                builder.stockMinimum(Integer.parseInt(stockMinStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Stock minimum invalide");
            }
        }

        return builder.build();
    }

    private boolean validateFields() {
        clearErrors();
        boolean valid = true;

        if (codeBarreField.getText().trim().isEmpty()) {
            setFieldError(codeBarreField, "Code barre requis");
            valid = false;
        }
        if (libelleField.getText().trim().isEmpty()) {
            setFieldError(libelleField, "Libellé requis");
            valid = false;
        }

        String dateStr = datePeremptionField.getText().trim();
        if (!dateStr.isEmpty()) {
            try {
                LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                setFieldError(datePeremptionField, "Format de date invalide (YYYY-MM-DD)");
                valid = false;
            }
        }

        String prixAchatStr = prixAchatField.getText().trim();
        if (!prixAchatStr.isEmpty()) {
            try {
                BigDecimal prixAchat = new BigDecimal(prixAchatStr);
                if (prixAchat.compareTo(BigDecimal.ZERO) < 0) {
                    setFieldError(prixAchatField, "Le prix d'achat ne peut pas être négatif");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                setFieldError(prixAchatField, "Format du prix d'achat invalide");
                valid = false;
            }
        }

        String prixReventeStr = prixReventeField.getText().trim();
        if (!prixReventeStr.isEmpty()) {
            try {
                BigDecimal prixRevente = new BigDecimal(prixReventeStr);
                if (prixRevente.compareTo(BigDecimal.ZERO) < 0) {
                    setFieldError(prixReventeField, "Le prix de revente ne peut pas être négatif");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                setFieldError(prixReventeField, "Format du prix de revente invalide");
                valid = false;
            }
        }

        String stockMinStr = stockMinimumField.getText().trim();
        if (!stockMinStr.isEmpty()) {
            try {
                int stockMin = Integer.parseInt(stockMinStr);
                if (stockMin < 0) {
                    setFieldError(stockMinimumField, "Le stock minimum ne peut pas être négatif");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                setFieldError(stockMinimumField, "Format du stock minimum invalide");
                valid = false;
            }
        }

        return valid;
    }

    private void clearFields() {
        clearErrors();
        currentProduit = null;
        codeBarreField.setText("");
        libelleField.setText("");
        descriptionArea.setText("");
        datePeremptionField.setText("");
        prixAchatField.setText("");
        prixReventeField.setText("");
        pvField.setText("");
        familleCombo.setSelectedIndex(-1);
        stockMinimumField.setText("");
        activeCheckBox.setSelected(true);
        produitTable.clearSelection();
    }
}