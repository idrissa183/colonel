package com.longrich.smartgestion.ui.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// import com.longrich.smartgestion.entity.Client;

import com.longrich.smartgestion.entity.Commande;
import com.longrich.smartgestion.entity.Facture;
// import com.longrich.smartgestion.entity.Produit;
// import com.longrich.smartgestion.enums.StatutCommande;
// import com.longrich.smartgestion.enums.TypeClient;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.VenteService;
import com.longrich.smartgestion.service.BonusAttribueService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.service.VentePromotionnelleService;
import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;
import com.longrich.smartgestion.ui.components.ModernDatePicker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class VentePanel extends JPanel {

    private final ClientService clientService;
    private final VenteService venteService;
    private final BonusAttribueService bonusAttribueService;
    private final ProduitService produitService;
    private final VentePromotionnelleService ventePromotionnelleService;

    // Composants UI
    private JTextField searchField;
    private JComboBox<String> periodeCombo;
    private JComboBox<String> statusCombo;
    private ModernDatePicker dateDebutPicker;
    private ModernDatePicker dateFinPicker;
    private JTable ventesTable;
    private DefaultTableModel tableModel;
    private JLabel totalVentesLabel;
    private JLabel caJourLabel;
    private JLabel caMoisLabel;
    private JLabel commandesEnCoursLabel;
    private JTabbedPane tabbedPane;
    // Promo tab components
    private JTable promosTable;
    private DefaultTableModel promosModel;
    private JTable bonusNonDistribuesTable;
    private DefaultTableModel bonusNonDistribuesModel;
    private JTable bonusRapportTable;
    private DefaultTableModel bonusRapportModel;
    private ModernDatePicker bonusDateDebutPicker;
    private ModernDatePicker bonusDateFinPicker;
    private JComboBox<String> bonusProduitCombo;

    // Données simulées
    private List<VenteRecord> ventes;
    private List<Commande> commandesEnCours;
    private List<Facture> facturesImpayes;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ComponentFactory.getBackgroundColor());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeData();
        createHeaderPanel();
        createMainContent();
        loadVentesData();
        updateStatistics();
    }

    private void initializeData() {
        ventes = new ArrayList<>();
        commandesEnCours = new ArrayList<>();
        facturesImpayes = new ArrayList<>();

        // Données simulées pour démonstration
        LocalDateTime now = LocalDateTime.now();

        // Ventes récentes
        ventes.add(new VenteRecord("VTE001", "FACT-001", "Martin Dupont", "Partenaire",
                now.minusHours(2), new BigDecimal("150000"), new BigDecimal("450"), "Payée"));
        ventes.add(new VenteRecord("VTE002", "FACT-002", "Sophie Laurent", "Semi-grossiste",
                now.minusHours(5), new BigDecimal("89000"), new BigDecimal("267"), "En attente"));
        ventes.add(new VenteRecord("VTE003", "FACT-003", "Ahmed Ben Ali", "Grossiste",
                now.minusDays(1), new BigDecimal("245000"), new BigDecimal("735"), "Payée"));
        ventes.add(new VenteRecord("VTE004", "FACT-004", "Fatou Ouédraogo", "Individuel",
                now.minusDays(2), new BigDecimal("25000"), new BigDecimal("75"), "Annulée"));
        ventes.add(new VenteRecord("VTE005", "FACT-005", "Ibrahim Sankara", "Partenaire",
                now.minusDays(3), new BigDecimal("180000"), new BigDecimal("540"), "Payée"));
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ComponentFactory.getBackgroundColor());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(ComponentFactory.getBackgroundColor());

        JLabel titleLabel = new JLabel("Gestion des Ventes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ComponentFactory.getTextPrimaryColor());
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Actions rapides
        JPanel actionsPanel = createQuickActionsPanel();
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(ComponentFactory.getBackgroundColor());

        JButton newSaleButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.PLUS, "Nouvelle Vente", ComponentFactory.getSuccessColor(),
                e -> createNewSale());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", ComponentFactory.getSecondaryColor(),
                e -> refreshData());
        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter", ComponentFactory.getPrimaryColor(),
                e -> exportSalesData());

        panel.add(newSaleButton);
        panel.add(refreshButton);
        panel.add(exportButton);

        return panel;
    }

    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(ComponentFactory.getBackgroundColor());

        // Onglet Tableau de bord
        tabbedPane.addTab("📊 Tableau de bord", createDashboardPanel());

        // Onglet Historique des ventes
        tabbedPane.addTab("📋 Historique", createHistoryPanel());

        // Onglet Commandes en cours
        tabbedPane.addTab("⏳ Commandes en cours", createOrdersPanel());

        // Onglet Factures impayées
        tabbedPane.addTab("💳 Factures impayées", createUnpaidInvoicesPanel());

        // Onglet Rapports
        tabbedPane.addTab("📈 Rapports", createReportsPanel());

        // Onglet Ventes promotionnelles
        tabbedPane.addTab("⭐ Promo", createPromotionsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // Statistiques en haut
        JPanel statsPanel = createStatisticsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);

        // Contenu principal
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(ComponentFactory.getBackgroundColor());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Bandeau promotions de la semaine
        JPanel promoBanner = createWeeklyPromotionsBanner();
        contentPanel.add(promoBanner, BorderLayout.NORTH);

        // Grille des cartes
        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
        grid.setBackground(ComponentFactory.getBackgroundColor());
        grid.add(createSalesChartCard());
        grid.add(createTopClientsCard());
        grid.add(createTopProductsCard());
        grid.add(createSalesTargetsCard());
        contentPanel.add(grid, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWeeklyPromotionsBanner() {
        JPanel banner = ComponentFactory.createCardPanel();
        banner.setLayout(new BorderLayout());
        JLabel title = ComponentFactory.createSectionTitle("Promotions en cours cette semaine");
        banner.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(ComponentFactory.getCardColor());

        try {
            var promos = venteService.getPromotionsActives();
            if (promos.isEmpty()) {
                list.add(new JLabel("Aucune promotion active"));
            } else {
                for (var pp : promos) {
                    String line = "• " + pp.getProduit().getLibelle() + " → Bonus: " + pp.getProduitBonus().getLibelle() +
                            " (seuil: " + pp.getQuantiteMinimum() + ", bonus: " + pp.getQuantiteBonus() + ")";
                    JLabel lbl = new JLabel(line);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    list.add(lbl);
                }
            }
        } catch (Exception ignored) {
            list.add(new JLabel("Promotions indisponibles"));
        }

        banner.add(list, BorderLayout.CENTER);
        return banner;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // CA Jour
        JPanel caJourCard = createStatCard("CA Aujourd'hui", "0 FCFA", ComponentFactory.getSuccessColor(),
                FontAwesomeSolid.CALENDAR_DAY);
        caJourLabel = (JLabel) ((JPanel) caJourCard.getComponent(1)).getComponent(0);
        panel.add(caJourCard);

        // CA Mois
        JPanel caMoisCard = createStatCard("CA ce Mois", "0 FCFA", ComponentFactory.getPrimaryColor(),
                FontAwesomeSolid.CALENDAR_ALT);
        caMoisLabel = (JLabel) ((JPanel) caMoisCard.getComponent(1)).getComponent(0);
        panel.add(caMoisCard);

        // Total ventes
        JPanel totalCard = createStatCard("Total Ventes", "0", ComponentFactory.getWarningColor(),
                FontAwesomeSolid.SHOPPING_CART);
        totalVentesLabel = (JLabel) ((JPanel) totalCard.getComponent(1)).getComponent(0);
        panel.add(totalCard);

        // Commandes en cours
        JPanel commandesCard = createStatCard("Commandes en cours", "0", ComponentFactory.getSecondaryColor(),
                FontAwesomeSolid.CLOCK);
        commandesEnCoursLabel = (JLabel) ((JPanel) commandesCard.getComponent(1)).getComponent(0);
        panel.add(commandesCard);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color, FontAwesomeSolid icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ComponentFactory.getBorderColor(), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Icône
        JLabel iconLabel = new JLabel(org.kordamp.ikonli.swing.FontIcon.of(icon, 32, color));
        card.add(iconLabel, BorderLayout.WEST);

        // Contenu
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(ComponentFactory.getTextSecondaryColor());

        contentPanel.add(valueLabel);
        contentPanel.add(titleLabel);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSalesChartCard() {
        JPanel card = ComponentFactory.createCardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Évolution des Ventes (30 derniers jours)");
        card.add(title, BorderLayout.NORTH);

        // Simulation d'un graphique
        JPanel chartArea = new JPanel();
        chartArea.setBackground(Color.WHITE);
        chartArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartArea.add(new JLabel("📈 Graphique des ventes en cours de développement"));
        card.add(chartArea, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTopClientsCard() {
        JPanel card = ComponentFactory.createCardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Top 5 Clients");
        card.add(title, BorderLayout.NORTH);

        JPanel clientsList = new JPanel();
        clientsList.setLayout(new BoxLayout(clientsList, BoxLayout.Y_AXIS));
        clientsList.setBackground(ComponentFactory.getCardColor());
        clientsList.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        String[] topClients = { "Martin Dupont - 450,000 FCFA", "Ahmed Ben Ali - 245,000 FCFA",
                "Ibrahim Sankara - 180,000 FCFA", "Sophie Laurent - 89,000 FCFA",
                "Fatou Ouédraogo - 25,000 FCFA" };

        for (int i = 0; i < topClients.length; i++) {
            JLabel clientLabel = new JLabel((i + 1) + ". " + topClients[i]);
            clientLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            clientLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            clientsList.add(clientLabel);
        }

        card.add(clientsList, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTopProductsCard() {
        JPanel card = ComponentFactory.createCardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Produits les plus vendus");
        card.add(title, BorderLayout.NORTH);

        JPanel productsList = new JPanel();
        productsList.setLayout(new BoxLayout(productsList, BoxLayout.Y_AXIS));
        productsList.setBackground(ComponentFactory.getCardColor());
        productsList.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        String[] topProducts = { "Longrich Superbklean - 45 unités", "Longrich Bamboo Salt - 32 unités",
                "Longrich Cordyceps - 28 unités", "Longrich Wig Shampoo - 25 unités",
                "Longrich Hand Cream - 20 unités" };

        for (int i = 0; i < topProducts.length; i++) {
            JLabel productLabel = new JLabel((i + 1) + ". " + topProducts[i]);
            productLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            productLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            productsList.add(productLabel);
        }

        card.add(productsList, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSalesTargetsCard() {
        JPanel card = ComponentFactory.createCardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Objectifs de Vente");
        card.add(title, BorderLayout.NORTH);

        JPanel targetsPanel = new JPanel();
        targetsPanel.setLayout(new BoxLayout(targetsPanel, BoxLayout.Y_AXIS));
        targetsPanel.setBackground(ComponentFactory.getCardColor());
        targetsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Objectif mensuel
        JPanel objectifMois = createProgressBar("Objectif Mensuel", 689000, 1000000,
                ComponentFactory.getPrimaryColor());
        targetsPanel.add(objectifMois);

        targetsPanel.add(Box.createVerticalStrut(15));

        // Objectif trimestriel
        JPanel objectifTrimestre = createProgressBar("Objectif Trimestriel", 1890000, 3000000,
                ComponentFactory.getSuccessColor());
        targetsPanel.add(objectifTrimestre);

        card.add(targetsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProgressBar(String label, long current, long target, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getCardColor());

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(titleLabel, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar(0, (int) target);
        progressBar.setValue((int) current);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%,d / %,d FCFA (%d%%)", current, target, (current * 100) / target));
        progressBar.setForeground(color);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        panel.add(progressBar, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // Filtres
        JPanel filtersPanel = createFiltersPanel();
        panel.add(filtersPanel, BorderLayout.NORTH);

        // Table des ventes
        JPanel tablePanel = createVentesTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFiltersPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Recherche
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(ComponentFactory.createLabel("Recherche:"), gbc);

        searchField = ComponentFactory.createStyledTextField("Numéro, client...");
        searchField.setPreferredSize(new Dimension(200, 38));
        searchField.addActionListener(e -> filterVentes());
        gbc.gridx = 1;
        panel.add(ComponentFactory.createSearchField(searchField), gbc);

        // Période
        gbc.gridx = 2;
        panel.add(ComponentFactory.createLabel("Période:"), gbc);

        String[] periodes = { "Aujourd'hui", "Cette semaine", "Ce mois", "Ce trimestre", "Cette année",
                "Personnalisée" };
        periodeCombo = ComponentFactory.createStyledComboBox(periodes);
        periodeCombo.addActionListener(this::onPeriodeChange);
        gbc.gridx = 3;
        panel.add(periodeCombo, gbc);

        // Statut
        gbc.gridx = 4;
        panel.add(ComponentFactory.createLabel("Statut:"), gbc);

        String[] statuts = { "Tous", "Payée", "En attente", "Annulée" };
        statusCombo = ComponentFactory.createStyledComboBox(statuts);
        statusCombo.addActionListener(e -> filterVentes());
        gbc.gridx = 5;
        panel.add(statusCombo, gbc);

        // Dates personnalisées (initialement cachées)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(ComponentFactory.getCardColor());
        datePanel.add(ComponentFactory.createLabel("Du:"));
        dateDebutPicker = new ModernDatePicker(LocalDateTime.now().minusMonths(1).toLocalDate());
        datePanel.add(dateDebutPicker);
        datePanel.add(Box.createHorizontalStrut(10));
        datePanel.add(ComponentFactory.createLabel("Au:"));
        dateFinPicker = new ModernDatePicker(LocalDateTime.now().toLocalDate());
        datePanel.add(dateFinPicker);
        datePanel.setVisible(false);
        panel.add(datePanel, gbc);

        return panel;
    }

    private JPanel createVentesTablePanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        JLabel tableTitle = ComponentFactory.createSectionTitle("Historique des Ventes");
        panel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = { "N° Vente", "N° Facture", "Client", "Type Client", "Date", "Montant (FCFA)", "PV",
                "Statut", "Actions" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Seulement la colonne Actions
            }
        };

        ventesTable = new JTable(tableModel);
        styleTable(ventesTable);

        // Ajuster les largeurs des colonnes
        ventesTable.getColumn("N° Vente").setPreferredWidth(80);
        ventesTable.getColumn("N° Facture").setPreferredWidth(80);
        ventesTable.getColumn("Client").setPreferredWidth(150);
        ventesTable.getColumn("Type Client").setPreferredWidth(100);
        ventesTable.getColumn("Date").setPreferredWidth(120);
        ventesTable.getColumn("Montant (FCFA)").setPreferredWidth(120);
        ventesTable.getColumn("PV").setPreferredWidth(80);
        ventesTable.getColumn("Statut").setPreferredWidth(80);
        ventesTable.getColumn("Actions").setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(ventesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Commandes en cours de traitement");
        panel.add(title, BorderLayout.NORTH);

        // Table des commandes en cours
        String[] columns = { "N° Commande", "Client", "Date Commande", "Date Livraison", "Statut", "Montant",
                "Actions" };
        DefaultTableModel ordersModel = new DefaultTableModel(columns, 0);
        JTable ordersTable = new JTable(ordersModel);
        styleTable(ordersTable);

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUnpaidInvoicesPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Factures impayées");
        panel.add(title, BorderLayout.NORTH);

        // Table des factures impayées
        String[] columns = { "N° Facture", "Client", "Date Émission", "Date Échéance", "Montant Total", "Montant Payé",
                "Reste à Payer", "Actions" };
        DefaultTableModel invoicesModel = new DefaultTableModel(columns, 0);
        JTable invoicesTable = new JTable(invoicesModel);
        styleTable(invoicesTable);

        JScrollPane scrollPane = new JScrollPane(invoicesTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // En-tête avec sélection de rapport
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(ComponentFactory.getBackgroundColor());

        headerPanel.add(ComponentFactory.createLabel("Type de rapport:"));
        String[] reportTypes = { "Ventes par période", "Ventes par client", "Ventes par produit", "CA par vendeur",
                "Comparatif mensuel" };
        JComboBox<String> reportCombo = ComponentFactory.createStyledComboBox(reportTypes);
        headerPanel.add(reportCombo);

        JButton generateButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.CHART_BAR, "Générer", ComponentFactory.getPrimaryColor(),
                e -> generateReport());
        headerPanel.add(generateButton);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Zone de contenu du rapport
        JPanel reportContent = ComponentFactory.createCardPanel();
        reportContent.add(new JLabel("Sélectionnez un type de rapport et cliquez sur Générer"));
        panel.add(reportContent, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(37, 99, 235, 20));
        table.setSelectionForeground(ComponentFactory.getTextPrimaryColor());
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(ComponentFactory.getTextSecondaryColor());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ComponentFactory.getBorderColor()));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }

                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setForeground(ComponentFactory.getTextPrimaryColor());

                // Couleurs spéciales pour les statuts
                if (table.getColumnName(column).equals("Statut")) {
                    if ("Payée".equals(value)) {
                        setForeground(ComponentFactory.getSuccessColor());
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("En attente".equals(value)) {
                        setForeground(ComponentFactory.getWarningColor());
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Annulée".equals(value)) {
                        setForeground(ComponentFactory.getDangerColor());
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }

                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    // Méthodes d'action
    private void onPeriodeChange(ActionEvent e) {
        JComboBox<?> combo = (JComboBox<?>) e.getSource();
        boolean isPersonnalisee = "Personnalisée".equals(combo.getSelectedItem());

        // Afficher/masquer les sélecteurs de date
        Container parent = dateDebutPicker.getParent();
        if (parent != null) {
            parent.setVisible(isPersonnalisee);
            parent.getParent().revalidate();
            parent.getParent().repaint();
        }

        if (!isPersonnalisee) {
            filterVentes();
        }
    }

    private void loadVentesData() {
        tableModel.setRowCount(0);
        for (VenteRecord vente : ventes) {
            Object[] row = {
                    vente.numeroVente,
                    vente.numeroFacture,
                    vente.clientNom,
                    vente.typeClient,
                    vente.dateVente.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    String.format("%,.0f", vente.montant),
                    vente.pv,
                    vente.statut,
                    "" // Actions
            };
            tableModel.addRow(row);
        }
    }

    private void filterVentes() {
        // TODO: Implémenter le filtrage réel
        loadVentesData();
    }

    private void updateStatistics() {
        // Calculs basiques pour démonstration
        BigDecimal caJour = ventes.stream()
                .filter(v -> v.dateVente.toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .map(v -> v.montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal caMois = ventes.stream()
                .filter(v -> v.dateVente.getMonth() == LocalDateTime.now().getMonth())
                .map(v -> v.montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalVentes = ventes.size();
        long commandesEnCours = 3; // Simulé

        caJourLabel.setText(String.format("%,.0f FCFA", caJour));
        caMoisLabel.setText(String.format("%,.0f FCFA", caMois));
        totalVentesLabel.setText(String.valueOf(totalVentes));
        commandesEnCoursLabel.setText(String.valueOf(commandesEnCours));
    }

    private void createNewSale() {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Nouvelle vente", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = ComponentFactory.createCardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JComboBox<String> clientCombo = ComponentFactory.createStyledComboBox();
        JComboBox<String> produitCombo = ComponentFactory.createStyledComboBox();
        JTextField quantiteField = ComponentFactory.createStyledTextField();

        // Charger données
        List<com.longrich.smartgestion.dto.ClientDTO> clients;
        List<com.longrich.smartgestion.dto.ProduitDto> produits;
        try {
            clients = clientService.getAllClients();
            produits = produitService.getActiveProduits();
        } catch (Exception ex) { clients = List.of(); produits = List.of(); }

        for (var c : clients) clientCombo.addItem((c.getNom() + " " + c.getPrenom()) + " (ID:" + c.getId() + ")");
        for (var pr : produits) produitCombo.addItem(pr.getLibelle() + " (ID:" + pr.getId() + ")");

        p.add(ComponentFactory.createFieldPanel("Client", clientCombo));
        p.add(ComponentFactory.createFieldPanel("Produit", produitCombo));
        p.add(ComponentFactory.createFieldPanel("Quantité", quantiteField));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(ComponentFactory.getCardColor());
        JButton cancel = ButtonFactory.createActionButton(FontAwesomeSolid.TIMES, "Annuler", ComponentFactory.getSecondaryColor(), e -> d.dispose());
        final java.util.List<com.longrich.smartgestion.dto.ClientDTO> clientsFinal = clients;
        final java.util.List<com.longrich.smartgestion.dto.ProduitDto> produitsFinal = produits;
        JButton save = ButtonFactory.createActionButton(FontAwesomeSolid.SAVE, "Valider", ComponentFactory.getSuccessColor(), e -> {
            try {
                int ci = clientCombo.getSelectedIndex();
                int pi = produitCombo.getSelectedIndex();
                if (ci < 0 || pi < 0) {
                    JOptionPane.showMessageDialog(d, "Sélectionnez un client et un produit", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int q = Integer.parseInt(quantiteField.getText().trim());
                if (q <= 0) throw new NumberFormatException();
                Long clientId = clientsFinal.get(ci).getId();
                Long produitId = produitsFinal.get(pi).getId();
                venteService.effectuerVente(clientId, produitId, q);
                JOptionPane.showMessageDialog(d, "Vente enregistrée (SALLE_VENTE)", "Succès", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                loadVentesData();
                updateStatistics();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Quantité invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(cancel);
        actions.add(save);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ComponentFactory.getBackgroundColor());
        container.add(p, BorderLayout.CENTER);
        container.add(actions, BorderLayout.SOUTH);

        d.setContentPane(container);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void refreshData() {
        loadVentesData();
        updateStatistics();
        JOptionPane.showMessageDialog(this, "Données actualisées", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportSalesData() {
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != 1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner l'onglet Historique pour exporter les ventes",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter les ventes");
        chooser.setSelectedFile(new File("ventes.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (FileWriter writer = new FileWriter(file)) {
            // Entêtes (on ignore la colonne Actions)
            for (int col = 0; col < ventesTable.getColumnCount() - 1; col++) {
                writer.append(ventesTable.getColumnName(col));
                if (col < ventesTable.getColumnCount() - 2) {
                    writer.append(',');
                }
            }
            writer.append('\n');

            // Données
            for (int row = 0; row < ventesTable.getRowCount(); row++) {
                for (int col = 0; col < ventesTable.getColumnCount() - 1; col++) {
                    Object value = ventesTable.getValueAt(row, col);
                    writer.append(value == null ? "" : value.toString());
                    if (col < ventesTable.getColumnCount() - 2) {
                        writer.append(',');
                    }
                }
                writer.append('\n');
            }

            JOptionPane.showMessageDialog(this, "Export réalisé avec succès", "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export: " + ex.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void generateReport() {
        JOptionPane.showMessageDialog(this, "Génération de rapport en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // === Promotions ===
    private JPanel createPromotionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        JTabbedPane promosTabs = new JTabbedPane();
        promosTabs.addTab("🎯 Actives", createActivePromosTab());
        promosTabs.addTab("🎁 Bonus à distribuer", createBonusDistributionTab());
        promosTabs.addTab("📊 Rapport Bonus", createBonusReportTab());
        promosTabs.addTab("⚙ Administration", createPromoAdminTab());

        panel.add(promosTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPromoAdminTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        JPanel form = ComponentFactory.createCardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField nomField = ComponentFactory.createStyledTextField();
        JTextArea descArea = ComponentFactory.createStyledTextArea(3);
        ModernDatePicker dDebut = new ModernDatePicker(LocalDateTime.now().toLocalDate());
        ModernDatePicker dFin = new ModernDatePicker(LocalDateTime.now().plusWeeks(1).toLocalDate());
        JCheckBox activeBox = new JCheckBox("Active");
        activeBox.setSelected(true);

        form.add(ComponentFactory.createFieldPanel("Nom", nomField));
        form.add(ComponentFactory.createFieldPanel("Description", new JScrollPane(descArea)));
        form.add(ComponentFactory.createFieldPanel("Date début", dDebut));
        form.add(ComponentFactory.createFieldPanel("Date fin", dFin));
        JPanel activePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        activePanel.setBackground(ComponentFactory.getCardColor());
        activePanel.add(activeBox);
        form.add(ComponentFactory.createFieldPanel("Statut", activePanel));

        // Lignes produits
        JPanel linesPanel = new JPanel();
        linesPanel.setLayout(new BoxLayout(linesPanel, BoxLayout.Y_AXIS));
        linesPanel.setBackground(ComponentFactory.getCardColor());

        JButton addLine = ButtonFactory.createActionButton(FontAwesomeSolid.PLUS, "Ajouter produit", ComponentFactory.getPrimaryColor(), e -> {
            linesPanel.add(createPromoLine());
            linesPanel.revalidate();
            linesPanel.repaint();
        });
        form.add(ComponentFactory.createSectionTitle("Produits en promotion"));
        form.add(linesPanel);
        JPanel lineActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lineActions.setBackground(ComponentFactory.getCardColor());
        lineActions.add(addLine);
        form.add(lineActions);

        // Boutons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(ComponentFactory.getBackgroundColor());
        JButton save = ButtonFactory.createActionButton(FontAwesomeSolid.SAVE, "Enregistrer", ComponentFactory.getSuccessColor(), e -> {
            try {
                var dto = com.longrich.smartgestion.dto.VentePromotionnelleDTO.builder()
                        .nom(nomField.getText().trim())
                        .description(descArea.getText().trim())
                        .dateDebut(dDebut.getSelectedDate())
                        .dateFin(dFin.getSelectedDate())
                        .active(activeBox.isSelected())
                        .build();

                java.util.List<com.longrich.smartgestion.dto.ProduitPromotionnelDTO> lignes = new java.util.ArrayList<>();
                for (java.awt.Component c : linesPanel.getComponents()) {
                    if (c instanceof JPanel pLine) {
                        @SuppressWarnings("unchecked") JComboBox<String> prodCombo = (JComboBox<String>) pLine.getClientProperty("prod");
                        @SuppressWarnings("unchecked") JComboBox<String> bonusCombo = (JComboBox<String>) pLine.getClientProperty("bonus");
                        JTextField qMinField = (JTextField) pLine.getClientProperty("qmin");
                        JTextField qBonusField = (JTextField) pLine.getClientProperty("qbonus");

                        int pi = prodCombo.getSelectedIndex();
                        int bi = bonusCombo.getSelectedIndex();
                        if (pi < 0 || bi < 0) continue;
                        var produits = produitService.getActiveProduits();
                        Long produitId = produits.get(pi).getId();
                        Long bonusId = produits.get(bi).getId();
                        int qmin = Integer.parseInt(qMinField.getText().trim());
                        int qbonus = Integer.parseInt(qBonusField.getText().trim());

                        lignes.add(com.longrich.smartgestion.dto.ProduitPromotionnelDTO.builder()
                                .produitId(produitId)
                                .produitBonusId(bonusId)
                                .quantiteMinimum(qmin)
                                .quantiteBonus(qbonus)
                                .build());
                    }
                }

                if (lignes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ajoutez au moins un produit en promotion", "Information", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                ventePromotionnelleService.createPromotion(dto, lignes);
                JOptionPane.showMessageDialog(this, "Promotion enregistrée", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(save);

        panel.add(form, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPromoLine() {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        line.setBackground(ComponentFactory.getCardColor());
        var produits = produitService.getActiveProduits();
        JComboBox<String> prodCombo = ComponentFactory.createStyledComboBox();
        JComboBox<String> bonusCombo = ComponentFactory.createStyledComboBox();
        for (var p : produits) { prodCombo.addItem(p.getLibelle() + " (ID:" + p.getId() + ")"); bonusCombo.addItem(p.getLibelle() + " (ID:" + p.getId() + ")"); }
        JTextField qMin = ComponentFactory.createStyledTextField(); qMin.setPreferredSize(new Dimension(80, 38)); qMin.setText("1");
        JTextField qBonus = ComponentFactory.createStyledTextField(); qBonus.setPreferredSize(new Dimension(80, 38)); qBonus.setText("1");
        line.putClientProperty("prod", prodCombo);
        line.putClientProperty("bonus", bonusCombo);
        line.putClientProperty("qmin", qMin);
        line.putClientProperty("qbonus", qBonus);
        line.add(new JLabel("Produit:")); line.add(prodCombo);
        line.add(new JLabel("Bonus:")); line.add(bonusCombo);
        line.add(new JLabel("Seuil:")); line.add(qMin);
        line.add(new JLabel("Bonus Qté:")); line.add(qBonus);
        return line;
    }

    private JPanel createActivePromosTab() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());
        JLabel title = ComponentFactory.createSectionTitle("Promotions actives");
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Promotion", "Période", "Produit", "Bonus", "Seuil", "Qté Bonus"};
        promosModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        promosTable = new JTable(promosModel);
        styleTable(promosTable);
        panel.add(new JScrollPane(promosTable), BorderLayout.CENTER);

        loadActivePromotions();
        return panel;
    }

    private JPanel createBonusDistributionTab() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());
        JLabel title = ComponentFactory.createSectionTitle("Bonus à distribuer");
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID Bonus", "Client", "Produit Bonus", "Quantité", "Attribué le", "Action"};
        bonusNonDistribuesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        bonusNonDistribuesTable = new JTable(bonusNonDistribuesModel);
        styleTable(bonusNonDistribuesTable);
        panel.add(new JScrollPane(bonusNonDistribuesTable), BorderLayout.CENTER);

        JButton refresh = ButtonFactory.createActionButton(FontAwesomeSolid.SYNC_ALT, "Actualiser",
                ComponentFactory.getSecondaryColor(), e -> loadNonDistributedBonus());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(ComponentFactory.getCardColor());
        actions.add(refresh);
        panel.add(actions, BorderLayout.SOUTH);

        loadNonDistributedBonus();
        return panel;
    }

    private JPanel createBonusReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        JPanel filters = ComponentFactory.createCardPanel();
        filters.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filters.add(ComponentFactory.createLabel("Produit:"));
        bonusProduitCombo = ComponentFactory.createStyledComboBox();
        populateBonusProductCombo();
        filters.add(bonusProduitCombo);
        filters.add(ComponentFactory.createLabel("Du:"));
        bonusDateDebutPicker = new ModernDatePicker(LocalDateTime.now().minusMonths(1).toLocalDate());
        filters.add(bonusDateDebutPicker);
        filters.add(ComponentFactory.createLabel("Au:"));
        bonusDateFinPicker = new ModernDatePicker(LocalDateTime.now().toLocalDate());
        filters.add(bonusDateFinPicker);
        JButton apply = ButtonFactory.createActionButton(FontAwesomeSolid.FILTER, "Filtrer",
                ComponentFactory.getPrimaryColor(), e -> loadBonusReport());
        filters.add(apply);

        panel.add(filters, BorderLayout.NORTH);

        String[] cols = {"Date", "Client", "Produit Bonus", "Quantité", "Observation"};
        bonusRapportModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bonusRapportTable = new JTable(bonusRapportModel);
        styleTable(bonusRapportTable);
        panel.add(new JScrollPane(bonusRapportTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(ComponentFactory.getBackgroundColor());
        JButton export = ButtonFactory.createActionButton(FontAwesomeSolid.FILE_EXPORT, "Exporter CSV",
                ComponentFactory.getSuccessColor(), e -> exportBonusReport());
        bottom.add(export);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void loadActivePromotions() {
        promosModel.setRowCount(0);
        // Minimal: pull from repository via service if available later; here placeholder to keep UI responsive
        try {
            var promos = venteService.getPromotionsActives();
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (var pp : promos) {
                String periode = pp.getVentePromotionnelle().getDateDebut().format(f) + " → " + pp.getVentePromotionnelle().getDateFin().format(f);
                promosModel.addRow(new Object[]{
                        pp.getVentePromotionnelle().getNom(),
                        periode,
                        pp.getProduit().getLibelle(),
                        pp.getProduitBonus().getLibelle(),
                        pp.getQuantiteMinimum(),
                        pp.getQuantiteBonus()
                });
            }
        } catch (Exception ignored) {}
    }

    private void loadNonDistributedBonus() {
        bonusNonDistribuesModel.setRowCount(0);
        try {
            var list = venteService.getBonusNonDistribues();
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var b : list) {
                bonusNonDistribuesModel.addRow(new Object[]{
                        b.getId(),
                        b.getClient().getNomComplet(),
                        b.getProduitPromotionnel().getProduitBonus().getLibelle(),
                        b.getQuantiteBonus(),
                        b.getDateAttribution().format(f),
                        "Distribuer"
                });
            }

            // Simple action-on-click to distribuer
            bonusNonDistribuesTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    int row = bonusNonDistribuesTable.rowAtPoint(e.getPoint());
                    int col = bonusNonDistribuesTable.columnAtPoint(e.getPoint());
                    if (row >= 0 && col == 5) {
                        Long id = Long.valueOf(bonusNonDistribuesModel.getValueAt(row, 0).toString());
                        try {
                            venteService.distribuerBonus(id);
                            JOptionPane.showMessageDialog(VentePanel.this, "Bonus distribué", "Succès", JOptionPane.INFORMATION_MESSAGE);
                            loadNonDistributedBonus();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(VentePanel.this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void populateBonusProductCombo() {
        try {
            var produits = produitService.getActiveProduits();
            bonusProduitCombo.removeAllItems();
            for (var p : produits) bonusProduitCombo.addItem(p.getLibelle() + " (ID:" + p.getId() + ")");
            if (bonusProduitCombo.getItemCount() > 0) bonusProduitCombo.setSelectedIndex(0);
        } catch (Exception ignored) {}
    }

    private void loadBonusReport() {
        bonusRapportModel.setRowCount(0);
        try {
            int idx = bonusProduitCombo.getSelectedIndex();
            if (idx < 0) return;
            var produits = produitService.getActiveProduits();
            Long produitId = produits.get(idx).getId();
            java.time.LocalDate sd = bonusDateDebutPicker.getSelectedDate() != null
                ? bonusDateDebutPicker.getSelectedDate() : java.time.LocalDate.now().minusMonths(1);
            java.time.LocalDate ed = bonusDateFinPicker.getSelectedDate() != null
                ? bonusDateFinPicker.getSelectedDate() : java.time.LocalDate.now();
            var start = sd.atStartOfDay();
            var end = ed.atTime(23,59,59);
            var list = bonusAttribueService.getBonusSortiesByProduitAndPeriod(produitId, start, end);
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var b : list) {
                bonusRapportModel.addRow(new Object[]{
                        b.getDateAttribution().format(f),
                        b.getClient().getNomComplet(),
                        b.getProduitPromotionnel().getProduitBonus().getLibelle(),
                        b.getQuantiteBonus(),
                        b.getObservation()
                });
            }
        } catch (Exception ignored) {}
    }

    private void exportBonusReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("bonus_report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try (FileWriter fw = new FileWriter(file)) {
            for (int c=0;c<bonusRapportTable.getColumnCount();c++) {
                fw.append(bonusRapportTable.getColumnName(c));
                if (c<bonusRapportTable.getColumnCount()-1) fw.append(',');
            }
            fw.append('\n');
            for (int r=0;r<bonusRapportTable.getRowCount();r++) {
                for (int c=0;c<bonusRapportTable.getColumnCount();c++) {
                    Object v = bonusRapportTable.getValueAt(r,c);
                    fw.append(v==null?"":v.toString());
                    if (c<bonusRapportTable.getColumnCount()-1) fw.append(',');
                }
                fw.append('\n');
            }
            JOptionPane.showMessageDialog(this, "Export réussi", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Classe interne pour les données de vente
    private static class VenteRecord {
        String numeroVente, numeroFacture, clientNom, typeClient, statut;
        LocalDateTime dateVente;
        BigDecimal montant, pv;

        VenteRecord(String numeroVente, String numeroFacture, String clientNom, String typeClient,
                LocalDateTime dateVente, BigDecimal montant, BigDecimal pv, String statut) {
            this.numeroVente = numeroVente;
            this.numeroFacture = numeroFacture;
            this.clientNom = clientNom;
            this.typeClient = typeClient;
            this.dateVente = dateVente;
            this.montant = montant;
            this.pv = pv;
            this.statut = statut;
        }
    }
}
