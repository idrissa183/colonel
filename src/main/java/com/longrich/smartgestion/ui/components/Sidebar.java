package com.longrich.smartgestion.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import lombok.Setter;

@org.springframework.stereotype.Component
@Profile("!headless")
public class Sidebar extends JPanel {

    // --- Constantes de style ---
    private static final int EXPANDED_WIDTH = 290;
    private static final int COLLAPSED_WIDTH = 90; // Légèrement plus large pour un meilleur visuel
    private static final Color PRIMARY_BG = new Color(30, 41, 59); // Dark Slate
    private static final Color SECONDARY_BG = new Color(15, 23, 42); // Darker Slate
    private static final Color ACCENT_COLOR = new Color(59, 130, 246); // Blue
    private static final Color HOVER_COLOR = new Color(51, 65, 85); // Lighter Slate
    private static final Color TEXT_COLOR = new Color(226, 232, 240);
    private static final Color MUTED_TEXT_COLOR = new Color(160, 174, 192);

    // --- État du composant ---
    private boolean isExpanded = true;
    private final List<ModernMenuButton> menuButtons = new ArrayList<>();
    private ModernMenuButton selectedButton;

    // --- Dépendances et Handlers ---
    @Setter
    private Consumer<String> navigationHandler;

    // --- Composants UI ---
    private JPanel menuPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton toggleButton;

    public Sidebar() {
        // Constructeur vide pour Spring
    }

    @PostConstruct
    public void init() {
        ToolTipManager.sharedInstance().setInitialDelay(200);
        initializeUI();
    }

    private void initializeUI() {
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setBackground(PRIMARY_BG);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMenuScrollPane(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        createMenuItems();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(SECONDARY_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 15));

        titleLabel = new JLabel("SmartGestion");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        subtitleLabel = new JLabel("Longrich Store");
        subtitleLabel.setForeground(MUTED_TEXT_COLOR);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel titleContainer = new JPanel();
        titleContainer.setOpaque(false);
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.add(titleLabel);
        titleContainer.add(subtitleLabel);

        toggleButton = createIconButton(FontAwesomeSolid.BARS, "Réduire/Agrandir le menu");
        toggleButton.addActionListener(e -> toggleSidebar());

        headerPanel.add(titleContainer, BorderLayout.CENTER);
        headerPanel.add(toggleButton, BorderLayout.EAST);
        return headerPanel;
    }

    private JScrollPane createMenuScrollPane() {
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(PRIMARY_BG);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        return scrollPane;
    }

    private void createMenuItems() {
        menuButtons.clear();
        menuPanel.removeAll();

        addMenuGroup("PRINCIPAL");
        addMenuItem(FontAwesomeSolid.CHART_BAR, "Tableau de bord", "dashboard");
        addMenuItem(FontAwesomeSolid.TACHOMETER_ALT, "Analytics", "analytics");

        addMenuSeparator();
        addMenuGroup("GESTION");
        addMenuItem(FontAwesomeSolid.USERS, "Clients", "clients");
        addMenuItem(FontAwesomeSolid.BOX, "Produits", "produits");
        addMenuItem(FontAwesomeSolid.WAREHOUSE, "Stock", "stock");
        addMenuItem(FontAwesomeSolid.INDUSTRY, "Fournisseurs", "fournisseurs");

        addMenuSeparator();
        addMenuGroup("COMMERCIAL");
        addMenuItem(FontAwesomeSolid.SHOPPING_CART, "Commandes", "commandes");
        addMenuItem(FontAwesomeSolid.FILE_INVOICE_DOLLAR, "Factures", "factures");
        addMenuItem(FontAwesomeSolid.CHART_LINE, "Ventes", "ventes");
        // addMenuItem(FontAwesomeSolid.COINS, "PV & Commissions", "pv"); // Désactivé temporairement

        addMenuSeparator();
        addMenuGroup("SYSTÈME");
        addMenuItem(FontAwesomeSolid.COG, "Paramètres", "settings");
        addMenuItem(FontAwesomeSolid.USER_SHIELD, "Utilisateurs", "users");
        addMenuItem(FontAwesomeSolid.DATABASE, "Sauvegarde", "backup");

        menuPanel.add(Box.createVerticalGlue()); // Pousse tout vers le haut
        updateMenuLayout(); // Applique l'état initial
    }

