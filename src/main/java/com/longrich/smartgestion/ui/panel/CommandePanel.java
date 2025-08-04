package com.longrich.smartgestion.ui.panel;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.dto.CommandeDTO;
import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ClientService;
import com.longrich.smartgestion.service.ProduitService;
import com.longrich.smartgestion.ui.components.ButtonFactory;

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
// import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class CommandePanel extends JPanel {

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

    private final ClientService clientService;
    private final ProduitService produitService;

    // Composants UI pour les commandes
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JTable commandesTable;
    private DefaultTableModel commandesTableModel;
    private JLabel statsLabel;

    // Composants pour nouvelle commande
    private JComboBox<String> clientCombo;
    private JTextField dateCommandeField;
    private JComboBox<String> statutCombo;
    private JTextArea observationsArea;

    // Table des lignes de commande
    private JTable lignesTable;
    private DefaultTableModel lignesTableModel;
    private JComboBox<String> produitCombo;
    private JTextField quantiteField;
    private JTextField prixUnitaireField;
    private JLabel totalCommandeLabel;

    private List<ClientDTO> clientsList;
    private List<ProduitDto> produitsList;
    private List<LigneCommandeTemp> lignesCommande;
    private CommandeDTO currentCommande;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lignesCommande = new ArrayList<>();

        createHeaderPanel();
        createMainContent();

        loadCommandes();
        loadClients();
        loadProduits();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Gestion des Commandes");
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

        JButton newCommandeButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.PLUS, "Nouvelle commande", SUCCESS_COLOR, e -> showNewCommandeDialog());
        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter", INFO_COLOR, e -> exportCommandes());
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", SECONDARY_COLOR, e -> refreshData());

        panel.add(newCommandeButton);
        panel.add(exportButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(BACKGROUND_COLOR);

        // Onglet liste des commandes
        JPanel listePanel = createListeCommandesPanel();
        tabbedPane.addTab("📋 Liste des Commandes", listePanel);

        // Onglet nouvelle commande
        JPanel nouvellePanel = createNouvelleCommandePanel();
        tabbedPane.addTab("➕ Nouvelle Commande", nouvellePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createListeCommandesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Panneau de recherche et filtres
        JPanel searchContainer = createSearchPanel();
        panel.add(searchContainer, BorderLayout.NORTH);

        // Table des commandes
        JPanel tablePanel = createCommandesTable();
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(CARD_COLOR);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel searchTitle = new JLabel("Liste des Commandes");
        searchTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchTitle.setForeground(TEXT_PRIMARY);

        // Panneau de recherche et filtres
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtersPanel.setBackground(CARD_COLOR);

        // Champ de recherche
        searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 36));
        searchField.addActionListener(e -> searchCommandes());

        // Filtre par statut
        statusFilterCombo = new JComboBox<>(
                new String[] { "Tous", "En attente", "Confirmée", "En cours", "Livrée", "Annulée" });
        styleComboBox(statusFilterCombo);
        statusFilterCombo.addActionListener(e -> filterCommandes());

        filtersPanel.add(new JLabel("Rechercher:"));
        filtersPanel.add(searchField);
        filtersPanel.add(new JLabel("Statut:"));
        filtersPanel.add(statusFilterCombo);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_COLOR);
        topPanel.add(searchTitle, BorderLayout.WEST);
        topPanel.add(filtersPanel, BorderLayout.EAST);

        searchPanel.add(topPanel, BorderLayout.NORTH);
        return searchPanel;
    }

    private JPanel createCommandesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Modèle de table
        String[] columns = { "N° Commande", "Client", "Date", "Statut", "Total", "Actions" };
        commandesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seule la colonne Actions est éditable
            }
        };

        commandesTable = new JTable(commandesTableModel);
        styleTable(commandesTable);

        // Renderer spécial pour la colonne Actions
        commandesTable.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        commandesTable.getColumn("Actions").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(commandesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createNouvelleCommandePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Panneau supérieur - Informations générales
        JPanel headerPanel = createCommandeHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panneau central - Lignes de commande
        JPanel centerPanel = createLignesCommandePanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panneau inférieur - Actions
        JPanel footerPanel = createCommandeFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createCommandeHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titleLabel = new JLabel("Informations de la Commande");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Première ligne - Client et Date
        JPanel firstRow = new JPanel(new GridLayout(1, 2, 20, 0));
        firstRow.setBackground(CARD_COLOR);
        firstRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        clientCombo = new JComboBox<>();
        styleComboBox(clientCombo);
        firstRow.add(createFieldPanel("Client:", clientCombo));

        dateCommandeField = createStyledTextField();
        dateCommandeField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        firstRow.add(createFieldPanel("Date:", dateCommandeField));

        panel.add(firstRow);

        // Deuxième ligne - Statut et Observations
        JPanel secondRow = new JPanel(new GridLayout(1, 2, 20, 0));
        secondRow.setBackground(CARD_COLOR);
        secondRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        statutCombo = new JComboBox<>(new String[] { "En attente", "Confirmée", "En cours", "Livrée", "Annulée" });
        styleComboBox(statutCombo);
        secondRow.add(createFieldPanel("Statut:", statutCombo));

        observationsArea = createStyledTextArea();
        JScrollPane obsScrollPane = new JScrollPane(observationsArea);
        obsScrollPane.setPreferredSize(new Dimension(0, 80));
        obsScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        secondRow.add(createFieldPanel("Observations:", obsScrollPane));

        panel.add(secondRow);

        return panel;
    }

    private JPanel createLignesCommandePanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.3);

        // Panneau supérieur - Ajout de ligne
        JPanel addLignePanel = createAddLignePanel();
        splitPane.setTopComponent(addLignePanel);

        // Panneau inférieur - Liste des lignes
        JPanel lignesListPanel = createLignesListPanel();
        splitPane.setBottomComponent(lignesListPanel);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        container.add(splitPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createAddLignePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Titre
        JLabel titleLabel = new JLabel("Ajouter un Produit");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        // Ligne de saisie
        JPanel inputRow = new JPanel(new GridLayout(1, 4, 15, 0));
        inputRow.setBackground(CARD_COLOR);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        produitCombo = new JComboBox<>();
        styleComboBox(produitCombo);
        produitCombo.addActionListener(e -> updatePrixUnitaire());
        inputRow.add(createFieldPanel("Produit:", produitCombo));

        quantiteField = createStyledTextField();
        inputRow.add(createFieldPanel("Quantité:", quantiteField));

        prixUnitaireField = createStyledTextField();
        inputRow.add(createFieldPanel("Prix unitaire:", prixUnitaireField));

        JButton addButton = createModernButton("Ajouter", FontAwesomeSolid.PLUS, SUCCESS_COLOR,
                e -> addLigneCommande());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.add(addButton);
        inputRow.add(buttonPanel);

        panel.add(inputRow);

        return panel;
    }

    private JPanel createLignesListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Header avec total
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("Lignes de Commande");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        totalCommandeLabel = new JLabel("Total: 0 FCFA");
        totalCommandeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalCommandeLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(totalCommandeLabel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table des lignes
        String[] columns = { "Produit", "Quantité", "Prix Unit.", "Total", "Action" };
        lignesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Seule la colonne Action est éditable
            }
        };

        lignesTable = new JTable(lignesTableModel);
        styleTable(lignesTable);

        // Renderer pour la colonne Action
        lignesTable.getColumn("Action").setCellRenderer(new DeleteButtonRenderer());
        lignesTable.getColumn("Action").setCellEditor(new DeleteButtonEditor());

        JScrollPane scrollPane = new JScrollPane(lignesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCommandeFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createModernButton("Enregistrer", FontAwesomeSolid.SAVE, SUCCESS_COLOR,
                e -> saveCommande());
        JButton clearButton = createModernButton("Vider", FontAwesomeSolid.ERASER, SECONDARY_COLOR,
                e -> clearCommande());

        panel.add(clearButton);
        panel.add(saveButton);

        return panel;
    }

    // Méthodes utilitaires pour le styling (réutilisation du code précédent)
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

    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea(3, 0);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(Color.WHITE);
        area.setForeground(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return area;
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

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        field.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(field);

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

        // Default renderer
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
                setForeground(TEXT_PRIMARY);

                return this;
            }
        };

        // Appliquer le renderer à toutes les colonnes sauf Actions
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
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

    private JButton createModernButton(String text, FontAwesomeSolid icon, Color backgroundColor,
            ActionListener action) {
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

    // Classes pour les renderers des boutons dans les tables
    private class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            JButton viewButton = new JButton(FontIcon.of(FontAwesomeSolid.EYE, 12, INFO_COLOR));
            JButton editButton = new JButton(FontIcon.of(FontAwesomeSolid.EDIT, 12, WARNING_COLOR));
            JButton deleteButton = new JButton(FontIcon.of(FontAwesomeSolid.TRASH, 12, DANGER_COLOR));

            for (JButton btn : new JButton[] { viewButton, editButton, deleteButton }) {
                btn.setPreferredSize(new Dimension(25, 25));
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.add(btn);
            }

            return panel;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        public ActionButtonEditor() {
            super(new JCheckBox());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return new ActionButtonRenderer().getTableCellRendererComponent(table, value, isSelected, false, row,
                    column);
        }
    }

    private class DeleteButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = new JButton(FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));
            button.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }
    }

    private class DeleteButtonEditor extends DefaultCellEditor {
        public DeleteButtonEditor() {
            super(new JCheckBox());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            JButton button = new JButton(FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));
            button.addActionListener(e -> removeLigneCommande(row));
            return button;
        }
    }

    // Classe temporaire pour les lignes de commande
    private static class LigneCommandeTemp {
        private String produitNom;
        private int quantite;
        private BigDecimal prixUnitaire;
        private BigDecimal total;

        public LigneCommandeTemp(String produitNom, int quantite, BigDecimal prixUnitaire) {
            this.produitNom = produitNom;
            this.quantite = quantite;
            this.prixUnitaire = prixUnitaire;
            this.total = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }

        // Getters
        public String getProduitNom() {
            return produitNom;
        }

        public int getQuantite() {
            return quantite;
        }

        public BigDecimal getPrixUnitaire() {
            return prixUnitaire;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    // Méthodes d'action
    private void loadCommandes() {
        commandesTableModel.setRowCount(0);

        // Données factices
        Object[][] sampleData = {
                { "CMD-001", "Client Test 1", "15/01/2024", "En cours", "150,000 FCFA", "" },
                { "CMD-002", "Client Test 2", "16/01/2024", "Livrée", "75,500 FCFA", "" },
                { "CMD-003", "Client Test 3", "17/01/2024", "En attente", "200,000 FCFA", "" }
        };

        for (Object[] row : sampleData) {
            commandesTableModel.addRow(row);
        }

        updateStats();
    }

    private void loadClients() {
        try {
            clientsList = clientService.getAllClients();
            clientCombo.removeAllItems();
            for (ClientDTO client : clientsList) {
                clientCombo.addItem(client.getNom() + " " + client.getPrenom());
            }
        } catch (Exception e) {
            // Gestion d'erreur silencieuse
        }
    }

    private void loadProduits() {
        try {
            produitsList = produitService.getActiveProduits();
            produitCombo.removeAllItems();
            for (ProduitDto produit : produitsList) {
                produitCombo.addItem(produit.getLibelle());
            }
        } catch (Exception e) {
            // Gestion d'erreur silencieuse
        }
    }

    private void updateStats() {
        statsLabel.setText("3 commandes • 2 en cours • 1 livrée");
    }

    private void updatePrixUnitaire() {
        int selectedIndex = produitCombo.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < produitsList.size()) {
            ProduitDto produit = produitsList.get(selectedIndex);
            if (produit.getPrixRevente() != null) {
                prixUnitaireField.setText(produit.getPrixRevente().toString());
            }
        }
    }

    private void addLigneCommande() {
        try {
            if (produitCombo.getSelectedIndex() == -1 || quantiteField.getText().trim().isEmpty() ||
                    prixUnitaireField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String produitNom = (String) produitCombo.getSelectedItem();
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            BigDecimal prix = new BigDecimal(prixUnitaireField.getText().trim());

            LigneCommandeTemp ligne = new LigneCommandeTemp(produitNom, quantite, prix);
            lignesCommande.add(ligne);

            // Ajouter à la table
            Object[] row = {
                    ligne.getProduitNom(),
                    ligne.getQuantite(),
                    ligne.getPrixUnitaire() + " FCFA",
                    ligne.getTotal() + " FCFA",
                    ""
            };
            lignesTableModel.addRow(row);

            // Calculer le total
            updateTotalCommande();

            // Vider les champs
            quantiteField.setText("");
            prixUnitaireField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeLigneCommande(int index) {
        if (index >= 0 && index < lignesCommande.size()) {
            lignesCommande.remove(index);
            lignesTableModel.removeRow(index);
            updateTotalCommande();
        }
    }

    private void updateTotalCommande() {
        BigDecimal total = lignesCommande.stream()
                .map(LigneCommandeTemp::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalCommandeLabel.setText("Total: " + total + " FCFA");
    }

    private void saveCommande() {
        if (clientCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (lignesCommande.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez ajouter au moins un produit",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simulation de sauvegarde
        JOptionPane.showMessageDialog(this, "✓ Commande sauvegardée avec succès",
                "Succès", JOptionPane.INFORMATION_MESSAGE);

        clearCommande();
        loadCommandes();
    }

    private void clearCommande() {
        clientCombo.setSelectedIndex(-1);
        dateCommandeField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        statutCombo.setSelectedIndex(0);
        observationsArea.setText("");
        produitCombo.setSelectedIndex(-1);
        quantiteField.setText("");
        prixUnitaireField.setText("");

        lignesCommande.clear();
        lignesTableModel.setRowCount(0);
        updateTotalCommande();
    }

    private void searchCommandes() {
        // Implémentation de la recherche
        JOptionPane.showMessageDialog(this, "Fonctionnalité de recherche en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void filterCommandes() {
        // Implémentation du filtrage
        loadCommandes(); // Pour l'instant, on recharge tout
    }

    private void showNewCommandeDialog() {
        // Basculer vers l'onglet nouvelle commande
        Container parent = getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedIndex(1);
        }
    }

    private void exportCommandes() {
        JOptionPane.showMessageDialog(this, "Fonctionnalité d'export en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadCommandes();
        loadClients();
        loadProduits();
    }
}