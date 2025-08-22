package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;

import com.longrich.smartgestion.dto.FamilleProduitDTO;
import com.longrich.smartgestion.service.FamilleProduitService;
import com.longrich.smartgestion.ui.components.ButtonFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Profile("!headless")
public class FamilleProduitPanel extends JPanel {

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

    private final FamilleProduitService familleProduitService;

    // Composants UI
    private JTextField libelleFamilleField;
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;
    private JPanel formPanel;

    private JTable famillesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statsLabel;

    private FamilleProduitDTO currentFamille;
    private final Map<JComponent, JLabel> errorLabels = new HashMap<>();
    
    // Boutons d'action
    private JButton saveButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    
    // Mode actuel (ajout, modification, suppression)
    private enum FormMode { ADD, EDIT, DELETE }
    private FormMode currentMode = FormMode.ADD;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createHeaderPanel();
        createMainContent();

        loadFamilles();
        updateStats();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Familles de Produits");
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
                FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportFamilles());
        JButton importButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_IMPORT, "Importer", PRIMARY_COLOR, e -> importFamilles());
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

        formPanel = createModernFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(createButtonPanel(), BorderLayout.SOUTH);

        return container;
    }

    private JPanel createModernFormPanel() {
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        // Titre du formulaire
        JLabel formTitle = new JLabel("Informations Famille");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Section Identification
        formPanel.add(createSectionTitle("Identification"));
        libelleFamilleField = createStyledTextField();
        formPanel.add(createFieldPanel("Libellé Famille *:", libelleFamilleField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Description
        formPanel.add(createSectionTitle("Description"));
        descriptionArea = createStyledTextArea();
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(0, 100));
        descScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        formPanel.add(createFieldPanel("Description:", descScrollPane));
        formPanel.add(Box.createVerticalStrut(15));

        // Checkbox
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        checkboxPanel.setBackground(CARD_COLOR);
        checkboxPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        activeCheckBox = createStyledCheckBox("Famille active");
        activeCheckBox.setSelected(true);
        checkboxPanel.add(activeCheckBox);

        formPanel.add(checkboxPanel);
        formPanel.add(Box.createVerticalGlue());

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

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

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
        panel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

        errorLabels.put(field, errorLabel);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Effet focus
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
        JTextArea area = new JTextArea(4, 0);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(Color.WHITE);
        area.setForeground(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return area;
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        checkBox.setForeground(TEXT_SECONDARY);
        checkBox.setBackground(CARD_COLOR);
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        saveButton = ButtonFactory.createActionButton(FontAwesomeSolid.SAVE, "Sauvegarder", SUCCESS_COLOR,
                e -> saveFamille());
        updateButton = ButtonFactory.createActionButton(FontAwesomeSolid.EDIT, "Modifier", WARNING_COLOR,
                e -> updateFamille());
        deleteButton = ButtonFactory.createActionButton(FontAwesomeSolid.TRASH, "Supprimer", DANGER_COLOR,
                e -> deleteFamille());
        clearButton = ButtonFactory.createActionButton(FontAwesomeSolid.ERASER, "Vider", SECONDARY_COLOR,
                e -> clearFields());

        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        updateButtonVisibility();

        return buttonPanel;
    }
    
    /**
     * Met à jour la visibilité des boutons selon le mode actuel
     */
    private void updateButtonVisibility() {
        switch (currentMode) {
            case ADD:
                saveButton.setVisible(true);
                updateButton.setVisible(false);
                deleteButton.setVisible(false);
                clearButton.setVisible(true);
                break;
            case EDIT:
                saveButton.setVisible(false);
                updateButton.setVisible(true);
                deleteButton.setVisible(true);
                clearButton.setVisible(true);
                break;
            case DELETE:
                saveButton.setVisible(false);
                updateButton.setVisible(false);
                deleteButton.setVisible(true);
                clearButton.setVisible(true);
                break;
        }
        
        // Forcer le rafraîchissement du layout
        if (saveButton.getParent() != null) {
            saveButton.getParent().revalidate();
            saveButton.getParent().repaint();
        }
    }
    
    /**
     * Change le mode du formulaire
     */
    private void setFormMode(FormMode mode) {
        this.currentMode = mode;
        updateButtonVisibility();
    }

    private JPanel createTableContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        // Panel de recherche
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

        JLabel searchLabel = new JLabel("Rechercher une famille");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchLabel.setForeground(TEXT_PRIMARY);
        panel.add(searchLabel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setBackground(CARD_COLOR);
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        searchField = createStyledTextField();
        searchField.addActionListener(e -> searchFamilles());

        JButton searchButton = ButtonFactory.createActionButton(FontAwesomeSolid.SEARCH, "", PRIMARY_COLOR,
                e -> searchFamilles());
        searchButton.setPreferredSize(new Dimension(50, 38));

        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);

        panel.add(searchInputPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // En-tête de table
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(CARD_COLOR);
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel tableTitle = new JLabel("Liste des Familles");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        panel.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table
        createTable();
        JScrollPane scrollPane = new JScrollPane(famillesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void createTable() {
        String[] columns = {
                "ID", "Libellé Famille", "Description", "Statut"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        famillesTable = new JTable(tableModel);
        famillesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        famillesTable.setRowHeight(45);
        famillesTable.setShowVerticalLines(false);
        famillesTable.setGridColor(new Color(243, 244, 246));
        famillesTable.setSelectionBackground(new Color(239, 246, 255));
        famillesTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = famillesTable.getTableHeader();
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 45));

        // Renderer personnalisé pour les cellules
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }

                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                // Coloration spéciale pour le statut
                if (column == 3 && value != null) { // Colonne Statut
                    if ("Actif".equals(value.toString())) {
                        setForeground(SUCCESS_COLOR);
                    } else {
                        setForeground(DANGER_COLOR);
                    }
                } else {
                    setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        };

        for (int i = 0; i < famillesTable.getColumnCount(); i++) {
            famillesTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        famillesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedFamille();
            }
        });
    }

    private void loadFamilles() {
        try {
            List<FamilleProduitDTO> familles = familleProduitService.getAllFamilles();
            tableModel.setRowCount(0);

            for (FamilleProduitDTO famille : familles) {
                Object[] row = {
                        famille.getId(),
                        famille.getLibelleFamille(),
                        famille.getDescription() != null ? famille.getDescription() : "",
                        famille.getActive() ? "Actif" : "Inactif"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            tableModel.setRowCount(0);
        }
    }

    private void updateStats() {
        try {
            List<FamilleProduitDTO> allFamilles = familleProduitService.getAllFamilles();
            long activeCount = allFamilles.stream().filter(FamilleProduitDTO::getActive).count();

            statsLabel.setText(String.format("Total: %d • Actives: %d",
                    allFamilles.size(), activeCount));
        } catch (Exception e) {
            statsLabel.setText("Statistiques non disponibles");
        }
    }

    private void searchFamilles() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadFamilles();
            return;
        }

        try {
            List<FamilleProduitDTO> familles = familleProduitService.getAllFamilles();
            tableModel.setRowCount(0);

            for (FamilleProduitDTO famille : familles) {
                if (famille.getLibelleFamille().toLowerCase().contains(searchText.toLowerCase()) ||
                    (famille.getDescription() != null && famille.getDescription().toLowerCase().contains(searchText.toLowerCase()))) {
                    Object[] row = {
                            famille.getId(),
                            famille.getLibelleFamille(),
                            famille.getDescription() != null ? famille.getDescription() : "",
                            famille.getActive() ? "Actif" : "Inactif"
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private void loadSelectedFamille() {
        int selectedRow = famillesTable.getSelectedRow();
        
        if (selectedRow >= 0) {
            try {
                Long familleId = (Long) tableModel.getValueAt(selectedRow, 0);
                
                List<FamilleProduitDTO> familles = familleProduitService.getAllFamilles();
                familles.stream()
                    .filter(f -> f.getId().equals(familleId))
                    .findFirst()
                    .ifPresent(famille -> {
                        currentFamille = famille;
                        populateFields(famille);
                        setFormMode(FormMode.EDIT);
                    });
            } catch (Exception e) {
                showErrorMessage("Erreur lors du chargement de la famille: " + e.getMessage());
            }
        }
    }

    private void populateFields(FamilleProduitDTO famille) {
        libelleFamilleField.setText(famille.getLibelleFamille() != null ? famille.getLibelleFamille() : "");
        descriptionArea.setText(famille.getDescription() != null ? famille.getDescription() : "");
        activeCheckBox.setSelected(famille.getActive() != null ? famille.getActive() : true);
    }

    private void clearErrors() {
        errorLabels.forEach((field, label) -> {
            label.setVisible(false);
            label.setText("");
            if (field instanceof JScrollPane) {
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
        if (field instanceof JScrollPane) {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }
    }

    private boolean validateFields() {
        clearErrors();
        boolean valid = true;

        if (libelleFamilleField.getText().trim().isEmpty()) {
            setFieldError(libelleFamilleField, "Libellé famille requis");
            valid = false;
        }

        return valid;
    }

    private void saveFamille() {
        if (!validateFields()) {
            return;
        }
        try {
            FamilleProduitDTO famille = createFamilleFromFields();
            familleProduitService.saveFamille(famille);
            showSuccessMessage("Famille sauvegardée avec succès");
            clearFields();
            loadFamilles();
            updateStats();
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void updateFamille() {
        if (currentFamille == null) {
            showWarningMessage("Veuillez sélectionner une famille à modifier");
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            FamilleProduitDTO famille = createFamilleFromFields();
            familleProduitService.updateFamille(currentFamille.getId(), famille);
            showSuccessMessage("Famille mise à jour avec succès");
            clearFields();
            loadFamilles();
            updateStats();
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void deleteFamille() {
        if (currentFamille == null) {
            showWarningMessage("Veuillez sélectionner une famille à supprimer");
            return;
        }

        String message = String.format(
                "🗑️ Êtes-vous sûr de vouloir supprimer cette famille ?\n\n" +
                "Famille: %s\n" +
                "ID: %d\n\n" +
                "Cette action est irréversible.",
                currentFamille.getLibelleFamille(),
                currentFamille.getId()
        );
        
        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                familleProduitService.deleteFamille(currentFamille.getId());
                showSuccessMessage("Famille supprimée avec succès");
                clearFields();
                loadFamilles();
                updateStats();
            } catch (Exception e) {
                showErrorMessage("Erreur: " + e.getMessage());
            }
        }
    }

    private FamilleProduitDTO createFamilleFromFields() {
        return FamilleProduitDTO.builder()
                .libelleFamille(libelleFamilleField.getText().trim())
                .description(descriptionArea.getText().trim())
                .active(activeCheckBox.isSelected())
                .build();
    }

    private void clearFields() {
        clearErrors();
        currentFamille = null;
        libelleFamilleField.setText("");
        descriptionArea.setText("");
        activeCheckBox.setSelected(true);
        famillesTable.clearSelection();
        setFormMode(FormMode.ADD);
    }

    private void exportFamilles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter les familles");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV", "csv"));
        fileChooser.setSelectedFile(new File("familles.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                int columnCount = tableModel.getColumnCount();

                for (int i = 0; i < columnCount; i++) {
                    writer.print(tableModel.getColumnName(i));
                    if (i < columnCount - 1) {
                        writer.print(",");
                    }
                }
                writer.println();

                int rowCount = tableModel.getRowCount();
                for (int r = 0; r < rowCount; r++) {
                    for (int c = 0; c < columnCount; c++) {
                        Object value = tableModel.getValueAt(r, c);
                        String cell = value != null ? value.toString() : "";
                        cell = cell.replace("\"", "\"\"");
                        if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                            cell = "\"" + cell + "\"";
                        }
                        writer.print(cell);
                        if (c < columnCount - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }

                showSuccessMessage("Familles exportées vers " + file.getAbsolutePath());
            } catch (IOException ex) {
                showErrorMessage("Erreur lors de l'exportation: " + ex.getMessage());
            }
        }
    }

    private void importFamilles() {
        showInfoMessage("Fonctionnalité d'import en cours de développement");
    }

    private void refreshData() {
        loadFamilles();
        updateStats();
        showSuccessMessage("Données actualisées");
    }

    // Méthodes utilitaires pour les messages
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