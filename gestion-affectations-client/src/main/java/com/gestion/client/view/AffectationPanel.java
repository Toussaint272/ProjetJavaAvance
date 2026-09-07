package com.gestion.client.view;

import com.gestion.client.model.Affecter;
import com.gestion.client.model.Employee;
import com.gestion.client.model.Lieu;
import com.gestion.client.service.ApiService;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.util.List;

/**
 * Panneau de gestion des affectations — interface professionnelle.
 * (Seule la présentation a été revue, la logique métier est inchangée.)
 * Icônes vectorielles Ikonli (Font Awesome 5).
 */
public class AffectationPanel extends JPanel {

    private final ApiService apiService;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCount;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel emptyTitleLabel;
    private JLabel emptySubtitleLabel;

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    // Colonne "Actions" dans le MODÈLE (l'ID est en colonne 0, masquée à l'affichage)
    private static final int MODEL_COL_ID = 0;
    private static final int MODEL_COL_ACTIONS = 4;

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

    public AffectationPanel(ApiService apiService) {
        this.apiService = apiService;
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

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
    }

    // ================================================================
    // EN-TÊTE : TITRE + COMPTEUR + BOUTONS RAFRAÎCHIR / AJOUTER
    // ================================================================
    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COLOR_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(14, 20, 14, 20)));

        // Titre + compteur à gauche
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(COLOR_WHITE);

        JLabel iconLabel = new JLabel(icon(FontAwesomeSolid.CLIPBOARD_LIST, 22, COLOR_TEXT));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Gestion des Affectations");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT);
        titlePanel.add(titleLabel);

        lblCount = new JLabel("0 affectations");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCount.setForeground(COLOR_TEXT_LIGHT);
        titlePanel.add(lblCount);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // À droite : Rafraîchir (à gauche) puis Nouvelle Affectation (à droite)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(COLOR_WHITE);

        JButton btnRefresh = new RoundedButton("Rafraîchir", COLOR_NEUTRAL, COLOR_NEUTRAL_HOVER);
        btnRefresh.setIcon(icon(FontAwesomeSolid.SYNC, 14, COLOR_WHITE));
        btnRefresh.setToolTipText("Recharger la liste depuis le serveur");
        btnRefresh.addActionListener(e -> loadData());

        JButton btnAdd = new RoundedButton("Nouvelle Affectation", COLOR_SUCCESS, COLOR_SUCCESS_HOVER);
        btnAdd.setIcon(icon(FontAwesomeSolid.PLUS, 15, COLOR_WHITE));
        btnAdd.setToolTipText("Créer une nouvelle affectation");
        btnAdd.addActionListener(e -> showAffectationDialog(null));

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAdd);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // ================================================================
    // TABLEAU + ÉTAT VIDE  (colonne ID masquée)
    // ================================================================
    private JPanel buildTableArea() {
        // L'ID reste dans le modèle (colonne 0) mais n'est pas affiché
        String[] columns = {"ID", "Employé", "Lieu", "Date", "Actions"};
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

        // ==== Masquer la colonne ID (elle reste dans le modèle pour les actions) ====
        table.removeColumn(table.getColumnModel().getColumn(MODEL_COL_ID));

        // Largeurs des colonnes visibles : Employé | Lieu | Date | Actions
        table.getColumnModel().getColumn(0).setPreferredWidth(230); // Employé
        table.getColumnModel().getColumn(1).setPreferredWidth(230); // Lieu
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Date
        table.getColumnModel().getColumn(3).setPreferredWidth(240); // Actions
        table.getColumnModel().getColumn(3).setMaxWidth(260);

        // Renderers personnalisés
        table.getColumnModel().getColumn(0).setCellRenderer(new TextCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new TextCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new DateCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Clic sur la colonne "Actions" : moitié gauche = modifier, moitié droite = supprimer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (row < 0) return;
                int modelCol = table.convertColumnIndexToModel(viewCol);
                if (modelCol == MODEL_COL_ACTIONS) {
                    long id = getIdAt(row);
                    Rectangle cellRect = table.getCellRect(row, viewCol, true);
                    int xInCell = e.getX() - cellRect.x;
                    if (xInCell < cellRect.width / 2) {
                        editAffectation(id);
                    } else {
                        deleteAffectation(id);
                    }
                }
            }
        });

        // Curseur "main" au survol de la colonne Actions
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int viewCol = table.columnAtPoint(e.getPoint());
                int modelCol = table.convertColumnIndexToModel(viewCol);
                table.setCursor(modelCol == MODEL_COL_ACTIONS
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

        JLabel emptyIcon = new JLabel(icon(FontAwesomeSolid.CLIPBOARD_LIST, 44, COLOR_TEXT_LIGHT), SwingConstants.CENTER);
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyTitleLabel = new JLabel("Aucune affectation", SwingConstants.CENTER);
        emptyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emptyTitleLabel.setForeground(COLOR_TEXT);
        emptyTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptySubtitleLabel = new JLabel("Cliquez sur « Nouvelle Affectation » pour commencer.", SwingConstants.CENTER);
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

    /** Récupère l'ID (masqué) de la ligne sélectionnée. */
    private long getIdAt(int row) {
        Object value = tableModel.getValueAt(row, MODEL_COL_ID);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Affecter> affectations = apiService.getAllAffectations();
            for (Affecter aff : affectations) {
                String employe = (aff.getCodeemp() != null ? aff.getCodeemp() : "N/A")
                        + " - " + (aff.getNomEmploye() != null ? aff.getNomEmploye() : "N/A");
                String lieu = (aff.getCodelieu() != null ? aff.getCodelieu() : "N/A")
                        + " - " + (aff.getDesignationLieu() != null ? aff.getDesignationLieu() : "N/A");
                tableModel.addRow(new Object[]{
                        aff.getId(),
                        employe,
                        lieu,
                        aff.getDateAffectation(),
                        "actions"
                });
            }
            lblCount.setText(affectations.size() + " affectations");
            if (affectations.isEmpty()) {
                showEmpty("Aucune affectation",
                        "Cliquez sur « Nouvelle Affectation » pour créer la première.");
            } else {
                showTable();
            }
        } catch (Exception e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void editAffectation(Long id) {
        try {
            List<Affecter> affectations = apiService.getAllAffectations();
            Affecter aff = affectations.stream()
                    .filter(a -> id.equals(a.getId()))
                    .findFirst().orElse(null);
            if (aff != null) showAffectationDialog(aff);
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void showAffectationDialog(Affecter existing) {
        boolean isEdit = existing != null;
        try {
            List<Employee> employees = apiService.getAllEmployees();
            List<Lieu> lieus = apiService.getAllLieus();

            if (employees.isEmpty() || lieus.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Créez d'abord des employés et des lieux !",
                        "Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parent, isEdit ? "Modifier l'affectation" : "Nouvelle affectation", true);
            dialog.setSize(500, 440);
            dialog.setMinimumSize(new Dimension(500, 440));
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(COLOR_WHITE);

            // ---------- En-tête ----------
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(COLOR_HEADER);
            headerPanel.setBorder(new EmptyBorder(16, 25, 16, 25));

            JLabel titleLabel = new JLabel(isEdit ? "Modifier l'affectation" : "Nouvelle affectation");
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

            // Champs Employé et Lieu : menus déroulants
            JComboBox<String> cbEmployee = createCombo();
            JComboBox<String> cbLieu = createCombo();

            // Champ date : sélection dans le calendrier (saisie manuelle désactivée)
            DatePickerSettings dateSettings = new DatePickerSettings();
            dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
            dateSettings.setFormatForDatesBeforeCommonEra("yyyy-MM-dd");
            dateSettings.setAllowKeyboardEditing(false);   // pas de saisie manuelle
            dateSettings.setAllowEmptyDates(false);        // une date est toujours choisie

            DatePicker datePicker = new DatePicker(dateSettings);
            datePicker.setDateToToday();
            datePicker.setPreferredSize(new Dimension(200, 38));
            datePicker.getComponentDateTextField().setFont(FONT_FIELD);
            datePicker.getComponentDateTextField().setForeground(COLOR_TEXT);
            datePicker.getComponentDateTextField().setBackground(COLOR_WHITE);
            datePicker.getComponentToggleCalendarButton().setToolTipText("Choisir une date dans le calendrier");
            datePicker.getComponentToggleCalendarButton().setCursor(new Cursor(Cursor.HAND_CURSOR));

            for (Employee e : employees) {
                cbEmployee.addItem(e.getCodeFormate() + " - " + e.getNom() + " " + e.getPrenom());
            }
            for (Lieu l : lieus) {
                cbLieu.addItem(l.getCodeFormate() + " - " + l.getDesignation());
            }

            // Pré-remplissage en mode édition
            if (isEdit) {
                selectByCode(cbEmployee, existing.getCodeemp());
                selectByCode(cbLieu, existing.getCodelieu());
                Object dateValue = existing.getDateAffectation();
                if (dateValue != null) {
                    try {
                        datePicker.setDate(LocalDate.parse(String.valueOf(dateValue).trim()));
                    } catch (Exception ignore) {
                        datePicker.setDateToToday();
                    }
                }

                // =======================================================
                // CORRECTION : Seul l'employé est désactivé en modification.
                // Le lieu reste modifiable.
                // =======================================================
                cbEmployee.setEnabled(false);
                cbEmployee.setForeground(COLOR_TEXT);

                // Le lieu reste activé pour permettre la modification
                cbLieu.setEnabled(true);
                cbLieu.setForeground(COLOR_TEXT);
            }

            JLabel labelEmployee = createRequiredLabel("Employé");
            JLabel labelLieu = createRequiredLabel("Lieu");
            JLabel labelDate = createRequiredLabel("Date");

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            contentPanel.add(labelEmployee, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(cbEmployee, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            contentPanel.add(labelLieu, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(cbLieu, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            contentPanel.add(labelDate, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(datePicker, gbc);

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
                    Object selEmp = cbEmployee.getSelectedItem();
                    Object selLieu = cbLieu.getSelectedItem();
                    if (selEmp == null || selLieu == null) {
                        JOptionPane.showMessageDialog(dialog,
                                "Veuillez sélectionner un employé et un lieu",
                                "Erreur",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String empCode = ((String) selEmp).split(" - ")[0];
                    String lieuCode = ((String) selLieu).split(" - ")[0];

                    LocalDate date = datePicker.getDate();
                    if (date == null) {
                        JOptionPane.showMessageDialog(dialog,
                                "Veuillez choisir une date dans le calendrier.",
                                "Erreur",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (isEdit) {
                        apiService.updateAffectation(existing.getId(), empCode, lieuCode, date);
                        JOptionPane.showMessageDialog(dialog,
                                "Affectation modifiée avec succès",
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        apiService.createAffectation(empCode, lieuCode, date);
                        JOptionPane.showMessageDialog(dialog,
                                "Affectation créée avec succès",
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
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void deleteAffectation(Long id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer l'affectation " + id + " ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                apiService.deleteAffectation(id);
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Affectation supprimée avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                showError("Erreur de suppression", e.getMessage());
            }
        }
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

    private static void selectByCode(JComboBox<String> combo, String code) {
        if (code == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.startsWith(code)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
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

    /** Menu déroulant stylé, aligné sur la hauteur des champs texte. */
    private static JComboBox<String> createCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(FONT_FIELD);
        combo.setForeground(COLOR_TEXT);
        combo.setBackground(COLOR_WHITE);
        combo.setBorder(createFieldBorder(COLOR_BORDER));
        combo.setPreferredSize(new Dimension(200, 38));
        combo.setMaximumRowCount(8);
        return combo;
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

    private class DateCellRenderer extends TextCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            return c;
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

    // Les deux boutons sont TOUJOURS sur la même ligne, centrés verticalement.
    private class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final RoundedButton btnEdit;
        private final RoundedButton btnDelete;

        ButtonRenderer() {
            super(new GridBagLayout());
            setOpaque(true);

            btnEdit = new RoundedButton("Modifier", COLOR_PRIMARY, COLOR_PRIMARY_HOVER);
            btnEdit.setFont(FONT_SMALL_BUTTON);
            btnEdit.setBorder(new EmptyBorder(6, 12, 6, 12));
            btnEdit.setIcon(icon(FontAwesomeSolid.EDIT, 12, COLOR_WHITE));

            btnDelete = new RoundedButton("Supprimer", COLOR_DANGER, COLOR_DANGER_HOVER);
            btnDelete.setFont(FONT_SMALL_BUTTON);
            btnDelete.setBorder(new EmptyBorder(6, 12, 6, 12));
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