package com.longrich.smartgestion.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Component
@Profile("!headless")
public class Sidebar extends JPanel {

    private static final int EXPANDED_WIDTH = 250;
    private static final int COLLAPSED_WIDTH = 60;

    private boolean isExpanded = true;
    private JPanel menuPanel;
    private JLabel titleLabel;

    @Setter
    private Consumer<String> navigationHandler;

    public Sidebar() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        createHeader();
        createMenuPanel();
        createFooter();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(35, 35, 35));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Logo et titre
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        logoPanel.setBackground(new Color(35, 35, 35));

        FontIcon logoIcon = FontIcon.of(FontAwesomeSolid.STORE, 24, Color.WHITE);
        JLabel logoLabel = new JLabel(logoIcon);
        logoPanel.add(logoLabel);

        titleLabel = new JLabel("SmartGestion");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoPanel.add(titleLabel);

        headerPanel.add(logoPanel, BorderLayout.CENTER);

        // Bouton toggle
        FontIcon toggleIcon = FontIcon.of(FontAwesomeSolid.BARS, 16, Color.WHITE);
        JButton toggleButton = new JButton(toggleIcon);
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setBackground(new Color(60, 60, 60));
        toggleButton.setBorder(BorderFactory.createEmptyBorder());
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> toggleSidebar());

        headerPanel.add(toggleButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(45, 45, 45));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Items du menu
        Object[][] menuItems = {
                { FontAwesomeSolid.CHART_BAR, "Tableau de bord", "dashboard" },
                { FontAwesomeSolid.USERS, "Clients", "clients" },
                { FontAwesomeSolid.BOX, "Produits", "produits" },
                { FontAwesomeSolid.WAREHOUSE, "Stock", "stock" },
                { FontAwesomeSolid.SHOPPING_CART, "Commandes", "commandes" },
                { FontAwesomeSolid.FILE_INVOICE, "Factures", "factures" },
                { FontAwesomeSolid.INDUSTRY, "Fournisseurs", "fournisseurs" },
                { FontAwesomeSolid.MONEY_BILL_WAVE, "Ventes", "ventes" }
        };

        for (Object[] item : menuItems) {
            JButton menuButton = createMenuButton((FontAwesomeSolid) item[0], (String) item[1], (String) item[2]);
            menuPanel.add(menuButton);
            menuPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(45, 45, 45));
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createMenuButton(FontAwesomeSolid iconType, String text, String action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(new Color(45, 45, 45));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setFocusPainted(false);
        button.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Icône
        FontIcon icon = FontIcon.of(iconType, 18, Color.WHITE);
        JLabel iconLabel = new JLabel(icon);
        button.add(iconLabel, BorderLayout.WEST);

        // Texte
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        button.add(textLabel, BorderLayout.CENTER);

        // Effets hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(45, 45, 45));
            }
        });

        // Action
        button.addActionListener(e -> {
            if (navigationHandler != null) {
                navigationHandler.accept(action);
            }
        });

        return button;
    }

    private void createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout());
        footerPanel.setBackground(new Color(35, 35, 35));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerPanel.add(versionLabel);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void toggleSidebar() {
        isExpanded = !isExpanded;

        int newWidth = isExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        setPreferredSize(new Dimension(newWidth, getHeight()));

        // Masquer/afficher le texte des boutons
        java.awt.Component[] components = menuPanel.getComponents();
        for (java.awt.Component comp : components) {
            if (comp instanceof JButton button) {
                java.awt.Component[] buttonComponents = button.getComponents();
                for (java.awt.Component buttonComp : buttonComponents) {
                    if (buttonComp instanceof JLabel label &&
                            label.getParent() == button &&
                            !label.getText().matches("[\uD83C-\uDBFF\uDC00-\uDFFF]+")) {
                        label.setVisible(isExpanded);
                    }
                }
            }
        }

        titleLabel.setVisible(isExpanded);

        revalidate();
        repaint();

        // Notifier le parent pour qu'il se redessine
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
}