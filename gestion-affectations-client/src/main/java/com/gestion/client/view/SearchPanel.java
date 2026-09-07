package com.gestion.client.view;

import com.gestion.client.model.Employee;
import com.gestion.client.service.ApiService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SearchPanel extends JPanel {
    private final ApiService apiService;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public SearchPanel(ApiService apiService) {
        this.apiService = apiService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 244, 248));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel("🔍");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Recherche d'Employés");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 73, 94));
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Barre de recherche
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBarPanel.setBackground(Color.WHITE);
        searchBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        txtSearch = new JTextField(30);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchBarPanel.add(txtSearch);

        JButton btnSearch = new JButton("🔍 Rechercher");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnSearch.addActionListener(e -> searchEmployees());
        searchBarPanel.add(btnSearch);

        JButton btnClear = new JButton("🗑️ Effacer");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            tableModel.setRowCount(0);
        });
        searchBarPanel.add(btnClear);

        add(searchBarPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Code", "Nom", "Prénom", "Poste"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219, 50));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void searchEmployees() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un mot clé");
            return;
        }
        try {
            tableModel.setRowCount(0);
            List<Employee> results = apiService.searchEmployees(keyword);
            for (Employee emp : results) {
                tableModel.addRow(new Object[]{
                        emp.getCodeFormate(),
                        emp.getNom(),
                        emp.getPrenom(),
                        emp.getPoste()
                });
            }
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucun résultat trouvé");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur: " + e.getMessage());
        }
    }
}