package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.enums.TypeClient;
import com.longrich.smartgestion.service.ClientService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class ClientPanel extends JPanel {

    private final ClientService clientService;

    private JTextField codeField;
    private JTextField nomField;
    private JTextField prenomField;
    private JComboBox<String> provinceCombo;
    private JTextField telephoneField;
    private JTextField emailField;
    private JComboBox<TypeClient> typeClientCombo;
    private JTextField adresseField;
    private JCheckBox activeCheckBox;

    private JTable clientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private ClientDTO currentClient;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        createFormPanel();
        createTablePanel();
        createButtonPanel();

        loadClients();
    }

    private void createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(0, 51, 204));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.WHITE),
                        "Données des Clients",
                        0, 0,
                        new Font("Segoe UI", Font.BOLD, 14),
                        Color.WHITE),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        formPanel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Type client
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Type client:"), gbc);
        gbc.gridx = 1;
        typeClientCombo = new JComboBox<>(TypeClient.values());
        typeClientCombo.setPreferredSize(new Dimension(200, 25));
        typeClientCombo.addActionListener(this::onTypeClientChange);
        formPanel.add(typeClientCombo, gbc);

        // Code
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Code Partenaire:"), gbc);
        gbc.gridx = 1;
        codeField = createTextField();
        formPanel.add(codeField, gbc);

        // Nom
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Nom:"), gbc);
        gbc.gridx = 1;
        nomField = createTextField();
        formPanel.add(nomField, gbc);

        // Prénom
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Prénom(s):"), gbc);
        gbc.gridx = 1;
        prenomField = createTextField();
        formPanel.add(prenomField, gbc);

        // Province
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Province:"), gbc);
        gbc.gridx = 1;
        provinceCombo = new JComboBox<>();
        provinceCombo.setPreferredSize(new Dimension(200, 25));
        loadProvinces();
        formPanel.add(provinceCombo, gbc);

        // Téléphone
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        telephoneField = createTextField();
        formPanel.add(telephoneField, gbc);

        // Email
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = createTextField();
        formPanel.add(emailField, gbc);

        // Adresse
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        adresseField = createTextField();
        formPanel.add(adresseField, gbc);

        // Actif
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createLabel("Actif:"), gbc);
        gbc.gridx = 1;
        activeCheckBox = new JCheckBox();
        activeCheckBox.setSelected(true);
        activeCheckBox.setBackground(new Color(0, 51, 204));
        formPanel.add(activeCheckBox, gbc);

        add(formPanel, BorderLayout.WEST);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 25));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return field;
    }

    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Liste des Clients"));

        // Barre de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchButton.addActionListener(e -> searchClients());

        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "#", "Code", "Nom", "Prénom(s)", "Province", "Téléphone", "Email", "Type" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        clientTable = new JTable(tableModel);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedClient();
            }
        });

        JScrollPane scrollPane = new JScrollPane(clientTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton saveButton = createButton("Sauvegarder", new Color(3, 168, 25), e -> saveClient());
        JButton updateButton = createButton("Mettre à jour", new Color(184, 101, 18), e -> updateClient());
        JButton clearButton = createButton("Vider", new Color(110, 14, 83), e -> clearFields());
        JButton deleteButton = createButton("Supprimer", new Color(207, 6, 26), e -> deleteClient());

        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private void onTypeClientChange(ActionEvent e) {
        TypeClient selectedType = (TypeClient) typeClientCombo.getSelectedItem();
        // Mettre à jour le label du code selon le type
        // Cette logique peut être implémentée selon les besoins
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
                        client.getTypeClient().getLibelle()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            // Database might not be ready yet, clear table
            tableModel.setRowCount(0);
        }
    }

    private void searchClients() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadClients();
            return;
        }

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
                    client.getTypeClient().getLibelle()
            };
            tableModel.addRow(row);
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
        provinceCombo.setSelectedItem(client.getProvince());
        telephoneField.setText(client.getTelephone());
        emailField.setText(client.getEmail());
        typeClientCombo.setSelectedItem(client.getTypeClient());
        adresseField.setText(client.getAdresse());
        activeCheckBox.setSelected(client.getActive());
    }

    private void saveClient() {
        try {
            ClientDTO client = createClientFromFields();
            clientService.saveClient(client);
            JOptionPane.showMessageDialog(this, "Client sauvegardé avec succès", "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadClients();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClient() {
        if (currentClient == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client à modifier", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ClientDTO client = createClientFromFields();
            clientService.updateClient(currentClient.getId(), client);
            JOptionPane.showMessageDialog(this, "Client mis à jour avec succès", "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadClients();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClient() {
        if (currentClient == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client à supprimer", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer ce client ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            try {
                clientService.deleteClient(currentClient.getId());
                JOptionPane.showMessageDialog(this, "Client supprimé avec succès", "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadClients();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ClientDTO createClientFromFields() {
        return ClientDTO.builder()
                .code(codeField.getText().trim())
                .nom(nomField.getText().trim())
                .prenom(prenomField.getText().trim())
                .province((String) provinceCombo.getSelectedItem())
                .telephone(telephoneField.getText().trim())
                .email(emailField.getText().trim())
                .typeClient((TypeClient) typeClientCombo.getSelectedItem())
                .adresse(adresseField.getText().trim())
                .active(activeCheckBox.isSelected())
                .build();
    }

    private void clearFields() {
        currentClient = null;
        codeField.setText("");
        nomField.setText("");
        prenomField.setText("");
        provinceCombo.setSelectedIndex(-1);
        telephoneField.setText("");
        emailField.setText("");
        typeClientCombo.setSelectedIndex(0);
        adresseField.setText("");
        activeCheckBox.setSelected(true);
        clientTable.clearSelection();
    }
}