package com.gestion.client.view;

import com.gestion.client.model.Lieu;
import com.gestion.client.service.ApiService;

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
import java.util.List;

/**
 * Panneau de gestion des lieux — interface professionnelle.
 * (Seule la présentation a été revue, la logique métier est inchangée.)
 * Icônes vectorielles Ikonli (Font Awesome 5).
 */
public class LieuPanel extends JPanel {

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
    private static final Color COLOR_DISABLED_BG    = new Color(245, 246, 247);

    // ==================== POLICES ====================
    private static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BUTTON       = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_SMALL_BUTTON = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_TABLE        = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TABLE_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL        = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_FIELD        = new Font("Segoe UI", Font.PLAIN, 14);

    public LieuPanel(ApiService apiService) {
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

        JLabel iconLabel = new JLabel(icon(FontAwesomeSolid.MAP_MARKER_ALT, 22, COLOR_TEXT));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Gestion des Lieux");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT);
        titlePanel.add(titleLabel);

        lblCount = new JLabel("0 lieux");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCount.setForeground(COLOR_TEXT_LIGHT);
        titlePanel.add(lblCount);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // À droite : Rafraîchir (à gauche) puis Nouveau Lieu (à droite)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(COLOR_WHITE);

        JButton btnRefresh = new RoundedButton("Rafraîchir", COLOR_NEUTRAL, COLOR_NEUTRAL_HOVER);
        btnRefresh.setIcon(icon(FontAwesomeSolid.SYNC, 14, COLOR_WHITE));
        btnRefresh.setToolTipText("Recharger la liste depuis le serveur");
        btnRefresh.addActionListener(e -> loadData());

        JButton btnAdd = new RoundedButton("Nouveau Lieu", COLOR_SUCCESS, COLOR_SUCCESS_HOVER);
        btnAdd.setIcon(icon(FontAwesomeSolid.PLUS, 15, COLOR_WHITE));
        btnAdd.setToolTipText("Créer un nouveau lieu");
        btnAdd.addActionListener(e -> showLieuDialog(null));

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAdd);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // ================================================================
    // TABLEAU + ÉTAT VIDE
    // ================================================================
    private JPanel buildTableArea() {
        String[] columns = {"Code", "Désignation", "Province", "Actions"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(220);

        // Renderers personnalisés
        table.getColumnModel().getColumn(0).setCellRenderer(new CodeCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new TextCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new TextCellRenderer());
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
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) {
                    String code = (String) tableModel.getValueAt(row, 0);
                    Rectangle cellRect = table.getCellRect(row, col, true);
                    int xInCell = e.getX() - cellRect.x;
                    if (xInCell < cellRect.width / 2) {
                        editLieu(code);
                    } else {
                        deleteLieu(code);
                    }
                }
            }
        });

        // Curseur "main" au survol de la colonne Actions
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 3
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

        JLabel emptyIcon = new JLabel(icon(FontAwesomeSolid.MAP_MARKER_ALT, 44, COLOR_TEXT_LIGHT), SwingConstants.CENTER);
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyTitleLabel = new JLabel("Aucun lieu", SwingConstants.CENTER);
        emptyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emptyTitleLabel.setForeground(COLOR_TEXT);
        emptyTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptySubtitleLabel = new JLabel("Cliquez sur « Nouveau Lieu » pour commencer.", SwingConstants.CENTER);
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
    // MÉTHODES FONCTIONNELLES (inchangées)
    // ================================================================

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Lieu> lieus = apiService.getAllLieus();
            for (Lieu lieu : lieus) {
                tableModel.addRow(new Object[]{
                        lieu.getCodeFormate(),
                        lieu.getDesignation(),
                        lieu.getProvince(),
                        "actions"
                });
            }
            lblCount.setText(lieus.size() + " lieux");
            if (lieus.isEmpty()) {
                showEmpty("Aucun lieu",
                        "Cliquez sur « Nouveau Lieu » pour créer le premier.");
            } else {
                showTable();
            }
        } catch (Exception e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void showLieuDialog(Lieu lieu) {
        boolean isEdit = lieu != null;
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, isEdit ? "Modifier Lieu" : "Nouveau Lieu", true);
        dialog.setSize(480, 400);
        dialog.setMinimumSize(new Dimension(480, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_WHITE);

        // ---------- En-tête ----------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new EmptyBorder(16, 25, 16, 25));

        JLabel titleLabel = new JLabel(isEdit ? "Modifier le lieu" : "Nouveau lieu");
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

        JTextField txtCode = createDisabledField();
        JTextField txtDesignation = createField();
        JTextField txtProvince = createField();

        if (isEdit) {
            txtCode.setText(lieu.getCodeFormate());
            txtDesignation.setText(lieu.getDesignation());
            txtProvince.setText(lieu.getProvince());
        }

        JLabel labelCode = createInfoLabel("Code");
        JLabel labelDesignation = createRequiredLabel("Désignation");
        JLabel labelProvince = createRequiredLabel("Province");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(labelCode, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(labelDesignation, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(txtDesignation, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(labelProvince, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(txtProvince, gbc);

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
                String designation = txtDesignation.getText().trim();
                String province = txtProvince.getText().trim();

                // Mise en évidence des champs vides
                txtDesignation.setBorder(createFieldBorder(designation.isEmpty() ? COLOR_DANGER : COLOR_BORDER));
                txtProvince.setBorder(createFieldBorder(province.isEmpty() ? COLOR_DANGER : COLOR_BORDER));

                if (designation.isEmpty() || province.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Tous les champs sont obligatoires",
                            "Erreur",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Lieu l = new Lieu();
                l.setDesignation(designation);
                l.setProvince(province);

                if (isEdit) {
                    apiService.updateLieu(lieu.getCodeFormate(), l);
                    JOptionPane.showMessageDialog(dialog,
                            "Lieu modifié avec succès",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    apiService.createLieu(l);
                    JOptionPane.showMessageDialog(dialog,
                            "Lieu créé avec succès",
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

    private void editLieu(String code) {
        try {
            List<Lieu> lieus = apiService.getAllLieus();
            Lieu lieu = lieus.stream()
                    .filter(l -> l.getCodeFormate().equals(code))
                    .findFirst().orElse(null);
            if (lieu != null) showLieuDialog(lieu);
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void deleteLieu(String code) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer le lieu " + code + " ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                apiService.deleteLieu(code);
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Lieu supprimé avec succès",
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

    private static JTextField createDisabledField() {
        JTextField field = new JTextField(20);
        field.setFont(FONT_FIELD);
        field.setForeground(COLOR_TEXT_LIGHT);
        field.setBackground(COLOR_DISABLED_BG);
        field.setEnabled(false);
        field.setBorder(createFieldBorder(COLOR_BORDER));
        return field;
    }

    private static JLabel createRequiredLabel(String text) {
        JLabel label = new JLabel("<html>" + text + " <font color='#E74C3C'>*</font></html>");
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    private static JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_TEXT_LIGHT);
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

    private class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final RoundedButton btnEdit;
        private final RoundedButton btnDelete;

        ButtonRenderer() {
            // GridBagLayout : les deux boutons restent TOUJOURS sur la même ligne
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
