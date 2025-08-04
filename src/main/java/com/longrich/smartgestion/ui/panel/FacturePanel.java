package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class FacturePanel extends JPanel {

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

    // Composants UI
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> periodFilter;
    
    // Formulaire de création/édition
    private ComponentFactory.FieldPanel clientField;
    private ComponentFactory.FieldPanel invoiceNumberField;
    private ComponentFactory.FieldPanel dateField;
    private ComponentFactory.FieldPanel dueDateField;
    private ComponentFactory.FieldPanel amountField;
    private ComponentFactory.FieldPanel taxField;
    private ComponentFactory.FieldPanel statusField;
    private ComponentFactory.FieldPanel notesField;
    
    private final Map<String, JLabel> statsLabels = new HashMap<>();
    private boolean isEditMode = false;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        createHeaderPanel();
        createMainContent();
        loadInvoiceData();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("🧾 Gestion des Factures");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Actions rapides
        JPanel actionsPanel = createQuickActionsPanel();
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);

        JButton newInvoiceButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.PLUS, "Nouvelle Facture", PRIMARY_COLOR, e -> showInvoiceForm(false));
        JButton exportButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.FILE_EXPORT, "Exporter", SUCCESS_COLOR, e -> exportInvoices());
        JButton printButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.PRINT, "Imprimer", INFO_COLOR, e -> printSelected());

        panel.add(newInvoiceButton);
        panel.add(exportButton);
        panel.add(printButton);

        return panel;
    }

    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.3);
        splitPane.setOneTouchExpandable(true);

        // Panneau supérieur - Statistiques et filtres
        JPanel topPanel = createTopPanel();
        splitPane.setTopComponent(topPanel);

        // Panneau inférieur - Liste des factures
        JPanel invoiceListPanel = createInvoiceListPanel();
        splitPane.setBottomComponent(invoiceListPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Statistiques
        mainPanel.add(createStatsPanel());
        mainPanel.add(Box.createVerticalStrut(15));

        // Filtres
        mainPanel.add(createFiltersPanel());

        return mainPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);

        statsPanel.add(createStatCard("💰 Total Factures", "125,450 €", "total"));
        statsPanel.add(createStatCard("✅ Payées", "89,230 €", "paid"));
        statsPanel.add(createStatCard("⏳ En Attente", "28,120 €", "pending"));
        statsPanel.add(createStatCard("⚠️ En Retard", "8,100 €", "overdue"));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String key) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(180, 80));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);

        statsLabels.put(key, valueLabel);
        return card;
    }

    private JPanel createFiltersPanel() {
        JPanel filterPanel = ComponentFactory.createFilterPanel();

        // Recherche
        searchField = ComponentFactory.createStyledTextField("Rechercher par numéro, client...");
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.addActionListener(e -> applyFilters());
        JPanel searchPanel = ComponentFactory.createSearchField(searchField);
        searchPanel.setPreferredSize(new Dimension(250, 38));

        // Filtre par statut
        JLabel statusLabel = ComponentFactory.createLabel("Statut:");
        statusFilter = ComponentFactory.createStyledComboBox(new String[] {
            "Tous", "Brouillon", "Envoyée", "Payée", "En retard", "Annulée"
        });
        statusFilter.setPreferredSize(new Dimension(120, 38));
        statusFilter.addActionListener(e -> applyFilters());

        // Filtre par période
        JLabel periodLabel = ComponentFactory.createLabel("Période:");
        periodFilter = ComponentFactory.createStyledComboBox(new String[] {
            "Tout", "Aujourd'hui", "Cette semaine", "Ce mois", "Ce trimestre"
        });
        periodFilter.setPreferredSize(new Dimension(120, 38));
        periodFilter.addActionListener(e -> applyFilters());

        // Bouton de réinitialisation
        JButton resetButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.UNDO, "Réinitialiser", SECONDARY_COLOR, e -> resetFilters());

        filterPanel.add(searchPanel);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(periodLabel);
        filterPanel.add(periodFilter);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(resetButton);

        return filterPanel;
    }

    private JPanel createInvoiceListPanel() {
        JPanel container = ComponentFactory.createCardPanel();
        container.setLayout(new BorderLayout());

        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel tableTitle = ComponentFactory.createSectionTitle("📋 Liste des Factures");
        headerPanel.add(tableTitle, BorderLayout.WEST);

        // Actions sur les factures sélectionnées
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActionsPanel.setBackground(CARD_COLOR);

        JButton editButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.EDIT, "Modifier", WARNING_COLOR, e -> editSelectedInvoice());
        JButton deleteButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.TRASH, "Supprimer", DANGER_COLOR, e -> deleteSelectedInvoice());
        JButton duplicateButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.COPY, "Dupliquer", INFO_COLOR, e -> duplicateSelectedInvoice());

        tableActionsPanel.add(editButton);
        tableActionsPanel.add(deleteButton);
        tableActionsPanel.add(duplicateButton);
        headerPanel.add(tableActionsPanel, BorderLayout.EAST);

        container.add(headerPanel, BorderLayout.NORTH);

        // Table des factures
        createInvoiceTable();
        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void createInvoiceTable() {
        String[] columns = {
            "N° Facture", "Client", "Date", "Échéance", "Montant HT", "TVA", "Total TTC", "Statut"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        invoiceTable = new JTable(tableModel);
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        invoiceTable.setRowHeight(45);
        invoiceTable.setShowVerticalLines(false);
        invoiceTable.setGridColor(new Color(243, 244, 246));
        invoiceTable.setSelectionBackground(new Color(239, 246, 255));
        invoiceTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = invoiceTable.getTableHeader();
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

                // Coloration du statut (dernière colonne)
                if (column == columns.length - 1 && value != null) {
                    String status = value.toString();
                    if ("✅ Payée".equals(status)) {
                        setForeground(SUCCESS_COLOR);
                    } else if ("⚠️ En retard".equals(status)) {
                        setForeground(DANGER_COLOR);
                    } else if ("⏳ En attente".equals(status)) {
                        setForeground(WARNING_COLOR);
                    } else if ("📝 Brouillon".equals(status)) {
                        setForeground(SECONDARY_COLOR);
                    } else {
                        setForeground(TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TEXT_PRIMARY);
                }

                // Alignement des colonnes numériques
                if (column >= 4 && column <= 6) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < invoiceTable.getColumnCount(); i++) {
            invoiceTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Double-clic pour éditer
        invoiceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedInvoice();
                }
            }
        });
    }

    private void loadInvoiceData() {
        tableModel.setRowCount(0);

        // Données d'exemple
        Object[][] sampleData = {
            {"FAC-2025-001", "Longrich Store Paris", "04/08/2025", "04/09/2025", "1,250.00", "250.00", "1,500.00", "✅ Payée"},
            {"FAC-2025-002", "Beauty Center Lyon", "03/08/2025", "03/09/2025", "890.50", "178.10", "1,068.60", "⏳ En attente"},
            {"FAC-2025-003", "Wellness Spa Nice", "02/08/2025", "02/09/2025", "2,100.00", "420.00", "2,520.00", "✅ Payée"},
            {"FAC-2025-004", "Health Shop Marseille", "01/08/2025", "01/09/2025", "675.25", "135.05", "810.30", "⚠️ En retard"},
            {"FAC-2025-005", "Longrich Partner Toulouse", "31/07/2025", "31/08/2025", "1,450.75", "290.15", "1,740.90", "📝 Brouillon"},
            {"FAC-2025-006", "Beauty World Bordeaux", "30/07/2025", "30/08/2025", "980.00", "196.00", "1,176.00", "✅ Payée"},
            {"FAC-2025-007", "Wellness Center Nantes", "29/07/2025", "29/08/2025", "1,120.50", "224.10", "1,344.60", "⏳ En attente"}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
    }

    private void showInvoiceForm(boolean editMode) {
        this.isEditMode = editMode;
        
        JPanel formPanel = createInvoiceFormPanel();
        
        String title = editMode ? "Modifier la Facture" : "Nouvelle Facture";
        int option = JOptionPane.showConfirmDialog(
            this, formPanel, title, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            if (validateForm()) {
                saveInvoice();
            }
        }
    }

    private JPanel createInvoiceFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setPreferredSize(new Dimension(600, 500));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ligne 1 - Client et N° facture
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        clientField = ComponentFactory.createFieldPanel("Client", 
            ComponentFactory.createStyledComboBox(new String[] {
                "Longrich Store Paris", "Beauty Center Lyon", "Wellness Spa Nice", 
                "Health Shop Marseille", "Longrich Partner Toulouse"
            }), true);
        panel.add(clientField, gbc);

        gbc.gridx = 1;
        invoiceNumberField = ComponentFactory.createFieldPanel("N° Facture", 
            ComponentFactory.createStyledTextField(), true);
        ((JTextField) invoiceNumberField.getField()).setText("FAC-2025-" + 
            String.format("%03d", tableModel.getRowCount() + 1));
        panel.add(invoiceNumberField, gbc);

        // Ligne 2 - Dates
        gbc.gridx = 0; gbc.gridy = 1;
        dateField = ComponentFactory.createFieldPanel("Date de facture", 
            ComponentFactory.createStyledTextField(), true);
        ((JTextField) dateField.getField()).setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        panel.add(dateField, gbc);

        gbc.gridx = 1;
        dueDateField = ComponentFactory.createFieldPanel("Date d'échéance", 
            ComponentFactory.createStyledTextField(), true);
        ((JTextField) dueDateField.getField()).setText(LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        panel.add(dueDateField, gbc);

        // Ligne 3 - Montants
        gbc.gridx = 0; gbc.gridy = 2;
        amountField = ComponentFactory.createFieldPanel("Montant HT (€)", 
            ComponentFactory.createStyledTextField(), true);
        panel.add(amountField, gbc);

        gbc.gridx = 1;
        taxField = ComponentFactory.createFieldPanel("TVA (%)", 
            ComponentFactory.createStyledTextField(), true);
        ((JTextField) taxField.getField()).setText("20.0");
        panel.add(taxField, gbc);

        // Ligne 4 - Statut
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        statusField = ComponentFactory.createFieldPanel("Statut", 
            ComponentFactory.createStyledComboBox(new String[] {
                "Brouillon", "Envoyée", "Payée", "En retard", "Annulée"
            }), true);
        panel.add(statusField, gbc);

        // Ligne 5 - Notes
        gbc.gridy = 4; gbc.weighty = 1.0;
        JTextArea notesArea = ComponentFactory.createStyledTextArea(4);
        notesArea.setToolTipText("Notes ou commentaires sur la facture");
        JScrollPane notesScroll = ComponentFactory.createStyledScrollPane(notesArea);
        notesField = ComponentFactory.createFieldPanel("Notes", notesScroll);
        panel.add(notesField, gbc);

        return panel;
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Réinitialiser les erreurs
        clientField.clearError();
        invoiceNumberField.clearError();
        dateField.clearError();
        dueDateField.clearError();
        amountField.clearError();
        taxField.clearError();

        // Validation du client
        if (((JComboBox<?>) clientField.getField()).getSelectedItem() == null) {
            clientField.setError("Le client est obligatoire");
            isValid = false;
        }

        // Validation du numéro de facture
        String invoiceNumber = ((JTextField) invoiceNumberField.getField()).getText().trim();
        if (invoiceNumber.isEmpty()) {
            invoiceNumberField.setError("Le numéro de facture est obligatoire");
            isValid = false;
        }

        // Validation des dates
        String date = ((JTextField) dateField.getField()).getText().trim();
        if (date.isEmpty()) {
            dateField.setError("La date de facture est obligatoire");
            isValid = false;
        }

        String dueDate = ((JTextField) dueDateField.getField()).getText().trim();
        if (dueDate.isEmpty()) {
            dueDateField.setError("La date d'échéance est obligatoire");
            isValid = false;
        }

        // Validation du montant
        String amount = ((JTextField) amountField.getField()).getText().trim();
        if (amount.isEmpty()) {
            amountField.setError("Le montant HT est obligatoire");
            isValid = false;
        } else {
            try {
                double amountValue = Double.parseDouble(amount.replace(",", "."));
                if (amountValue <= 0) {
                    amountField.setError("Le montant doit être positif");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                amountField.setError("Montant invalide");
                isValid = false;
            }
        }

        // Validation de la TVA
        String tax = ((JTextField) taxField.getField()).getText().trim();
        if (tax.isEmpty()) {
            taxField.setError("Le taux de TVA est obligatoire");
            isValid = false;
        } else {
            try {
                double taxValue = Double.parseDouble(tax.replace(",", "."));
                if (taxValue < 0 || taxValue > 100) {
                    taxField.setError("Taux de TVA invalide (0-100%)");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                taxField.setError("Taux de TVA invalide");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveInvoice() {
        // Récupération des données du formulaire
        String client = ((JComboBox<?>) clientField.getField()).getSelectedItem().toString();
        String invoiceNumber = ((JTextField) invoiceNumberField.getField()).getText().trim();
        String date = ((JTextField) dateField.getField()).getText().trim();
        String dueDate = ((JTextField) dueDateField.getField()).getText().trim();
        String amountText = ((JTextField) amountField.getField()).getText().trim();
        String taxText = ((JTextField) taxField.getField()).getText().trim();
        String status = ((JComboBox<?>) statusField.getField()).getSelectedItem().toString();

        try {
            double amount = Double.parseDouble(amountText.replace(",", "."));
            double taxRate = Double.parseDouble(taxText.replace(",", "."));
            double taxAmount = amount * taxRate / 100;
            double totalAmount = amount + taxAmount;

            // Formatage des montants
            String formattedAmount = String.format("%.2f", amount).replace(".", ",");
            String formattedTax = String.format("%.2f", taxAmount).replace(".", ",");
            String formattedTotal = String.format("%.2f", totalAmount).replace(".", ",");

            // Formatage du statut avec icône
            String statusIcon = switch (status) {
                case "Brouillon" -> "📝 Brouillon";
                case "Envoyée" -> "📧 Envoyée";
                case "Payée" -> "✅ Payée";
                case "En retard" -> "⚠️ En retard";
                case "Annulée" -> "❌ Annulée";
                default -> status;
            };

            Object[] newRow = {
                invoiceNumber, client, date, dueDate, 
                formattedAmount, formattedTax, formattedTotal, statusIcon
            };

            if (isEditMode && invoiceTable.getSelectedRow() >= 0) {
                // Modification
                int selectedRow = invoiceTable.getSelectedRow();
                for (int i = 0; i < newRow.length; i++) {
                    tableModel.setValueAt(newRow[i], selectedRow, i);
                }
                JOptionPane.showMessageDialog(this, 
                    "✓ Facture modifiée avec succès !", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Création
                tableModel.insertRow(0, newRow);
                JOptionPane.showMessageDialog(this, 
                    "✓ Facture créée avec succès !", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur dans les montants saisis", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une facture à modifier", 
                "Sélection requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showInvoiceForm(true);
        
        // Pré-remplir le formulaire avec les données sélectionnées
        // (Ce code serait normalement dans showInvoiceForm mais simplifié ici)
    }

    private void deleteSelectedInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une facture à supprimer", 
                "Sélection requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String invoiceNumber = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer la facture " + invoiceNumber + " ?",
            "Confirmation de suppression", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, 
                "✓ Facture supprimée avec succès !", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void duplicateSelectedInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une facture à dupliquer", 
                "Sélection requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Dupliquer la ligne avec un nouveau numéro
        Object[] originalRow = new Object[tableModel.getColumnCount()];
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            originalRow[i] = tableModel.getValueAt(selectedRow, i);
        }

        // Nouveau numéro de facture
        originalRow[0] = "FAC-2025-" + String.format("%03d", tableModel.getRowCount() + 1);
        originalRow[2] = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        originalRow[3] = LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        originalRow[7] = "📝 Brouillon";

        tableModel.insertRow(0, originalRow);
        JOptionPane.showMessageDialog(this, 
            "✓ Facture dupliquée avec succès !", 
            "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyFilters() {
        // TODO: Implémenter le filtrage des factures
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedPeriod = (String) periodFilter.getSelectedItem();
        
        // Logique de filtrage simplifiée pour la démo
        if (!searchText.isEmpty() || !"Tous".equals(selectedStatus) || !"Tout".equals(selectedPeriod)) {
            JOptionPane.showMessageDialog(this, 
                "Filtres appliqués : " + searchText + " | " + selectedStatus + " | " + selectedPeriod, 
                "Filtrage", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        periodFilter.setSelectedIndex(0);
        loadInvoiceData();
    }

    private void exportInvoices() {
        JOptionPane.showMessageDialog(this, 
            "Export des factures en cours de développement", 
            "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void printSelected() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une facture à imprimer", 
                "Sélection requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String invoiceNumber = (String) tableModel.getValueAt(selectedRow, 0);
        JOptionPane.showMessageDialog(this, 
            "Impression de la facture " + invoiceNumber + " en cours...", 
            "Impression", JOptionPane.INFORMATION_MESSAGE);
    }
}