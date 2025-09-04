package com.longrich.smartgestion.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.longrich.smartgestion.ui.components.Navbar;
import com.longrich.smartgestion.ui.components.Sidebar;
import com.longrich.smartgestion.ui.panel.AnalyticsPanel;
import com.longrich.smartgestion.ui.panel.BackupPanel;
import com.longrich.smartgestion.ui.panel.ClientPanel;
import com.longrich.smartgestion.ui.panel.CommandePanel;
import com.longrich.smartgestion.ui.panel.DashboardPanel;
import com.longrich.smartgestion.ui.panel.FacturePanel;
import com.longrich.smartgestion.ui.panel.FournisseurPanel;
import com.longrich.smartgestion.ui.panel.ProduitContainerPanel;
import com.longrich.smartgestion.ui.panel.PvPanel;
import com.longrich.smartgestion.ui.panel.SettingsPanel;
import com.longrich.smartgestion.ui.panel.ModernStockPanel;
import com.longrich.smartgestion.ui.panel.UserPanel;
import com.longrich.smartgestion.ui.panel.VentePanel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!headless")
public class MainFrame extends JFrame {

    private final Sidebar sidebar;
    private final Navbar navbar;
    private final DashboardPanel dashboardPanel;
    private final ClientPanel clientPanel;
    private final ProduitContainerPanel produitContainerPanel;
    private final ModernStockPanel stockPanel;
    private final CommandePanel commandePanel;
    private final FournisseurPanel fournisseurPanel;
    private final AnalyticsPanel analyticsPanel;
    private final FacturePanel facturePanel;
    private final VentePanel ventePanel;
    private final PvPanel pvPanel;
    private final SettingsPanel settingsPanel;
    private final UserPanel userPanel;
    private final BackupPanel backupPanel;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    @PostConstruct
    public void initializeUI() {
        setupFrame();
        createComponents();
        layoutComponents();
        setupEventHandlers();
    }

    private void setupFrame() {
        setTitle("SmartGestion - Longrich");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));

        // Icône de l'application
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icône par défaut si le fichier n'est pas trouvé
        }
    }

    private void createComponents() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Initialiser le nouveau panneau de stock
        stockPanel.initializeUI();
        
        // Ajouter les panneaux au CardLayout
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(clientPanel, "clients");
        contentPanel.add(produitContainerPanel, "produits");
        contentPanel.add(stockPanel, "stock");
        contentPanel.add(commandePanel, "commandes");
        contentPanel.add(fournisseurPanel, "fournisseurs");
        contentPanel.add(analyticsPanel, "analytics");
        contentPanel.add(facturePanel, "factures");
        contentPanel.add(ventePanel, "ventes");
        contentPanel.add(pvPanel, "pv");
        contentPanel.add(settingsPanel, "settings");
        contentPanel.add(userPanel, "users");
        contentPanel.add(backupPanel, "backup");

        // Configurer la sidebar pour la navigation
        sidebar.setNavigationHandler(this::showPanel);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Sidebar à gauche
        add(sidebar, BorderLayout.WEST);

        // Panel principal avec navbar et contenu
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(navbar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Êtes-vous sûr de vouloir quitter l'application ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);

        // Mettre à jour le titre de la navbar
        String title = switch (panelName) {
            case "dashboard" -> "Tableau de bord";
            case "clients" -> "Gestion des clients";
            case "produits" -> "Gestion des produits";
            case "stock" -> "Gestion du stock";
            case "commandes" -> "Gestion des commandes";
            case "fournisseurs" -> "Gestion des fournisseurs";
            case "factures" -> "Gestion des factures";
            case "ventes" -> "Gestion des ventes";
            case "analytics" -> "Analytics";
            case "pv" -> "Gestion des PV";
            case "settings" -> "Paramètres";
            case "users" -> "Gestion des utilisateurs";
            case "backup" -> "Sauvegardes";
            default -> "SmartGestion";
        };

        navbar.setTitle(title);
    }

    public void showDashboard() {
        showPanel("dashboard");
    }
}