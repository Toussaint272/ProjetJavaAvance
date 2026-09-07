package com.gestion.client.view;

import com.gestion.client.model.Employee;
import com.gestion.client.service.ApiService;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

/**
 * Panneau de gestion des employés — interface professionnelle.
 * Recherche live (débounce) branchée sur la recherche LIKE du backend.
 * Icônes vectorielles Ikonli (Font Awesome 5).
 */
public class EmployeePanel extends JPanel {

    private final ApiService apiService;
    private DefaultTableModel tableModel;
    private JTable table;
    private PlaceholderTextField txtSearch;
    private JLabel lblCount;
    private Timer searchDebounceTimer;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel emptyTitleLabel;
    private JLabel emptySubtitleLabel;

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    // ==================== COULEURS ====================
    private static final Color COLOR_PRIMARY        = new Color(41, 128, 185);
    private static final Color COLOR_PRIMARY_HOVER  = new Color(33, 103, 151);
    private static final Color COLOR_SUCCESS        = new Color(39, 174, 96);
    private static final Color COLOR_SUCCESS_HOVER  = new Color(31, 140, 78);
    private static final Color COLOR_DANGER         = new Color(231, 76, 60);
    private static final Color COLOR_DANGER_HOVER   = new Color(192, 57, 43);
    private static final Color COLOR_NEUTRAL        = new Color(116, 125, 140);
    private static final Color COLOR_NEUTRAL_HOVER  = new Color(92, 100, 114);
    private static final Color COLOR_HEADER         = new Color(44, 62, 80);
    private static final Color COLOR_HEADER_HOVER   = new Color(36, 50, 64);
    private static final Color COLOR_BACKGROUND     = new Color(236, 240, 241);
    private static final Color COLOR_WHITE          = Color.WHITE;
    private static final Color COLOR_TEXT           = new Color(44, 62, 80);
    private static final Color COLOR_TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color COLOR_BORDER         = new Color(213, 219, 224);
    private static final Color COLOR_ROW_ALTERNATE  = new Color(248, 249, 250);
    private static final Color COLOR_SELECTION      = new Color(214, 234, 248);

    // ==================== POLICES ====================
    private static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BUTTON       = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_SMALL_BUTTON = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_TABLE        = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TABLE_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL        = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_FIELD        = new Font("Segoe UI", Font.PLAIN, 14);

    public EmployeePanel(ApiService apiService) {
        this.apiService = apiService;

        // Recherche automatique 400 ms après la dernière frappe (compatible LIKE backend)
        searchDebounceTimer = new Timer(400, e -> searchEmployees());
        searchDebounceTimer.setRepeats(false);

        initUI();
        loadData();
    }

