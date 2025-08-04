package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.enums.TypeClient;
import com.longrich.smartgestion.service.ClientService;

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

    // Composants UI
    private JTextField codeField;
    private JTextField nomField;
    private JTextField prenomField;
    private JComboBox<String> provinceCombo;
    private JTextField telephoneField;
    private JTextField emailField;
    private JComboBox<TypeClient> typeClientCombo;
    private JTextField adresseField;
    private JCheckBox activeCheckBox;
    private JTextField cnibField;
    private JTextField lieuNaissanceField;
    private JTextField codeParrainField;
    private JTextField codePlacementField;
    private JTextField totalPvField;
    private JCheckBox codeDefinitifCheckBox;

    private JTable clientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statsLabel;

    private ClientDTO currentClient;

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

        JButton exportButton = createIconButton(FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR);
        JButton importButton = createIconButton(FontAwesomeSolid.FILE_IMPORT, "Importer", PRIMARY_COLOR);
        JButton refreshButton = createIconButton(FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR);

        exportButton.addActionListener(e -> exportClients());
        importButton.addActionListener(e -> importClients());
        refreshButton.addActionListener(e -> refreshData());

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

        codeField = createStyledTextField();
        nomField = createStyledTextField();
        prenomField = createStyledTextField();
        lieuNaissanceField = createStyledTextField();
        cnibField = createStyledTextField();

        formPanel.add(createFieldPanel("Code Partenaire:", codeField));
        formPanel.add(createFieldPanel("Nom:", nomField));
        formPanel.add(createFieldPanel("Prénom(s):", prenomField));
        formPanel.add(createFieldPanel("Lieu de naissance:", lieuNaissanceField));
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

        formPanel.add(createFieldPanel("Province:", provinceCombo));
        formPanel.add(createFieldPanel("Téléphone:", telephoneField));
        formPanel.add(createFieldPanel("Email:", emailField));
        formPanel.add(createFieldPanel("Adresse:", adresseField));
        formPanel.add(Box.createVerticalStrut(15));

        // Section Longrich
        formPanel.add(createSectionTitle("Informations Longrich"));

        codeParrainField = createStyledTextField();
        codePlacementField = createStyledTextField();
        totalPvField = createStyledTextField();
        totalPvField.setEditable(false);
        totalPvField.setBackground(new Color(249, 250, 251));

        formPanel.add(createFieldPanel("Code Parrain:", codeParrainField));
        formPanel.add(createFieldPanel("Code Placement:", codePlacementField));
        formPanel.add(createFieldPanel("Total PV:", totalPvField));

        // Checkboxes
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        checkboxPanel.setBackground(CARD_COLOR);
        checkboxPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        activeCheckBox = createStyledCheckBox("Client actif");
        activeCheckBox.setSelected(true);
        codeDefinitifCheckBox = createStyledCheckBox("Code définitif");

        checkboxPanel.add(activeCheckBox);
        checkboxPanel.add(Box.createHorizontalStrut(20));
        checkboxPanel.add(codeDefinitifCheckBox);

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

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

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

    private JButton createIconButton(FontAwesomeSolid icon, String text, Color color) {
        FontIcon fontIcon = FontIcon.of(icon, 16, Color.WHITE);
        JButton button = new JButton(text, fontIcon);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Effet hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createIconButton(FontAwesomeSolid.SAVE, "Sauvegarder", SUCCESS_COLOR);
        JButton updateButton = createIconButton(FontAwesomeSolid.EDIT, "Modifier", WARNING_COLOR);
        JButton deleteButton = createIconButton(FontAwesomeSolid.TRASH, "Supprimer", DANGER_COLOR);
        JButton clearButton = createIconButton(FontAwesomeSolid.ERASER, "Vider", SECONDARY_COLOR);

        saveButton.addActionListener(e -> saveClient());
        updateButton.addActionListener(e -> updateClient());
        deleteButton.addActionListener(e -> deleteClient());
        clearButton.addActionListener(e -> clearFields());

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

        JLabel searchLabel = new JLabel("Rechercher un client");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchLabel.setForeground(TEXT_PRIMARY);
        panel.add(searchLabel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setBackground(CARD_COLOR);
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        searchField = createStyledTextField();
        searchField.addActionListener(e -> searchClients());

        JButton searchButton = createIconButton(FontAwesomeSolid.SEARCH, "", PRIMARY_COLOR);
        searchButton.setPreferredSize(new Dimension(50, 38));
        searchButton.addActionListener(e -> searchClients());

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
                "#", "Code", "Nom", "Prénom", "Province", "Téléphone",
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
            List<String> provinces = clientService.getAllProvinces();
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
                        client.getCode(),
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
                        client.getCode(),
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
            String code = (String) tableModel.getValueAt(selectedRow, 1);
            clientService.getClientByCode(code).ifPresent(client -> {
                currentClient = client;
                populateFields(client);
            });
        }
    }

    private void populateFields(ClientDTO client) {
        codeField.setText(client.getCode());
        nomField.setText(client.getNom());
        prenomField.setText(client.getPrenom());
        lieuNaissanceField.setText(client.getLieuNaissance());
        cnibField.setText(client.getCnib());
        provinceCombo.setSelectedItem(client.getProvince());
        telephoneField.setText(client.getTelephone());
        emailField.setText(client.getEmail());
        typeClientCombo.setSelectedItem(client.getTypeClient());
        adresseField.setText(client.getAdresse());
        codeParrainField.setText(client.getCodeParrain());
        codePlacementField.setText(client.getCodePlacement());
        totalPvField.setText(client.getTotalPv() != null ? client.getTotalPv().toString() : "0");
        activeCheckBox.setSelected(client.getActive());
        codeDefinitifCheckBox.setSelected(client.getCodeDefinitif() != null ? client.getCodeDefinitif() : false);
    }

    private void saveClient() {
        try {
            ClientDTO client = createClientFromFields();
            clientService.saveClient(client);
            showSuccessMessage("Client sauvegardé avec succès");
            clearFields();
            loadClients();
            updateStats();
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void updateClient() {
        if (currentClient == null) {
            showWarningMessage("Veuillez sélectionner un client à modifier");
            return;
        }

        try {
            ClientDTO client = createClientFromFields();
            clientService.updateClient(currentClient.getId(), client);
            showSuccessMessage("Client mis à jour avec succès");
            clearFields();
            loadClients();
            updateStats();
        } catch (Exception e) {
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private void deleteClient() {
        if (currentClient == null) {
            showWarningMessage("Veuillez sélectionner un client à supprimer");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer ce client ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

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
        return ClientDTO.builder()
                .code(codeField.getText().trim())
                .nom(nomField.getText().trim())
                .prenom(prenomField.getText().trim())
                .lieuNaissance(lieuNaissanceField.getText().trim())
                .cnib(cnibField.getText().trim())
                .province((String) provinceCombo.getSelectedItem())
                .telephone(telephoneField.getText().trim())
                .email(emailField.getText().trim())
                .typeClient((TypeClient) typeClientCombo.getSelectedItem())
                .adresse(adresseField.getText().trim())
                .codeParrain(codeParrainField.getText().trim())
                .codePlacement(codePlacementField.getText().trim())
                .active(activeCheckBox.isSelected())
                .codeDefinitif(codeDefinitifCheckBox.isSelected())
                .build();
    }

    private void clearFields() {
        currentClient = null;
        codeField.setText("");
        nomField.setText("");
        prenomField.setText("");
        lieuNaissanceField.setText("");
        cnibField.setText("");
        provinceCombo.setSelectedIndex(-1);
        telephoneField.setText("");
        emailField.setText("");
        typeClientCombo.setSelectedIndex(0);
        adresseField.setText("");
        codeParrainField.setText("");
        codePlacementField.setText("");
        totalPvField.setText("0");
        activeCheckBox.setSelected(true);
        codeDefinitifCheckBox.setSelected(false);
        clientTable.clearSelection();
    }

    private void onTypeClientChange(ActionEvent e) {
        // Logique spécifique selon le type de client
    }

    private void exportClients() {
        showInfoMessage("Fonctionnalité d'export en cours de développement");
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