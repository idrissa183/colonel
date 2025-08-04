package com.longrich.smartgestion.ui.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;
import com.longrich.smartgestion.ui.components.ModernDatePicker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class PvPanel extends JPanel {

    private final ClientService clientService;

    // Composants UI
    private JTextField searchField;
    private JComboBox<String> periodeCombo;
    private JComboBox<String> typeCombo;
    private ModernDatePicker dateDebutPicker;
    private ModernDatePicker dateFinPicker;
    private JTable pvTable;
    private DefaultTableModel pvTableModel;
    private JTable commissionsTable;
    private DefaultTableModel commissionsTableModel;
    private JLabel totalPvLabel;
    private JLabel totalCommissionsLabel;
    private JLabel statsLabel;

    // Données simulées
    private List<PvRecord> pvRecords;
    private List<CommissionRecord> commissionRecords;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ComponentFactory.getBackgroundColor());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeData();
        createHeaderPanel();
        createMainContent();
        loadPvData();
        updateStatistics();
    }

    private void initializeData() {
        pvRecords = new ArrayList<>();
        commissionRecords = new ArrayList<>();
        
        // Données simulées pour démonstration
        pvRecords.add(new PvRecord("CLT001", "Martin Dupont", "Partenaire", 
                LocalDate.now().minusDays(5), new BigDecimal("150"), new BigDecimal("45000")));
        pvRecords.add(new PvRecord("CLT002", "Sophie Laurent", "Semi-grossiste", 
                LocalDate.now().minusDays(3), new BigDecimal("89"), new BigDecimal("26700")));
        pvRecords.add(new PvRecord("CLT003", "Ahmed Ben Ali", "Grossiste", 
                LocalDate.now().minusDays(1), new BigDecimal("245"), new BigDecimal("73500")));
        
        commissionRecords.add(new CommissionRecord("Martin Dupont", "Commission Directe", 
                LocalDate.now().minusDays(5), new BigDecimal("150"), new BigDecimal("15000")));
        commissionRecords.add(new CommissionRecord("Sophie Laurent", "Commission Niveau 2", 
                LocalDate.now().minusDays(3), new BigDecimal("89"), new BigDecimal("4450")));
        commissionRecords.add(new CommissionRecord("Ahmed Ben Ali", "Bonus Leadership", 
                LocalDate.now().minusDays(1), new BigDecimal("245"), new BigDecimal("24500")));
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ComponentFactory.getBackgroundColor());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(ComponentFactory.getBackgroundColor());

        JLabel titleLabel = new JLabel("Gestion des PV & Commissions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ComponentFactory.getTextPrimaryColor());
        titlePanel.add(titleLabel);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(ComponentFactory.getTextSecondaryColor());
        statsLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 0, 0));
        titlePanel.add(statsLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Actions rapides
        JPanel actionsPanel = createQuickActionsPanel();
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(ComponentFactory.getBackgroundColor());

        JButton calculateButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.CALCULATOR, "Calculer", ComponentFactory.getPrimaryColor(), 
                e -> calculateCommissions());
        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter", ComponentFactory.getSuccessColor(), 
                e -> exportPvData());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", ComponentFactory.getSecondaryColor(), 
                e -> refreshData());

        panel.add(calculateButton);
        panel.add(exportButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(ComponentFactory.getBackgroundColor());

        // Onglet Historique PV
        JPanel pvPanel = createPvHistoryPanel();
        tabbedPane.addTab("📊 Historique des PV", pvPanel);

        // Onglet Commissions
        JPanel commissionsPanel = createCommissionsPanel();
        tabbedPane.addTab("💰 Commissions", commissionsPanel);

        // Onglet Simulation
        JPanel simulationPanel = createSimulationPanel();
        tabbedPane.addTab("🔮 Simulation", simulationPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPvHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // Filtres
        JPanel filtersPanel = createFiltersPanel();
        panel.add(filtersPanel, BorderLayout.NORTH);

        // Table des PV
        JPanel tablePanel = createPvTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);

        // Résumé en bas
        JPanel summaryPanel = createPvSummaryPanel();
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFiltersPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Recherche
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(ComponentFactory.createLabel("Recherche:"), gbc);
        
        searchField = ComponentFactory.createStyledTextField("Nom du client...");
        searchField.setPreferredSize(new Dimension(200, 38));
        searchField.addActionListener(e -> filterPvData());
        gbc.gridx = 1;
        panel.add(ComponentFactory.createSearchField(searchField), gbc);

        // Période
        gbc.gridx = 2;
        panel.add(ComponentFactory.createLabel("Période:"), gbc);
        
        String[] periodes = {"Cette semaine", "Ce mois", "Ce trimestre", "Cette année", "Personnalisée"};
        periodeCombo = ComponentFactory.createStyledComboBox(periodes);
        periodeCombo.addActionListener(this::onPeriodeChange);
        gbc.gridx = 3;
        panel.add(periodeCombo, gbc);

        // Type de client
        gbc.gridx = 4;
        panel.add(ComponentFactory.createLabel("Type:"), gbc);
        
        String[] types = {"Tous", "Partenaire", "Grossiste", "Semi-grossiste", "Individuel"};
        typeCombo = ComponentFactory.createStyledComboBox(types);
        typeCombo.addActionListener(e -> filterPvData());
        gbc.gridx = 5;
        panel.add(typeCombo, gbc);

        // Dates personnalisées (initialement cachées)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(ComponentFactory.getCardColor());
        datePanel.add(ComponentFactory.createLabel("Du:"));
        dateDebutPicker = new ModernDatePicker(LocalDate.now().minusMonths(1));
        datePanel.add(dateDebutPicker);
        datePanel.add(Box.createHorizontalStrut(10));
        datePanel.add(ComponentFactory.createLabel("Au:"));
        dateFinPicker = new ModernDatePicker(LocalDate.now());
        datePanel.add(dateFinPicker);
        datePanel.setVisible(false);
        panel.add(datePanel, gbc);

        return panel;
    }

    private JPanel createPvTablePanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        // En-tête
        JLabel tableTitle = ComponentFactory.createSectionTitle("Historique des Points de Vente (PV)");
        panel.add(tableTitle, BorderLayout.NORTH);

        // Table
        String[] columns = {"Client", "Code", "Type", "Date", "PV Gagné", "Montant (FCFA)", "Statut"};
        pvTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pvTable = new JTable(pvTableModel);
        styleTable(pvTable);

        JScrollPane scrollPane = new JScrollPane(pvTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPvSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Total PV
        JPanel pvSummaryCard = createSummaryCard("Total PV", "0", ComponentFactory.getPrimaryColor(), FontAwesomeSolid.STAR);
        totalPvLabel = (JLabel) ((JPanel) pvSummaryCard.getComponent(1)).getComponent(0);
        panel.add(pvSummaryCard);

        // Total Montant
        JPanel montantSummaryCard = createSummaryCard("Montant Total", "0 FCFA", ComponentFactory.getSuccessColor(), FontAwesomeSolid.MONEY_BILL_WAVE);
        panel.add(montantSummaryCard);

        return panel;
    }

    private JPanel createCommissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // Table des commissions
        JPanel tablePanel = createCommissionsTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);

        // Résumé des commissions
        JPanel summaryPanel = createCommissionsSummaryPanel();
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCommissionsTablePanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        JLabel tableTitle = ComponentFactory.createSectionTitle("Commissions et Bonus");
        panel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Bénéficiaire", "Type Commission", "Date", "PV Base", "Montant (FCFA)", "Statut"};
        commissionsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        commissionsTable = new JTable(commissionsTableModel);
        styleTable(commissionsTable);

        JScrollPane scrollPane = new JScrollPane(commissionsTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCommissionsSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Total commissions
        JPanel totalCard = createSummaryCard("Total Commissions", "0 FCFA", ComponentFactory.getWarningColor(), FontAwesomeSolid.COINS);
        totalCommissionsLabel = (JLabel) ((JPanel) totalCard.getComponent(1)).getComponent(0);
        panel.add(totalCard);

        // Commissions ce mois
        JPanel moisCard = createSummaryCard("Ce Mois", "0 FCFA", ComponentFactory.getPrimaryColor(), FontAwesomeSolid.CALENDAR_ALT);
        panel.add(moisCard);

        // Bonus leadership
        JPanel bonusCard = createSummaryCard("Bonus Leadership", "0 FCFA", ComponentFactory.getSuccessColor(), FontAwesomeSolid.TROPHY);
        panel.add(bonusCard);

        return panel;
    }

    private JPanel createSimulationPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        JLabel title = ComponentFactory.createSectionTitle("Simulateur de Commissions");
        panel.add(title, BorderLayout.NORTH);

        // Formulaire de simulation
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ComponentFactory.getCardColor());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Montant de vente
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(ComponentFactory.createLabel("Montant de vente (FCFA):"), gbc);
        JTextField montantField = ComponentFactory.createStyledTextField();
        gbc.gridx = 1;
        formPanel.add(montantField, gbc);

        // Type de client
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(ComponentFactory.createLabel("Type de client:"), gbc);
        String[] typesClient = {"Partenaire", "Grossiste", "Semi-grossiste", "Individuel"};
        JComboBox<String> typeClientCombo = ComponentFactory.createStyledComboBox(typesClient);
        gbc.gridx = 1;
        formPanel.add(typeClientCombo, gbc);

        // Bouton simuler
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton simulerButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.CALCULATOR, "Simuler", ComponentFactory.getPrimaryColor(), 
                e -> simulateCommission(montantField, typeClientCombo));
        formPanel.add(simulerButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Résultats
        JPanel resultPanel = new JPanel();
        resultPanel.setBackground(ComponentFactory.getCardColor());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Résultats de la simulation"));
        panel.add(resultPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryCard(String title, String value, Color color, FontAwesomeSolid icon) {
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
            filterPvData();
        }
    }

    private void loadPvData() {
        pvTableModel.setRowCount(0);
        for (PvRecord record : pvRecords) {
            Object[] row = {
                record.clientNom,
                record.clientCode,
                record.typeClient,
                record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                record.pvGagne,
                String.format("%,.0f FCFA", record.montant),
                "Validé"
            };
            pvTableModel.addRow(row);
        }
        
        // Charger aussi les commissions
        commissionsTableModel.setRowCount(0);
        for (CommissionRecord record : commissionRecords) {
            Object[] row = {
                record.beneficiaire,
                record.typeCommission,
                record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                record.pvBase,
                String.format("%,.0f FCFA", record.montant),
                "Payée"
            };
            commissionsTableModel.addRow(row);
        }
    }

    private void filterPvData() {
        // TODO: Implémenter le filtrage réel
        loadPvData();
    }

    private void updateStatistics() {
        BigDecimal totalPv = pvRecords.stream()
                .map(r -> r.pvGagne)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCommissions = commissionRecords.stream()
                .map(r -> r.montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalPvLabel.setText(totalPv.toString());
        totalCommissionsLabel.setText(String.format("%,.0f FCFA", totalCommissions));
        
        statsLabel.setText(String.format("Total PV: %s • Commissions: %,.0f FCFA • Partenaires actifs: %d",
                totalPv, totalCommissions, pvRecords.size()));
    }

    private void calculateCommissions() {
        JOptionPane.showMessageDialog(this, "Calcul des commissions effectué avec succès!", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        updateStatistics();
    }

    private void exportPvData() {
        JOptionPane.showMessageDialog(this, "Export des données PV en cours de développement", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadPvData();
        updateStatistics();
        JOptionPane.showMessageDialog(this, "Données actualisées", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void simulateCommission(JTextField montantField, JComboBox<String> typeClientCombo) {
        try {
            BigDecimal montant = new BigDecimal(montantField.getText().trim());
            String typeClient = (String) typeClientCombo.getSelectedItem();
            
            // Simulation simple basée sur le type de client
            BigDecimal commission = switch (typeClient) {
                case "Partenaire" -> montant.multiply(new BigDecimal("0.10"));
                case "Grossiste" -> montant.multiply(new BigDecimal("0.15"));
                case "Semi-grossiste" -> montant.multiply(new BigDecimal("0.08"));
                default -> montant.multiply(new BigDecimal("0.05"));
            };
            
            JOptionPane.showMessageDialog(this, 
                    String.format("Commission simulée pour %s:\n\nMontant: %,.0f FCFA\nCommission: %,.0f FCFA (%.0f%%)", 
                            typeClient, montant, commission, commission.divide(montant, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))),
                    "Résultat de la simulation", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un montant valide", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Classes internes pour les données
    private static class PvRecord {
        String clientCode, clientNom, typeClient;
        LocalDate date;
        BigDecimal pvGagne, montant;
        
        PvRecord(String clientCode, String clientNom, String typeClient, LocalDate date, BigDecimal pvGagne, BigDecimal montant) {
            this.clientCode = clientCode;
            this.clientNom = clientNom;
            this.typeClient = typeClient;
            this.date = date;
            this.pvGagne = pvGagne;
            this.montant = montant;
        }
    }

    private static class CommissionRecord {
        String beneficiaire, typeCommission;
        LocalDate date;
        BigDecimal pvBase, montant;
        
        CommissionRecord(String beneficiaire, String typeCommission, LocalDate date, BigDecimal pvBase, BigDecimal montant) {
            this.beneficiaire = beneficiaire;
            this.typeCommission = typeCommission;
            this.date = date;
            this.pvBase = pvBase;
            this.montant = montant;
        }
    }
}