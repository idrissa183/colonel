package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.springframework.stereotype.Component;

import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.ProduitService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DashboardPanel extends JPanel {

    private final ClientService clientService;
    private final ProduitService produitService;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        createStatsPanel();
        createChartsPanel();
        createRecentActivitiesPanel();
    }

    private void createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Cartes de statistiques
        statsPanel.add(createStatCard("Clients", "150", "👥", new Color(0, 123, 255)));
        statsPanel.add(createStatCard("Produits", "89", "📦", new Color(40, 167, 69)));
        statsPanel.add(createStatCard("Commandes", "45", "🛒", new Color(255, 193, 7)));
        statsPanel.add(createStatCard("CA du mois", "2,450,000 F", "💰", new Color(220, 53, 69)));

        add(statsPanel, BorderLayout.NORTH);
    }

    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(200, 120));

        // Icône
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.WEST);

        // Contenu
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        contentPanel.add(valueLabel, BorderLayout.CENTER);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private void createChartsPanel() {
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Graphique des ventes
        JPanel salesChart = createChartPanel("Évolution des ventes", "Graphique des ventes mensuelles");
        chartsPanel.add(salesChart);

        // Graphique des produits populaires
        JPanel productsChart = createChartPanel("Produits populaires", "Top 10 des produits les plus vendus");
        chartsPanel.add(productsChart);

        add(chartsPanel, BorderLayout.CENTER);
    }

    private JPanel createChartPanel(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(descLabel, BorderLayout.CENTER);

        return panel;
    }

    private void createRecentActivitiesPanel() {
        JPanel activitiesPanel = new JPanel(new BorderLayout());
        activitiesPanel.setBackground(Color.WHITE);
        activitiesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Activités récentes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activitiesPanel.add(titleLabel, BorderLayout.NORTH);

        // Liste des activités
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Nouvelle commande #CMD-001 créée");
        listModel.addElement("Client Jean Dupont ajouté");
        listModel.addElement("Stock du produit XYZ mis à jour");
        listModel.addElement("Facture #FACT-001 générée");

        JList<String> activitiesList = new JList<>(listModel);
        activitiesList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activitiesList.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(activitiesList);
        scrollPane.setBorder(null);
        activitiesPanel.add(scrollPane, BorderLayout.CENTER);

        add(activitiesPanel, BorderLayout.SOUTH);
    }
}