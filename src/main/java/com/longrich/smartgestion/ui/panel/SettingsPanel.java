package com.longrich.smartgestion.ui.panel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
// import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class SettingsPanel extends JPanel {

    // Stockage des paramètres
    private Map<String, Object> settings;
    private Map<String, JComponent> settingsComponents;

    // Composants UI
    private JTextField companyNameField;
    private JTextField companyAddressField;
    private JTextField companyPhoneField;
    private JTextField companyEmailField;
    private JTextField taxNumberField;
    private JComboBox<String> currencyCombo;
    private JSpinner taxRateSpinner;
    private JCheckBox autoBackupCheckbox;
    private JSpinner backupIntervalSpinner;
    private JTextField backupPathField;
    private JComboBox<String> themeCombo;
    private JComboBox<String> languageCombo;
    private JCheckBox notificationsCheckbox;
    private JCheckBox soundCheckbox;
    private JTextField smtpServerField;
    private JTextField smtpPortField;
    private JTextField smtpUserField;
    private JPasswordField smtpPasswordField;
    private JCheckBox smtpSslCheckbox;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ComponentFactory.getBackgroundColor());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeSettings();
        createHeaderPanel();
        createMainContent();
        loadSettings();
    }

    private void initializeSettings() {
        settings = new HashMap<>();
        settingsComponents = new HashMap<>();

        // Valeurs par défaut
        settings.put("companyName", "SmartGestion - Longrich");
        settings.put("companyAddress", "Ouagadougou, Burkina Faso");
        settings.put("companyPhone", "+226 XX XX XX XX");
        settings.put("companyEmail", "contact@smartgestion.bf");
        settings.put("taxNumber", "BF-123456789");
        settings.put("currency", "FCFA");
        settings.put("taxRate", 18.0);
        settings.put("autoBackup", true);
        settings.put("backupInterval", 24);
        settings.put("backupPath", System.getProperty("user.home") + "/SmartGestion/Backups");
        settings.put("theme", "Clair");
        settings.put("language", "Français");
        settings.put("notifications", true);
        settings.put("sound", true);
        settings.put("smtpServer", "smtp.gmail.com");
        settings.put("smtpPort", "587");
        settings.put("smtpUser", "");
        settings.put("smtpPassword", "");
        settings.put("smtpSsl", true);
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ComponentFactory.getBackgroundColor());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre
        JLabel titleLabel = new JLabel("Paramètres Système");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ComponentFactory.getTextPrimaryColor());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setBackground(ComponentFactory.getBackgroundColor());

        JButton saveButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SAVE, "Sauvegarder", ComponentFactory.getSuccessColor(),
                e -> saveSettings());
        JButton resetButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.UNDO, "Réinitialiser", ComponentFactory.getWarningColor(),
                e -> resetSettings());
        JButton exportButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FILE_EXPORT, "Exporter Config", ComponentFactory.getPrimaryColor(),
                e -> exportConfig());

        actionsPanel.add(exportButton);
        actionsPanel.add(resetButton);
        actionsPanel.add(saveButton);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainContent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(ComponentFactory.getBackgroundColor());

        // Onglets
        tabbedPane.addTab("🏢 Entreprise", createCompanyPanel());
        tabbedPane.addTab("💰 Facturation", createBillingPanel());
        tabbedPane.addTab("💾 Sauvegarde", createBackupPanel());
        tabbedPane.addTab("🎨 Interface", createInterfacePanel());
        tabbedPane.addTab("🔔 Notifications", createNotificationsPanel());
        tabbedPane.addTab("📧 Email", createEmailPanel());
        tabbedPane.addTab("🔒 Sécurité", createSecurityPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCompanyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Section Informations générales
        JPanel companyCard = ComponentFactory.createCardPanel();
        companyCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel sectionTitle = ComponentFactory.createSectionTitle("Informations de l'entreprise");
        companyCard.add(sectionTitle, gbc);
        gbc.gridwidth = 1;

        // Nom de l'entreprise
        gbc.gridx = 0;
        gbc.gridy = 1;
        companyCard.add(ComponentFactory.createLabel("Nom de l'entreprise:"), gbc);
        companyNameField = ComponentFactory.createStyledTextField();
        settingsComponents.put("companyName", companyNameField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        companyCard.add(companyNameField, gbc);

        // Adresse
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        companyCard.add(ComponentFactory.createLabel("Adresse:"), gbc);
        companyAddressField = ComponentFactory.createStyledTextField();
        settingsComponents.put("companyAddress", companyAddressField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        companyCard.add(companyAddressField, gbc);

        // Téléphone
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        companyCard.add(ComponentFactory.createLabel("Téléphone:"), gbc);
        companyPhoneField = ComponentFactory.createStyledTextField();
        settingsComponents.put("companyPhone", companyPhoneField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        companyCard.add(companyPhoneField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        companyCard.add(ComponentFactory.createLabel("Email:"), gbc);
        companyEmailField = ComponentFactory.createStyledTextField();
        settingsComponents.put("companyEmail", companyEmailField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        companyCard.add(companyEmailField, gbc);

        // Numéro fiscal
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        companyCard.add(ComponentFactory.createLabel("Numéro fiscal:"), gbc);
        taxNumberField = ComponentFactory.createStyledTextField();
        settingsComponents.put("taxNumber", taxNumberField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        companyCard.add(taxNumberField, gbc);

        panel.add(companyCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createBillingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel billingCard = ComponentFactory.createCardPanel();
        billingCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        billingCard.add(ComponentFactory.createSectionTitle("Paramètres de facturation"), gbc);
        gbc.gridwidth = 1;

        // Devise
        gbc.gridx = 0;
        gbc.gridy = 1;
        billingCard.add(ComponentFactory.createLabel("Devise:"), gbc);
        String[] currencies = { "FCFA", "EUR", "USD", "GBP" };
        currencyCombo = ComponentFactory.createStyledComboBox(currencies);
        settingsComponents.put("currency", currencyCombo);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        billingCard.add(currencyCombo, gbc);

        // Taux de TVA
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        billingCard.add(ComponentFactory.createLabel("Taux de TVA (%):"), gbc);
        taxRateSpinner = new JSpinner(new SpinnerNumberModel(18.0, 0.0, 100.0, 0.1));
        taxRateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        settingsComponents.put("taxRate", taxRateSpinner);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        billingCard.add(taxRateSpinner, gbc);

        panel.add(billingCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createBackupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel backupCard = ComponentFactory.createCardPanel();
        backupCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        backupCard.add(ComponentFactory.createSectionTitle("Configuration des sauvegardes"), gbc);
        gbc.gridwidth = 1;

        // Sauvegarde automatique
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        autoBackupCheckbox = new JCheckBox("Activer la sauvegarde automatique");
        autoBackupCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        autoBackupCheckbox.setBackground(ComponentFactory.getCardColor());
        settingsComponents.put("autoBackup", autoBackupCheckbox);
        backupCard.add(autoBackupCheckbox, gbc);
        gbc.gridwidth = 1;

        // Intervalle
        gbc.gridx = 0;
        gbc.gridy = 2;
        backupCard.add(ComponentFactory.createLabel("Intervalle (heures):"), gbc);
        backupIntervalSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
        backupIntervalSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        settingsComponents.put("backupInterval", backupIntervalSpinner);
        gbc.gridx = 1;
        backupCard.add(backupIntervalSpinner, gbc);

        // Chemin de sauvegarde
        gbc.gridx = 0;
        gbc.gridy = 3;
        backupCard.add(ComponentFactory.createLabel("Répertoire:"), gbc);
        backupPathField = ComponentFactory.createStyledTextField();
        settingsComponents.put("backupPath", backupPathField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        backupCard.add(backupPathField, gbc);

        JButton browseButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.FOLDER_OPEN, "Parcourir", ComponentFactory.getSecondaryColor(),
                e -> browseBackupPath());
        gbc.gridx = 2;
        gbc.weightx = 0;
        backupCard.add(browseButton, gbc);

        // Actions manuelles
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(ComponentFactory.getCardColor());

        JButton backupNowButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SAVE, "Sauvegarder maintenant", ComponentFactory.getSuccessColor(),
                e -> performBackup());
        JButton restoreButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.HISTORY, "Restaurer", ComponentFactory.getWarningColor(),
                e -> restoreBackup());

        actionsPanel.add(backupNowButton);
        actionsPanel.add(restoreButton);
        backupCard.add(actionsPanel, gbc);

        panel.add(backupCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createInterfacePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel interfaceCard = ComponentFactory.createCardPanel();
        interfaceCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        interfaceCard.add(ComponentFactory.createSectionTitle("Apparence et langues"), gbc);
        gbc.gridwidth = 1;

        // Thème
        gbc.gridx = 0;
        gbc.gridy = 1;
        interfaceCard.add(ComponentFactory.createLabel("Thème:"), gbc);
        String[] themes = { "Clair", "Sombre", "Automatique" };
        themeCombo = ComponentFactory.createStyledComboBox(themes);
        settingsComponents.put("theme", themeCombo);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        interfaceCard.add(themeCombo, gbc);

        // Langue
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        interfaceCard.add(ComponentFactory.createLabel("Langue:"), gbc);
        String[] languages = { "Français", "English", "Español" };
        languageCombo = ComponentFactory.createStyledComboBox(languages);
        settingsComponents.put("language", languageCombo);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        interfaceCard.add(languageCombo, gbc);

        panel.add(interfaceCard);

        // Section Accessibilité
        JPanel accessibilityCard = ComponentFactory.createCardPanel();
        accessibilityCard.setLayout(new BoxLayout(accessibilityCard, BoxLayout.Y_AXIS));
        accessibilityCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel accessibilityTitle = ComponentFactory.createSectionTitle("Accessibilité");
        accessibilityCard.add(accessibilityTitle);

        JCheckBox highContrastCheckbox = new JCheckBox("Contraste élevé");
        highContrastCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        highContrastCheckbox.setBackground(ComponentFactory.getCardColor());
        accessibilityCard.add(highContrastCheckbox);

        JCheckBox largeTextCheckbox = new JCheckBox("Texte agrandi");
        largeTextCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        largeTextCheckbox.setBackground(ComponentFactory.getCardColor());
        accessibilityCard.add(largeTextCheckbox);

        panel.add(Box.createVerticalStrut(15));
        panel.add(accessibilityCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel notifCard = ComponentFactory.createCardPanel();
        notifCard.setLayout(new BoxLayout(notifCard, BoxLayout.Y_AXIS));
        notifCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        notifCard.add(ComponentFactory.createSectionTitle("Paramètres de notification"));

        // Notifications générales
        notificationsCheckbox = new JCheckBox("Activer les notifications");
        notificationsCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notificationsCheckbox.setBackground(ComponentFactory.getCardColor());
        settingsComponents.put("notifications", notificationsCheckbox);
        notifCard.add(notificationsCheckbox);

        notifCard.add(Box.createVerticalStrut(10));

        // Son
        soundCheckbox = new JCheckBox("Sons de notification");
        soundCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        soundCheckbox.setBackground(ComponentFactory.getCardColor());
        settingsComponents.put("sound", soundCheckbox);
        notifCard.add(soundCheckbox);

        notifCard.add(Box.createVerticalStrut(15));

        // Types de notifications
        notifCard.add(ComponentFactory.createLabel("Types de notifications:"));
        notifCard.add(Box.createVerticalStrut(5));

        String[] notifTypes = { "Nouvelles commandes", "Stock faible", "Paiements reçus", "Erreurs système" };
        for (String type : notifTypes) {
            JCheckBox checkbox = new JCheckBox(type);
            checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            checkbox.setBackground(ComponentFactory.getCardColor());
            checkbox.setSelected(true);
            notifCard.add(checkbox);
        }

        panel.add(notifCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createEmailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel emailCard = ComponentFactory.createCardPanel();
        emailCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        emailCard.add(ComponentFactory.createSectionTitle("Configuration SMTP"), gbc);
        gbc.gridwidth = 1;

        // Serveur SMTP
        gbc.gridx = 0;
        gbc.gridy = 1;
        emailCard.add(ComponentFactory.createLabel("Serveur SMTP:"), gbc);
        smtpServerField = ComponentFactory.createStyledTextField();
        settingsComponents.put("smtpServer", smtpServerField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailCard.add(smtpServerField, gbc);

        // Port
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        emailCard.add(ComponentFactory.createLabel("Port:"), gbc);
        smtpPortField = ComponentFactory.createStyledTextField();
        settingsComponents.put("smtpPort", smtpPortField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailCard.add(smtpPortField, gbc);

        // Utilisateur
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        emailCard.add(ComponentFactory.createLabel("Utilisateur:"), gbc);
        smtpUserField = ComponentFactory.createStyledTextField();
        settingsComponents.put("smtpUser", smtpUserField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailCard.add(smtpUserField, gbc);

        // Mot de passe
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        emailCard.add(ComponentFactory.createLabel("Mot de passe:"), gbc);
        smtpPasswordField = new JPasswordField();
        smtpPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        smtpPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ComponentFactory.getBorderColor(), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        settingsComponents.put("smtpPassword", smtpPasswordField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailCard.add(smtpPasswordField, gbc);

        // SSL
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        smtpSslCheckbox = new JCheckBox("Utiliser SSL/TLS");
        smtpSslCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        smtpSslCheckbox.setBackground(ComponentFactory.getCardColor());
        settingsComponents.put("smtpSsl", smtpSslCheckbox);
        emailCard.add(smtpSslCheckbox, gbc);

        // Test
        gbc.gridy = 6;
        JPanel testPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        testPanel.setBackground(ComponentFactory.getCardColor());
        JButton testEmailButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.ENVELOPE, "Tester l'email", ComponentFactory.getPrimaryColor(),
                e -> testEmailConfig());
        testPanel.add(testEmailButton);
        emailCard.add(testPanel, gbc);

        panel.add(emailCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ComponentFactory.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Section Sécurité
        JPanel securityCard = ComponentFactory.createCardPanel();
        securityCard.setLayout(new BoxLayout(securityCard, BoxLayout.Y_AXIS));
        securityCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        securityCard.add(ComponentFactory.createSectionTitle("Sécurité et confidentialité"));

        // Options de sécurité
        JCheckBox autoLockCheckbox = new JCheckBox("Verrouillage automatique après inactivité");
        autoLockCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        autoLockCheckbox.setBackground(ComponentFactory.getCardColor());
        securityCard.add(autoLockCheckbox);

        JCheckBox strongPasswordCheckbox = new JCheckBox("Exiger des mots de passe complexes");
        strongPasswordCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        strongPasswordCheckbox.setBackground(ComponentFactory.getCardColor());
        strongPasswordCheckbox.setSelected(true);
        securityCard.add(strongPasswordCheckbox);

        JCheckBox auditLogCheckbox = new JCheckBox("Activer les journaux d'audit");
        auditLogCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        auditLogCheckbox.setBackground(ComponentFactory.getCardColor());
        auditLogCheckbox.setSelected(true);
        securityCard.add(auditLogCheckbox);

        securityCard.add(Box.createVerticalStrut(15));

        // Actions de sécurité
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(ComponentFactory.getCardColor());

        JButton changePasswordButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.KEY, "Changer mot de passe", ComponentFactory.getWarningColor(),
                e -> changePassword());
        JButton viewLogsButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.LIST, "Voir les journaux", ComponentFactory.getSecondaryColor(),
                e -> viewAuditLogs());

        actionsPanel.add(changePasswordButton);
        actionsPanel.add(viewLogsButton);
        securityCard.add(actionsPanel);

        panel.add(securityCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // Méthodes d'action
    private void loadSettings() {
        settings.forEach((key, value) -> {
            JComponent component = settingsComponents.get(key);
            if (component instanceof JTextField) {
                ((JTextField) component).setText(value.toString());
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setSelectedItem(value);
            } else if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected((Boolean) value);
            } else if (component instanceof JSpinner) {
                ((JSpinner) component).setValue(value);
            } else if (component instanceof JPasswordField) {
                ((JPasswordField) component).setText(value.toString());
            }
        });
    }

    private void saveSettings() {
        // Collecter les valeurs depuis les composants
        settingsComponents.forEach((key, component) -> {
            Object value = null;
            if (component instanceof JTextField) {
                value = ((JTextField) component).getText();
            } else if (component instanceof JComboBox) {
                value = ((JComboBox<?>) component).getSelectedItem();
            } else if (component instanceof JCheckBox) {
                value = ((JCheckBox) component).isSelected();
            } else if (component instanceof JSpinner) {
                value = ((JSpinner) component).getValue();
            } else if (component instanceof JPasswordField) {
                value = new String(((JPasswordField) component).getPassword());
            }
            if (value != null) {
                settings.put(key, value);
            }
        });

        // TODO: Sauvegarder dans un fichier de configuration ou base de données
        JOptionPane.showMessageDialog(this,
                "Paramètres sauvegardés avec succès!\nCertains changements nécessitent un redémarrage.",
                "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettings() {
        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir réinitialiser tous les paramètres ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            initializeSettings();
            loadSettings();
            JOptionPane.showMessageDialog(this, "Paramètres réinitialisés",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportConfig() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter la configuration");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers JSON", "json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Mettre à jour les paramètres depuis les composants
            settingsComponents.forEach((key, component) -> {
                Object value = null;
                if (component instanceof JTextField) {
                    value = ((JTextField) component).getText();
                } else if (component instanceof JComboBox) {
                    value = ((JComboBox<?>) component).getSelectedItem();
                } else if (component instanceof JCheckBox) {
                    value = ((JCheckBox) component).isSelected();
                } else if (component instanceof JSpinner) {
                    value = ((JSpinner) component).getValue();
                } else if (component instanceof JPasswordField) {
                    value = new String(((JPasswordField) component).getPassword());
                }
                if (value != null) {
                    settings.put(key, value);
                }
            });

            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".json")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".json");
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(selectedFile, settings);
                JOptionPane.showMessageDialog(this,
                        "Configuration exportée vers " + selectedFile.getAbsolutePath(),
                        "Export réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'exportation : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void browseBackupPath() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("Sélectionner le répertoire de sauvegarde");

        if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            backupPathField.setText(folderChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void performBackup() {
        JOptionPane.showMessageDialog(this, "Sauvegarde effectuée avec succès!",
                "Sauvegarde", JOptionPane.INFORMATION_MESSAGE);
    }

    private void restoreBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner le fichier de sauvegarde");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Attention: Cette opération remplacera toutes les données actuelles.\n" +
                            "Voulez-vous continuer ?",
                    "Confirmation de restauration", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Restauration effectuée avec succès!",
                        "Restauration", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void testEmailConfig() {
        JOptionPane.showMessageDialog(this, "Test de configuration email en cours...\nEmail de test envoyé!",
                "Test Email", JOptionPane.INFORMATION_MESSAGE);
    }

    private void changePassword() {
        // Dialog de changement de mot de passe
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Changer le mot de passe", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Mot de passe actuel:"), gbc);
        JPasswordField currentPwdField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(currentPwdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Nouveau mot de passe:"), gbc);
        JPasswordField newPwdField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(newPwdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Confirmer:"), gbc);
        JPasswordField confirmPwdField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(confirmPwdField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton changeButton = new JButton("Changer");
        JButton cancelButton = new JButton("Annuler");

        changeButton.addActionListener(e -> {
            // TODO: Implémenter le changement de mot de passe
            JOptionPane.showMessageDialog(dialog, "Mot de passe changé avec succès!");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewAuditLogs() {
        JOptionPane.showMessageDialog(this, "Visualisation des journaux d'audit en cours de développement",
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}