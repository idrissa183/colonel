package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class ProduitContainerPanel extends JPanel {

    // Couleurs modernes
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);

    private final ProduitPanel produitPanel;
    private final FamilleProduitPanel familleProduitPanel;

    private JTabbedPane tabbedPane;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Styliser le JTabbedPane
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Ajouter les tabs avec icônes FontAwesome
        tabbedPane.addTab("Produits", null, produitPanel, "Gestion des produits");
        tabbedPane.addTab("Familles", null, familleProduitPanel, "Gestion des familles de produits");

        // Style des tabs
        styleTabComponent();
    }

    private void styleTabComponent() {
        // Personnaliser l'apparence des tabs
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, CARD_COLOR);
            tabbedPane.setForegroundAt(i, TEXT_PRIMARY);
        }

        // Définir les couleurs pour les tabs
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_PRIMARY);
        
        // Taille des tabs
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    /**
     * Méthode pour obtenir l'index de la tab actuellement sélectionnée
     */
    public int getSelectedTabIndex() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Méthode pour sélectionner une tab par index
     */
    public void setSelectedTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * Méthode pour sélectionner la tab Produits
     */
    public void showProduitsTab() {
        setSelectedTab(0);
    }

    /**
     * Méthode pour sélectionner la tab Familles
     */
    public void showFamillesTab() {
        setSelectedTab(1);
    }
}