package com.longrich.smartgestion.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("!headless")
public class Navbar extends JPanel {

    private static final Color NAVBAR_BG = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    // private static final Color ACCENT_COLOR = new Color(59, 130, 246);
    private static final Color BUTTON_HOVER = new Color(243, 244, 246);

    private JLabel titleLabel;
    private JLabel dateTimeLabel;
    private JLabel userLabel;
    private Timer timer;

    public Navbar() {
        // Constructeur vide pour Spring
    }

    @PostConstruct
    public void init() {
        initializeUI();
        startClock();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 70));
        setBackground(NAVBAR_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        createRightSection();
    }

    private void createRightSection() {
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(NAVBAR_BG);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        // Date et heure
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateTimeLabel.setForeground(TEXT_SECONDARY);
        rightPanel.add(dateTimeLabel);

        // Séparateur
        JLabel separator = new JLabel("•");
        separator.setForeground(new Color(209, 213, 219));
        separator.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rightPanel.add(separator);

        // Notifications
        JButton notificationButton = createIconButton(FontAwesomeSolid.BELL, "Notifications");
        addNotificationBadge(notificationButton, 3);
        rightPanel.add(notificationButton);

        // Messages
        JButton messageButton = createIconButton(FontAwesomeSolid.ENVELOPE, "Messages");
        addNotificationBadge(messageButton, 1);
        rightPanel.add(messageButton);

        // Profil utilisateur
        JPanel userPanel = createUserPanel();
        rightPanel.add(userPanel);

        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createIconButton(FontAwesomeSolid icon, String tooltip) {
        FontIcon fontIcon = FontIcon.of(icon, 18, TEXT_SECONDARY);
        JButton button = new JButton(fontIcon);
        button.setPreferredSize(new Dimension(40, 40));
        button.setBackground(NAVBAR_BG);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);

        // Effet hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
                FontIcon hoverIcon = FontIcon.of(icon, 18, TEXT_PRIMARY);
                button.setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(NAVBAR_BG);
                FontIcon normalIcon = FontIcon.of(icon, 18, TEXT_SECONDARY);
                button.setIcon(normalIcon);
            }
        });

        return button;
    }

    private void addNotificationBadge(JButton button, int count) {
        if (count > 0) {
            button.addPropertyChangeListener("icon", evt -> {
                // Ajouter un badge de notification
                SwingUtilities.invokeLater(() -> {
                    Graphics g = button.getGraphics();
                    if (g != null) {
                        g.setColor(new Color(239, 68, 68));
                        g.fillOval(28, 8, 12, 12);
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Segoe UI", Font.BOLD, 8));
                        g.drawString(String.valueOf(count), 32, 16);
                    }
                });
            });
        }
    }

    private JPanel createUserPanel() {
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(NAVBAR_BG);
        userPanel.setPreferredSize(new Dimension(180, 40));
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        userPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Avatar
        JLabel avatarLabel = new JLabel();
        FontIcon userIcon = FontIcon.of(FontAwesomeSolid.USER_CIRCLE, 32, new Color(156, 163, 175));
        avatarLabel.setIcon(userIcon);
        userPanel.add(avatarLabel, BorderLayout.WEST);

        // Informations utilisateur
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setBackground(NAVBAR_BG);
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        userLabel = new JLabel("John Doe");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(TEXT_PRIMARY);
        userInfoPanel.add(userLabel, BorderLayout.NORTH);

        JLabel roleLabel = new JLabel("Administrateur");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(TEXT_SECONDARY);
        userInfoPanel.add(roleLabel, BorderLayout.SOUTH);

        userPanel.add(userInfoPanel, BorderLayout.CENTER);

        // Flèche dropdown
        FontIcon dropdownIcon = FontIcon.of(FontAwesomeSolid.CHEVRON_DOWN, 12, TEXT_SECONDARY);
        JLabel dropdownLabel = new JLabel(dropdownIcon);
        userPanel.add(dropdownLabel, BorderLayout.EAST);

        // Effet hover
        userPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                userPanel.setBackground(BUTTON_HOVER);
                userInfoPanel.setBackground(BUTTON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                userPanel.setBackground(NAVBAR_BG);
                userInfoPanel.setBackground(NAVBAR_BG);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showUserMenu(e);
            }
        });

        return userPanel;
    }

    private void showUserMenu(MouseEvent e) {
        JPopupMenu userMenu = new JPopupMenu();
        userMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        JMenuItem profileItem = createMenuItem("Mon profil", FontAwesomeSolid.USER);
        JMenuItem settingsItem = createMenuItem("Paramètres", FontAwesomeSolid.COG);
        JMenuItem helpItem = createMenuItem("Aide", FontAwesomeSolid.QUESTION_CIRCLE);
        userMenu.addSeparator();
        JMenuItem logoutItem = createMenuItem("Déconnexion", FontAwesomeSolid.SIGN_OUT_ALT);

        userMenu.add(profileItem);
        userMenu.add(settingsItem);
        userMenu.add(helpItem);
        userMenu.add(logoutItem);

        userMenu.show(e.getComponent(), e.getX(), e.getY() + 10);
    }

    private JMenuItem createMenuItem(String text, FontAwesomeSolid icon) {
        JMenuItem item = new JMenuItem(text);
        item.setIcon(FontIcon.of(icon, 14, TEXT_SECONDARY));
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return item;
    }

    private void startClock() {
        timer = new Timer(1000, e -> updateDateTime());
        timer.start();
        updateDateTime();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        dateTimeLabel.setText(formattedDateTime);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setUser(String username, String role) {
        userLabel.setText(username);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timer != null) {
            timer.stop();
        }
    }

}