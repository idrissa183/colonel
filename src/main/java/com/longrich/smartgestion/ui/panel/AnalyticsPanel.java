package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.ui.components.ButtonFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class AnalyticsPanel extends JPanel {

    // Couleurs modernes
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);

    // Composants UI
    private JComboBox<String> periodCombo;
    private JTable analyticsTable;
    private DefaultTableModel tableModel;
    private final Map<String, JLabel> kpiLabels = new HashMap<>();

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        createHeaderPanel();
        createMainContent();
        loadAnalyticsData();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("📊 Analytics & Tableaux de Bord");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Contrôles
        JPanel controlsPanel = createControlsPanel();
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);

        // Sélecteur de période
        JLabel periodLabel = new JLabel("Période:");
        periodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        periodLabel.setForeground(TEXT_SECONDARY);
        
        periodCombo = new JComboBox<>(new String[] {
            "Aujourd'hui", "Cette semaine", "Ce mois", "Ce trimestre", "Cette année", "Personnalisé"
        });
        styleComboBox(periodCombo);
        periodCombo.setSelectedItem("Ce mois");
        periodCombo.addActionListener(e -> loadAnalyticsData());

        JButton exportButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportAnalytics());
        JButton refreshButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR, e -> refreshData());

        panel.add(periodLabel);
        panel.add(periodCombo);
        panel.add(exportButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.4);
        splitPane.setOneTouchExpandable(true);

        // Panneau supérieur - KPIs
        JPanel kpiPanel = createKPIPanel();
        splitPane.setTopComponent(kpiPanel);

        // Panneau inférieur - Tableaux et graphiques
        JPanel chartsPanel = createChartsPanel();
        splitPane.setBottomComponent(chartsPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createKPIPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Titre de section
        JLabel sectionTitle = new JLabel("📈 Indicateurs Clés de Performance (KPI)");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(sectionTitle, BorderLayout.NORTH);

        // Grille de KPIs
        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 15, 15));
        kpiGrid.setBackground(BACKGROUND_COLOR);

        // KPIs principaux
        kpiGrid.add(createKPICard("💰 Chiffre d'Affaires", "125 450 €", "+12.5%", SUCCESS_COLOR, "ca"));
        kpiGrid.add(createKPICard("📦 Ventes", "1,234", "+8.3%", INFO_COLOR, "ventes"));
        kpiGrid.add(createKPICard("👥 Nouveaux Clients", "89", "+15.2%", PRIMARY_COLOR, "clients"));
        kpiGrid.add(createKPICard("📊 Marge Moyenne", "34.8%", "+2.1%", SUCCESS_COLOR, "marge"));
        
        kpiGrid.add(createKPICard("🏆 PV Générés", "45,678", "+18.9%", WARNING_COLOR, "pv"));
        kpiGrid.add(createKPICard("💸 Commissions", "12,340 €", "+22.4%", SUCCESS_COLOR, "commissions"));
        kpiGrid.add(createKPICard("📈 Conversion", "3.4%", "+0.8%", INFO_COLOR, "conversion"));
        kpiGrid.add(createKPICard("⚠️ Stock Critique", "12", "-3", DANGER_COLOR, "stock"));

        mainPanel.add(kpiGrid, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createKPICard(String title, String value, String change, Color accentColor, String key) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Valeur principale
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Évolution
        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeLabel.setForeground(change.startsWith("+") ? SUCCESS_COLOR : 
                                 change.startsWith("-") ? DANGER_COLOR : TEXT_SECONDARY);
        changeLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(changeLabel);

        // Stocker la référence pour les mises à jour
        kpiLabels.put(key + "_value", valueLabel);
        kpiLabels.put(key + "_change", changeLabel);

        return card;
    }

    private JPanel createChartsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Split horizontal pour les graphiques et données
        JSplitPane chartsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        chartsSplit.setBackground(BACKGROUND_COLOR);
        chartsSplit.setBorder(null);
        chartsSplit.setDividerSize(8);
        chartsSplit.setResizeWeight(0.6);
        chartsSplit.setOneTouchExpandable(true);

        // Panneau gauche - Graphiques (simulés)
        JPanel chartsContainer = createChartsContainer();
        chartsSplit.setLeftComponent(chartsContainer);

        // Panneau droit - Données tabulaires
        JPanel dataContainer = createDataContainer();
        chartsSplit.setRightComponent(dataContainer);

        mainPanel.add(chartsSplit, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createChartsContainer() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(BACKGROUND_COLOR);

        // Graphique des ventes
        container.add(createChartPlaceholder("📈 Évolution des Ventes", "Graphique linéaire des ventes par période"));
        container.add(Box.createVerticalStrut(15));
        
        // Graphique des produits top
        container.add(createChartPlaceholder("🏆 Top Produits", "Graphique en barres des produits les plus vendus"));

        return container;
    }

    private JPanel createChartPlaceholder(String title, String description) {
        JPanel chart = new JPanel(new BorderLayout());
        chart.setBackground(CARD_COLOR);
        chart.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        chart.setPreferredSize(new Dimension(0, 200));

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        chart.add(titleLabel, BorderLayout.NORTH);

        // Zone de graphique simulée
        JPanel chartArea = new JPanel();
        chartArea.setBackground(new Color(248, 250, 252));
        chartArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JLabel placeholder = new JLabel("<html><center>" + description + "<br><br>" +
            "<span style='color: #64748b; font-size: 12px;'>Graphique interactif disponible<br>dans la version complète</span></center></html>");
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholder.setForeground(TEXT_SECONDARY);
        chartArea.add(placeholder);

        chart.add(chartArea, BorderLayout.CENTER);
        return chart;
    }

    private JPanel createDataContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CARD_COLOR);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        JLabel tableTitle = new JLabel("📊 Données Détaillées");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);
        headerPanel.add(tableTitle, BorderLayout.WEST);

        container.add(headerPanel, BorderLayout.NORTH);

        // Table des données
        createAnalyticsTable();
        JScrollPane scrollPane = new JScrollPane(analyticsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void createAnalyticsTable() {
        String[] columns = {
            "Période", "Ventes", "CA (€)", "Clients", "PV", "Commissions (€)", "Marge (%)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        analyticsTable = new JTable(tableModel);
        analyticsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        analyticsTable.setRowHeight(40);
        analyticsTable.setShowVerticalLines(false);
        analyticsTable.setGridColor(new Color(243, 244, 246));
        analyticsTable.setSelectionBackground(new Color(239, 246, 255));
        analyticsTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = analyticsTable.getTableHeader();
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 45));

        // Renderer personnalisé
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }

                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));

                // Alignement des colonnes numériques
                if (column > 0) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                setForeground(TEXT_PRIMARY);
                return c;
            }
        };

        for (int i = 0; i < analyticsTable.getColumnCount(); i++) {
            analyticsTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        comboBox.setPreferredSize(new Dimension(0, 38));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadAnalyticsData() {
        tableModel.setRowCount(0);

        // Données d'exemple basées sur la période sélectionnée
        String period = (String) periodCombo.getSelectedItem();
        String[][] sampleData;

        if ("Aujourd'hui".equals(period)) {
            sampleData = new String[][] {
                {"Aujourd'hui", "15", "3,250", "8", "1,234", "485", "32.1"},
            };
        } else if ("Cette semaine".equals(period)) {
            sampleData = new String[][] {
                {"Lundi", "18", "4,120", "12", "1,567", "623", "33.8"},
                {"Mardi", "22", "5,340", "15", "2,123", "845", "31.2"},
                {"Mercredi", "19", "4,580", "11", "1,834", "734", "35.1"},
                {"Jeudi", "25", "6,120", "18", "2,456", "982", "33.4"},
                {"Vendredi", "21", "5,210", "14", "2,089", "836", "32.8"},
                {"Samedi", "28", "7,340", "22", "2,934", "1,174", "34.2"},
                {"Dimanche", "15", "3,680", "9", "1,478", "591", "30.9"},
            };
        } else {
            // Données mensuelles par défaut
            sampleData = new String[][] {
                {"Semaine 1", "145", "32,450", "89", "12,567", "5,027", "33.2"},
                {"Semaine 2", "167", "38,120", "102", "14,234", "5,694", "34.1"},
                {"Semaine 3", "134", "29,890", "78", "11,456", "4,582", "32.8"},
                {"Semaine 4", "189", "42,340", "125", "16,789", "6,716", "35.6"},
            };
        }

        for (String[] row : sampleData) {
            tableModel.addRow(row);
        }

        // Mise à jour des KPIs (simulation)
        updateKPIs(period);
    }

    private void updateKPIs(String period) {
        // Simulation de mise à jour des KPIs selon la période
        Map<String, String[]> periodData = new HashMap<>();
        
        periodData.put("Aujourd'hui", new String[] {
            "3,250 €", "+5.2%", "15", "+12%", "8", "+25%", "32.1%", "+1.1%",
            "1,234", "+8%", "485 €", "+15%", "2.1%", "+0.3%", "2", "-1"
        });
        
        periodData.put("Cette semaine", new String[] {
            "36,390 €", "+8.7%", "148", "+18%", "109", "+22%", "33.1%", "+1.8%",
            "15,681", "+12%", "6,272 €", "+19%", "2.8%", "+0.5%", "5", "-2"
        });

        String[] data = periodData.getOrDefault(period, new String[] {
            "125,450 €", "+12.5%", "1,234", "+8.3%", "89", "+15.2%", "34.8%", "+2.1%",
            "45,678", "+18.9%", "12,340 €", "+22.4%", "3.4%", "+0.8%", "12", "-3"
        });

        // Mise à jour des labels KPI
        updateKPILabel("ca", data[0], data[1]);
        updateKPILabel("ventes", data[2], data[3]);
        updateKPILabel("clients", data[4], data[5]);
        updateKPILabel("marge", data[6], data[7]);
        updateKPILabel("pv", data[8], data[9]);
        updateKPILabel("commissions", data[10], data[11]);
        updateKPILabel("conversion", data[12], data[13]);
        updateKPILabel("stock", data[14], data[15]);
    }

    private void updateKPILabel(String key, String value, String change) {
        JLabel valueLabel = kpiLabels.get(key + "_value");
        JLabel changeLabel = kpiLabels.get(key + "_change");
        
        if (valueLabel != null) valueLabel.setText(value);
        if (changeLabel != null) {
            changeLabel.setText(change);
            changeLabel.setForeground(change.startsWith("+") ? SUCCESS_COLOR : 
                                     change.startsWith("-") ? DANGER_COLOR : TEXT_SECONDARY);
        }
    }

    private void exportAnalytics() {
        // TODO: Implémenter l'export des analytics
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Fonctionnalité d'export en cours de développement", 
            "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadAnalyticsData();
        javax.swing.JOptionPane.showMessageDialog(this, 
            "✓ Données analytics actualisées", 
            "Succès", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}