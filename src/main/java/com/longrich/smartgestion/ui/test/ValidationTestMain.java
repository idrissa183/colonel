package com.longrich.smartgestion.ui.test;

import com.longrich.smartgestion.ui.dialog.ValidationTestDialog;
import com.longrich.smartgestion.ui.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Classe de test standalone pour démontrer la validation progressive
 * Exécutez cette classe pour voir la validation en temps réel
 */
public class ValidationTestMain {
    
    public static void main(String[] args) {
        // Configuration Look and Feel - utilisation par défaut
        
        SwingUtilities.invokeLater(() -> {
            createAndShowMainWindow();
        });
    }
    
    private static void createAndShowMainWindow() {
        JFrame frame = new JFrame("Test de Validation Temps Réel - SmartGestion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel titleLabel = new JLabel("Test de Validation Progressive SmartGestion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Panel de formulaire avec validation temps réel
        JPanel formPanel = createValidationFormPanel();
        
        // Panel des boutons
        JPanel buttonPanel = createButtonPanel(frame);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private static JPanel createValidationFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Testez la validation en temps réel"));
        
        // Instructions
        JLabel instructionLabel = new JLabel(
            "<html><b>Instructions :</b> Tapez dans les champs ci-dessous et observez la validation en temps réel :<br>" +
            "• <font color='red'>Rouge</font> : Invalide<br>" +
            "• <font color='orange'>Orange</font> : En cours (partiellement valide)<br>" +
            "• <font color='green'>Vert</font> : Valide<br>" +
            "• Gris : Neutre (vide)</html>");
        instructionLabel.setBorder(new EmptyBorder(10, 10, 20, 10));
        formPanel.add(instructionLabel);
        
        // Champ téléphone
        JTextField phoneField = ValidationUtils.createTextField("Tapez un numéro burkinabè (ex: +22670123456)");
        JLabel phoneErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Téléphone Burkina (+22670123456 ou 70123456)", phoneField, phoneErrorLabel));
        ValidationUtils.addBurkinaPhoneValidator(phoneField, phoneErrorLabel, false);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Champ CNIB
        JTextField cnibField = ValidationUtils.createTextField("Tapez un numéro CNIB (ex: B12345678)");
        JLabel cnibErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Numéro CNIB (B + 8 chiffres)", cnibField, cnibErrorLabel));
        ValidationUtils.addCNIBValidator(cnibField, cnibErrorLabel, false);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Champ Code Partenaire
        JTextField codePartenaireField = ValidationUtils.createTextField("Tapez un code partenaire (ex: BF12345678)");
        JLabel codePartenaireErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Code Partenaire (ISO2 + 8 chiffres)", codePartenaireField, codePartenaireErrorLabel));
        ValidationUtils.addCodePartenaireValidator(codePartenaireField, codePartenaireErrorLabel, false);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Champ Code Stockiste
        JTextField codeStockisteField = ValidationUtils.createTextField("Tapez un code stockiste (ex: BF1234)");
        JLabel codeStockisteErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Code Stockiste (ISO2 + 4 chiffres)", codeStockisteField, codeStockisteErrorLabel));
        ValidationUtils.addCodeStockisteValidator(codeStockisteField, codeStockisteErrorLabel, false);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Champ Email
        JTextField emailField = ValidationUtils.createTextField("Tapez un email (ex: test@example.com)");
        JLabel emailErrorLabel = ValidationUtils.createErrorLabel();
        formPanel.add(ValidationUtils.createFieldPanel("Email", emailField, emailErrorLabel));
        ValidationUtils.addEmailValidator(emailField, emailErrorLabel, false);
        
        return formPanel;
    }
    
    private static JPanel createButtonPanel(JFrame parentFrame) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton testDialogBtn = new JButton("Ouvrir Dialog de Test");
        testDialogBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        testDialogBtn.setBackground(new Color(0, 123, 255));
        testDialogBtn.setForeground(Color.WHITE);
        testDialogBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        testDialogBtn.setFocusPainted(false);
        testDialogBtn.addActionListener(e -> ValidationTestDialog.showTestDialog(parentFrame));
        
        JButton fillExampleBtn = new JButton("Remplir Exemples");
        fillExampleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fillExampleBtn.setBackground(new Color(40, 167, 69));
        fillExampleBtn.setForeground(Color.WHITE);
        fillExampleBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        fillExampleBtn.setFocusPainted(false);
        fillExampleBtn.addActionListener(e -> {
            // Remplir automatiquement avec des exemples
            JPanel formPanel = (JPanel) ((JPanel) parentFrame.getContentPane().getComponent(0)).getComponent(1);
            fillFormWithExamples(formPanel);
        });
        
        JButton clearBtn = new JButton("Effacer Tout");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setBackground(new Color(220, 53, 69));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            // Effacer tous les champs
            JPanel formPanel = (JPanel) ((JPanel) parentFrame.getContentPane().getComponent(0)).getComponent(1);
            clearAllFields(formPanel);
        });
        
        buttonPanel.add(testDialogBtn);
        buttonPanel.add(fillExampleBtn);
        buttonPanel.add(clearBtn);
        
        return buttonPanel;
    }
    
    private static void fillFormWithExamples(JPanel formPanel) {
        // Recherche et remplissage des champs de texte avec des exemples
        Component[] components = formPanel.getComponents();
        int fieldIndex = 0;
        String[] examples = {"+22670123456", "B12345678", "BF12345678", "BF1234", "test@example.com"};
        
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel fieldPanel = (JPanel) comp;
                for (Component fieldComp : fieldPanel.getComponents()) {
                    if (fieldComp instanceof JTextField && fieldIndex < examples.length) {
                        ((JTextField) fieldComp).setText(examples[fieldIndex++]);
                    }
                }
            }
        }
    }
    
    private static void clearAllFields(JPanel formPanel) {
        // Recherche et effacement de tous les champs de texte
        Component[] components = formPanel.getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel fieldPanel = (JPanel) comp;
                for (Component fieldComp : fieldPanel.getComponents()) {
                    if (fieldComp instanceof JTextField) {
                        ((JTextField) fieldComp).setText("");
                    }
                }
            }
        }
    }
}