package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import com.longrich.smartgestion.dto.FournisseurDTO;
import com.longrich.smartgestion.enums.TypeStockiste;
import com.longrich.smartgestion.service.FournisseurService;
import com.longrich.smartgestion.ui.components.ButtonFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Profile("!headless")
public class FournisseurPanel extends JPanel {

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

    private final FournisseurService fournisseurService;

    // Composants UI
    private JComboBox<TypeStockiste> typeStockisteCombo;
    private JTextField codeStockisteField;
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField adresseField;
    private JTextField telephoneField;
    private JTextField emailField;
    private JCheckBox activeCheckBox;
    private JPanel formPanel;

    private JTable fournisseurTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statsLabel;

    private FournisseurDTO currentFournisseur;
    private final Map<JComponent, JLabel> errorLabels = new HashMap<>();

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createMainContent();

        loadFournisseurs();
        updateStats();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Fournisseurs");
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
                FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportFournisseurs());
        JButton importButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_IMPORT, "Importer", PRIMARY_COLOR, e -> importFournisseurs());
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
        JLabel formTitle = new JLabel("Informations Fournisseur");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Section Type de stockiste
        formPanel.add(createSectionTitle("Type de Stockiste"));
        typeStockisteCombo = new JComboBox<>(TypeStockiste.values());
        styleComboBox(typeStockisteCombo);
        typeStockisteCombo.addActionListener(this::onTypeStockisteChange);
        formPanel.add(createFieldPanel("Type *:", typeStockisteCombo));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Identification
        formPanel.add(createSectionTitle("Identification"));

        codeStockisteField = createStyledTextField();
        nomField = createStyledTextField();
        prenomField = createStyledTextField();

        formPanel.add(createFieldPanel("Code Stockiste *:", codeStockisteField));
        formPanel.add(createFieldPanel("Nom *:", nomField));
        formPanel.add(createFieldPanel("Prénom(s):", prenomField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Contact
        formPanel.add(createSectionTitle("Contact"));

        adresseField = createStyledTextField();
        telephoneField = createStyledTextField();
        emailField = createStyledTextField();

        formPanel.add(createFieldPanel("Adresse:", adresseField));
        formPanel.add(createFieldPanel("Téléphone:", telephoneField));
        formPanel.add(createFieldPanel("Email:", emailField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Statut
        formPanel.add(createSectionTitle("Statut"));

        // Checkbox
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        checkboxPanel.setBackground(CARD_COLOR);
        checkboxPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        activeCheckBox = createStyledCheckBox("Fournisseur actif");
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
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

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

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(0, 38));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        // Custom renderer pour afficher le libellé
        comboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TypeStockiste) {
                    setText(((TypeStockiste) value).getLibelle());
                }
                return this;
            }
        });
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

        JButton saveButton = ButtonFactory.createActionButton(FontAwesomeSolid.SAVE, "Sauvegarder", SUCCESS_COLOR,
                e -> saveFournisseur());
        JButton updateButton = ButtonFactory.createActionButton(FontAwesomeSolid.EDIT, "Modifier", WARNING_COLOR,
                e -> updateFournisseur());
        JButton deleteButton = ButtonFactory.createActionButton(FontAwesomeSolid.TRASH, "Supprimer", DANGER_COLOR,
                e -> deleteFournisseur());
        JButton clearButton = ButtonFactory.createActionButton(FontAwesomeSolid.ERASER, "Vider", SECONDARY_COLOR,
                e -> clearFields());

        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        return buttonPanel;
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

        JLabel searchLabel = new JLabel("Rechercher un fournisseur");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchLabel.setForeground(TEXT_PRIMARY);
        panel.add(searchLabel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setBackground(CARD_COLOR);
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        searchField = createStyledTextField();
        searchField.addActionListener(e -> searchFournisseurs());

        JButton searchButton = ButtonFactory.createActionButton(FontAwesomeSolid.SEARCH, "", PRIMARY_COLOR, e -> searchFournisseurs());
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

        JLabel tableTitle = new JLabel("Liste des Fournisseurs");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        panel.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table
        createTable();
        JScrollPane scrollPane = new JScrollPane(fournisseurTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void createTable() {
        String[] columns = {
                "#", "Code Stockiste", "Type", "Nom", "Prénom", "Téléphone",
                "Email", "Statut"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fournisseurTable = new JTable(tableModel);
        fournisseurTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fournisseurTable.setRowHeight(45);
        fournisseurTable.setShowVerticalLines(false);
        fournisseurTable.setGridColor(new Color(243, 244, 246));
        fournisseurTable.setSelectionBackground(new Color(239, 246, 255));
        fournisseurTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = fournisseurTable.getTableHeader();
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
                if (column == 7 && value != null) { // Colonne Statut
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

        for (int i = 0; i < fournisseurTable.getColumnCount(); i++) {
            fournisseurTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        fournisseurTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedFournisseur();
            }
        });
    }

    private void loadFournisseurs() {
        try {
            List<FournisseurDTO> fournisseurs = fournisseurService.getActiveFournisseurs();
            tableModel.setRowCount(0);

            for (int i = 0; i < fournisseurs.size(); i++) {
                FournisseurDTO fournisseur = fournisseurs.get(i);
                Object[] row = {
                        i + 1,
                        fournisseur.getCodeStockiste(),
                        fournisseur.getTypeStockiste().getLibelle(),
                        fournisseur.getNom(),
                        fournisseur.getPrenom() != null ? fournisseur.getPrenom() : "-",
                        fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "-",
                        fournisseur.getEmail() != null ? fournisseur.getEmail() : "-",
                        fournisseur.getActive() ? "Actif" : "Inactif"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            tableModel.setRowCount(0);
        }
    }

    private void updateStats() {
        try {
            List<FournisseurDTO> allFournisseurs = fournisseurService.getAllFournisseurs();
            long activeCount = allFournisseurs.stream().filter(FournisseurDTO::getActive).count();
            long personnePhysiqueCount = allFournisseurs.stream()
                    .filter(f -> f.getTypeStockiste() == TypeStockiste.PERSONNE_PHYSIQUE).count();
            long personneMoraleCount = allFournisseurs.stream()
                    .filter(f -> f.getTypeStockiste() == TypeStockiste.PERSONNE_MORALE).count();

            statsLabel.setText(String.format("Total: %d • Actifs: %d • Pers. Physique: %d • Pers. Morale: %d",
                    allFournisseurs.size(), activeCount, personnePhysiqueCount, personneMoraleCount));
        } catch (Exception e) {
            statsLabel.setText("Statistiques non disponibles");
        }
    }

    private void searchFournisseurs() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadFournisseurs();
            return;
        }

        try {
            List<FournisseurDTO> fournisseurs = fournisseurService.searchFournisseurs(searchText);
            tableModel.setRowCount(0);

            for (int i = 0; i < fournisseurs.size(); i++) {
                FournisseurDTO fournisseur = fournisseurs.get(i);
                Object[] row = {
                        i + 1,
                        fournisseur.getCodeStockiste(),
                        fournisseur.getTypeStockiste().getLibelle(),
                        fournisseur.getNom(),
                        fournisseur.getPrenom() != null ? fournisseur.getPrenom() : "-",
                        fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "-",
                        fournisseur.getEmail() != null ? fournisseur.getEmail() : "-",
                        fournisseur.getActive() ? "Actif" : "Inactif"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private void loadSelectedFournisseur() {
        int selectedRow = fournisseurTable.getSelectedRow();
        if (selectedRow >= 0) {
            String codeStockiste = (String) tableModel.getValueAt(selectedRow, 1);
            fournisseurService.getFournisseurByCodeStockiste(codeStockiste).ifPresent(fournisseur -> {
                currentFournisseur = fournisseur;
                populateFields(fournisseur);
            });
        }
    }

    private void populateFields(FournisseurDTO fournisseur) {
        typeStockisteCombo.setSelectedItem(fournisseur.getTypeStockiste());
        codeStockisteField.setText(fournisseur.getCodeStockiste());
        nomField.setText(fournisseur.getNom());
        prenomField.setText(fournisseur.getPrenom() != null ? fournisseur.getPrenom() : "");
        adresseField.setText(fournisseur.getAdresse() != null ? fournisseur.getAdresse() : "");
        telephoneField.setText(fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "");
        emailField.setText(fournisseur.getEmail() != null ? fournisseur.getEmail() : "");
        activeCheckBox.setSelected(fournisseur.getActive());

        // Actualiser la visibilité du champ prénom
        updatePrenomFieldVisibility();
    }

    private void clearErrors() {
        errorLabels.forEach((field, label) -> {
            label.setVisible(false);
            label.setText("");
            if (field instanceof JComboBox) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
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
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }
    }

    private boolean validateFields() {
        clearErrors();
        boolean valid = true;

        if (codeStockisteField.getText().trim().isEmpty()) {
            setFieldError(codeStockisteField, "Code stockiste requis");
            valid = false;
        }
        if (nomField.getText().trim().isEmpty()) {
            setFieldError(nomField, "Nom requis");
            valid = false;
        }

        TypeStockiste type = (TypeStockiste) typeStockisteCombo.getSelectedItem();
        if (type == TypeStockiste.PERSONNE_PHYSIQUE && prenomField.getText().trim().isEmpty()) {
            setFieldError(prenomField, "Prénom requis pour une personne physique");
            valid = false;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            setFieldError(emailField, "Email invalide");
            valid = false;
        }

        return valid;
    }

    private void saveFournisseur() {
        if (!validateFields()) {
            return;
        }
        try {
            FournisseurDTO fournisseur = createFournisseurFromFields();
            fournisseurService.saveFournisseur(fournisseur);
            showSuccessMessage("Fournisseur sauvegardé avec succès");
            clearFields();
            loadFournisseurs();
            updateStats();
        } catch (IllegalArgumentException e) {
            setFieldError(codeStockisteField, e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void updateFournisseur() {
        if (currentFournisseur == null) {
            showWarningMessage("Veuillez sélectionner un fournisseur à modifier");
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            FournisseurDTO fournisseur = createFournisseurFromFields();
            fournisseurService.updateFournisseur(currentFournisseur.getId(), fournisseur);
            showSuccessMessage("Fournisseur mis à jour avec succès");
            clearFields();
            loadFournisseurs();
            updateStats();
        } catch (IllegalArgumentException e) {
            setFieldError(codeStockisteField, e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void deleteFournisseur() {
        if (currentFournisseur == null) {
            showWarningMessage("Veuillez sélectionner un fournisseur à supprimer");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer ce fournisseur ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                fournisseurService.deleteFournisseur(currentFournisseur.getId());
                showSuccessMessage("Fournisseur supprimé avec succès");
                clearFields();
                loadFournisseurs();
                updateStats();
            } catch (Exception e) {
                showErrorMessage("Erreur: " + e.getMessage());
            }
        }
    }

    private FournisseurDTO createFournisseurFromFields() {
        return FournisseurDTO.builder()
                .typeStockiste((TypeStockiste) typeStockisteCombo.getSelectedItem())
                .codeStockiste(codeStockisteField.getText().trim())
                .nom(nomField.getText().trim())
                .prenom(prenomField.getText().trim())
                .adresse(adresseField.getText().trim())
                .telephone(telephoneField.getText().trim())
                .email(emailField.getText().trim())
                .active(activeCheckBox.isSelected())
                .build();
    }

    private void clearFields() {
        clearErrors();
        currentFournisseur = null;
        typeStockisteCombo.setSelectedIndex(0);
        codeStockisteField.setText("");
        nomField.setText("");
        prenomField.setText("");
        adresseField.setText("");
        telephoneField.setText("");
        emailField.setText("");
        activeCheckBox.setSelected(true);
        fournisseurTable.clearSelection();
        updatePrenomFieldVisibility();
    }

    private void onTypeStockisteChange(ActionEvent e) {
        updatePrenomFieldVisibility();
    }

    private void updatePrenomFieldVisibility() {
        TypeStockiste selectedType = (TypeStockiste) typeStockisteCombo.getSelectedItem();
        boolean isPersonnePhysique = selectedType == TypeStockiste.PERSONNE_PHYSIQUE;

        // Rendre le champ prénom obligatoire visuellement pour personne physique
        JPanel prenomFieldPanel = (JPanel) prenomField.getParent().getParent();
        JLabel prenomLabel = null;
        for (java.awt.Component comp : prenomFieldPanel.getComponents()) {
            if (comp instanceof JLabel) {
                prenomLabel = (JLabel) comp;
                break;
            }
        }

        if (prenomLabel != null) {
            if (isPersonnePhysique) {
                prenomLabel.setText("Prénom(s) *:");
                prenomLabel.setForeground(TEXT_PRIMARY);
            } else {
                prenomLabel.setText("Prénom(s):");
                prenomLabel.setForeground(TEXT_SECONDARY);
            }
        }

        formPanel.revalidate();
        formPanel.repaint();
    }

    private void exportFournisseurs() {
        showInfoMessage("Fonctionnalité d'export en cours de développement");
    }

    private void importFournisseurs() {
        showInfoMessage("Fonctionnalité d'import en cours de développement");
    }

    private void refreshData() {
        loadFournisseurs();
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