    private void addMenuGroup(String groupName) {
        JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupPanel.setOpaque(false);
        groupPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 8, 0));
        JLabel groupLabel = new JLabel(groupName);
        groupLabel.setForeground(MUTED_TEXT_COLOR);
        groupLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        groupPanel.add(groupLabel);
        menuPanel.add(groupPanel);
    }

    private void addMenuItem(FontAwesomeSolid icon, String text, String action) {
        ModernMenuButton button = new ModernMenuButton(icon, text, action);
        button.addActionListener(e -> {
            setSelectedButton(button);
            if (navigationHandler != null) {
                navigationHandler.accept(action);
            }
        });
        menuButtons.add(button);
        menuPanel.add(button);
        menuPanel.add(Box.createVerticalStrut(2));
    }

    private void addMenuSeparator() {
        JPanel separatorPanel = new JPanel();
        separatorPanel.setOpaque(false);
        separatorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        menuPanel.add(separatorPanel);
    }

    private void setSelectedButton(ModernMenuButton button) {
        if (selectedButton != null) {
            selectedButton.setSelected(false);
        }
        selectedButton = button;
        selectedButton.setSelected(true);
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footerPanel.setBackground(SECONDARY_BG);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 20));

        FontIcon versionIcon = FontIcon.of(FontAwesomeSolid.INFO_CIRCLE, 12, MUTED_TEXT_COLOR);
        JLabel versionLabel = new JLabel("v1.0.0", versionIcon, SwingConstants.LEFT);
        versionLabel.setForeground(MUTED_TEXT_COLOR);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerPanel.add(versionLabel);
        return footerPanel;
    }

    private void toggleSidebar() {
        isExpanded = !isExpanded;
        int targetWidth = isExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;

        // Animateur simple pour la largeur
        Timer timer = new Timer(8, null);
        timer.addActionListener(e -> {
            int currentWidth = getPreferredSize().width;
            int newWidth;
            if (isExpanded) {
                newWidth = Math.min(targetWidth, currentWidth + 20);
            } else {
                newWidth = Math.max(targetWidth, currentWidth - 20);
            }
            setPreferredSize(new Dimension(newWidth, getHeight()));
            revalidateParent();
            if (newWidth == targetWidth) {
                timer.stop();
                updateMenuLayout(); // Mettre à jour après la fin de l'animation
            }
        });

        // Mettre à jour le titre pendant l'animation
        titleLabel.setVisible(isExpanded);
        subtitleLabel.setVisible(isExpanded);
        timer.start();
    }

    private void updateMenuLayout() {
        for (ModernMenuButton button : menuButtons) {
            button.setExpanded(isExpanded);
        }
        for (java.awt.Component comp : menuPanel.getComponents()) {
            if (comp instanceof JPanel) { // Groupes et séparateurs
                comp.setVisible(isExpanded);
            }
        }
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private void revalidateParent() {
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    private JButton createIconButton(FontAwesomeSolid icon, String tooltip) {
        JButton button = new JButton(FontIcon.of(icon, 18, TEXT_COLOR));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(32, 32));
        button.setBackground(SECONDARY_BG);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
                button.setOpaque(true);
            }

            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
            }
        });
        return button;
    }

    // --- Classe interne pour un bouton de menu moderne ---
    private static class ModernMenuButton extends JButton {
        private final JLabel textLabel;
        private final JLabel iconLabel;
        private final FontIcon icon;
        private final String text;
        private final String tooltip;
        private boolean isExpanded;

        public ModernMenuButton(FontAwesomeSolid iconCode, String text, String actionCommand) {
            super();
            this.icon = FontIcon.of(iconCode, 20, TEXT_COLOR);
            this.text = text;
            this.tooltip = text;
            this.isExpanded = true;

            setLayout(new BorderLayout());
            setBackground(PRIMARY_BG);
            setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActionCommand(actionCommand);

            iconLabel = new JLabel(icon);
            iconLabel.setPreferredSize(new Dimension(30, 24));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(iconLabel, BorderLayout.WEST);

            textLabel = new JLabel(text);
            textLabel.setForeground(TEXT_COLOR);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        public void setExpanded(boolean expanded) {
            this.isExpanded = expanded;
            textLabel.setVisible(expanded);
            setToolTipText(expanded ? null : tooltip); // Active/désactive le tooltip
            if (expanded) {
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconLabel.setPreferredSize(new Dimension(30, 24));
            } else {
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconLabel.setPreferredSize(new Dimension(50, 24));
            }
        }

        @Override
        public void setSelected(boolean b) {
            super.setSelected(b);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected()) {
                g2.setColor(ACCENT_COLOR);
            } else if (getModel().isRollover()) {
                g2.setColor(HOVER_COLOR);
            } else {
                g2.setColor(getBackground());
            }

            if (isExpanded) {
                g2.fillRoundRect(5, 0, getWidth() - 10, getHeight(), 12, 12);
            } else {
                g2.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private final Dimension zeroDim = new Dimension(0, 0);

        @Override
        protected void configureScrollBarColors() {
            // Couleur de la barre de défilement (le "pouce")
            thumbColor = HOVER_COLOR; 
            // Couleur de fond de la piste
            trackColor = PRIMARY_BG;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            // Dessiner un pouce arrondi
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        // Masquer les boutons fléchés en haut et en bas
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
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }
}
