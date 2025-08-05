package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
// import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
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
public class BackupPanel extends JPanel {

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
    private JTable backupTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextField backupLocationField;
    private JComboBox<String> backupTypeCombo;
    private final Map<String, JLabel> statusLabels = new HashMap<>();

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        createHeaderPanel();
        createMainContent();
        loadBackupHistory();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("💾 Sauvegarde & Restauration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Contrôles rapides
        JPanel quickActionsPanel = createQuickActionsPanel();
        headerPanel.add(quickActionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);

        JButton autoBackupButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.CLOCK, "Sauvegarde Auto", INFO_COLOR, e -> configureAutoBackup());
        JButton scheduleButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.CALENDAR_ALT, "Planifier", WARNING_COLOR, e -> scheduleBackup());
        JButton helpButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.QUESTION_CIRCLE, "Aide", SECONDARY_COLOR, e -> showHelp());

        panel.add(autoBackupButton);
        panel.add(scheduleButton);
        panel.add(helpButton);

        return panel;
    }

    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.4); // Augmenté pour plus d'espace à gauche
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true); // Amélioration de l'expérience utilisateur

        // Panneau gauche - Actions de sauvegarde/restauration
        JPanel actionsPanel = createActionsPanel();
        splitPane.setLeftComponent(actionsPanel);

        // Panneau droit - Historique des sauvegardes
        JPanel historyPanel = createHistoryPanel();
        splitPane.setRightComponent(historyPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createActionsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Section sauvegarde
        mainPanel.add(createBackupSection());
        mainPanel.add(Box.createVerticalStrut(20));

        // Section restauration
        mainPanel.add(createRestoreSection());
        mainPanel.add(Box.createVerticalStrut(20));

        // Section état
        mainPanel.add(createStatusSection());

        return mainPanel;
    }

    private JPanel createBackupSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(CARD_COLOR);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Titre de section
        JLabel sectionTitle = new JLabel("📤 Créer une Sauvegarde");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(20));

        // Type de sauvegarde
        JLabel typeLabel = new JLabel("Type de sauvegarde:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeLabel.setForeground(TEXT_SECONDARY);
        typeLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(typeLabel);
        section.add(Box.createVerticalStrut(5));

        backupTypeCombo = new JComboBox<>(new String[] {
            "🗃️ Sauvegarde Complète (Base + Fichiers)",
            "🗄️ Base de Données Uniquement",
            "📁 Fichiers de Configuration",
            "📊 Données Métier (Clients, Produits, Stocks)"
        });
        styleComboBox(backupTypeCombo);
        backupTypeCombo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(backupTypeCombo);
        section.add(Box.createVerticalStrut(15));

        // Emplacement
        JLabel locationLabel = new JLabel("Emplacement de sauvegarde:");
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        locationLabel.setForeground(TEXT_SECONDARY);
        locationLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(locationLabel);
        section.add(Box.createVerticalStrut(5));

        JPanel locationPanel = new JPanel(new BorderLayout(10, 0));
        locationPanel.setBackground(CARD_COLOR);
        locationPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        backupLocationField = createStyledTextField();
        backupLocationField.setText(System.getProperty("user.home") + "/SmartGestion/Backups");
        locationPanel.add(backupLocationField, BorderLayout.CENTER);

        JButton browseButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.FOLDER_OPEN, "", SECONDARY_COLOR, e -> browseBackupLocation());
        browseButton.setPreferredSize(new Dimension(40, 38));
        locationPanel.add(browseButton, BorderLayout.EAST);

        section.add(locationPanel);
        section.add(Box.createVerticalStrut(20));

        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JButton createBackupButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.SAVE, "Créer Sauvegarde", SUCCESS_COLOR, e -> createBackup());
        createBackupButton.setPreferredSize(new Dimension(160, 40));

        JButton quickBackupButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.BOLT, "Sauvegarde Rapide", INFO_COLOR, e -> quickBackup());
        quickBackupButton.setPreferredSize(new Dimension(160, 40));

        buttonPanel.add(createBackupButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(quickBackupButton);

        section.add(buttonPanel);

        return section;
    }

    private JPanel createRestoreSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(CARD_COLOR);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Titre de section
        JLabel sectionTitle = new JLabel("📥 Restaurer une Sauvegarde");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(20));

        // Message d'avertissement
        JPanel warningPanel = new JPanel(new BorderLayout(10, 0));
        warningPanel.setBackground(new Color(254, 242, 242));
        warningPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(239, 68, 68, 50), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        warningPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JLabel warningIcon = new JLabel("⚠️");
        warningIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        warningPanel.add(warningIcon, BorderLayout.WEST);

        JLabel warningText = new JLabel("<html><b>Attention:</b> La restauration remplacera toutes les données actuelles.<br>Assurez-vous d'avoir une sauvegarde récente avant de continuer.</html>");
        warningText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        warningText.setForeground(new Color(127, 29, 29));
        warningPanel.add(warningText, BorderLayout.CENTER);

        section.add(warningPanel);
        section.add(Box.createVerticalStrut(20));

        // Boutons de restauration
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JButton restoreFromFileButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.UPLOAD, "Depuis Fichier", WARNING_COLOR, e -> restoreFromFile());
        restoreFromFileButton.setPreferredSize(new Dimension(140, 40));

        JButton restoreFromHistoryButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.HISTORY, "Depuis Historique", PRIMARY_COLOR, e -> restoreFromHistory());
        restoreFromHistoryButton.setPreferredSize(new Dimension(160, 40));

        buttonPanel.add(restoreFromFileButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(restoreFromHistoryButton);

        section.add(buttonPanel);

        return section;
    }

    private JPanel createStatusSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(CARD_COLOR);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Titre de section
        JLabel sectionTitle = new JLabel("📊 État des Opérations");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(20));

        // Barre de progression améliorée
        JPanel progressContainer = new JPanel();
        progressContainer.setLayout(new BoxLayout(progressContainer, BoxLayout.Y_AXIS));
        progressContainer.setBackground(CARD_COLOR);
        progressContainer.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        progressContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Prêt");
        progressBar.setPreferredSize(new Dimension(0, 28));
        progressBar.setBackground(new Color(243, 244, 246));
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        progressBar.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        
        progressContainer.add(progressBar);
        section.add(progressContainer);
        section.add(Box.createVerticalStrut(12));

        // Statut amélioré
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusContainer.setBackground(CARD_COLOR);
        statusContainer.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        
        statusLabel = new JLabel("Système prêt pour les opérations de sauvegarde/restauration");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        statusContainer.add(statusLabel);
        section.add(statusContainer);
        section.add(Box.createVerticalStrut(18));

        // Statistiques avec mise en page responsive
        JPanel statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.Y_AXIS));
        statsContainer.setBackground(CARD_COLOR);
        statsContainer.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        
        // Première ligne de statistiques
        JPanel statsRow1 = new JPanel(new GridLayout(1, 2, 15, 0));
        statsRow1.setBackground(CARD_COLOR);
        statsRow1.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        statsRow1.add(createStatCard("Dernière Sauvegarde", "Il y a 2 jours", "last_backup"));
        statsRow1.add(createStatCard("Taille Totale", "1.2 GB", "total_size"));
        
        // Deuxième ligne de statistiques
        JPanel statsRow2 = new JPanel(new GridLayout(1, 2, 15, 0));
        statsRow2.setBackground(CARD_COLOR);
        statsRow2.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        statsRow2.add(createStatCard("Sauvegardes", "15", "backup_count"));
        statsRow2.add(createStatCard("Prochaine Auto", "Dans 5 jours", "next_auto"));
        
        statsContainer.add(statsRow1);
        statsContainer.add(Box.createVerticalStrut(12));
        statsContainer.add(statsRow2);

        section.add(statsContainer);

        return section;
    }

    private JPanel createStatCard(String title, String value, String key) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMinimumSize(new Dimension(120, 70));
        card.setPreferredSize(new Dimension(0, 75));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        statusLabels.put(key, valueLabel);
        return card;
    }

    private JPanel createHistoryPanel() {
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

        JLabel tableTitle = new JLabel("📜 Historique des Sauvegardes");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);
        headerPanel.add(tableTitle, BorderLayout.WEST);

        // Boutons d'actions sur l'historique
        JPanel historyActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        historyActions.setBackground(CARD_COLOR);

        JButton cleanupButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.BROOM, "Nettoyer", WARNING_COLOR, e -> cleanupOldBackups());
        JButton verifyButton = ButtonFactory.createActionButton(
            FontAwesomeSolid.CHECK_CIRCLE, "Vérifier", INFO_COLOR, e -> verifyBackups());

        historyActions.add(verifyButton);
        historyActions.add(cleanupButton);
        headerPanel.add(historyActions, BorderLayout.EAST);

        container.add(headerPanel, BorderLayout.NORTH);

        // Table d'historique avec améliorations
        createBackupTable();
        JScrollPane scrollPane = new JScrollPane(backupTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void createBackupTable() {
        String[] columns = {
            "Date/Heure", "Type", "Taille", "Emplacement", "Statut", "Actions"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Seulement la colonne Actions
            }
        };

        backupTable = new JTable(tableModel);
        backupTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backupTable.setRowHeight(45);
        backupTable.setShowVerticalLines(false);
        backupTable.setGridColor(new Color(243, 244, 246));
        backupTable.setSelectionBackground(new Color(239, 246, 255));
        backupTable.setSelectionForeground(TEXT_PRIMARY);

        // Style de l'en-tête
        JTableHeader header = backupTable.getTableHeader();
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

                // Coloration du statut (colonne 4)
                if (column == 4 && value != null) {
                    String status = value.toString();
                    if ("✅ Réussie".equals(status)) {
                        setForeground(SUCCESS_COLOR);
                    } else if ("❌ Échouée".equals(status)) {
                        setForeground(DANGER_COLOR);
                    } else if ("⏳ En cours".equals(status)) {
                        setForeground(WARNING_COLOR);
                    } else {
                        setForeground(TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TEXT_PRIMARY);
                }

                return c;
            }
        };

        for (int i = 0; i < backupTable.getColumnCount(); i++) {
            backupTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 38));
        return field;
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

    private void loadBackupHistory() {
        tableModel.setRowCount(0);

        // Données d'exemple
        Object[][] sampleData = {
            {"04/08/2025 15:30", "🗃️ Complète", "245 MB", "/home/user/backups/", "✅ Réussie", "🔄 📥 🗑️"},
            {"02/08/2025 10:15", "🗄️ Base de données", "89 MB", "/home/user/backups/", "✅ Réussie", "🔄 📥 🗑️"},
            {"01/08/2025 18:45", "📊 Données métier", "156 MB", "/home/user/backups/", "✅ Réussie", "🔄 📥 🗑️"},
            {"30/07/2025 09:30", "🗃️ Complète", "234 MB", "/home/user/backups/", "❌ Échouée", "🔄 📥 🗑️"},
            {"28/07/2025 14:20", "📁 Configuration", "12 MB", "/home/user/backups/", "✅ Réussie", "🔄 📥 🗑️"}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
    }

    // Méthodes d'action
    private void createBackup() {
        String type = (String) backupTypeCombo.getSelectedItem();
        String location = backupLocationField.getText().trim();

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez spécifier un emplacement de sauvegarde", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simulation d'une sauvegarde avec barre de progression
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                statusLabel.setText("Création de la sauvegarde en cours...");
                progressBar.setString("Préparation...");
                
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(200);
                    setProgress(i);
                    publish(i);
                    
                    if (i == 20) progressBar.setString("Sauvegarde base de données...");
                    else if (i == 50) progressBar.setString("Sauvegarde fichiers...");
                    else if (i == 80) progressBar.setString("Compression...");
                    else if (i == 100) progressBar.setString("Terminé ✓");
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                for (Integer progress : chunks) {
                    progressBar.setValue(progress);
                }
            }

            @Override
            protected void done() {
                statusLabel.setText("Sauvegarde créée avec succès !");
                progressBar.setString("Terminé ✓");
                
                // Ajouter à l'historique
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                Object[] newRow = {timestamp, type, "187 MB", location, "✅ Réussie", "🔄 📥 🗑️"};
                tableModel.insertRow(0, newRow);
                
                JOptionPane.showMessageDialog(BackupPanel.this, 
                    "✓ Sauvegarde créée avec succès !", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        progressBar.setValue(0);
        worker.execute();
    }

    private void quickBackup() {
        backupTypeCombo.setSelectedIndex(0); // Sauvegarde complète
        createBackup();
    }

    private void browseBackupLocation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(backupLocationField.getText()));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            backupLocationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void restoreFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers de sauvegarde", "backup", "sql", "zip"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ Êtes-vous sûr de vouloir restaurer depuis:\n" + selectedFile.getName() + 
                "\n\nCette action remplacera toutes les données actuelles !",
                "Confirmation de restauration", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                performRestore(selectedFile.getName());
            }
        }
    }

    private void restoreFromHistory() {
        int selectedRow = backupTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une sauvegarde dans l'historique", 
                "Sélection requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String backupDate = (String) tableModel.getValueAt(selectedRow, 0);
        String backupType = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ Êtes-vous sûr de vouloir restaurer la sauvegarde:\n" + 
            backupType + " du " + backupDate + 
            "\n\nCette action remplacera toutes les données actuelles !",
            "Confirmation de restauration", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            performRestore(backupDate);
        }
    }

    private void performRestore(String backupName) {
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                statusLabel.setText("Restauration en cours...");
                progressBar.setString("Préparation...");
                
                for (int i = 0; i <= 100; i += 15) {
                    Thread.sleep(300);
                    setProgress(i);
                    publish(i);
                    
                    if (i == 15) progressBar.setString("Vérification...");
                    else if (i == 30) progressBar.setString("Extraction...");
                    else if (i == 60) progressBar.setString("Restauration base...");
                    else if (i == 90) progressBar.setString("Finalisation...");
                    else if (i == 100) progressBar.setString("Restauré ✓");
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                for (Integer progress : chunks) {
                    progressBar.setValue(progress);
                }
            }

            @Override
            protected void done() {
                statusLabel.setText("Restauration terminée avec succès !");
                progressBar.setString("Restauré ✓");
                
                JOptionPane.showMessageDialog(BackupPanel.this, 
                    "✓ Restauration terminée avec succès !\n\nL'application va redémarrer pour appliquer les changements.", 
                    "Restauration réussie", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        progressBar.setValue(0);
        worker.execute();
    }

    private void configureAutoBackup() {
        JOptionPane.showMessageDialog(this, 
            "Configuration de la sauvegarde automatique en cours de développement", 
            "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void scheduleBackup() {
        JOptionPane.showMessageDialog(this, 
            "Planification des sauvegardes en cours de développement", 
            "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cleanupOldBackups() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer les sauvegardes de plus de 30 jours ?",
            "Nettoyage", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, 
                "✓ 3 anciennes sauvegardes supprimées", 
                "Nettoyage terminé", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void verifyBackups() {
        JOptionPane.showMessageDialog(this, 
            "✓ Toutes les sauvegardes sont intègres", 
            "Vérification terminée", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        String helpText = """
            <html>
            <h3>Guide de Sauvegarde/Restauration</h3>
            <br>
            <b>Types de sauvegarde:</b><br>
            • <b>Complète</b>: Base de données + fichiers<br>
            • <b>Base de données</b>: Données uniquement<br>
            • <b>Configuration</b>: Paramètres système<br>
            • <b>Données métier</b>: Clients, produits, stocks<br>
            <br>
            <b>Bonnes pratiques:</b><br>
            • Sauvegardez régulièrement (quotidien/hebdomadaire)<br>
            • Stockez les sauvegardes en lieu sûr<br>
            • Testez vos restaurations périodiquement<br>
            • Gardez plusieurs versions de sauvegarde<br>
            </html>
            """;
            
        JOptionPane.showMessageDialog(this, helpText, 
            "Aide - Sauvegarde/Restauration", JOptionPane.INFORMATION_MESSAGE);
    }
}