package com.longrich.smartgestion.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Component
@Profile("!headless")
public class Sidebar extends JPanel {

    private static final int EXPANDED_WIDTH = 280;
    private static final int COLLAPSED_WIDTH = 70;
    private static final Color PRIMARY_COLOR = new Color(45, 55, 72);
    private static final Color SECONDARY_COLOR = new Color(54, 66, 87);
    private static final Color ACCENT_COLOR = new Color(66, 153, 225);
    private static final Color HOVER_COLOR = new Color(74, 85, 104);
    private static final Color TEXT_COLOR = new Color(226, 232, 240);
    private static final Color MUTED_TEXT_COLOR = new Color(160, 174, 192);

    private boolean isExpanded = true;
    private JPanel menuPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton selectedButton;

    @Setter
    private Consumer<String> navigationHandler;

    public Sidebar() {
        ToolTipManager.sharedInstance().setInitialDelay(300);
        ToolTipManager.sharedInstance().setDismissDelay(8000);
        initializeUI();
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setBackground(PRIMARY_COLOR);
        setLayout(new BorderLayout());

        createHeaderPanel();
        createMenuPanel();
        createFooterPanel();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SECONDARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 15));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(SECONDARY_COLOR);

        // Titre principal
        titleLabel = new JLabel("SmartGestion");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        // Sous-titre
        subtitleLabel = new JLabel("Longrich Store");
        subtitleLabel.setForeground(MUTED_TEXT_COLOR);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // Bouton toggle moderne
        JButton toggleButton = createToggleButton();
        headerPanel.add(toggleButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JButton createToggleButton() {
        FontIcon toggleIcon = FontIcon.of(FontAwesomeSolid.BARS, 18, TEXT_COLOR);
        JButton toggleButton = new JButton(toggleIcon);
        toggleButton.setPreferredSize(new Dimension(40, 40));
        toggleButton.setBackground(SECONDARY_COLOR);
        toggleButton.setBorder(BorderFactory.createEmptyBorder());
        toggleButton.setFocusPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Réduire/Agrandir le menu");

        // Animation hover
        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                toggleButton.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                toggleButton.setBackground(SECONDARY_COLOR);
            }
        });

        toggleButton.addActionListener(e -> toggleSidebar());
        return toggleButton;
    }

    private void createMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(PRIMARY_COLOR);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        createMenuItems();

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createMenuItems() {
        menuPanel.removeAll();

        if (isExpanded) {
            // Mode étendu avec groupes
            addMenuGroup("PRINCIPAL");
            Object[][] mainItems = {
                    { FontAwesomeSolid.CHART_BAR, "Tableau de bord", "dashboard" },
                    { FontAwesomeSolid.TACHOMETER_ALT, "Analytics", "analytics" }
            };
            addMenuItems(mainItems);

            addMenuSeparator();
            addMenuGroup("GESTION");
            Object[][] managementItems = {
                    { FontAwesomeSolid.USERS, "Clients", "clients" },
                    { FontAwesomeSolid.BOX, "Produits", "produits" },
                    { FontAwesomeSolid.WAREHOUSE, "Stock", "stock" },
                    { FontAwesomeSolid.INDUSTRY, "Fournisseurs", "fournisseurs" }
            };
            addMenuItems(managementItems);

            addMenuSeparator();
            addMenuGroup("COMMERCIAL");
            Object[][] commercialItems = {
                    { FontAwesomeSolid.SHOPPING_CART, "Commandes", "commandes" },
                    { FontAwesomeSolid.FILE_INVOICE_DOLLAR, "Factures", "factures" },
                    { FontAwesomeSolid.CHART_LINE, "Ventes", "ventes" },
                    { FontAwesomeSolid.COINS, "PV & Commissions", "pv" }
            };
            addMenuItems(commercialItems);

            addMenuSeparator();
            addMenuGroup("SYSTÈME");
            Object[][] systemItems = {
                    { FontAwesomeSolid.COG, "Paramètres", "settings" },
                    { FontAwesomeSolid.USER_SHIELD, "Utilisateurs", "users" },
                    { FontAwesomeSolid.DATABASE, "Sauvegarde", "backup" }
            };
            addMenuItems(systemItems);
        } else {
            // Mode réduit - icônes seulement
            Object[][] allItems = {
                    { FontAwesomeSolid.CHART_BAR, "Tableau de bord", "dashboard" },
                    { FontAwesomeSolid.USERS, "Clients", "clients" },
                    { FontAwesomeSolid.BOX, "Produits", "produits" },
                    { FontAwesomeSolid.WAREHOUSE, "Stock", "stock" },
                    { FontAwesomeSolid.SHOPPING_CART, "Commandes", "commandes" },
                    { FontAwesomeSolid.FILE_INVOICE_DOLLAR, "Factures", "factures" },
                    { FontAwesomeSolid.INDUSTRY, "Fournisseurs", "fournisseurs" },
                    { FontAwesomeSolid.CHART_LINE, "Ventes", "ventes" },
                    { FontAwesomeSolid.COG, "Paramètres", "settings" }
            };

            for (Object[] item : allItems) {
                JButton button = createCompactMenuButton(
                        (FontAwesomeSolid) item[0],
                        (String) item[1],
                        (String) item[2]);
                menuPanel.add(button);
                menuPanel.add(Box.createVerticalStrut(5));
            }
        }
    }

    private void addMenuGroup(String groupName) {
        JLabel groupLabel = new JLabel(groupName);
        groupLabel.setForeground(MUTED_TEXT_COLOR);
        groupLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        groupLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 8, 0));
        menuPanel.add(groupLabel);
    }

    private void addMenuSeparator() {
        menuPanel.add(Box.createVerticalStrut(10));
    }

    private void addMenuItems(Object[][] items) {
        for (Object[] item : items) {
            JButton menuButton = createModernMenuButton(
                    (FontAwesomeSolid) item[0],
                    (String) item[1],
                    (String) item[2]);
            menuPanel.add(menuButton);
            menuPanel.add(Box.createVerticalStrut(2));
        }
    }

    private JButton createModernMenuButton(FontAwesomeSolid icon, String text, String action) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (this == selectedButton) {
                    g2.setColor(ACCENT_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(HOVER_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setLayout(new BorderLayout());
        button.setBackground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icône
        FontIcon fontIcon = FontIcon.of(icon, 20, TEXT_COLOR);
        JLabel iconLabel = new JLabel(fontIcon);
        iconLabel.setPreferredSize(new Dimension(30, 24));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        button.add(iconLabel, BorderLayout.WEST);

        // Texte
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(TEXT_COLOR);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        button.add(textLabel, BorderLayout.CENTER);

        // Tooltip pour mode réduit
        button.setToolTipText(text);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        button.addActionListener(e -> {
            setSelectedButton(button);
            if (navigationHandler != null) {
                navigationHandler.accept(action);
            }
        });

        return button;
    }

    private JButton createCompactMenuButton(FontAwesomeSolid icon, String text, String action) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (this == selectedButton) {
                    g2.setColor(ACCENT_COLOR);
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(HOVER_COLOR);
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        FontIcon fontIcon = FontIcon.of(icon, 22, TEXT_COLOR);
        button.setIcon(fontIcon);
        button.setPreferredSize(new Dimension(50, 45));
        button.setBackground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(text);

        button.addActionListener(e -> {
            setSelectedButton(button);
            if (navigationHandler != null) {
                navigationHandler.accept(action);
            }
        });

        return button;
    }

    private void setSelectedButton(JButton button) {
        if (selectedButton != null) {
            selectedButton.repaint();
        }
        selectedButton = button;
        button.repaint();
    }

    private void createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(SECONDARY_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        versionPanel.setBackground(SECONDARY_COLOR);

        FontIcon versionIcon = FontIcon.of(FontAwesomeSolid.INFO_CIRCLE, 12, MUTED_TEXT_COLOR);
        JLabel versionIconLabel = new JLabel(versionIcon);
        versionPanel.add(versionIconLabel);

        JLabel versionLabel = new JLabel(" v1.0.0 - Beta");
        versionLabel.setForeground(MUTED_TEXT_COLOR);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionPanel.add(versionLabel);

        footerPanel.add(versionPanel, BorderLayout.WEST);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void toggleSidebar() {
        isExpanded = !isExpanded;

        Timer animationTimer = new Timer(10, null);
        int targetWidth = isExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        int currentWidth = getPreferredSize().width;
        int step = (targetWidth - currentWidth) / 10;

        animationTimer.addActionListener(e -> {
            int newWidth = getPreferredSize().width + step;
            if ((step > 0 && newWidth >= targetWidth) || (step < 0 && newWidth <= targetWidth)) {
                newWidth = targetWidth;
                animationTimer.stop();
                updateVisibility();
            }
            setPreferredSize(new Dimension(newWidth, getHeight()));
            revalidate();
            repaint();

            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        });

        animationTimer.start();
    }

    private void updateVisibility() {
        titleLabel.setVisible(isExpanded);
        subtitleLabel.setVisible(isExpanded);
        createMenuItems();
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    // Classe interne pour personnaliser la scrollbar
    private static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(74, 85, 104);
            this.trackColor = new Color(45, 55, 72);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}