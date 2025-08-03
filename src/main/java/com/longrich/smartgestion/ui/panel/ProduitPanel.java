package com.longrich.smartgestion.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.springframework.stereotype.Component;

import com.longrich.smartgestion.dto.ProduitDto;
import com.longrich.smartgestion.service.ProduitService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProduitPanel extends JPanel {

    private final ProduitService produitService;

    private JTextField codeBarreField;
    private JTextField libelleField;
    private JTextArea descriptionArea;
    private JTextField datePeremptionField;
    private JTextField prixAchatField;
    private JTextField prixReventeField;
    private JTextField pvField;
    private JComboBox<String> familleCombo;
    private JTextField stockMinimumField;
    private JCheckBox activeCheckBox;

    private JTable produitTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private ProduitDto currentProduit;

    @PostConstruct
    public void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        createFormPanel();
        createTablePanel();
        createButtonPanel();
        
        loadProduits();
    }

    private void createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(0, 51, 204));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                "Données des Produits",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                Color.WHITE
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Code barre
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Code barre:"), gbc);
        gbc.gridx = 1;
        codeBarreField = createTextField();
        formPanel.add(codeBarreField, gbc);

        // Libellé
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Libellé:"), gbc);
        gbc.gridx = 1;
        libelleField = createTextField();
        formPanel.add(libelleField, gbc);

        // Description
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(200, 60));
        formPanel.add(descScrollPane, gbc);

        // Date de péremption
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Date péremption:"), gbc);
        gbc.gridx = 1;
        datePeremptionField = createTextField();
        datePeremptionField.setToolTipText("Format: YYYY-MM-DD");
        formPanel.add(datePeremptionField, gbc);

        // Prix d'achat
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Prix d'achat:"), gbc);
        gbc.gridx = 1;
        prixAchatField = createTextField();
        formPanel.add(prixAchatField, gbc);

        // Prix de revente
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Prix de revente:"), gbc);
        gbc.gridx = 1;
        prixReventeField = createTextField();
        formPanel.add(prixReventeField, gbc);

        // PV
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Nombre de PV:"), gbc);
        gbc.gridx = 1;
        pvField = createTextField();
        formPanel.add(pvField, gbc);

        // Famille
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Famille:"), gbc);
        gbc.gridx = 1;
        familleCombo = new JComboBox<>();
        familleCombo.setPreferredSize(new Dimension(200, 25));
        loadFamilles();
        formPanel.add(familleCombo, gbc);

        // Stock minimum
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Stock minimum:"), gbc);
        gbc.gridx = 1;
        stockMinimumField = createTextField();
        formPanel.add(stockMinimumField, gbc);

        // Actif
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Actif:"), gbc);
        gbc.gridx = 1;
        activeCheckBox = new JCheckBox();
        activeCheckBox.setSelected(true);
        activeCheckBox.setBackground(new Color(0, 51, 204));
        formPanel.add(activeCheckBox, gbc);

        add(formPanel, BorderLayout.WEST);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 25));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return field;
    }

    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Liste des Produits"));

        // Barre de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchButton.addActionListener(e -> searchProduits());
        
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Code barre", "Libellé", "Description", "Prix d'achat", "Prix de revente", "PV", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        produitTable = new JTable(tableModel);
        produitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        produitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedProduit();
            }
        });

        JScrollPane scrollPane = new JScrollPane(produitTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = createButton("Sauvegarder", new Color(3, 168, 25), e -> saveProduit());
        JButton updateButton = createButton("Mettre à jour", new Color(184, 101, 18), e -> updateProduit());
        JButton clearButton = createButton("Vider", new Color(110, 14, 83), e -> clearFields());
        JButton deleteButton = createButton("Supprimer", new Color(207, 6, 26), e -> deleteProduit());

        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private void loadFamilles() {
        // Charger les familles de produits
        familleCombo.removeAllItems();
        familleCombo.addItem("Beauté");
        familleCombo.addItem("Soins");
        familleCombo.addItem("Autre");
    }

    private void loadProduits() {
        List<ProduitDto> produits = produitService.getActiveProduits();
        tableModel.setRowCount(0);
        
        for (ProduitDto produit : produits) {
            Object[] row = {
                produit.getId(),
                produit.getCodeBarre(),
                produit.getLibelle(),
                produit.getDescription(),
                produit.getPrixAchat(),
                produit.getPrixRevente(),
                produit.getPv(),
                produit.getQuantiteStock() != null ? produit.getQuantiteStock() : 0
            };
            tableModel.addRow(row);
        }
    }

    private void searchProduits() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadProduits();
            return;
        }

        List<ProduitDto> produits = produitService.searchProduits(searchText);
        tableModel.setRowCount(0);
        
        for (ProduitDto produit : produits) {
            Object[] row = {
                produit.getId(),
                produit.getCodeBarre(),
                produit.getLibelle(),
                produit.getDescription(),
                produit.getPrixAchat(),
                produit.getPrixRevente(),
                produit.getPv(),
                produit.getQuantiteStock() != null ? produit.getQuantiteStock() : 0
            };
            tableModel.addRow(row);
        }
    }

    private void loadSelectedProduit() {
        int selectedRow = produitTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            produitService.getProduitById(id).ifPresent(produit -> {
                currentProduit = produit;
                populateFields(produit);
            });
        }
    }

    private void populateFields(ProduitDto produit) {
        codeBarreField.setText(produit.getCodeBarre());
        libelleField.setText(produit.getLibelle());
        descriptionArea.setText(produit.getDescription());
        datePeremptionField.setText(produit.getDatePeremption() != null ? produit.getDatePeremption().toString() : "");
        prixAchatField.setText(produit.getPrixAchat() != null ? produit.getPrixAchat().toString() : "");
        prixReventeField.setText(produit.getPrixRevente() != null ? produit.getPrixRevente().toString() : "");
        pvField.setText(produit.getPv() != null ? produit.getPv().toString() : "");
        familleCombo.setSelectedItem(produit.getFamilleName());
        stockMinimumField.setText(produit.getStockMinimum() != null ? produit.getStockMinimum().toString() : "");
        activeCheckBox.setSelected(produit.getActive());
    }

    private void saveProduit() {
        try {
            ProduitDto produit = createProduitFromFields();
            produitService.saveProduit(produit);
            JOptionPane.showMessageDialog(this, "Produit sauvegardé avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadProduits();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduit() {
        if (currentProduit == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit à modifier", "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ProduitDto produit = createProduitFromFields();
            produitService.updateProduit(currentProduit.getId(), produit);
            JOptionPane.showMessageDialog(this, "Produit mis à jour avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadProduits();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduit() {
        if (currentProduit == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit à supprimer", "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir supprimer ce produit ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            try {
                produitService.deleteProduit(currentProduit.getId());
                JOptionPane.showMessageDialog(this, "Produit supprimé avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadProduits();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ProduitDto createProduitFromFields() {
        ProduitDto.ProduitDtoBuilder builder = ProduitDto.builder()
                .codeBarre(codeBarreField.getText().trim())
                .libelle(libelleField.getText().trim())
                .description(descriptionArea.getText().trim())
                .active(activeCheckBox.isSelected());

        // Date de péremption
        String dateStr = datePeremptionField.getText().trim();
        if (!dateStr.isEmpty()) {
            try {
                builder.datePeremption(LocalDate.parse(dateStr));
            } catch (Exception e) {
                throw new IllegalArgumentException("Format de date invalide. Utilisez YYYY-MM-DD");
            }
        }

        // Prix d'achat
        String prixAchatStr = prixAchatField.getText().trim();
        if (!prixAchatStr.isEmpty()) {
            try {
                builder.prixAchat(new BigDecimal(prixAchatStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Prix d'achat invalide");
            }
        }

        // Prix de revente
        String prixReventeStr = prixReventeField.getText().trim();
        if (!prixReventeStr.isEmpty()) {
            try {
                builder.prixRevente(new BigDecimal(prixReventeStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Prix de revente invalide");
            }
        }

        // PV
        String pvStr = pvField.getText().trim();
        if (!pvStr.isEmpty()) {
            try {
                builder.pv(new BigDecimal(pvStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Nombre de PV invalide");
            }
        }

        // Stock minimum
        String stockMinStr = stockMinimumField.getText().trim();
        if (!stockMinStr.isEmpty()) {
            try {
                builder.stockMinimum(Integer.parseInt(stockMinStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Stock minimum invalide");
            }
        }

        return builder.build();
    }

    private void clearFields() {
        currentProduit = null;
        codeBarreField.setText("");
        libelleField.setText("");
        descriptionArea.setText("");
        datePeremptionField.setText("");
        prixAchatField.setText("");
        prixReventeField.setText("");
        pvField.setText("");
        familleCombo.setSelectedIndex(-1);
        stockMinimumField.setText("");
        activeCheckBox.setSelected(true);
        produitTable.clearSelection();
    }
}