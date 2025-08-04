package com.longrich.smartgestion.ui.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
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

import com.longrich.smartgestion.entity.User;
import com.longrich.smartgestion.enums.UserRole;
import com.longrich.smartgestion.ui.components.ButtonFactory;
import com.longrich.smartgestion.ui.components.ComponentFactory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class UserPanel extends JPanel {

    // Composants UI
    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JLabel totalUsersLabel;
    private JLabel activeUsersLabel;
    private JLabel adminsLabel;

    // Données simulées
    private List<User> users;
    private User selectedUser;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ComponentFactory.getBackgroundColor());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeData();
        createHeaderPanel();
        createMainContent();
        loadUsersData();
        updateStatistics();
    }

    private void initializeData() {
        users = new ArrayList<>();
        
        // Données simulées pour démonstration
        User admin = User.builder()
                .username("admin")
                .email("admin@smartgestion.bf")
                .nom("Administrateur")
                .prenom("Système")
                .telephone("+226 70 00 00 01")
                .role(UserRole.ADMIN)
                .active(true)
                .lastLogin(LocalDateTime.now().minusHours(2))
                .build();
        admin.setId(1L);
        users.add(admin);

        User user1 = User.builder()
                .username("marie.traore")
                .email("marie.traore@smartgestion.bf")
                .nom("Traoré")
                .prenom("Marie")
                .telephone("+226 70 12 34 56")
                .role(UserRole.USER)
                .active(true)
                .lastLogin(LocalDateTime.now().minusMinutes(30))
                .build();
        user1.setId(2L);
        users.add(user1);

        User user2 = User.builder()
                .username("ibrahim.sankara")
                .email("ibrahim.sankara@smartgestion.bf")
                .nom("Sankara")
                .prenom("Ibrahim")
                .telephone("+226 70 98 76 54")
                .role(UserRole.USER)
                .active(false)
                .lastLogin(LocalDateTime.now().minusDays(5))
                .build();
        user2.setId(3L);
        users.add(user2);

        User user3 = User.builder()
                .username("fatou.ouedraogo")
                .email("fatou.ouedraogo@smartgestion.bf")
                .nom("Ouédraogo")
                .prenom("Fatou")
                .telephone("+226 70 11 22 33")
                .role(UserRole.USER)
                .active(true)
                .lastLogin(LocalDateTime.now().minusHours(1))
                .build();
        user3.setId(4L);
        users.add(user3);
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ComponentFactory.getBackgroundColor());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre et statistiques
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(ComponentFactory.getBackgroundColor());

        JLabel titleLabel = new JLabel("Gestion des Utilisateurs");
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

        JButton addUserButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.USER_PLUS, "Nouvel utilisateur", ComponentFactory.getSuccessColor(), 
                e -> showUserDialog(null));
        JButton refreshButton = ButtonFactory.createActionButton(
                FontAwesomeSolid.SYNC_ALT, "Actualiser", ComponentFactory.getSecondaryColor(), 
                e -> refreshUsersData());

        panel.add(addUserButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(ComponentFactory.getBackgroundColor());

        // Statistiques
        mainPanel.add(createStatisticsPanel(), BorderLayout.NORTH);

        // Contenu principal avec filtres et table
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ComponentFactory.getBackgroundColor());
        
        // Filtres
        contentPanel.add(createFiltersPanel(), BorderLayout.NORTH);
        
        // Table des utilisateurs
        contentPanel.add(createUsersTablePanel(), BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(ComponentFactory.getBackgroundColor());

        // Total utilisateurs
        JPanel totalCard = createStatCard("Total Utilisateurs", "0", ComponentFactory.getPrimaryColor(), FontAwesomeSolid.USERS);
        totalUsersLabel = (JLabel) ((JPanel) totalCard.getComponent(1)).getComponent(0);
        panel.add(totalCard);

        // Utilisateurs actifs
        JPanel activeCard = createStatCard("Actifs", "0", ComponentFactory.getSuccessColor(), FontAwesomeSolid.USER_CHECK);
        activeUsersLabel = (JLabel) ((JPanel) activeCard.getComponent(1)).getComponent(0);
        panel.add(activeCard);

        // Administrateurs
        JPanel adminsCard = createStatCard("Administrateurs", "0", ComponentFactory.getWarningColor(), FontAwesomeSolid.USER_SHIELD);
        adminsLabel = (JLabel) ((JPanel) adminsCard.getComponent(1)).getComponent(0);
        panel.add(adminsCard);

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
        
        searchField = ComponentFactory.createStyledTextField("Nom, prénom ou username...");
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.addActionListener(e -> filterUsers());
        gbc.gridx = 1;
        panel.add(ComponentFactory.createSearchField(searchField), gbc);

        // Filtre par rôle
        gbc.gridx = 2;
        panel.add(ComponentFactory.createLabel("Rôle:"), gbc);
        
        String[] roles = {"Tous", "Administrateur", "Utilisateur"};
        roleFilterCombo = ComponentFactory.createStyledComboBox(roles);
        roleFilterCombo.addActionListener(e -> filterUsers());
        gbc.gridx = 3;
        panel.add(roleFilterCombo, gbc);

        // Filtre par statut
        gbc.gridx = 4;
        panel.add(ComponentFactory.createLabel("Statut:"), gbc);
        
        String[] statuts = {"Tous", "Actif", "Inactif"};
        statusFilterCombo = ComponentFactory.createStyledComboBox(statuts);
        statusFilterCombo.addActionListener(e -> filterUsers());
        gbc.gridx = 5;
        panel.add(statusFilterCombo, gbc);

        return panel;
    }

    private JPanel createUsersTablePanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new BorderLayout());

        // En-tête
        JLabel tableTitle = ComponentFactory.createSectionTitle("Liste des Utilisateurs");
        panel.add(tableTitle, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Username", "Nom Complet", "Email", "Téléphone", "Rôle", "Dernière Connexion", "Statut", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Seulement la colonne Actions
            }
        };

        usersTable = new JTable(tableModel);
        styleTable(usersTable);
        
        // Renderer pour la colonne Actions
        usersTable.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        usersTable.getColumn("Actions").setCellEditor(new ActionButtonEditor());
        usersTable.getColumn("Actions").setMaxWidth(120);
        usersTable.getColumn("Actions").setMinWidth(120);
        
        // Ajuster les largeurs des colonnes
        usersTable.getColumn("ID").setMaxWidth(50);
        usersTable.getColumn("Username").setPreferredWidth(120);
        usersTable.getColumn("Nom Complet").setPreferredWidth(150);
        usersTable.getColumn("Email").setPreferredWidth(200);
        usersTable.getColumn("Téléphone").setPreferredWidth(120);
        usersTable.getColumn("Rôle").setMaxWidth(100);
        usersTable.getColumn("Statut").setMaxWidth(80);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(45);
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
                
                // Couleur spéciale pour le statut
                if (column == 7) { // Colonne Statut
                    if ("Actif".equals(value)) {
                        setForeground(ComponentFactory.getSuccessColor());
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Inactif".equals(value)) {
                        setForeground(ComponentFactory.getDangerColor());
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                
                return this;
            }
        };
        
        for (int i = 0; i < table.getColumnCount() - 1; i++) { // Exclure la colonne Actions
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    // Méthodes d'action
    private void loadUsersData() {
        tableModel.setRowCount(0);
        for (User user : users) {
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getPrenom() + " " + user.getNom(),
                user.getEmail(),
                user.getTelephone() != null ? user.getTelephone() : "-",
                user.getRole().getLibelle(),
                user.getLastLogin() != null ? 
                    user.getLastLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Jamais",
                user.getActive() ? "Actif" : "Inactif",
                "" // Colonne Actions
            };
            tableModel.addRow(row);
        }
    }

    private void filterUsers() {
        // TODO: Implémenter le filtrage réel
        loadUsersData();
    }

    private void updateStatistics() {
        long totalUsers = users.size();
        long activeUsers = users.stream().mapToLong(u -> u.getActive() ? 1 : 0).sum();
        long admins = users.stream().mapToLong(u -> u.getRole() == UserRole.ADMIN ? 1 : 0).sum();
        
        totalUsersLabel.setText(String.valueOf(totalUsers));
        activeUsersLabel.setText(String.valueOf(activeUsers));
        adminsLabel.setText(String.valueOf(admins));
    }

    private void refreshUsersData() {
        loadUsersData();
        updateStatistics();
        JOptionPane.showMessageDialog(this, "Données utilisateurs actualisées", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUserDialog(User user) {
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), user);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User resultUser = dialog.getUser();
            if (user == null) {
                // Nouvel utilisateur
                resultUser.setId((long) (users.size() + 1));
                users.add(resultUser);
            } else {
                // Modification
                int index = users.indexOf(user);
                if (index >= 0) {
                    users.set(index, resultUser);
                }
            }
            loadUsersData();
            updateStatistics();
        }
    }

    private void editUser(User user) {
        showUserDialog(user);
    }

    private void toggleUserStatus(User user) {
        String action = user.getActive() ? "désactiver" : "activer";
        int option = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment " + action + " l'utilisateur " + user.getUsername() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            user.setActive(!user.getActive());
            loadUsersData();
            updateStatistics();
            JOptionPane.showMessageDialog(this, 
                    "Utilisateur " + (user.getActive() ? "activé" : "désactivé") + " avec succès", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteUser(User user) {
        if (user.getRole() == UserRole.ADMIN && users.stream().filter(u -> u.getRole() == UserRole.ADMIN).count() == 1) {
            JOptionPane.showMessageDialog(this, 
                    "Impossible de supprimer le dernier administrateur du système", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer l'utilisateur " + user.getUsername() + " ?\n" +
                "Cette action est irréversible.",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            users.remove(user);
            loadUsersData();
            updateStatistics();
            JOptionPane.showMessageDialog(this, "Utilisateur supprimé avec succès", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Classes internes pour les actions dans le tableau
    private class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editButton;
        private JButton toggleButton;
        private JButton deleteButton;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            setOpaque(true);
            
            editButton = new JButton(org.kordamp.ikonli.swing.FontIcon.of(FontAwesomeSolid.EDIT, 12));
            editButton.setPreferredSize(new Dimension(30, 30));
            editButton.setToolTipText("Modifier");
            
            toggleButton = new JButton(org.kordamp.ikonli.swing.FontIcon.of(FontAwesomeSolid.POWER_OFF, 12));
            toggleButton.setPreferredSize(new Dimension(30, 30));
            
            deleteButton = new JButton(org.kordamp.ikonli.swing.FontIcon.of(FontAwesomeSolid.TRASH, 12));
            deleteButton.setPreferredSize(new Dimension(30, 30));
            deleteButton.setToolTipText("Supprimer");
            
            add(editButton);
            add(toggleButton);
            add(deleteButton);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (row < users.size()) {
                User user = users.get(row);
                toggleButton.setIcon(org.kordamp.ikonli.swing.FontIcon.of(
                    user.getActive() ? FontAwesomeSolid.POWER_OFF : FontAwesomeSolid.PLAY, 12));
                toggleButton.setToolTipText(user.getActive() ? "Désactiver" : "Activer");
            }
            
            setBackground(isSelected ? table.getSelectionBackground() : 
                         (row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250)));
            return this;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editButton;
        private JButton toggleButton;
        private JButton deleteButton;
        private int currentRow;

        public ActionButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            
            editButton = new JButton(org.kordamp.ikonli.swing.FontIcon.of(FontAwesomeSolid.EDIT, 12));
            editButton.setPreferredSize(new Dimension(30, 30));
            editButton.addActionListener(this::editAction);
            
            toggleButton = new JButton();
            toggleButton.setPreferredSize(new Dimension(30, 30));
            toggleButton.addActionListener(this::toggleAction);
            
            deleteButton = new JButton(org.kordamp.ikonli.swing.FontIcon.of(FontAwesomeSolid.TRASH, 12));
            deleteButton.setPreferredSize(new Dimension(30, 30));
            deleteButton.addActionListener(this::deleteAction);
            
            panel.add(editButton);
            panel.add(toggleButton);
            panel.add(deleteButton);
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            if (row < users.size()) {
                User user = users.get(row);
                toggleButton.setIcon(org.kordamp.ikonli.swing.FontIcon.of(
                    user.getActive() ? FontAwesomeSolid.POWER_OFF : FontAwesomeSolid.PLAY, 12));
            }
            
            return panel;
        }

        private void editAction(ActionEvent e) {
            fireEditingStopped();
            if (currentRow < users.size()) {
                editUser(users.get(currentRow));
            }
        }

        private void toggleAction(ActionEvent e) {
            fireEditingStopped();
            if (currentRow < users.size()) {
                toggleUserStatus(users.get(currentRow));
            }
        }

        private void deleteAction(ActionEvent e) {
            fireEditingStopped();
            if (currentRow < users.size()) {
                deleteUser(users.get(currentRow));
            }
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // Dialog pour créer/modifier un utilisateur
    private class UserDialog extends JDialog {
        private ComponentFactory.FieldPanel usernamePanel;
        private ComponentFactory.FieldPanel emailPanel;
        private ComponentFactory.FieldPanel nomPanel;
        private ComponentFactory.FieldPanel prenomPanel;
        private ComponentFactory.FieldPanel telephonePanel;
        private JPasswordField passwordField;
        private JComboBox<UserRole> roleCombo;
        private JCheckBox activeCheckbox;
        private boolean confirmed = false;
        private User user;
        private boolean isEditing;

        public UserDialog(Frame parent, User user) {
            super(parent, user == null ? "Nouvel Utilisateur" : "Modifier Utilisateur", true);
            this.user = user;
            this.isEditing = user != null;
            
            initializeDialog();
            if (isEditing) {
                loadUserData();
            }
        }

        private void initializeDialog() {
            setLayout(new BorderLayout());
            setSize(500, 600);
            setLocationRelativeTo(getParent());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // Formulaire
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            formPanel.setBackground(ComponentFactory.getBackgroundColor());

            // Username
            usernamePanel = ComponentFactory.createFieldPanel("Nom d'utilisateur:", 
                ComponentFactory.createStyledTextField());
            formPanel.add(usernamePanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Email
            emailPanel = ComponentFactory.createFieldPanel("Email:", 
                ComponentFactory.createStyledTextField());
            formPanel.add(emailPanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Nom
            nomPanel = ComponentFactory.createFieldPanel("Nom:", 
                ComponentFactory.createStyledTextField());
            formPanel.add(nomPanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Prénom
            prenomPanel = ComponentFactory.createFieldPanel("Prénom:", 
                ComponentFactory.createStyledTextField());
            formPanel.add(prenomPanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Téléphone
            telephonePanel = ComponentFactory.createFieldPanel("Téléphone:", 
                ComponentFactory.createStyledTextField());
            formPanel.add(telephonePanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Mot de passe
            JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            passwordPanel.setBackground(ComponentFactory.getBackgroundColor());
            passwordPanel.add(ComponentFactory.createLabel("Mot de passe:"));
            passwordField = new JPasswordField(20);
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ComponentFactory.getBorderColor(), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            passwordPanel.add(passwordField);
            formPanel.add(passwordPanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Rôle
            JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            rolePanel.setBackground(ComponentFactory.getBackgroundColor());
            rolePanel.add(ComponentFactory.createLabel("Rôle:"));
            roleCombo = new JComboBox<>(UserRole.values());
            roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            rolePanel.add(roleCombo);
            formPanel.add(rolePanel);
            formPanel.add(Box.createVerticalStrut(15));

            // Actif
            activeCheckbox = new JCheckBox("Compte actif");
            activeCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            activeCheckbox.setBackground(ComponentFactory.getBackgroundColor());
            activeCheckbox.setSelected(true);
            formPanel.add(activeCheckbox);

            add(formPanel, BorderLayout.CENTER);

            // Boutons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(ComponentFactory.getBackgroundColor());
            
            JButton saveButton = ButtonFactory.createActionButton(
                    FontAwesomeSolid.SAVE, "Sauvegarder", ComponentFactory.getSuccessColor(), 
                    e -> saveUser());
            JButton cancelButton = ButtonFactory.createActionButton(
                    FontAwesomeSolid.TIMES, "Annuler", ComponentFactory.getDangerColor(), 
                    e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void loadUserData() {
            ((JTextField) usernamePanel.getField()).setText(user.getUsername());
            ((JTextField) emailPanel.getField()).setText(user.getEmail());
            ((JTextField) nomPanel.getField()).setText(user.getNom());
            ((JTextField) prenomPanel.getField()).setText(user.getPrenom());
            ((JTextField) telephonePanel.getField()).setText(user.getTelephone() != null ? user.getTelephone() : "");
            roleCombo.setSelectedItem(user.getRole());
            activeCheckbox.setSelected(user.getActive());
        }

        private void saveUser() {
            // Validation
            if (!validateForm()) {
                return;
            }

            // Créer ou mettre à jour l'utilisateur
            if (user == null) {
                user = new User();
            }

            user.setUsername(((JTextField) usernamePanel.getField()).getText().trim());
            user.setEmail(((JTextField) emailPanel.getField()).getText().trim());
            user.setNom(((JTextField) nomPanel.getField()).getText().trim());
            user.setPrenom(((JTextField) prenomPanel.getField()).getText().trim());
            user.setTelephone(((JTextField) telephonePanel.getField()).getText().trim());
            user.setRole((UserRole) roleCombo.getSelectedItem());
            user.setActive(activeCheckbox.isSelected());
            
            String password = new String(passwordField.getPassword());
            if (!password.trim().isEmpty()) {
                user.setPassword(password); // TODO: Hasher le mot de passe
            }

            confirmed = true;
            dispose();
        }

        private boolean validateForm() {
            boolean valid = true;

            // Reset des erreurs
            usernamePanel.clearError();
            emailPanel.clearError();
            nomPanel.clearError();
            prenomPanel.clearError();

            String username = ((JTextField) usernamePanel.getField()).getText().trim();
            if (username.isEmpty()) {
                usernamePanel.setError("Le nom d'utilisateur est obligatoire");
                valid = false;
            } else if (username.length() < 3) {
                usernamePanel.setError("Le nom d'utilisateur doit contenir au moins 3 caractères");
                valid = false;
            }

            String email = ((JTextField) emailPanel.getField()).getText().trim();
            if (email.isEmpty()) {
                emailPanel.setError("L'email est obligatoire");
                valid = false;
            } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailPanel.setError("Format d'email invalide");
                valid = false;
            }

            String nom = ((JTextField) nomPanel.getField()).getText().trim();
            if (nom.isEmpty()) {
                nomPanel.setError("Le nom est obligatoire");
                valid = false;
            }

            String prenom = ((JTextField) prenomPanel.getField()).getText().trim();
            if (prenom.isEmpty()) {
                prenomPanel.setError("Le prénom est obligatoire");
                valid = false;
            }

            String password = new String(passwordField.getPassword());
            if (!isEditing && password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le mot de passe est obligatoire pour un nouvel utilisateur", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                valid = false;
            } else if (!password.trim().isEmpty() && password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Le mot de passe doit contenir au moins 6 caractères", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                valid = false;
            }

            return valid;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public User getUser() {
            return user;
        }
    }
}