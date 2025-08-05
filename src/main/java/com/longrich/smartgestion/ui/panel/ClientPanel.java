package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.enums.TypeClient;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.ProvinceService;
import com.longrich.smartgestion.ui.components.ButtonFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Profile("!headless")
public class ClientPanel extends JPanel {

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

    private final ClientService clientService;
    private final ProvinceService provinceService;

    // Composants UI
    private JTextField nomField;
    private JTextField prenomField;
    private JComboBox<String> provinceCombo;
    private JTextField telephoneField;
    private JTextField emailField;
    private JComboBox<TypeClient> typeClientCombo;
    private JTextField adresseField;
    private JCheckBox activeCheckBox;
    private JTextField cnibField;
    private JTextField codePartenaireField;
    private JTextField totalPvField;
    private JCheckBox codeDefinitifCheckBox;
    private JPanel formPanel;
    private JPanel longrichSection;

    private JTable clientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statsLabel;

    private ClientDTO currentClient;
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
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createMainContent();

        loadClients();
        updateStats();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Clients");
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
                FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportClients());
        JButton importButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_IMPORT, "Importer", PRIMARY_COLOR, e -> importClients());
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
        JLabel formTitle = new JLabel("Informations Client");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Section Type de client
        formPanel.add(createSectionTitle("Type de Client"));
        typeClientCombo = new JComboBox<>(TypeClient.values());
        styleComboBox(typeClientCombo);
        typeClientCombo.addActionListener(this::onTypeClientChange);
        formPanel.add(createFieldPanel("Type:", typeClientCombo));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Informations personnelles
        formPanel.add(createSectionTitle("Informations Personnelles"));

        nomField = createStyledTextField();
        prenomField = createStyledTextField();
        codePartenaireField = createStyledTextField();
        // codePartenaireField.setEditable(false); // Le code partenaire est généré
        // automatiquement
        cnibField = createStyledTextField();

        formPanel.add(createFieldPanel("Nom *:", nomField));
        formPanel.add(createFieldPanel("Prénom(s) *:", prenomField));
        formPanel.add(createFieldPanel("CNIB:", cnibField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Contact
        formPanel.add(createSectionTitle("Contact"));

        provinceCombo = new JComboBox<>();
        styleComboBox(provinceCombo);
        loadProvinces();
        provinceCombo.setEditable(true);
        AutoCompleteDecorator.decorate(provinceCombo);
        telephoneField = createStyledTextField();
        emailField = createStyledTextField();
        adresseField = createStyledTextField();

        formPanel.add(createFieldPanel("Province *:", provinceCombo));
        formPanel.add(createFieldPanel("Téléphone:", telephoneField));
        formPanel.add(createFieldPanel("Email:", emailField));
        formPanel.add(createFieldPanel("Adresse:", adresseField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Longrich (séparée pour pouvoir la cacher)
        longrichSection = createLongrichSection();
        formPanel.add(longrichSection);

        // Checkbox Client actif (hors section Longrich)
        JPanel generalCheckboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        generalCheckboxPanel.setBackground(CARD_COLOR);
        generalCheckboxPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        activeCheckBox = createStyledCheckBox("Client actif");
        activeCheckBox.setSelected(true);
        generalCheckboxPanel.add(activeCheckBox);

        formPanel.add(generalCheckboxPanel);
        formPanel.add(Box.createVerticalGlue());

        // Initialiser la visibilité de la section Longrich (masquée par défaut)
        if (longrichSection != null) {
            longrichSection.setVisible(false);
        }

        return formPanel;
    }

    private JPanel createLongrichSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(CARD_COLOR);
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Titre de section
        section.add(createSectionTitle("Informations Longrich"));

        // Code Partenaire field
        section.add(createFieldPanel("Code Partenaire *:", codePartenaireField));

        // Total PV field
        totalPvField = createStyledTextField();
        // totalPvField.setEditable(false);
        // totalPvField.setBackground(new Color(249, 250, 251));
        section.add(createFieldPanel("Total PV *:", totalPvField));

        // Code définitif checkbox
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        checkboxPanel.setBackground(CARD_COLOR);
        checkboxPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        codeDefinitifCheckBox = createStyledCheckBox("Code définitif");
        checkboxPanel.add(codeDefinitifCheckBox);

        section.add(checkboxPanel);
        section.add(Box.createVerticalStrut(15));

        return section;
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
                e -> saveClient());
        updateButton = ButtonFactory.createActionButton(FontAwesomeSolid.EDIT, "Modifier", WARNING_COLOR,
                e -> updateClient());
        deleteButton = ButtonFactory.createActionButton(FontAwesomeSolid.TRASH, "Supprimer", DANGER_COLOR,
                e -> deleteClient());
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
                deleteButton.setVisible(false);
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

        JLabel searchLabel = new JLabel("Rechercher un client");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchLabel.setForeground(TEXT_PRIMARY);
        panel.add(searchLabel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setBackground(CARD_COLOR);
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        searchField = createStyledTextField();
        searchField.addActionListener(e -> searchClients());

        JButton searchButton = ButtonFactory.createActionButton(FontAwesomeSolid.SEARCH, "", PRIMARY_COLOR,
                e -> searchClients());
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

        JLabel tableTitle = new JLabel("Liste des Clients");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        panel.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table
        createTable();
        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void createTable() {
        String[] columns = {
                "ID", "Code", "Nom", "Prénom", "Province", "Téléphone",
                "Email", "Type", "PV", "Statut"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        clientTable = new JTable(tableModel);
        clientTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientTable.setRowHeight(45);
        clientTable.setShowVerticalLines(false);
        clientTable.setGridColor(new Color(243, 244, 246));
        clientTable.setSelectionBackground(new Color(239, 246, 255));
        clientTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = clientTable.getTableHeader();
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
                if (column == 9 && value != null) { // Colonne Statut
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

        for (int i = 0; i < clientTable.getColumnCount(); i++) {
            clientTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedClient();
            }
        });
    }

    private void loadProvinces() {
        try {
            List<String> provinces = provinceService.findAll().stream()
                    .map(province -> province.getNom())
                    .toList();
            provinceCombo.removeAllItems();
            for (String province : provinces) {
                provinceCombo.addItem(province);
            }
        } catch (Exception e) {
            // Database might not be ready yet, add default provinces
            provinceCombo.removeAllItems();
            String[] defaultProvinces = {
                    "Balé", "Bam", "Banwa", "Bazèga", "Bougouriba", "Boulgou", "Boulkiemdé",
                    "Comoé", "Ganzourgou", "Gnagna", "Gourma", "Houet", "Ioba", "Kadiogo",
                    "Kénédougou", "Komondjari", "Kompienga", "Kossi", "Koulpélogo", "Kouritenga",
                    "Kourwéogo", "Léraba", "Loroum", "Mouhoun", "Nahouri", "Namentenga", "Nayala",
                    "Noumbiel", "Oubritenga", "Oudalan", "Passoré", "Poni", "Sanguié", "Sanmatenga",
                    "Séno", "Sissili", "Soum", "Sourou", "Tapoa", "Tuy", "Yagha", "Yatenga", "Ziro", "Zondoma",
                    "Zoundwéogo"
            };
            for (String province : defaultProvinces) {
                provinceCombo.addItem(province);
            }
        }
    }

    private void loadClients() {
        try {
            List<ClientDTO> clients = clientService.getActiveClients();
            tableModel.setRowCount(0);

            for (int i = 0; i < clients.size(); i++) {
                ClientDTO client = clients.get(i);
                Object[] row = {
                        i + 1,
                        client.getId(),
                        client.getNom(),
                        client.getPrenom(),
                        client.getProvince(),
                        client.getTelephone(),
                        client.getEmail(),
                        client.getTypeClient().getLibelle(),
                        client.getTotalPv() != null ? client.getTotalPv() : 0,
                        client.getActive() ? "Actif" : "Inactif"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            tableModel.setRowCount(0);
        }
    }

    private void updateStats() {
        try {
            List<ClientDTO> allClients = clientService.getAllClients();
            long activeCount = allClients.stream().filter(ClientDTO::getActive).count();
            long partnerCount = allClients.stream()
                    .filter(c -> c.getTypeClient() == TypeClient.PARTENAIRE).count();

            statsLabel.setText(String.format("Total: %d • Actifs: %d • Partenaires: %d",
                    allClients.size(), activeCount, partnerCount));
        } catch (Exception e) {
            statsLabel.setText("Statistiques non disponibles");
        }
    }

    private void searchClients() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadClients();
            return;
        }

        try {
            List<ClientDTO> clients = clientService.searchClients(searchText);
            tableModel.setRowCount(0);

            for (int i = 0; i < clients.size(); i++) {
                ClientDTO client = clients.get(i);
                Object[] row = {
                        i + 1,
                        client.getId(),
                        client.getNom(),
                        client.getPrenom(),
                        client.getProvince(),
                        client.getTelephone(),
                        client.getEmail(),
                        client.getTypeClient().getLibelle(),
                        client.getTotalPv() != null ? client.getTotalPv() : 0,
                        client.getActive() ? "Actif" : "Inactif"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private void loadSelectedClient() {
        int selectedRow = clientTable.getSelectedRow();
        
        if (selectedRow >= 0) {
            try {
                // L'ID du client est dans la colonne 1 (la colonne 0 contient le numéro de ligne) 
                Long clientId = (Long) tableModel.getValueAt(selectedRow, 1);
                
                clientService.getClientById(clientId).ifPresent(client -> {
                    currentClient = client;
                    populateFields(client);
                    setFormMode(FormMode.EDIT); // Passer en mode édition
                });
            } catch (Exception e) {
                showErrorMessage("Erreur lors du chargement du client: " + e.getMessage());
            }
        }
    }

    private void populateFields(ClientDTO client) {
        // Code supprimé - utilisation de l'ID auto-généré
        nomField.setText(client.getNom() != null ? client.getNom() : "");
        prenomField.setText(client.getPrenom() != null ? client.getPrenom() : "");
        codePartenaireField.setText(client.getCodePartenaire() != null ? client.getCodePartenaire() : "");
        cnibField.setText(client.getCnib() != null ? client.getCnib() : "");
        provinceCombo.setSelectedItem(client.getProvince());
        telephoneField.setText(client.getTelephone() != null ? client.getTelephone() : "");
        emailField.setText(client.getEmail() != null ? client.getEmail() : "");
        typeClientCombo.setSelectedItem(client.getTypeClient());
        adresseField.setText(client.getAdresse() != null ? client.getAdresse() : "");
        totalPvField.setText(client.getTotalPv() != null ? client.getTotalPv().toString() : "0");
        activeCheckBox.setSelected(client.getActive() != null ? client.getActive() : true);
        codeDefinitifCheckBox.setSelected(client.getCodeDefinitif() != null ? client.getCodeDefinitif() : false);
        
        // Déclencher la logique de visibilité de la section Longrich
        onTypeClientChange(null);
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

        // Validation du code supprimée - ID auto-généré
        if (nomField.getText().trim().isEmpty()) {
            setFieldError(nomField, "Nom requis");
            valid = false;
        }
        if (prenomField.getText().trim().isEmpty()) {
            setFieldError(prenomField, "Prénom requis");
            valid = false;
        }
        // if (telephoneField.getText().trim().isEmpty()) {
        // setFieldError(telephoneField, "Téléphone requis");
        // valid = false;
        // }
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            setFieldError(emailField, "Email invalide");
            valid = false;
        }

        TypeClient type = (TypeClient) typeClientCombo.getSelectedItem();
        if (type == TypeClient.PARTENAIRE && codePartenaireField.getText().trim().isEmpty()) {
            setFieldError(codePartenaireField, "Code partenaire requis");
            valid = false;
        }
        // Validation spécifique pour les partenaires : Total PV obligatoire
        if (type == TypeClient.PARTENAIRE) {
            String totalPvText = totalPvField.getText().trim();
            if (totalPvText.isEmpty()) {
                setFieldError(totalPvField, "Le total PV est obligatoire pour les partenaires");
                valid = false;
            } else {
                try {
                    int totalPv = Integer.parseInt(totalPvText);
                    if (totalPv <= 0) {
                        setFieldError(totalPvField, "Le total PV doit être supérieur à 0 pour les partenaires");
                        valid = false;
                    }
                } catch (NumberFormatException e) {
                    setFieldError(totalPvField, "Le total PV doit être un nombre entier valide");
                    valid = false;
                }
            }
        }

        return valid;
    }

    private void saveClient() {
        if (!validateFields()) {
            return;
        }
        try {
            ClientDTO client = createClientFromFields();
            clientService.saveClient(client);
            showSuccessMessage("Client sauvegardé avec succès");
            clearFields();
            loadClients();
            updateStats();
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void updateClient() {
        if (currentClient == null) {
            showWarningMessage("Veuillez sélectionner un client à modifier");
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            ClientDTO client = createClientFromFields();
            clientService.updateClient(currentClient.getId(), client);
            showSuccessMessage("Client mis à jour avec succès");
            clearFields();
            loadClients();
            updateStats();
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void deleteClient() {
        if (currentClient == null) {
            showWarningMessage("Veuillez sélectionner un client à supprimer");
            return;
        }

        String message = String.format(
                "🗑️ Êtes-vous sûr de vouloir supprimer ce client ?\n\n" +
                "Client: %s\n" +
                "Type: %s\n" +
                "ID: %d\n\n" +
                "Cette action est irréversible.",
                currentClient.getNomComplet() != null ? currentClient.getNomComplet() : 
                    (currentClient.getNom() + " " + currentClient.getPrenom()),
                currentClient.getTypeClient() != null ? currentClient.getTypeClient().getDisplayName() : "Non défini",
                currentClient.getId()
        );
        
        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                clientService.deleteClient(currentClient.getId());
                showSuccessMessage("Client supprimé avec succès");
                clearFields();
                loadClients();
                updateStats();
            } catch (Exception e) {
                showErrorMessage("Erreur: " + e.getMessage());
            }
        }
    }

    private ClientDTO createClientFromFields() {
        TypeClient typeClient = (TypeClient) typeClientCombo.getSelectedItem();
        String codePartenaire = null;
        String codeSaisi = codePartenaireField.getText().trim();
        if (typeClient == TypeClient.PARTENAIRE && !codeSaisi.isBlank()) {
            codePartenaire = codeSaisi;
        }
        
        // Récupération du Total PV pour les partenaires
        Integer totalPv = null;
        if (typeClient == TypeClient.PARTENAIRE && !totalPvField.getText().trim().isEmpty()) {
            try {
                totalPv = Integer.parseInt(totalPvField.getText().trim());
            } catch (NumberFormatException e) {
                // Géré par la validation du formulaire
            }
        }
        
        return ClientDTO.builder()
                // Code supprimé - utilisation de l'ID auto-généré
                .nom(nomField.getText().trim())
                .prenom(prenomField.getText().trim())
                .cnib(cnibField.getText().trim())
                .province((String) provinceCombo.getSelectedItem())
                .telephone(telephoneField.getText().trim())
                .email(emailField.getText().trim())
                .typeClient(typeClient)
                .adresse(adresseField.getText().trim())
                .codePartenaire(codePartenaire)
                .totalPv(totalPv)
                .active(activeCheckBox.isSelected())
                .codeDefinitif(codeDefinitifCheckBox.isSelected())
                .build();
    }

    private void clearFields() {
        clearErrors();
        currentClient = null;
        // Code field supprimé
        nomField.setText("");
        prenomField.setText("");
        codePartenaireField.setText("");
        cnibField.setText("");
        provinceCombo.setSelectedIndex(-1);
        telephoneField.setText("");
        emailField.setText("");
        typeClientCombo.setSelectedIndex(0);
        adresseField.setText("");
        totalPvField.setText("0");
        activeCheckBox.setSelected(true);
        codeDefinitifCheckBox.setSelected(false);
        clientTable.clearSelection();
        setFormMode(FormMode.ADD);
    }

    private void onTypeClientChange(ActionEvent e) {
        TypeClient selectedType = (TypeClient) typeClientCombo.getSelectedItem();
        boolean isPartenaire = selectedType == TypeClient.PARTENAIRE;

        // Masquer/afficher toute la section Longrich selon le type de client
        if (longrichSection != null) {
            longrichSection.setVisible(isPartenaire);
        }

        // Activer/désactiver les champs spécifiques Longrich
        codePartenaireField.setEditable(isPartenaire);
        totalPvField.setEditable(isPartenaire);

        Color bg = isPartenaire ? Color.WHITE : BACKGROUND_COLOR;
        codePartenaireField.setBackground(bg);
        totalPvField.setBackground(bg);

        formPanel.revalidate();
        formPanel.repaint();
    }

    private void exportClients() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter les clients");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV", "csv"));
        fileChooser.setSelectedFile(new File("clients.csv"));

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

                showSuccessMessage("Clients exportés vers " + file.getAbsolutePath());
            } catch (IOException ex) {
                showErrorMessage("Erreur lors de l'exportation: " + ex.getMessage());
            }
        }
    }

    private void importClients() {
        showInfoMessage("Fonctionnalité d'import en cours de développement");
    }

    private void refreshData() {
        loadClients();
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