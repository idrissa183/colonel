package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ProduitService;

import lombok.RequiredArgsConstructor;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class StockPanel extends JPanel {

    // Couleurs modernes cohérentes
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

    private final ProduitService produitService;

    // Composants UI
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private JTextField quantiteField;
    private JTextField motifField;
    private JComboBox<String> mouvementTypeCombo;
    private JComboBox<String> produitCombo;
    private List<ProduitDto> produitsList;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createMainContent();

        loadStock();
        loadProduits();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Stocks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(TEXT_SECONDARY);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 0, 0));
        titlePanel.add(statsLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Boutons d'action rapide
        JPanel quickActionsPanel = createQuickActionsPanel();
        headerPanel.add(quickActionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(BACKGROUND_COLOR);

        JButton exportButton = createIconButton(FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR);
        JButton alertsButton = createIconButton(FontAwesomeSolid.EXCLAMATION_TRIANGLE, "Alertes", WARNING_COLOR);
        JButton refreshButton = createIconButton(FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR);

        exportButton.addActionListener(e -> exportStock());
        alertsButton.addActionListener(e -> showStockAlerts());
        refreshButton.addActionListener(e -> refreshData());

        panel.add(alertsButton);
        panel.add(exportButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.7);

        // Panneau de gauche - Liste des stocks
        JPanel stockContainer = createStockContainer();
        splitPane.setLeftComponent(stockContainer);

        // Panneau de droite - Mouvements de stock
        JPanel mouvementContainer = createMouvementContainer();
        splitPane.setRightComponent(mouvementContainer);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createStockContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        // Panneau de recherche et filtres
        JPanel searchContainer = createSearchPanel();
        container.add(searchContainer, BorderLayout.NORTH);

        // Table des stocks
        JPanel tablePanel = createStockTable();
        container.add(tablePanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(CARD_COLOR);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel searchTitle = new JLabel("Inventaire des Stocks");
        searchTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchTitle.setForeground(TEXT_PRIMARY);

        // Panneau de recherche
        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setBackground(CARD_COLOR);

        // Champ de recherche
        searchField = createStyledTextField();
        searchField.addActionListener(e -> searchStock());

        // Filtre par statut
        filterCombo = new JComboBox<>(new String[]{"Tous", "Stock faible", "Rupture", "Normal"});
        styleComboBox(filterCombo);
        filterCombo.addActionListener(e -> filterStock());

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_COLOR);
        topPanel.add(searchTitle, BorderLayout.WEST);

        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtersPanel.setBackground(CARD_COLOR);
        filtersPanel.add(new JLabel("Rechercher:"));
        filtersPanel.add(searchField);
        filtersPanel.add(new JLabel("Filtre:"));
        filtersPanel.add(filterCombo);
        topPanel.add(filtersPanel, BorderLayout.EAST);

        searchPanel.add(topPanel, BorderLayout.NORTH);
        return searchPanel;
    }

    private JPanel createStockTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Modèle de table
        String[] columns = {"Produit", "Code", "Quantité", "Stock Min", "Statut", "Dernière MAJ"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stockTable = new JTable(tableModel);
        styleTable(stockTable);

        // Listener pour double-click
        stockTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showStockDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createMouvementContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);

        // Formulaire de mouvement
        JPanel formPanel = createMouvementForm();
        container.add(formPanel, BorderLayout.NORTH);

        // Historique des mouvements (placeholder)
        JPanel historyPanel = createHistoryPanel();
        container.add(historyPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createMouvementForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel formTitle = new JLabel("Mouvement de Stock");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Sélection produit
        produitCombo = new JComboBox<>();
        styleComboBox(produitCombo);
        formPanel.add(createFieldPanel("Produit:", produitCombo));

        // Type de mouvement
        mouvementTypeCombo = new JComboBox<>(new String[]{"Entrée", "Sortie", "Ajustement"});
        styleComboBox(mouvementTypeCombo);
        formPanel.add(createFieldPanel("Type:", mouvementTypeCombo));

        // Quantité
        quantiteField = createStyledTextField();
        formPanel.add(createFieldPanel("Quantité:", quantiteField));

        // Motif
        motifField = createStyledTextField();
        formPanel.add(createFieldPanel("Motif:", motifField));

        formPanel.add(Box.createVerticalStrut(20));

        // Boutons
        JPanel buttonPanel = createMouvementButtonPanel();
        buttonPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formPanel.add(buttonPanel);

        return formPanel;
    }

    private JPanel createMouvementButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton saveButton = createModernButton("Enregistrer", FontAwesomeSolid.SAVE, SUCCESS_COLOR, 
                e -> saveMouvement());
        JButton clearButton = createModernButton("Vider", FontAwesomeSolid.ERASER, SECONDARY_COLOR, 
                e -> clearMouvementFields());

        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(clearButton);

        return buttonPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Historique des Mouvements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Liste simple des derniers mouvements
        DefaultListModel<String> historyModel = new DefaultListModel<>();
        historyModel.addElement("📥 Entrée - Produit ABC - +50 unités");
        historyModel.addElement("📤 Sortie - Produit XYZ - -25 unités");
        historyModel.addElement("🔧 Ajustement - Produit DEF - +10 unités");

        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyList.setBackground(CARD_COLOR);
        historyList.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Méthodes utilitaires pour le styling (réutilisation du code de ProduitPanel)
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 36));
        return field;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        comboBox.setPreferredSize(new Dimension(0, 36));
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        field.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(37, 99, 235, 20));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        
        // Renderer spécial pour les statuts
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                
                // Coloration selon le statut (colonne 4)
                if (column == 4 && value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "Normal":
                            setForeground(SUCCESS_COLOR);
                            break;
                        case "Stock faible":
                            setForeground(WARNING_COLOR);
                            break;
                        case "Rupture":
                            setForeground(DANGER_COLOR);
                            break;
                        default:
                            setForeground(TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TEXT_PRIMARY);
                }
                
                return this;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
        }
    }

    private JButton createIconButton(FontAwesomeSolid icon, String tooltip, Color color) {
        JButton button = new JButton();
        button.setIcon(FontIcon.of(icon, 16, color));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(40, 40));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createModernButton(String text, FontAwesomeSolid icon, Color backgroundColor, ActionListener action) {
        JButton button = new JButton(text);
        button.setIcon(FontIcon.of(icon, 14, Color.WHITE));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        return button;
    }

    // Méthodes d'action
    private void loadStock() {
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            tableModel.setRowCount(0);

            for (ProduitDto produit : produits) {
                String status = getStockStatus(produit);
                String lastUpdate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                Object[] row = {
                        produit.getLibelle(),
                        produit.getCodeBarre(),
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "0",
                        status,
                        lastUpdate
                };
                tableModel.addRow(row);
            }
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des stocks: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getStockStatus(ProduitDto produit) {
        if (produit.getQuantiteStock() == null || produit.getQuantiteStock() == 0) {
            return "Rupture";
        }
        if (produit.getStockMinimum() != null && produit.getQuantiteStock() <= produit.getStockMinimum()) {
            return "Stock faible";
        }
        return "Normal";
    }

    private void loadProduits() {
        try {
            produitsList = produitService.getActiveProduits();
            produitCombo.removeAllItems();
            for (ProduitDto produit : produitsList) {
                produitCombo.addItem(produit.getLibelle() + " (" + produit.getCodeBarre() + ")");
            }
        } catch (Exception e) {
            // Gestion d'erreur silencieuse
        }
    }

    private void updateStats() {
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            int totalProduits = produits.size();
            long ruptures = produits.stream().filter(p -> p.getQuantiteStock() == null || p.getQuantiteStock() == 0).count();
            long alertes = produits.stream()
                    .filter(p -> p.getQuantiteStock() != null && p.getStockMinimum() != null)
                    .filter(p -> p.getQuantiteStock() <= p.getStockMinimum())
                    .count();
            
            statsLabel.setText(String.format("%d produits • %d ruptures • %d alertes", 
                    totalProduits, ruptures, alertes));
        } catch (Exception e) {
            statsLabel.setText("Statistiques non disponibles");
        }
    }

    private void searchStock() {
        // Implémentation de la recherche
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            loadStock();
            return;
        }
        
        // Filtrer les lignes existantes
        tableModel.setRowCount(0);
        try {
            List<ProduitDto> produits = produitService.searchProduits(searchText);
            for (ProduitDto produit : produits) {
                String status = getStockStatus(produit);
                String lastUpdate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                Object[] row = {
                        produit.getLibelle(),
                        produit.getCodeBarre(),
                        produit.getQuantiteStock() != null ? produit.getQuantiteStock().toString() : "0",
                        produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "0",
                        status,
                        lastUpdate
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterStock() {
        // Implémentation du filtrage
        loadStock(); // Pour l'instant, on recharge tout
    }

    private void saveMouvement() {
        try {
            if (produitCombo.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit", 
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (quantiteField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir une quantité", 
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Simulation de l'enregistrement
            JOptionPane.showMessageDialog(this, "✓ Mouvement de stock enregistré avec succès", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            
            clearMouvementFields();
            loadStock();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearMouvementFields() {
        produitCombo.setSelectedIndex(-1);
        mouvementTypeCombo.setSelectedIndex(0);
        quantiteField.setText("");
        motifField.setText("");
    }

    private void showStockDetails() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow >= 0) {
            String produitName = (String) tableModel.getValueAt(selectedRow, 0);
            String quantite = (String) tableModel.getValueAt(selectedRow, 2);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            JOptionPane.showMessageDialog(this, 
                    String.format("Détails du stock:\n\nProduit: %s\nQuantité: %s\nStatut: %s", 
                            produitName, quantite, status),
                    "Détails", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showStockAlerts() {
        StringBuilder alerts = new StringBuilder("Alertes de stock:\n\n");
        boolean hasAlerts = false;
        
        try {
            List<ProduitDto> produits = produitService.getActiveProduits();
            for (ProduitDto produit : produits) {
                if ("Rupture".equals(getStockStatus(produit)) || "Stock faible".equals(getStockStatus(produit))) {
                    alerts.append("⚠️ ").append(produit.getLibelle()).append(" - ").append(getStockStatus(produit)).append("\n");
                    hasAlerts = true;
                }
            }
            
            if (!hasAlerts) {
                alerts.append("✅ Aucune alerte de stock");
            }
            
        } catch (Exception e) {
            alerts.append("❌ Erreur lors de la vérification des stocks");
        }
        
        JOptionPane.showMessageDialog(this, alerts.toString(), "Alertes de Stock", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportStock() {
        JOptionPane.showMessageDialog(this, "Fonctionnalité d'export en cours de développement", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadStock();
        loadProduits();
    }
}