package com.gestion.client;

import com.gestion.client.model.Affecter;
import com.gestion.client.model.Employee;
import com.gestion.client.model.Lieu;
import com.gestion.client.service.ApiService;
import com.gestion.client.view.AccueilPanel;
import com.gestion.client.view.ModernButtonPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;

public class MainApp extends JFrame {
    private final ApiService apiService;
    private JTabbedPane tabbedPane;
    private AccueilPanel accueilPanel;

    // Tables
    private DefaultTableModel employeeTableModel;
    private DefaultTableModel lieuTableModel;
    private DefaultTableModel affectationTableModel;
    private DefaultTableModel searchTableModel;

    public MainApp() {
        this.apiService = new ApiService();
        initUI();
        loadAllData();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void initUI() {
        setTitle("🏢 Gestion des Affectations des Employés");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 244, 248));

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(52, 73, 94));
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JMenu menuFichier = new JMenu("📁 Fichier");
        menuFichier.setForeground(Color.WHITE);
        JMenuItem menuQuitter = new JMenuItem("❌ Quitter");
        menuQuitter.addActionListener(e -> System.exit(0));
        menuFichier.add(menuQuitter);

        JMenu menuAide = new JMenu("❓ Aide");
        menuAide.setForeground(Color.WHITE);
        JMenuItem menuAbout = new JMenuItem("📖 À propos");
        menuAbout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "📌 Gestion des Affectations v1.0\n" +
                            "🔧 Technologies: Spring Boot + Java Swing\n" +
                            "📅 2026 - Tous droits réservés",
                    "À propos", JOptionPane.INFORMATION_MESSAGE);
        });
        menuAide.add(menuAbout);

        menuBar.add(menuFichier);
        menuBar.add(menuAide);
        setJMenuBar(menuBar);

        // Onglets
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(240, 244, 248));

        // 1. Page d'accueil
        accueilPanel = new AccueilPanel(apiService);
        tabbedPane.addTab("🏠 Accueil", accueilPanel);

        // 2. Employés
        tabbedPane.addTab("👤 Employés", createEmployeePanel());

        // 3. Lieux
        tabbedPane.addTab("📍 Lieux", createLieuPanel());

        // 4. Affectations
        tabbedPane.addTab("📋 Affectations", createAffectationPanel());

        // 5. Recherche
        tabbedPane.addTab("🔍 Recherche", createSearchPanel());

        // 6. Statistiques
        tabbedPane.addTab("📊 Statistiques", createStatisticsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ============ PANEL EMPLOYÉS ============
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"Code", "Nom", "Prénom", "Poste"};
        employeeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(employeeTableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Boutons
        JButton btnAdd = new JButton("➕ Ajouter");
        JButton btnEdit = new JButton("✏️ Modifier");
        JButton btnDelete = new JButton("🗑️ Supprimer");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");

        btnAdd.addActionListener(e -> showEmployeeDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String code = (String) table.getValueAt(row, 0);
                try {
                    List<Employee> employees = apiService.getAllEmployees();
                    Employee emp = employees.stream()
                            .filter(em -> em.getCodeFormate().equals(code))
                            .findFirst().orElse(null);
                    if (emp != null) showEmployeeDialog(emp);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez un employé");
            }
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String code = (String) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Supprimer l'employé " + code + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        apiService.deleteEmployee(code);
                        loadEmployees();
                        accueilPanel.refreshStats();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                    }
                }
            }
        });
        btnRefresh.addActionListener(e -> {
            loadEmployees();
            accueilPanel.refreshStats();
        });

        ModernButtonPanel buttonPanel = new ModernButtonPanel(btnAdd, btnEdit, btnDelete, btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============ DIALOGUE EMPLOYÉ ============
    private void showEmployeeDialog(Employee employee) {
        boolean isEdit = employee != null;
        JDialog dialog = new JDialog(this, isEdit ? "✏️ Modifier Employé" : "➕ Ajouter Employé", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel(isEdit ? "✏️ Modification de l'employé" : "➕ Nouvel employé");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);

        // Séparateur
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        dialog.add(new JSeparator(), gbc);

        JTextField txtCode = new JTextField(15);
        txtCode.setEnabled(false);
        JTextField txtNom = new JTextField(15);
        JTextField txtPrenom = new JTextField(15);
        JTextField txtPoste = new JTextField(15);

        if (isEdit) {
            txtCode.setText(employee.getCodeFormate());
            txtNom.setText(employee.getNom());
            txtPrenom.setText(employee.getPrenom());
            txtPoste.setText(employee.getPoste());
        }

        gbc.gridwidth = 1;

        // Code
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("📌 Code:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtCode, gbc);

        // Nom
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("👤 Nom:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNom, gbc);

        // Prénom
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("👤 Prénom:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtPrenom, gbc);

        // Poste
        gbc.gridx = 0;
        gbc.gridy = 5;
        dialog.add(new JLabel("💼 Poste:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtPoste, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnSave = new JButton("💾 Enregistrer");
        JButton btnCancel = new JButton("❌ Annuler");

        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        btnCancel.setBackground(new Color(231, 76, 60));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        btnSave.addActionListener(e -> {
            try {
                Employee emp = new Employee();
                emp.setNom(txtNom.getText().trim());
                emp.setPrenom(txtPrenom.getText().trim());
                emp.setPoste(txtPoste.getText().trim());

                if (emp.getNom().isEmpty() || emp.getPrenom().isEmpty() || emp.getPoste().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires !");
                    return;
                }

                if (isEdit) {
                    apiService.updateEmployee(employee.getCodeFormate(), emp);
                } else {
                    apiService.createEmployee(emp);
                }
                loadEmployees();
                accueilPanel.refreshStats();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Erreur: " + ex.getMessage());
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // ============ PANEL LIEUX ============
    private JPanel createLieuPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Code", "Désignation", "Province"};
        lieuTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(lieuTableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnAdd = new JButton("➕ Ajouter");
        JButton btnEdit = new JButton("✏️ Modifier");
        JButton btnDelete = new JButton("🗑️ Supprimer");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");

        btnAdd.addActionListener(e -> showLieuDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String code = (String) table.getValueAt(row, 0);
                try {
                    List<Lieu> lieus = apiService.getAllLieus();
                    Lieu lieu = lieus.stream()
                            .filter(l -> l.getCodeFormate().equals(code))
                            .findFirst().orElse(null);
                    if (lieu != null) showLieuDialog(lieu);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez un lieu");
            }
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String code = (String) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Supprimer le lieu " + code + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        apiService.deleteLieu(code);
                        loadLieus();
                        accueilPanel.refreshStats();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                    }
                }
            }
        });
        btnRefresh.addActionListener(e -> {
            loadLieus();
            accueilPanel.refreshStats();
        });

        ModernButtonPanel buttonPanel = new ModernButtonPanel(btnAdd, btnEdit, btnDelete, btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============ DIALOGUE LIEU ============
    private void showLieuDialog(Lieu lieu) {
        boolean isEdit = lieu != null;
        JDialog dialog = new JDialog(this, isEdit ? "✏️ Modifier Lieu" : "➕ Ajouter Lieu", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel(isEdit ? "✏️ Modification du lieu" : "➕ Nouveau lieu");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);

        gbc.gridy = 1;
        dialog.add(new JSeparator(), gbc);

        JTextField txtCode = new JTextField(15);
        txtCode.setEnabled(false);
        JTextField txtDesignation = new JTextField(15);
        JTextField txtProvince = new JTextField(15);

        if (isEdit) {
            txtCode.setText(lieu.getCodeFormate());
            txtDesignation.setText(lieu.getDesignation());
            txtProvince.setText(lieu.getProvince());
        }

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("📌 Code:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtCode, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("📍 Désignation:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtDesignation, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("🌍 Province:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtProvince, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnSave = new JButton("💾 Enregistrer");
        JButton btnCancel = new JButton("❌ Annuler");

        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        btnCancel.setBackground(new Color(231, 76, 60));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        btnSave.addActionListener(e -> {
            try {
                Lieu l = new Lieu();
                l.setDesignation(txtDesignation.getText().trim());
                l.setProvince(txtProvince.getText().trim());

                if (l.getDesignation().isEmpty() || l.getProvince().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires !");
                    return;
                }

                if (isEdit) {
                    apiService.updateLieu(lieu.getCodeFormate(), l);
                } else {
                    apiService.createLieu(l);
                }
                loadLieus();
                accueilPanel.refreshStats();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Erreur: " + ex.getMessage());
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // ============ PANEL AFFECTATIONS ============
    private JPanel createAffectationPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Employé", "Lieu", "Date"};
        affectationTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(affectationTableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnAdd = new JButton("➕ Ajouter");
        JButton btnDelete = new JButton("🗑️ Supprimer");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");

        btnAdd.addActionListener(e -> showAffectationDialog());
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Supprimer l'affectation ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        apiService.deleteAffectation(id);
                        loadAffectations();
                        accueilPanel.refreshStats();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                    }
                }
            }
        });
        btnRefresh.addActionListener(e -> {
            loadAffectations();
            accueilPanel.refreshStats();
        });

        ModernButtonPanel buttonPanel = new ModernButtonPanel(btnAdd, btnDelete, btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showAffectationDialog() {
        try {
            List<Employee> employees = apiService.getAllEmployees();
            List<Lieu> lieus = apiService.getAllLieus();

            if (employees.isEmpty() || lieus.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Veuillez d'abord créer des employés et des lieux !");
                return;
            }

            JDialog dialog = new JDialog(this, "➕ Ajouter Affectation", true);
            dialog.setSize(500, 350);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new GridBagLayout());
            dialog.getContentPane().setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel titleLabel = new JLabel("➕ Nouvelle affectation");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(new Color(52, 73, 94));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            dialog.add(titleLabel, gbc);

            gbc.gridy = 1;
            dialog.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;

            JComboBox<String> cbEmployee = new JComboBox<>();
            JComboBox<String> cbLieu = new JComboBox<>();
            JTextField txtDate = new JTextField(LocalDate.now().toString());

            for (Employee e : employees) {
                cbEmployee.addItem(e.getCodeFormate() + " - " + e.getNom() + " " + e.getPrenom());
            }
            for (Lieu l : lieus) {
                cbLieu.addItem(l.getCodeFormate() + " - " + l.getDesignation());
            }

            gbc.gridx = 0;
            gbc.gridy = 2;
            dialog.add(new JLabel("👤 Employé:"), gbc);
            gbc.gridx = 1;
            dialog.add(cbEmployee, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            dialog.add(new JLabel("📍 Lieu:"), gbc);
            gbc.gridx = 1;
            dialog.add(cbLieu, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            dialog.add(new JLabel("📅 Date (YYYY-MM-DD):"), gbc);
            gbc.gridx = 1;
            dialog.add(txtDate, gbc);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
            buttonPanel.setBackground(Color.WHITE);
            JButton btnSave = new JButton("💾 Enregistrer");
            JButton btnCancel = new JButton("❌ Annuler");

            btnSave.setBackground(new Color(46, 204, 113));
            btnSave.setForeground(Color.WHITE);
            btnSave.setFocusPainted(false);
            btnSave.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

            btnCancel.setBackground(new Color(231, 76, 60));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFocusPainted(false);
            btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

            btnSave.addActionListener(e -> {
                try {
                    String empCode = ((String) cbEmployee.getSelectedItem()).split(" - ")[0];
                    String lieuCode = ((String) cbLieu.getSelectedItem()).split(" - ")[0];
                    LocalDate date = LocalDate.parse(txtDate.getText().trim());

                    apiService.createAffectation(empCode, lieuCode, date);
                    loadAffectations();
                    accueilPanel.refreshStats();
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "❌ Erreur: " + ex.getMessage());
                }
            });
            btnCancel.addActionListener(e -> dialog.dispose());

            buttonPanel.add(btnSave);
            buttonPanel.add(btnCancel);

            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            dialog.add(buttonPanel, gbc);

            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur: " + e.getMessage());
        }
    }

    // ============ PANEL RECHERCHE ============
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JTextField txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton btnSearch = new JButton("🔍 Rechercher");
        JButton btnClear = new JButton("🗑️ Effacer");

        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        searchPanel.add(new JLabel("🔎 Mot clé:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Code", "Nom", "Prénom", "Poste"};
        searchTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(searchTableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            try {
                String keyword = txtSearch.getText().trim();
                if (!keyword.isEmpty()) {
                    searchTableModel.setRowCount(0);
                    List<Employee> results = apiService.searchEmployees(keyword);
                    for (Employee emp : results) {
                        searchTableModel.addRow(new Object[]{
                                emp.getCodeFormate(),
                                emp.getNom(),
                                emp.getPrenom(),
                                emp.getPoste()
                        });
                    }
                    if (results.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "❌ Aucun résultat trouvé");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Erreur: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            searchTableModel.setRowCount(0);
        });

        return panel;
    }

    // ============ PANEL STATISTIQUES ============
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("🔄 Rafraîchir");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        btnRefresh.addActionListener(e -> {
            try {
                var stats = apiService.getStatistics();
                StringBuilder sb = new StringBuilder();
                sb.append("📊  STATISTIQUES DES AFFECTATIONS\n");
                sb.append("═".repeat(50)).append("\n\n");
                if (stats.isEmpty()) {
                    sb.append("   Aucune affectation enregistrée.\n");
                } else {
                    for (var entry : stats.entrySet()) {
                        sb.append(String.format("   📌 %-15s : %d affectation(s)\n",
                                entry.getKey(), entry.getValue()));
                    }
                    sb.append("\n").append("═".repeat(50)).append("\n");
                    sb.append("   Total affectations : ").append(stats.values().stream().mapToLong(Long::longValue).sum());
                }
                textArea.setText(sb.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Erreur: " + ex.getMessage());
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(new Color(240, 244, 248));
        buttonPanel.add(btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnRefresh.doClick();

        return panel;
    }

    // ============ CHARGEMENT DES DONNÉES ============

    private void loadAllData() {
        loadEmployees();
        loadLieus();
        loadAffectations();
    }

    private void loadEmployees() {
        employeeTableModel.setRowCount(0);
        try {
            List<Employee> employees = apiService.getAllEmployees();
            for (Employee emp : employees) {
                employeeTableModel.addRow(new Object[]{
                        emp.getCodeFormate(),
                        emp.getNom(),
                        emp.getPrenom(),
                        emp.getPoste()
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement employés: " + e.getMessage());
        }
    }

    private void loadLieus() {
        lieuTableModel.setRowCount(0);
        try {
            List<Lieu> lieus = apiService.getAllLieus();
            for (Lieu lieu : lieus) {
                lieuTableModel.addRow(new Object[]{
                        lieu.getCodeFormate(),
                        lieu.getDesignation(),
                        lieu.getProvince()
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement lieux: " + e.getMessage());
        }
    }

    private void loadAffectations() {
        affectationTableModel.setRowCount(0);
        try {
            List<Affecter> affectations = apiService.getAllAffectations();
            for (Affecter aff : affectations) {
                String employeDisplay = (aff.getCodeemp() != null ? aff.getCodeemp() : "N/A")
                        + " - " + (aff.getNomEmploye() != null ? aff.getNomEmploye() : "N/A");
                String lieuDisplay = (aff.getCodelieu() != null ? aff.getCodelieu() : "N/A")
                        + " - " + (aff.getDesignationLieu() != null ? aff.getDesignationLieu() : "N/A");
                affectationTableModel.addRow(new Object[]{
                        aff.getId(),
                        employeDisplay,
                        lieuDisplay,
                        aff.getDateAffectation()
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement affectations: " + e.getMessage());
        }
    }

    // ============ MAIN ============

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MainApp().setVisible(true);
        });
    }
}