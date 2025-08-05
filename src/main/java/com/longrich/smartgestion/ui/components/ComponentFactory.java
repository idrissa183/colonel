package com.longrich.smartgestion.ui.components;

import java.awt.*;
// import java.util.HashMap;
// import java.util.Map;

import javax.swing.*;
// import javax.swing.border.Border;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Factory pour créer des composants UI stylisés de manière cohérente
 */
public final class ComponentFactory {
    
    // Couleurs modernes cohérentes
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
    // private static final Color HOVER_COLOR = new Color(243, 244, 246);

    private ComponentFactory() {}

    /**
     * Crée un TextField stylisé moderne
     */
    public static JTextField createStyledTextField() {
        return createStyledTextField(null);
    }

    public static JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Placeholder text
        if (placeholder != null && !placeholder.isEmpty()) {
            field.setToolTipText(placeholder);
        }

        // Effets focus
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

    /**
     * Crée un ComboBox stylisé moderne
     */
    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        styleComboBox(comboBox);
        return comboBox;
    }

    public static <T> JComboBox<T> createStyledComboBox() {
        JComboBox<T> comboBox = new JComboBox<>();
        styleComboBox(comboBox);
        return comboBox;
    }

    private static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setPreferredSize(new Dimension(0, 38));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        comboBox.setFocusable(true);
        
        // Custom renderer pour un meilleur style
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (isSelected) {
                    setBackground(PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(TEXT_PRIMARY);
                }
                
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return this;
            }
        });
    }

    /**
     * Crée un TextArea stylisé moderne
     */
    public static JTextArea createStyledTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 0);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(Color.WHITE);
        area.setForeground(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return area;
    }

    /**
     * Crée un ScrollPane stylisé pour un TextArea
     */
    public static JScrollPane createStyledScrollPane(JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(0, 80));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        scrollPane.setBackground(Color.WHITE);
        return scrollPane;
    }

    /**
     * Crée un panel de champ avec label et gestion d'erreur
     */
    public static FieldPanel createFieldPanel(String labelText, JComponent field) {
        return new FieldPanel(labelText, field);
    }

    public static FieldPanel createFieldPanel(String labelText, JComponent field, boolean required) {
        return new FieldPanel(labelText, field, required);
    }

    /**
     * Crée un SearchField avec icône de recherche
     */
    public static JPanel createSearchField(JTextField textField) {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new OverlayLayout(searchPanel));
        searchPanel.setBackground(Color.WHITE);

        // Icône de recherche
        JLabel searchIcon = new JLabel(FontIcon.of(FontAwesomeSolid.SEARCH, 14, TEXT_SECONDARY));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 11));
        iconPanel.setOpaque(false);
        iconPanel.add(searchIcon);

        // Ajuster le padding du TextField pour l'icône
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 35, 8, 12)));

        searchPanel.add(iconPanel);
        searchPanel.add(textField);

        return searchPanel;
    }

    /**
     * Crée un FilterPanel moderne avec plusieurs filtres
     */
    public static JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        return panel;
    }

    /**
     * Crée un CardPanel moderne
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        return panel;
    }

    /**
     * Crée un titre de section stylisé
     */
    public static JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        return label;
    }

    /**
     * Crée un label stylisé
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * Classe pour gérer un champ avec label et validation
     */
    public static class FieldPanel extends JPanel {
        private final JLabel label;
        private final JLabel errorLabel;
        private final JComponent field;
        private final boolean required;

        public FieldPanel(String labelText, JComponent field) {
            this(labelText, field, false);
        }

        public FieldPanel(String labelText, JComponent field, boolean required) {
            this.field = field;
            this.required = required;
            
            setLayout(new BorderLayout(0, 5));
            setBackground(CARD_COLOR);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                field instanceof JScrollPane ? 120 : 70));

            // Label avec indicateur obligatoire
            this.label = new JLabel(required ? labelText + " *" : labelText);
            this.label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            this.label.setForeground(required ? TEXT_PRIMARY : TEXT_SECONDARY);

            // Label d'erreur
            this.errorLabel = new JLabel();
            this.errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            this.errorLabel.setForeground(DANGER_COLOR);
            this.errorLabel.setVisible(false);

            // Container pour le champ et l'erreur
            JPanel fieldWrapper = new JPanel(new BorderLayout());
            fieldWrapper.setBackground(CARD_COLOR);
            fieldWrapper.add(field, BorderLayout.CENTER);
            fieldWrapper.add(errorLabel, BorderLayout.SOUTH);

            add(label, BorderLayout.NORTH);
            add(fieldWrapper, BorderLayout.CENTER);
            add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        }

        public void setError(String message) {
            errorLabel.setText(message);
            errorLabel.setVisible(message != null && !message.isEmpty());
            
            // Changer la bordure du champ
            if (message != null && !message.isEmpty()) {
                if (field instanceof JComboBox) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(DANGER_COLOR, 1),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
                } else if (field instanceof JScrollPane) {
                    field.setBorder(BorderFactory.createLineBorder(DANGER_COLOR, 1));
                } else {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(DANGER_COLOR, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                }
            } else {
                clearError();
            }
        }

        public void clearError() {
            errorLabel.setVisible(false);
            errorLabel.setText("");
            
            // Restaurer la bordure normale
            if (field instanceof JComboBox) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            } else if (field instanceof JScrollPane) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            } else {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        }

        public JComponent getField() {
            return field;
        }

        public boolean isRequired() {
            return required;
        }
    }

    // Getters pour les couleurs (pour une utilisation externe cohérente)
    public static Color getPrimaryColor() { return PRIMARY_COLOR; }
    public static Color getSecondaryColor() { return SECONDARY_COLOR; }
    public static Color getSuccessColor() { return SUCCESS_COLOR; }
    public static Color getWarningColor() { return WARNING_COLOR; }
    public static Color getDangerColor() { return DANGER_COLOR; }
    public static Color getBackgroundColor() { return BACKGROUND_COLOR; }
    public static Color getCardColor() { return CARD_COLOR; }
    public static Color getBorderColor() { return BORDER_COLOR; }
    public static Color getTextPrimaryColor() { return TEXT_PRIMARY; }
    public static Color getTextSecondaryColor() { return TEXT_SECONDARY; }
}