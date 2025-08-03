package com.longrich.smartgestion.ui.components;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class Navbar extends JPanel {

    private JLabel titleLabel;
    private JLabel dateTimeLabel;
    private JLabel userLabel;
    private Timer timer;

    public Navbar() {
        initializeUI();
        startClock();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 60));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        createLeftSection();
        createCenterSection();
        createRightSection();
    }

    private void createLeftSection() {
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(248, 249, 250));

        titleLabel = new JLabel("Tableau de bord");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));
        leftPanel.add(titleLabel);

        add(leftPanel, BorderLayout.WEST);
    }

    private void createCenterSection() {
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.setBackground(new Color(248, 249, 250));

        // Barre de recherche
        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton searchButton = new JButton("🔍");
        searchButton.setPreferredSize(new Dimension(35, 35));
        searchButton.setBackground(new Color(0, 123, 255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder());
        searchButton.setFocusPainted(false);

        centerPanel.add(searchField);
        centerPanel.add(searchButton);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void createRightSection() {
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(248, 249, 250));

        // Date et heure
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeLabel.setForeground(new Color(108, 117, 125));
        rightPanel.add(dateTimeLabel);

        // Séparateur
        rightPanel.add(new JLabel(" | "));

        // Notifications
        JButton notificationButton = new JButton("🔔");
        notificationButton.setPreferredSize(new Dimension(35, 35));
        notificationButton.setBackground(new Color(248, 249, 250));
        notificationButton.setBorder(BorderFactory.createEmptyBorder());
        notificationButton.setFocusPainted(false);
        rightPanel.add(notificationButton);

        // Utilisateur
        userLabel = new JLabel("👤 Admin");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(33, 37, 41));
        rightPanel.add(userLabel);

        // Menu utilisateur
        JButton userMenuButton = new JButton("▼");
        userMenuButton.setPreferredSize(new Dimension(20, 35));
        userMenuButton.setBackground(new Color(248, 249, 250));
        userMenuButton.setBorder(BorderFactory.createEmptyBorder());
        userMenuButton.setFocusPainted(false);
        rightPanel.add(userMenuButton);

        add(rightPanel, BorderLayout.EAST);
    }

    private void startClock() {
        timer = new Timer(1000, e -> updateDateTime());
        timer.start();
        updateDateTime(); // Mise à jour initiale
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        dateTimeLabel.setText(formattedDateTime);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setUser(String username) {
        userLabel.setText("👤 " + username);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timer != null) {
            timer.stop();
        }
    }
}