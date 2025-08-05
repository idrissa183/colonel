package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.ProduitService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class DashboardPanel extends JPanel {

    // Couleurs modernes identiques à ProduitPanel
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    // private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);

    private final ClientService clientService;
    private final ProduitService produitService;

    // Components pour les stats dynamiques
    private JLabel clientsCountLabel;
    private JLabel produitsCountLabel;
    private JLabel stockAlertsLabel;
    private JLabel caLabel;
    private DefaultListModel<String> activitiesModel;
    // private JLabel lastUpdateLabel;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createMainContent();
        createFooterPanel();

        // Charger les données initiales
        loadDashboardData();
        startAutoRefresh();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre principal
        JLabel titleLabel = new JLabel("Tableau de Bord");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Dernière mise à jour
        // lastUpdateLabel = new JLabel();
        // lastUpdateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // lastUpdateLabel.setForeground(TEXT_SECONDARY);
        // headerPanel.add(lastUpdateLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Panneau supérieur - Statistiques
        JPanel statsContainer = createStatsPanel();
        mainPanel.add(statsContainer, BorderLayout.NORTH);

        // Panneau central - Graphiques et activités
        JPanel centerContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        centerContainer.setBackground(BACKGROUND_COLOR);
        
        // Graphiques fictifs (en attendant une vraie implémentation)
        JPanel chartsPanel = createChartsPanel();
        centerContainer.add(chartsPanel);
        
        // Activités récentes
        JPanel activitiesPanel = createActivitiesPanel();
        centerContainer.add(activitiesPanel);
        
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Panneau inférieur - Alerts
        JPanel alertsPanel = createAlertsPanel();
        mainPanel.add(alertsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel container = new JPanel(new GridLayout(1, 4, 15, 0));
        container.setBackground(BACKGROUND_COLOR);

        // Cartes de statistiques modernes
        container.add(createStatCard("Clients", "0", FontAwesomeSolid.USERS, PRIMARY_COLOR, "clientsCount"));
        container.add(createStatCard("Produits", "0", FontAwesomeSolid.BOX, SUCCESS_COLOR, "produitsCount"));
        container.add(createStatCard("Alertes Stock", "0", FontAwesomeSolid.EXCLAMATION_TRIANGLE, WARNING_COLOR, "stockAlerts"));
        container.add(createStatCard("CA du mois", "0 FCFA", FontAwesomeSolid.CHART_LINE, INFO_COLOR, "ca"));

        return container;
    }

    private JPanel createStatCard(String title, String value, FontAwesomeSolid icon, Color color, String type) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Header avec icône
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        FontIcon fontIcon = FontIcon.of(icon, 24, color);
        JLabel iconLabel = new JLabel(fontIcon);
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        card.add(headerPanel);
        card.add(Box.createVerticalStrut(15));

        // Valeur principale
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        card.add(valueLabel);

        // Stocker la référence pour mise à jour
        switch (type) {
            case "clientsCount":
                clientsCountLabel = valueLabel;
                break;
            case "produitsCount":
                produitsCountLabel = valueLabel;
                break;
            case "stockAlerts":
                stockAlertsLabel = valueLabel;
                break;
            case "ca":
                caLabel = valueLabel;
                break;
        }

        // Effet hover
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_COLOR);
                card.repaint();
            }
        });

        return card;
    }

    private JPanel createChartsPanel() {
        JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        chartsContainer.setBackground(BACKGROUND_COLOR);

        // Graphique des ventes (placeholder)
        JPanel salesChart = createChartPlaceholder("Évolution des Ventes", 
                "📈 Graphique des ventes mensuelles", SUCCESS_COLOR);
        chartsContainer.add(salesChart);

        // Graphique des produits populaires (placeholder)
        JPanel popularChart = createChartPlaceholder("Produits Populaires", 
                "🏆 Top 5 des produits les plus vendus", PRIMARY_COLOR);
        chartsContainer.add(popularChart);

        return chartsContainer;
    }

    private JPanel createChartPlaceholder(String title, String description, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Contenu central avec icône
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(descLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        JLabel comingSoonLabel = new JLabel("Graphique interactif bientôt disponible");
        comingSoonLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        comingSoonLabel.setForeground(TEXT_SECONDARY);
        comingSoonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(comingSoonLabel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActivitiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Activités Récentes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        FontIcon refreshIcon = FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, TEXT_SECONDARY);
        JLabel refreshLabel = new JLabel(refreshIcon);
        refreshLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshLabel.setToolTipText("Actualiser les activités");
        refreshLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadRecentActivities();
            }
        });
        headerPanel.add(refreshLabel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Liste des activités
        activitiesModel = new DefaultListModel<>();
        JList<String> activitiesList = new JList<>(activitiesModel);
        activitiesList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activitiesList.setBackground(CARD_COLOR);
        activitiesList.setSelectionBackground(new Color(37, 99, 235, 20));
        activitiesList.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(activitiesList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNING_COLOR, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Header avec icône d'alerte
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_COLOR);

        FontIcon alertIcon = FontIcon.of(FontAwesomeSolid.EXCLAMATION_TRIANGLE, 18, WARNING_COLOR);
        JLabel iconLabel = new JLabel(alertIcon);
        titlePanel.add(iconLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("  Alertes Importantes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(WARNING_COLOR);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.WEST);

        // Message d'alerte
        JLabel alertLabel = new JLabel("Aucune alerte critique pour le moment");
        alertLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        alertLabel.setForeground(TEXT_SECONDARY);
        panel.add(alertLabel, BorderLayout.CENTER);

        return panel;
    }

    private void createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel footerLabel = new JLabel("SmartGestion • Longrich Store Management System");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(TEXT_SECONDARY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(footerLabel, BorderLayout.CENTER);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadDashboardData() {
        try {
            // Charger les statistiques réelles
            List<ClientDTO> clients = clientService.getAllClients();
            List<ProduitDto> produits = produitService.getActiveProduits();

            if (clientsCountLabel != null) {
                clientsCountLabel.setText(String.valueOf(clients.size()));
            }

            if (produitsCountLabel != null) {
                produitsCountLabel.setText(String.valueOf(produits.size()));
            }

            // Calculer les alertes de stock (produits avec stock faible)
            long stockAlerts = produits.stream()
                    .filter(p -> p.getQuantiteStock() != null && p.getStockMinimum() != null)
                    .filter(p -> p.getQuantiteStock() <= p.getStockMinimum())
                    .count();

            if (stockAlertsLabel != null) {
                stockAlertsLabel.setText(String.valueOf(stockAlerts));
                stockAlertsLabel.setForeground(stockAlerts > 0 ? DANGER_COLOR : SUCCESS_COLOR);
            }

            // CA fictif (à implémenter avec les vraies données de vente)
            if (caLabel != null) {
                caLabel.setText("Bientôt disponible");
                caLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            }

            loadRecentActivities();
            // updateLastUpdateTime();

        } catch (Exception e) {
            // Gestion d'erreur silencieuse pour ne pas casser l'interface
            System.err.println("Erreur lors du chargement des données du dashboard: " + e.getMessage());
        }
    }

    private void loadRecentActivities() {
        if (activitiesModel != null) {
            activitiesModel.clear();
            
            // Ajouter des activités avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            
            try {
                List<ClientDTO> recentClients = clientService.getAllClients();
                List<ProduitDto> recentProduits = produitService.getActiveProduits();

                if (!recentClients.isEmpty()) {
                    activitiesModel.addElement(String.format("🆕 %s - %d clients enregistrés", 
                            timestamp, recentClients.size()));
                }

                if (!recentProduits.isEmpty()) {
                    activitiesModel.addElement(String.format("📦 %s - %d produits actifs", 
                            timestamp, recentProduits.size()));
                }

                activitiesModel.addElement(String.format("🔄 %s - Données actualisées", timestamp));
                activitiesModel.addElement(String.format("💡 %s - Système opérationnel", timestamp));

            } catch (Exception e) {
                activitiesModel.addElement(String.format("⚠️ %s - Erreur de chargement des données", timestamp));
            }
        }
    }

    // private void updateLastUpdateTime() {
    //     if (lastUpdateLabel != null) {
    //         String currentTime = LocalDateTime.now().format(
    //                 DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss"));
    //         lastUpdateLabel.setText("Dernière mise à jour: " + currentTime);
    //     }
    // }

    private void startAutoRefresh() {
        // Actualisation automatique toutes les 5 minutes
        Timer refreshTimer = new Timer(300000, e -> {
            loadDashboardData();
        });
        refreshTimer.start();
    }
}