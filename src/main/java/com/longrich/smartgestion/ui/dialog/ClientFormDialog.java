package com.longrich.smartgestion.ui.dialog;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.entity.Province;
import com.longrich.smartgestion.enums.TypeClient;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.ProvinceService;
import com.longrich.smartgestion.ui.util.ValidationUtils;
import com.longrich.smartgestion.ui.util.ValidationUtils.ValidationField;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ClientFormDialog extends JDialog {
    
    private final ClientService clientService;
    private final ProvinceService provinceService;
    private ClientDTO client;
    private boolean confirmed = false;

    // Form fields
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField emailField;
    private JTextField telephoneField;
    private JTextField cnibField;
    private JTextField codePartenaireField;
    private JTextField adresseField;
    private JComboBox<TypeClient> typeClientCombo;
    private JComboBox<Province> provinceCombo;
    private JCheckBox codeDefinitifCheckBox;

    // Error labels
    private JLabel nomErrorLabel;
    private JLabel prenomErrorLabel;
    private JLabel emailErrorLabel;
    private JLabel telephoneErrorLabel;
    private JLabel cnibErrorLabel;
    private JLabel codePartenaireErrorLabel;
    private JLabel typeClientErrorLabel;

    // Validation fields
    private ValidationField nomValidation;
    private ValidationField prenomValidation;
    private ValidationField emailValidation;
    private ValidationField telephoneValidation;
    private ValidationField cnibValidation;
    private ValidationField codePartenaireValidation;

    public ClientFormDialog(Window parent, String title, ClientDTO client, 
                           ClientService clientService, ProvinceService provinceService) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.client = client;
        this.clientService = clientService;
        this.provinceService = provinceService;
        
        initComponents();
        setupValidation();
        
        if (client != null) {
            populateFields();
        }
        
        setSize(600, 700);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel(client == null ? "Nouveau Client" : "Modifier Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel in scroll pane
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Nom (required)
        nomField = ValidationUtils.createTextField("Entrez le nom");
        nomErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Nom *", nomField, nomErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Prénom (required)
        prenomField = ValidationUtils.createTextField("Entrez le prénom");
        prenomErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Prénom *", prenomField, prenomErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Email (optional)
        emailField = ValidationUtils.createTextField("exemple@email.com");
        emailErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Email", emailField, emailErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Téléphone (optional)
        telephoneField = ValidationUtils.createTextField("+226 XX XX XX XX ou XX XX XX XX");
        telephoneErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Téléphone", telephoneField, telephoneErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // CNIB (optional)
        cnibField = ValidationUtils.createTextField("BXXXXXXXX");
        cnibErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Numéro CNIB", cnibField, cnibErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Code Partenaire (optional)
        codePartenaireField = ValidationUtils.createTextField("Ex: BF12345678");
        codePartenaireErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Code Partenaire", codePartenaireField, codePartenaireErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Type Client (required)
        typeClientCombo = new JComboBox<>(TypeClient.values());
        typeClientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeClientCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TypeClient) {
                    setText(((TypeClient) value).getDisplayName());
                }
                return this;
            }
        });
        AutoCompleteDecorator.decorate(typeClientCombo);
        typeClientErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Type Client *", typeClientCombo, typeClientErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Province (optional)
        provinceCombo = new JComboBox<>();
        provinceCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        provinceCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Province) {
                    setText(((Province) value).getNom());
                }
                return this;
            }
        });
        AutoCompleteDecorator.decorate(provinceCombo);
        loadProvinces();
        formPanel.add(ValidationUtils.createFieldPanel("Province", provinceCombo, ValidationUtils.createErrorLabel()));
        formPanel.add(Box.createVerticalStrut(15));

        // Adresse (optional)
        adresseField = ValidationUtils.createTextField("Adresse complète");
        formPanel.add(ValidationUtils.createFieldPanel("Adresse", adresseField, ValidationUtils.createErrorLabel()));
        formPanel.add(Box.createVerticalStrut(15));

        // Code définitif checkbox
        codeDefinitifCheckBox = new JCheckBox("Code partenaire définitif");
        codeDefinitifCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(codeDefinitifCheckBox);

        return formPanel;
    }

    private void setupValidation() {
        // Setup progressive real-time validation for each field
        nomValidation = new ValidationField(nomField, nomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, true);
        ValidationUtils.addFieldValidator(nomField, nomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, true);

        prenomValidation = new ValidationField(prenomField, prenomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, true);
        ValidationUtils.addFieldValidator(prenomField, prenomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, true);

        // Progressive validation for fields with regex patterns
        emailValidation = new ValidationField(emailField, emailErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_EMAIL, 
                ValidationUtils::isValidEmail, false);
        ValidationUtils.addEmailValidator(emailField, emailErrorLabel, false);

        telephoneValidation = new ValidationField(telephoneField, telephoneErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_PHONE, 
                ValidationUtils::isValidBurkinaPhone, false);
        ValidationUtils.addBurkinaPhoneValidator(telephoneField, telephoneErrorLabel, false);

        cnibValidation = new ValidationField(cnibField, cnibErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_CNIB, 
                ValidationUtils::isValidCNIB, false);
        ValidationUtils.addCNIBValidator(cnibField, cnibErrorLabel, false);

        codePartenaireValidation = new ValidationField(codePartenaireField, codePartenaireErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_CODE_PARTENAIRE, 
                ValidationUtils::isValidCodePartenaire, false);
        ValidationUtils.addCodePartenaireValidator(codePartenaireField, codePartenaireErrorLabel, false);

        // Type client validation
        typeClientCombo.addActionListener(e -> {
            if (typeClientCombo.getSelectedItem() == null) {
                ValidationUtils.showFieldError(typeClientErrorLabel, "Veuillez sélectionner un type de client");
            } else {
                ValidationUtils.hideFieldError(typeClientErrorLabel);
            }
        });
    }

    private void loadProvinces() {
        try {
            List<Province> provinces = provinceService.findAll();
            provinceCombo.removeAllItems();
            provinceCombo.addItem(null); // Empty option
            for (Province province : provinces) {
                provinceCombo.addItem(province);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors du chargement des provinces: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields() {
        if (client != null) {
            nomField.setText(client.getNom());
            prenomField.setText(client.getPrenom());
            emailField.setText(client.getEmail() != null ? client.getEmail() : "");
            telephoneField.setText(client.getTelephone() != null ? client.getTelephone() : "");
            cnibField.setText(client.getCnib() != null ? client.getCnib() : "");
            codePartenaireField.setText(client.getCodePartenaire() != null ? client.getCodePartenaire() : "");
            adresseField.setText(client.getAdresse() != null ? client.getAdresse() : "");
            typeClientCombo.setSelectedItem(client.getTypeClient());
            codeDefinitifCheckBox.setSelected(client.getCodeDefinitif() != null ? client.getCodeDefinitif() : false);
            
            // Set province if exists
            if (client.getProvince() != null) {
                for (int i = 0; i < provinceCombo.getItemCount(); i++) {
                    Province item = provinceCombo.getItemAt(i);
                    if (item != null && item.getNom().equals(client.getProvince())) {
                        provinceCombo.setSelectedItem(item);
                        break;
                    }
                }
            }
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(client == null ? "Créer" : "Modifier");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(this::save);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(saveBtn);

        return buttonPanel;
    }

    private void save(ActionEvent e) {
        // Validate all fields
        boolean isValid = ValidationUtils.validateAllFields(
                nomValidation, prenomValidation, emailValidation, 
                telephoneValidation, cnibValidation, codePartenaireValidation);

        // Validate type client
        if (typeClientCombo.getSelectedItem() == null) {
            ValidationUtils.showFieldError(typeClientErrorLabel, "Veuillez sélectionner un type de client");
            isValid = false;
        }

        if (!isValid) {
            JOptionPane.showMessageDialog(this, 
                    "Veuillez corriger les erreurs dans le formulaire", 
                    "Erreurs de validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Create or update client
            if (client == null) {
                client = new ClientDTO();
            }

            client.setNom(nomValidation.getValue());
            client.setPrenom(prenomValidation.getValue());
            client.setEmail(emailValidation.getValue().isEmpty() ? null : emailValidation.getValue());
            client.setTelephone(telephoneValidation.getValue().isEmpty() ? null : telephoneValidation.getValue());
            client.setCnib(cnibValidation.getValue().isEmpty() ? null : cnibValidation.getValue());
            client.setCodePartenaire(codePartenaireValidation.getValue().isEmpty() ? null : codePartenaireValidation.getValue());
            client.setAdresse(adresseField.getText().trim().isEmpty() ? null : adresseField.getText().trim());
            client.setTypeClient((TypeClient) typeClientCombo.getSelectedItem());
            client.setCodeDefinitif(codeDefinitifCheckBox.isSelected());

            Province selectedProvince = (Province) provinceCombo.getSelectedItem();
            client.setProvince(selectedProvince != null ? selectedProvince.getNom() : null);

            confirmed = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la préparation des données: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ClientDTO getClient() {
        return client;
    }
}