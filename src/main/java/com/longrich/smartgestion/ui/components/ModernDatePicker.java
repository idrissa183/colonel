package com.longrich.smartgestion.ui.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.Border;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Composant DatePicker moderne avec calendrier popup
 */
public class ModernDatePicker extends JPanel {

    // Couleurs modernes
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color HOVER_COLOR = new Color(243, 244, 246);

    private JTextField dateField;
    private JButton calendarButton;
    private JPopupMenu calendarPopup;
    private LocalDate selectedDate;
    private LocalDate currentDisplayMonth;
    private DateTimeFormatter formatter;

    public ModernDatePicker() {
        this(LocalDate.now());
    }

    public ModernDatePicker(LocalDate initialDate) {
        this.selectedDate = initialDate;
        this.currentDisplayMonth = initialDate != null ? initialDate : LocalDate.now();
        this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(CARD_COLOR);

        // Champ de texte pour afficher la date
        dateField = new JTextField();
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateField.setBackground(Color.WHITE);
        dateField.setForeground(TEXT_PRIMARY);
        dateField.setPreferredSize(new Dimension(120, 36));
        dateField.setEditable(false);
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 35)));

        // Bouton calendrier
        calendarButton = new JButton(FontIcon.of(FontAwesomeSolid.CALENDAR_ALT, 14, TEXT_SECONDARY));
        calendarButton.setPreferredSize(new Dimension(30, 36));
        calendarButton.setBackground(Color.WHITE);
        calendarButton.setBorder(BorderFactory.createEmptyBorder());
        calendarButton.setFocusPainted(false);
        calendarButton.setContentAreaFilled(false);
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Popup du calendrier
        calendarPopup = new JPopupMenu();
        calendarPopup.setBackground(CARD_COLOR);
        calendarPopup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        updateDateDisplay();
    }

    private void layoutComponents() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new OverlayLayout(inputPanel));
        inputPanel.setBackground(Color.WHITE);

        // Position du bouton calendrier
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(calendarButton);

        inputPanel.add(buttonPanel);
        inputPanel.add(dateField);

        add(inputPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        calendarButton.addActionListener(e -> showCalendar());
        
        // Permettre le clic sur le champ de texte aussi
        dateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCalendar();
            }
        });
    }

    private void showCalendar() {
        calendarPopup.removeAll();
        calendarPopup.add(createCalendarPanel());
        calendarPopup.pack();
        
        // Positionner le popup
        // Point location = dateField.getLocationOnScreen();
        calendarPopup.show(this, 0, getHeight());
    }

    private JPanel createCalendarPanel() {
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(CARD_COLOR);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        calendarPanel.setPreferredSize(new Dimension(280, 250));

        // En-tête avec navigation
        JPanel headerPanel = createCalendarHeader();
        calendarPanel.add(headerPanel, BorderLayout.NORTH);

        // Jours de la semaine
        JPanel daysOfWeekPanel = createDaysOfWeekPanel();
        calendarPanel.add(daysOfWeekPanel, BorderLayout.CENTER);

        // Grille des jours
        JPanel daysPanel = createDaysPanel();
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(CARD_COLOR);
        centerPanel.add(daysOfWeekPanel, BorderLayout.NORTH);
        centerPanel.add(daysPanel, BorderLayout.CENTER);
        
        calendarPanel.add(centerPanel, BorderLayout.CENTER);

        // Boutons d'action
        JPanel actionPanel = createActionPanel();
        calendarPanel.add(actionPanel, BorderLayout.SOUTH);

        return calendarPanel;
    }

    private JPanel createCalendarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Bouton précédent
        JButton prevButton = new JButton(FontIcon.of(FontAwesomeSolid.CHEVRON_LEFT, 12, TEXT_SECONDARY));
        styleNavigationButton(prevButton);
        prevButton.addActionListener(e -> {
            currentDisplayMonth = currentDisplayMonth.minusMonths(1);
            showCalendar();
        });

        // Bouton suivant
        JButton nextButton = new JButton(FontIcon.of(FontAwesomeSolid.CHEVRON_RIGHT, 12, TEXT_SECONDARY));
        styleNavigationButton(nextButton);
        nextButton.addActionListener(e -> {
            currentDisplayMonth = currentDisplayMonth.plusMonths(1);
            showCalendar();
        });

        // Titre du mois/année
        String monthYear = currentDisplayMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE) 
                + " " + currentDisplayMonth.getYear();
        JLabel titleLabel = new JLabel(monthYear, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_PRIMARY);

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void styleNavigationButton(JButton button) {
        button.setPreferredSize(new Dimension(30, 30));
        button.setBackground(CARD_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
                button.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
            }
        });
    }

    private JPanel createDaysOfWeekPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 7));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        String[] daysOfWeek = {"Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            dayLabel.setForeground(TEXT_SECONDARY);
            panel.add(dayLabel);
        }

        return panel;
    }

    private JPanel createDaysPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 7, 2, 2));
        panel.setBackground(CARD_COLOR);

        YearMonth yearMonth = YearMonth.from(currentDisplayMonth);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        
        // Calculer le premier jour à afficher (peut être du mois précédent)
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Lundi
        LocalDate startDate = firstOfMonth.minusDays(dayOfWeek - 1);

        LocalDate currentDate = startDate;
        for (int i = 0; i < 42; i++) { // 6 semaines * 7 jours
            JButton dayButton = createDayButton(currentDate, yearMonth);
            panel.add(dayButton);
            currentDate = currentDate.plusDays(1);
        }

        return panel;
    }

    private JButton createDayButton(LocalDate date, YearMonth currentMonth) {
        JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(35, 30));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Style basé sur le contexte
        boolean isCurrentMonth = YearMonth.from(date).equals(currentMonth);
        boolean isSelected = date.equals(selectedDate);
        boolean isToday = date.equals(LocalDate.now());

        if (isSelected) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
        } else if (isToday) {
            button.setBackground(SUCCESS_COLOR);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
        } else if (isCurrentMonth) {
            button.setBackground(CARD_COLOR);
            button.setForeground(TEXT_PRIMARY);
            button.setOpaque(true);
        } else {
            button.setBackground(CARD_COLOR);
            button.setForeground(TEXT_SECONDARY.brighter());
            button.setOpaque(true);
        }

        // Effets hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) {
                    button.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) {
                    if (isToday) {
                        button.setBackground(SUCCESS_COLOR);
                    } else {
                        button.setBackground(CARD_COLOR);
                    }
                }
            }
        });

        // Action de sélection
        button.addActionListener(e -> {
            selectedDate = date;
            updateDateDisplay();
            calendarPopup.setVisible(false);
            firePropertyChange("selectedDate", null, selectedDate);
        });

        return button;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton todayButton = new JButton("Aujourd'hui");
        todayButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        todayButton.setBackground(SECONDARY_COLOR);
        todayButton.setForeground(Color.WHITE);
        todayButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        todayButton.setFocusPainted(false);
        todayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        todayButton.addActionListener(e -> {
            selectedDate = LocalDate.now();
            currentDisplayMonth = selectedDate;
            updateDateDisplay();
            calendarPopup.setVisible(false);
            firePropertyChange("selectedDate", null, selectedDate);
        });

        JButton clearButton = new JButton("Effacer");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearButton.setBackground(Color.WHITE);
        clearButton.setForeground(TEXT_SECONDARY);
        clearButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> {
            selectedDate = null;
            updateDateDisplay();
            calendarPopup.setVisible(false);
            firePropertyChange("selectedDate", null, selectedDate);
        });

        panel.add(clearButton);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(todayButton);

        return panel;
    }

    private void updateDateDisplay() {
        if (selectedDate != null) {
            dateField.setText(selectedDate.format(formatter));
        } else {
            dateField.setText("");
        }
    }

    // Méthodes publiques pour l'API
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            this.currentDisplayMonth = date;
        }
        updateDateDisplay();
        firePropertyChange("selectedDate", null, selectedDate);
    }

    public String getDateText() {
        return dateField.getText();
    }

    public void setDateFormat(DateTimeFormatter formatter) {
        this.formatter = formatter;
        updateDateDisplay();
    }

    public void addDateChangeListener(ActionListener listener) {
        addPropertyChangeListener("selectedDate", evt -> 
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dateChanged")));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dateField.setEnabled(enabled);
        calendarButton.setEnabled(enabled);
        
        if (enabled) {
            dateField.setBackground(Color.WHITE);
            dateField.setForeground(TEXT_PRIMARY);
        } else {
            dateField.setBackground(BACKGROUND_COLOR);
            dateField.setForeground(TEXT_SECONDARY);
        }
    }

    // Méthode pour styliser le composant depuis l'extérieur
    public void applyCustomBorder(Border border) {
        JPanel inputPanel = (JPanel) getComponent(0);
        inputPanel.setBorder(border);
    }
}