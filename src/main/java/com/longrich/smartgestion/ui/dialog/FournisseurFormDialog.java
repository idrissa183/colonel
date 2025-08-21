package com.longrich.smartgestion.ui.dialog;

import com.longrich.smartgestion.dto.FournisseurDTO;
import com.longrich.smartgestion.enums.TypeStockiste;
import com.longrich.smartgestion.service.FournisseurService;
import com.longrich.smartgestion.ui.util.ValidationUtils;
import com.longrich.smartgestion.ui.util.ValidationUtils.ValidationField;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FournisseurFormDialog extends JDialog {
    
    private final FournisseurService fournisseurService;
    private FournisseurDTO fournisseur;
    private boolean confirmed = false;

    // Form fields
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField emailField;
    private JTextField telephoneField;
    private JTextField codeStockisteField;
    private JTextField adresseField;
    private JComboBox<TypeStockiste> typeStockisteCombo;
    private JCheckBox activeCheckBox;

    // Error labels
    private JLabel nomErrorLabel;
    private JLabel prenomErrorLabel;
    private JLabel emailErrorLabel;
    private JLabel telephoneErrorLabel;
    private JLabel codeStockisteErrorLabel;
    private JLabel typeStockisteErrorLabel;

    // Validation fields
    private ValidationField nomValidation;
    private ValidationField prenomValidation;
    private ValidationField emailValidation;
    private ValidationField telephoneValidation;
    private ValidationField codeStockisteValidation;

    public FournisseurFormDialog(Window parent, String title, FournisseurDTO fournisseur, 
                                FournisseurService fournisseurService) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.fournisseur = fournisseur;
        this.fournisseurService = fournisseurService;
        
        initComponents();
        setupValidation();
        
        if (fournisseur != null) {
            populateFields();
        }
        
        setSize(600, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel(fournisseur == null ? "Nouveau Fournisseur" : "Modifier Fournisseur");
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

        // Type Stockiste (required) - placed first to influence prenom requirement
        typeStockisteCombo = new JComboBox<>(TypeStockiste.values());
        typeStockisteCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeStockisteCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TypeStockiste) {
                    setText(((TypeStockiste) value).getDisplayName());
                }
                return this;
            }
        });
        AutoCompleteDecorator.decorate(typeStockisteCombo);
        typeStockisteErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Type Stockiste *", typeStockisteCombo, typeStockisteErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Code Stockiste (required)
        codeStockisteField = ValidationUtils.createTextField("Ex: BF1234");
        codeStockisteErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Code Stockiste *", codeStockisteField, codeStockisteErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Nom (required)
        nomField = ValidationUtils.createTextField("Entrez le nom");
        nomErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Nom *", nomField, nomErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Prénom (required for PERSONNE_PHYSIQUE)
        prenomField = ValidationUtils.createTextField("Entrez le prénom");
        prenomErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Prénom", prenomField, prenomErrorLabel));
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

        // Adresse (optional)
        adresseField = ValidationUtils.createTextField("Adresse complète");
        formPanel.add(ValidationUtils.createFieldPanel("Adresse", adresseField, ValidationUtils.createErrorLabel()));
        formPanel.add(Box.createVerticalStrut(15));

        // Active checkbox
        activeCheckBox = new JCheckBox("Fournisseur actif", true);
        activeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(activeCheckBox);

        return formPanel;
    }

    private void setupValidation() {
        // Setup real-time validation for each field
        nomValidation = new ValidationField(nomField, nomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, true);
        ValidationUtils.addFieldValidator(nomField, nomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, true);

        prenomValidation = new ValidationField(prenomField, prenomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, false);
        ValidationUtils.addFieldValidator(prenomField, prenomErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_NAME, 
                ValidationUtils::isValidName, false);

        emailValidation = new ValidationField(emailField, emailErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_EMAIL, 
                ValidationUtils::isValidEmail, false);
        ValidationUtils.addFieldValidator(emailField, emailErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_EMAIL, 
                text -> text.trim().isEmpty() || ValidationUtils.isValidEmail(text), false);

        telephoneValidation = new ValidationField(telephoneField, telephoneErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_PHONE, 
                ValidationUtils::isValidBurkinaPhone, false);
        ValidationUtils.addFieldValidator(telephoneField, telephoneErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_PHONE, 
                ValidationUtils::isValidBurkinaPhone, false);

        codeStockisteValidation = new ValidationField(codeStockisteField, codeStockisteErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_CODE_STOCKISTE, 
                ValidationUtils::isValidCodeStockiste, true);
        ValidationUtils.addFieldValidator(codeStockisteField, codeStockisteErrorLabel, 
                ValidationUtils.ErrorMessages.INVALID_CODE_STOCKISTE, 
                ValidationUtils::isValidCodeStockiste, true);

        // Type stockiste validation
        typeStockisteCombo.addActionListener(e -> {
            if (typeStockisteCombo.getSelectedItem() == null) {
                ValidationUtils.showFieldError(typeStockisteErrorLabel, "Veuillez sélectionner un type de stockiste");
            } else {
                ValidationUtils.hideFieldError(typeStockisteErrorLabel);
                
                // Update prenom requirement based on type
                TypeStockiste selected = (TypeStockiste) typeStockisteCombo.getSelectedItem();
                boolean prenomRequired = selected == TypeStockiste.PERSONNE_PHYSIQUE;
                
                // Update prenom validation
                if (prenomRequired) {
                    prenomValidation = new ValidationField(prenomField, prenomErrorLabel, 
                            ValidationUtils.ErrorMessages.INVALID_NAME, 
                            ValidationUtils::isValidName, true);
                    // Re-validate current content
                    if (prenomField.getText().trim().isEmpty()) {
                        ValidationUtils.showFieldError(prenomErrorLabel, "Le prénom est obligatoire pour une personne physique");
                        ValidationUtils.setErrorBorder(prenomField);
                    }
                } else {
                    ValidationUtils.hideFieldError(prenomErrorLabel);
                    if (!prenomField.getText().trim().isEmpty() && ValidationUtils.isValidName(prenomField.getText())) {
                        ValidationUtils.setSuccessBorder(prenomField);
                    } else {
                        ValidationUtils.setDefaultBorder(prenomField);
                    }
                }
            }
        });
    }

    private void populateFields() {
        if (fournisseur != null) {
            nomField.setText(fournisseur.getNom());
            prenomField.setText(fournisseur.getPrenom() != null ? fournisseur.getPrenom() : "");
            emailField.setText(fournisseur.getEmail() != null ? fournisseur.getEmail() : "");
            telephoneField.setText(fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "");
            codeStockisteField.setText(fournisseur.getCodeStockiste());
            adresseField.setText(fournisseur.getAdresse() != null ? fournisseur.getAdresse() : "");
            typeStockisteCombo.setSelectedItem(fournisseur.getTypeStockiste());
            activeCheckBox.setSelected(fournisseur.getActive() != null ? fournisseur.getActive() : true);
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

        JButton saveBtn = new JButton(fournisseur == null ? "Créer" : "Modifier");
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
        // Check prenom requirement based on type
        TypeStockiste selectedType = (TypeStockiste) typeStockisteCombo.getSelectedItem();
        boolean prenomRequired = selectedType == TypeStockiste.PERSONNE_PHYSIQUE;
        
        // Validate all fields
        boolean isValid = true;
        
        // Basic validations
        if (!nomValidation.validate()) isValid = false;
        if (!emailValidation.validate()) isValid = false;
        if (!telephoneValidation.validate()) isValid = false;
        if (!codeStockisteValidation.validate()) isValid = false;
        
        // Special prenom validation
        if (prenomRequired && prenomField.getText().trim().isEmpty()) {
            ValidationUtils.showFieldError(prenomErrorLabel, "Le prénom est obligatoire pour une personne physique");
            ValidationUtils.setErrorBorder(prenomField);
            isValid = false;
        } else if (!prenomField.getText().trim().isEmpty() && !ValidationUtils.isValidName(prenomField.getText())) {
            ValidationUtils.showFieldError(prenomErrorLabel, ValidationUtils.ErrorMessages.INVALID_NAME);
            ValidationUtils.setErrorBorder(prenomField);
            isValid = false;
        } else {
            ValidationUtils.hideFieldError(prenomErrorLabel);
            if (!prenomField.getText().trim().isEmpty()) {
                ValidationUtils.setSuccessBorder(prenomField);
            } else {
                ValidationUtils.setDefaultBorder(prenomField);
            }
        }

        // Validate type stockiste
        if (typeStockisteCombo.getSelectedItem() == null) {
            ValidationUtils.showFieldError(typeStockisteErrorLabel, "Veuillez sélectionner un type de stockiste");
            isValid = false;
        }

        if (!isValid) {
            JOptionPane.showMessageDialog(this, 
                    "Veuillez corriger les erreurs dans le formulaire", 
                    "Erreurs de validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Create or update fournisseur
            if (fournisseur == null) {
                fournisseur = new FournisseurDTO();
            }

            fournisseur.setNom(nomValidation.getValue());
            fournisseur.setPrenom(prenomField.getText().trim().isEmpty() ? null : prenomField.getText().trim());
            fournisseur.setEmail(emailValidation.getValue().isEmpty() ? null : emailValidation.getValue());
            fournisseur.setTelephone(telephoneValidation.getValue().isEmpty() ? null : telephoneValidation.getValue());
            fournisseur.setCodeStockiste(codeStockisteValidation.getValue());
            fournisseur.setAdresse(adresseField.getText().trim().isEmpty() ? null : adresseField.getText().trim());
            fournisseur.setTypeStockiste(selectedType);
            fournisseur.setActive(activeCheckBox.isSelected());

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

    public FournisseurDTO getFournisseur() {
        return fournisseur;
    }
}