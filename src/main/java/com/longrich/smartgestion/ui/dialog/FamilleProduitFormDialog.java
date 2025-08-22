package com.longrich.smartgestion.ui.dialog;

import com.longrich.smartgestion.dto.FamilleProduitDTO;
import com.longrich.smartgestion.service.FamilleProduitService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FamilleProduitFormDialog extends JDialog {
    
    private final FamilleProduitService familleProduitService;
    private FamilleProduitDTO familleProduit;
    private boolean confirmed = false;

    // Form fields
    private JTextField libelleFamilleField;
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;

    // Colors
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);

    public FamilleProduitFormDialog(Window parent, String title, FamilleProduitDTO familleProduit, 
                                  FamilleProduitService familleProduitService) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.familleProduit = familleProduit;
        this.familleProduitService = familleProduitService;
        
        initComponents();
        
        if (familleProduit != null) {
            populateFields();
        }
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel(familleProduit == null ? "Nouvelle Famille de Produits" : "Modifier Famille de Produits");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Libellé Famille
        panel.add(createFieldPanel("Libellé Famille *:", 
            libelleFamilleField = createStyledTextField()));

        panel.add(Box.createVerticalStrut(15));

        // Description
        panel.add(createFieldPanel("Description:", 
            createDescriptionPanel()));

        panel.add(Box.createVerticalStrut(15));

        // Active
        JPanel activePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        activeCheckBox = new JCheckBox("Famille active");
        activeCheckBox.setSelected(true);
        activeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        activePanel.add(activeCheckBox);
        panel.add(activePanel);

        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(this::onCancel);

        JButton saveButton = new JButton(familleProduit == null ? "Créer" : "Modifier");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setBackground(SUCCESS_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(this::onSave);

        panel.add(cancelButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(saveButton);

        return panel;
    }

    private void populateFields() {
        if (familleProduit != null) {
            libelleFamilleField.setText(familleProduit.getLibelleFamille() != null ? familleProduit.getLibelleFamille() : "");
            descriptionArea.setText(familleProduit.getDescription() != null ? familleProduit.getDescription() : "");
            activeCheckBox.setSelected(familleProduit.getActive() != null ? familleProduit.getActive() : true);
        }
    }

    private boolean validateFields() {
        if (libelleFamilleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Le libellé de la famille est obligatoire.", 
                "Validation", JOptionPane.WARNING_MESSAGE);
            libelleFamilleField.requestFocus();
            return false;
        }
        return true;
    }

    private void onSave(ActionEvent e) {
        if (!validateFields()) {
            return;
        }

        try {
            if (familleProduit == null) {
                familleProduit = new FamilleProduitDTO();
            }

            familleProduit.setLibelleFamille(libelleFamilleField.getText().trim());
            familleProduit.setDescription(descriptionArea.getText().trim());
            familleProduit.setActive(activeCheckBox.isSelected());

            confirmed = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la validation: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel(ActionEvent e) {
        confirmed = false;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public FamilleProduitDTO getFamilleProduit() {
        return familleProduit;
    }
}