    // ================================================================
    // CONSTRUCTION DE L'INTERFACE
    // ================================================================
    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Regroupe l'en-tête et la barre de recherche dans la zone NORD
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(buildHeaderPanel());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildSearchPanel());

        add(topPanel, BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
    }

    // ================================================================
    // EN-TÊTE : TITRE + COMPTEUR + BOUTON AJOUTER
    // ================================================================
    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COLOR_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(14, 20, 14, 20)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(COLOR_WHITE);

        JLabel iconLabel = new JLabel(icon(FontAwesomeSolid.USER, 22, COLOR_TEXT));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Gestion des Employés");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT);
        titlePanel.add(titleLabel);

        lblCount = new JLabel("(0)");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCount.setForeground(COLOR_TEXT_LIGHT);
        titlePanel.add(lblCount);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        JButton btnAdd = new RoundedButton("Ajouter un employé", COLOR_SUCCESS, COLOR_SUCCESS_HOVER);
        btnAdd.setIcon(icon(FontAwesomeSolid.PLUS, 15, COLOR_WHITE));
        btnAdd.setToolTipText("Créer un nouvel employé");
        btnAdd.addActionListener(e -> showEmployeeDialog(null));
        headerPanel.add(btnAdd, BorderLayout.EAST);

        return headerPanel;
    }

    // ================================================================
    // BARRE DE RECHERCHE (live)
    // ================================================================
    private JPanel buildSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(COLOR_WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(10, 15, 10, 15)));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchLeft.setBackground(COLOR_WHITE);

        JLabel searchIcon = new JLabel(icon(FontAwesomeSolid.SEARCH, 16, COLOR_TEXT_LIGHT));
        searchLeft.add(searchIcon);

        txtSearch = new PlaceholderTextField("Rechercher par nom ou code  ...", 25);
        txtSearch.setPreferredSize(new Dimension(270, 36));
        txtSearch.setToolTipText("La recherche démarre automatiquement pendant la saisie");
        txtSearch.addActionListener(e -> searchEmployees());
        // Recherche en temps réel : redémarre le minuteur à chaque frappe
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }

            @Override
            public void removeUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }

            @Override
            public void changedUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
        });
        searchLeft.add(txtSearch);

        JButton btnSearch = new RoundedButton("Rechercher", COLOR_PRIMARY, COLOR_PRIMARY_HOVER);
        btnSearch.setIcon(icon(FontAwesomeSolid.SEARCH, 14, COLOR_WHITE));
        btnSearch.setToolTipText("Lancer la recherche");
        btnSearch.addActionListener(e -> searchEmployees());
        searchLeft.add(btnSearch);


        searchPanel.add(searchLeft, BorderLayout.WEST);

        JButton btnRefresh = new RoundedButton("Rafraîchir", COLOR_HEADER, COLOR_HEADER_HOVER);
        btnRefresh.setIcon(icon(FontAwesomeSolid.SYNC, 14, COLOR_WHITE));
        btnRefresh.setToolTipText("Recharger la liste depuis le serveur");
        btnRefresh.addActionListener(e -> loadData());
        searchPanel.add(btnRefresh, BorderLayout.EAST);

        return searchPanel;
    }

    // ================================================================
    // TABLEAU + ÉTAT VIDE
    // ================================================================
    private JPanel buildTableArea() {
        String[] columns = {"Code", "Nom", "Prénom", "Poste", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setFont(FONT_TABLE);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        // En-tête du tableau
        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setBackground(COLOR_HEADER);
        table.getTableHeader().setForeground(COLOR_WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());

        // Largeurs des colonnes
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(230);
        table.getColumnModel().getColumn(4).setMaxWidth(300);

        // Renderers personnalisés
        table.getColumnModel().getColumn(0).setCellRenderer(new CodeCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new TextCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new TextCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new TextCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Clic sur la colonne "Actions" : moitié gauche = modifier, moitié droite = supprimer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 4) {
                    String code = (String) tableModel.getValueAt(row, 0);
                    Rectangle cellRect = table.getCellRect(row, col, true);
                    int xInCell = e.getX() - cellRect.x;
                    try {
                        List<Employee> employees = apiService.getAllEmployees();
                        Employee emp = employees.stream()
                                .filter(em -> em.getCodeFormate().equals(code))
                                .findFirst().orElse(null);
                        if (emp != null) {
                            if (xInCell < cellRect.width / 2) {
                                showEmployeeDialog(emp);
                            } else {
                                deleteEmployee(emp);
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(EmployeePanel.this,
                                ex.getMessage(),
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Curseur "main" au survol de la colonne Actions
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 4
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        // ---------- État vide ----------
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(COLOR_WHITE);
        emptyPanel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        JPanel emptyContent = new JPanel();
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));
        emptyContent.setBackground(COLOR_WHITE);
        emptyContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyIcon = new JLabel(icon(FontAwesomeSolid.SEARCH, 44, COLOR_TEXT_LIGHT), SwingConstants.CENTER);
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyTitleLabel = new JLabel("Aucun employé", SwingConstants.CENTER);
        emptyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emptyTitleLabel.setForeground(COLOR_TEXT);
        emptyTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptySubtitleLabel = new JLabel("Cliquez sur « Ajouter un employé » pour commencer.", SwingConstants.CENTER);
        emptySubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptySubtitleLabel.setForeground(COLOR_TEXT_LIGHT);
        emptySubtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyContent.add(emptyIcon);
        emptyContent.add(Box.createVerticalStrut(10));
        emptyContent.add(emptyTitleLabel);
        emptyContent.add(Box.createVerticalStrut(4));
        emptyContent.add(emptySubtitleLabel);

        emptyPanel.add(emptyContent);

        // ---------- Cartes : tableau / état vide ----------
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(scrollPane, CARD_TABLE);
        cardPanel.add(emptyPanel, CARD_EMPTY);
        cardLayout.show(cardPanel, CARD_TABLE);

        return cardPanel;
    }

    private void showTable() {
        cardLayout.show(cardPanel, CARD_TABLE);
    }

    private void showEmpty(String title, String subtitle) {
        emptyTitleLabel.setText(title);
        emptySubtitleLabel.setText(subtitle);
        cardLayout.show(cardPanel, CARD_EMPTY);
    }

    // ================================================================
    // MÉTHODES FONCTIONNELLES
    // ================================================================

    private void addEmployeeRow(Employee emp) {
        tableModel.addRow(new Object[]{
                emp.getCodeFormate(),
                emp.getNom(),
                emp.getPrenom(),
                emp.getPoste(),
                "actions"
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Employee> employees = apiService.getAllEmployees();
            for (Employee emp : employees) {
                addEmployeeRow(emp);
            }
            lblCount.setText("(" + employees.size() + ")");
            if (employees.isEmpty()) {
                showEmpty("Aucun employé",
                        "Cliquez sur « Ajouter un employé » pour créer le premier.");
            } else {
                showTable();
            }
        } catch (Exception e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void searchEmployees() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        try {
            tableModel.setRowCount(0);
            List<Employee> results = apiService.searchEmployees(keyword);
            for (Employee emp : results) {
                addEmployeeRow(emp);
            }
            lblCount.setText("(" + results.size() + ")");
            if (results.isEmpty()) {
                showEmpty("Aucun résultat",
                        "Aucun employé ne correspond à « " + keyword + " ».");
            } else {
                showTable();
            }
        } catch (Exception e) {
            showError("Erreur de recherche", e.getMessage());
        }
    }

    private void deleteEmployee(Employee employee) {
        if (employee == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Supprimer l'employé " + employee.getCodeFormate() + " ?\n\n" +
                        "Nom: " + employee.getNom() + " " + employee.getPrenom(),
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                apiService.deleteEmployee(employee.getCodeFormate());
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Employé supprimé avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                showError("Erreur de suppression", e.getMessage());
            }
        }
    }

    // ================================================================
    // DIALOGUE AJOUT / MODIFICATION
    // ================================================================
    private void showEmployeeDialog(Employee employee) {
        boolean isEdit = employee != null;
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, isEdit ? "Modifier l'employé" : "Nouvel employé", true);
        dialog.setSize(480, 400);
        dialog.setMinimumSize(new Dimension(480, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_WHITE);

        // ---------- En-tête ----------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new EmptyBorder(16, 25, 16, 25));

        JLabel titleLabel = new JLabel(isEdit ? "Modifier l'employé" : "Nouvel employé");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(COLOR_WHITE);
        titleLabel.setIcon(icon(isEdit ? FontAwesomeSolid.EDIT : FontAwesomeSolid.PLUS, 18, COLOR_WHITE));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        dialog.add(headerPanel, BorderLayout.NORTH);

        // ---------- Formulaire ----------
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(COLOR_WHITE);
        contentPanel.setBorder(new EmptyBorder(25, 30, 15, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtNom = createField();
        JTextField txtPrenom = createField();
        JTextField txtPoste = createField();

        if (isEdit) {
            txtNom.setText(employee.getNom());
            txtPrenom.setText(employee.getPrenom());
            txtPoste.setText(employee.getPoste());
        }

        JLabel labelNom = createRequiredLabel("Nom");
        JLabel labelPrenom = createRequiredLabel("Prénom");
        JLabel labelPoste = createRequiredLabel("Poste");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(labelNom, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(txtNom, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(labelPrenom, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(txtPrenom, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(labelPoste, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(txtPoste, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // ---------- Boutons ----------
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(COLOR_WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 22, 0));

        JButton btnSave = new RoundedButton("Enregistrer", COLOR_SUCCESS, COLOR_SUCCESS_HOVER);
        btnSave.setIcon(icon(FontAwesomeSolid.SAVE, 14, COLOR_WHITE));

        JButton btnCancel = new RoundedButton("Annuler", COLOR_NEUTRAL, COLOR_NEUTRAL_HOVER);
        btnCancel.setIcon(icon(FontAwesomeSolid.TIMES, 14, COLOR_WHITE));

        btnSave.addActionListener(e -> {
            try {
                String nom = txtNom.getText().trim();
                String prenom = txtPrenom.getText().trim();
                String poste = txtPoste.getText().trim();

                // Mise en évidence des champs vides
                txtNom.setBorder(createFieldBorder(nom.isEmpty() ? COLOR_DANGER : COLOR_BORDER));
                txtPrenom.setBorder(createFieldBorder(prenom.isEmpty() ? COLOR_DANGER : COLOR_BORDER));
                txtPoste.setBorder(createFieldBorder(poste.isEmpty() ? COLOR_DANGER : COLOR_BORDER));

                if (nom.isEmpty() || prenom.isEmpty() || poste.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Tous les champs sont obligatoires",
                            "Erreur",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Employee emp = new Employee();
                emp.setNom(nom);
                emp.setPrenom(prenom);
                emp.setPoste(poste);

                if (isEdit) {
                    apiService.updateEmployee(employee.getCodeFormate(), emp);
                    JOptionPane.showMessageDialog(dialog,
                            "Employé modifié avec succès",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    apiService.createEmployee(emp);
                    JOptionPane.showMessageDialog(dialog,
                            "Employé créé avec succès",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                loadData();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Entrée pour valider, Échap pour annuler
        dialog.getRootPane().setDefaultButton(btnSave);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setVisible(true);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    // HELPERS (icônes + champs + labels)
    // ================================================================
    private static Icon icon(Ikon ikon, int size, Color color) {
        return FontIcon.of(ikon, size, color);
    }

    private static Border createFieldBorder(Color lineColor) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lineColor, 1),
                new EmptyBorder(8, 12, 8, 12));
    }

    private static JTextField createField() {
        JTextField field = new JTextField(20);
        field.setFont(FONT_FIELD);
        field.setForeground(COLOR_TEXT);
        field.setBorder(createFieldBorder(COLOR_BORDER));
        return field;
    }

    private static JLabel createRequiredLabel(String text) {
        JLabel label = new JLabel("<html>" + text + " <font color='#E74C3C'>*</font></html>");
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    // ================================================================
    // RENDERERS DU TABLEAU
    // ================================================================
    private class TextCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(FONT_TABLE);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            if (isSelected) {
                setBackground(COLOR_SELECTION);
                setForeground(COLOR_TEXT);
            } else {
                setBackground(row % 2 == 0 ? COLOR_WHITE : COLOR_ROW_ALTERNATE);
                setForeground(COLOR_TEXT);
            }
            return this;
        }
    }

    private class CodeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(FONT_TABLE_BOLD);
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            if (isSelected) {
                setBackground(COLOR_SELECTION);
                setForeground(COLOR_PRIMARY_HOVER);
            } else {
                setBackground(row % 2 == 0 ? COLOR_WHITE : COLOR_ROW_ALTERNATE);
                setForeground(COLOR_PRIMARY);
            }
            return this;
        }
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(CENTER);
            setFont(FONT_HEADER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(COLOR_HEADER);
            setForeground(COLOR_WHITE);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    // CORRECTION : GridBagLayout -> les deux boutons restent TOUJOURS sur la même ligne
    private class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final RoundedButton btnEdit;
        private final RoundedButton btnDelete;

        ButtonRenderer() {
            super(new GridBagLayout());
            setOpaque(true);

            btnEdit = new RoundedButton("Modifier", COLOR_PRIMARY, COLOR_PRIMARY_HOVER);
            btnEdit.setFont(FONT_SMALL_BUTTON);
            btnEdit.setBorder(new EmptyBorder(6, 10, 6, 10));
            btnEdit.setIcon(icon(FontAwesomeSolid.EDIT, 12, COLOR_WHITE));

            btnDelete = new RoundedButton("Supprimer", COLOR_DANGER, COLOR_DANGER_HOVER);
            btnDelete.setFont(FONT_SMALL_BUTTON);
            btnDelete.setBorder(new EmptyBorder(6, 10, 6, 10));
            btnDelete.setIcon(icon(FontAwesomeSolid.TRASH_ALT, 12, COLOR_WHITE));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;                       // même ligne pour les deux boutons
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 3, 0, 3);

            gbc.gridx = 0;
            add(btnEdit, gbc);
            gbc.gridx = 1;
            add(btnDelete, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(isSelected ? COLOR_SELECTION
                    : (row % 2 == 0 ? COLOR_WHITE : COLOR_ROW_ALTERNATE));
            btnEdit.setBackground(COLOR_PRIMARY);
            btnDelete.setBackground(COLOR_DANGER);
            return this;
        }
    }

    // ================================================================
    // CHAMP DE TEXTE AVEC PLACEHOLDER
    // ================================================================
    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setFont(FONT_FIELD);
            setForeground(COLOR_TEXT);
            setBorder(createFieldBorder(COLOR_BORDER));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(COLOR_TEXT_LIGHT);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, ins.left, y);
                g2.dispose();
            }
        }
    }

    // ================================================================
    // BOUTON ARRONDI AVEC EFFET HOVER
    // ================================================================
    private static class RoundedButton extends JButton {
        private static final int ARC = 12;
        private final Color normalColor;
        private final Color hoverColor;
        private final Color pressedColor;

        RoundedButton(String text, Color normal, Color hover) {
            super(text);
            this.normalColor = normal;
            this.hoverColor = hover;
            this.pressedColor = hover.darker();

            setFont(FONT_BUTTON);
            setForeground(COLOR_WHITE);
            setBackground(normal);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 20, 9, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { setBackground(hoverColor); }

                @Override
                public void mouseExited(MouseEvent e) { setBackground(normalColor); }

                @Override
                public void mousePressed(MouseEvent e) { setBackground(pressedColor); }

                @Override
                public void mouseReleased(MouseEvent e) { setBackground(hoverColor); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // La bordure est peinte par paintComponent (forme arrondie).
        }
    }
}
