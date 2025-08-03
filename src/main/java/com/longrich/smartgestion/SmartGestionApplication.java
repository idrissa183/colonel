package com.longrich.smartgestion;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.longrich.smartgestion.ui.main.MainFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.swing.*;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
public class SmartGestionApplication {

    public static void main(String[] args) {
        // Configuration du Look and Feel avant le démarrage de l'interface
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            log.error("Erreur lors de la configuration du Look and Feel", ex);
        }

        // Démarrage du contexte Spring
        System.setProperty("java.awt.headless", "false");
        ConfigurableApplicationContext context = SpringApplication.run(SmartGestionApplication.class, args);

        // Lancement de l'interface Swing
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = context.getBean(MainFrame.class);
                mainFrame.setVisible(true);
            } catch (Exception e) {
                log.error("Erreur lors du lancement de l'interface", e);
            }
        });
    }
}