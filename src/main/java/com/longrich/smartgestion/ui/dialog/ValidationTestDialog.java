package com.longrich.smartgestion.ui.dialog;

import com.longrich.smartgestion.ui.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog de test pour démontrer la validation progressive en temps réel
 */
public class ValidationTestDialog extends JDialog {

    private JTextField phoneField;
    private JTextField cnibField;
    private JTextField codePartenaireField;
    private JTextField codeStockisteField;
    private JTextField emailField;
    
    private JLabel phoneErrorLabel;
    private JLabel cnibErrorLabel;
    private JLabel codePartenaireErrorLabel;
    private JLabel codeStockisteErrorLabel;
    private JLabel emailErrorLabel;

    public ValidationTestDialog(Window parent) {
        super(parent, "Test de Validation Progressive", ModalityType.APPLICATION_MODAL);
        
        initComponents();
        setupProgressiveValidation();
        
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Test de Validation Progressive");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
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

        // Instructions
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBackground(new Color(230, 244, 255));
        instructionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel instructionTitle = new JLabel("📝 Instructions");
        instructionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        instructionTitle.setForeground(new Color(0, 123, 255));

        JLabel instructionText = new JLabel(
                "<html>Tapez dans les champs ci-dessous pour voir la validation en temps réel :<br>" +
                "• <b>Rouge</b> : Invalide<br>" +
                "• <b>Orange</b> : En cours (partiellement valide)<br>" +
                "• <b>Vert</b> : Valide<br>" +
                "• <b>Gris</b> : Neutre (vide)</html>");
        instructionText.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        instructionsPanel.add(instructionTitle);
        instructionsPanel.add(Box.createVerticalStrut(5));
        instructionsPanel.add(instructionText);

        formPanel.add(instructionsPanel);
        formPanel.add(Box.createVerticalStrut(20));

        // Téléphone Burkina (exemples: +22670123456, 70123456)
        phoneField = ValidationUtils.createTextField("Ex: +22670123456 ou 70123456");
        phoneErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Téléphone Burkinabè", phoneField, phoneErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // CNIB (exemple: B12345678)
        cnibField = ValidationUtils.createTextField("Ex: B12345678");
        cnibErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Numéro CNIB", cnibField, cnibErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Code Partenaire (exemple: BF12345678)
        codePartenaireField = ValidationUtils.createTextField("Ex: BF12345678");
        codePartenaireErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Code Partenaire (ISO2 + 8 chiffres)", codePartenaireField, codePartenaireErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Code Stockiste (exemple: BF1234)
        codeStockisteField = ValidationUtils.createTextField("Ex: BF1234");
        codeStockisteErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Code Stockiste (ISO2 + 4 chiffres)", codeStockisteField, codeStockisteErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        // Email (exemple: test@example.com)
        emailField = ValidationUtils.createTextField("Ex: test@example.com");
        emailErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Email", emailField, emailErrorLabel));
        formPanel.add(Box.createVerticalStrut(15));

        return formPanel;
    }

    private void setupProgressiveValidation() {
        // Configuration de la validation progressive pour chaque champ
        ValidationUtils.addBurkinaPhoneValidator(phoneField, phoneErrorLabel, false);
        ValidationUtils.addCNIBValidator(cnibField, cnibErrorLabel, false);
        ValidationUtils.addCodePartenaireValidator(codePartenaireField, codePartenaireErrorLabel, false);
        ValidationUtils.addCodeStockisteValidator(codeStockisteField, codeStockisteErrorLabel, false);
        ValidationUtils.addEmailValidator(emailField, emailErrorLabel, false);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton clearBtn = new JButton("Effacer tout");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setBackground(new Color(108, 117, 125));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clearAllFields());

        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(40, 167, 69));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());

        JButton fillExampleBtn = new JButton("Remplir exemples");
        fillExampleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fillExampleBtn.setBackground(new Color(23, 162, 184));
        fillExampleBtn.setForeground(Color.WHITE);
        fillExampleBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        fillExampleBtn.setFocusPainted(false);
        fillExampleBtn.addActionListener(e -> fillExampleData());

        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(fillExampleBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(closeBtn);

        return buttonPanel;
    }

    private void clearAllFields() {
        phoneField.setText("");
        cnibField.setText("");
        codePartenaireField.setText("");
        codeStockisteField.setText("");
        emailField.setText("");
    }

    private void fillExampleData() {
        // Remplir avec des exemples valides
        phoneField.setText("+22670123456");
        cnibField.setText("B12345678");
        codePartenaireField.setText("BF12345678");
        codeStockisteField.setText("BF1234");
        emailField.setText("test@example.com");
    }

    // Méthode statique pour faciliter l'ouverture du dialog de test
    public static void showTestDialog(Window parent) {
        SwingUtilities.invokeLater(() -> {
            ValidationTestDialog dialog = new ValidationTestDialog(parent);
            dialog.setVisible(true);
        });
    }
}