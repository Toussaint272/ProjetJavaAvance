package com.gestion.client.view;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Menu latéral de l'application — interface professionnelle.
 * (Seule la présentation a été revue, la logique de navigation est inchangée.)
 * Icônes vectorielles Ikonli (Font Awesome 5).
 */
public class SideMenu extends JPanel {

    private int selectedIndex = 0;

    private final String[] menuItems = {
            "Accueil",
            "Employés",
            "Lieux",
            "Affectations",
    };

    private final Ikon[] menuIcons = {
            FontAwesomeSolid.HOME,
            FontAwesomeSolid.USER,
            FontAwesomeSolid.MAP_MARKER_ALT,
            FontAwesomeSolid.CLIPBOARD_LIST,
            FontAwesomeSolid.CHART_BAR
    };

    private final Runnable onMenuClick;
    private final MenuItemButton[] buttons = new MenuItemButton[menuItems.length];

    // ==================== COULEURS ====================
    private static final Color COLOR_BG        = new Color(44, 62, 80);
    private static final Color COLOR_PRIMARY   = new Color(41, 128, 185);
    private static final Color COLOR_HOVER     = new Color(54, 76, 99);
    private static final Color COLOR_TEXT      = new Color(203, 213, 220);
    private static final Color COLOR_MUTED     = new Color(150, 158, 168);

    public SideMenu(Runnable onMenuClick) {
        this.onMenuClick = onMenuClick;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(230, 0));

        // ================================================================
        // EN-TÊTE : LOGO + VERSION
        // ================================================================
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(24, 20, 20, 20));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);

        JPanel logoBadge = new IconBadge(
                FontIcon.of(FontAwesomeSolid.BUILDING, 20, Color.WHITE),
                COLOR_PRIMARY);
        logoPanel.add(logoBadge);

        JLabel logoLabel = new JLabel("GESTION");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);

        headerPanel.add(logoPanel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("v2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(COLOR_MUTED);
        headerPanel.add(versionLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ================================================================
        // MENU
        // ================================================================
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(COLOR_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(3, 10, 3, 10);

        for (int i = 0; i < menuItems.length; i++) {
            MenuItemButton btn = new MenuItemButton(
                    menuItems[i],
                    FontIcon.of(menuIcons[i], 16, COLOR_TEXT));
            buttons[i] = btn;

            final int index = i;
            btn.addActionListener(e -> {
                selectedIndex = index;
                updateSelection();
                onMenuClick.run();
            });

            gbc.gridy = i;
            menuPanel.add(btn, gbc);
        }

        // Espace en bas
        gbc.gridy = menuItems.length;
        gbc.weighty = 1.0;
        menuPanel.add(Box.createVerticalGlue(), gbc);

        add(menuPanel, BorderLayout.CENTER);

        // ================================================================
        // PIED DE PAGE
        // ================================================================
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(COLOR_BG);
        footerPanel.setBorder(new EmptyBorder(16, 20, 24, 20));

        JLabel footerLabel = new JLabel("© 2026 - v2.0");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(COLOR_MUTED);
        footerPanel.add(footerLabel);

        add(footerPanel, BorderLayout.SOUTH);

        // Sélection initiale
        updateSelection();
    }

    /** Met à jour l'apparence des boutons selon la sélection courante. */
    private void updateSelection() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected(i == selectedIndex);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    // ================================================================
    // BOUTON DE MENU PERSONNALISÉ
    // ================================================================
    private static class MenuItemButton extends JButton {
        private static final int ARC = 10;

        private final Color normalForeground;
        private boolean selected = false;
        private boolean hover = false;

        MenuItemButton(String text, Icon icon) {
            super(text, icon);
            this.normalForeground = COLOR_TEXT;

            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(14);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(normalForeground);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        public void setSelected(boolean selected) {
            this.selected = selected;
            setForeground(selected ? Color.WHITE : normalForeground);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fond arrondi selon l'état
            Color bg = COLOR_BG;
            if (selected) {
                bg = COLOR_PRIMARY;
            } else if (hover) {
                bg = COLOR_HOVER;
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

            // Barre indicatrice blanche à gauche quand l'élément est sélectionné
            if (selected) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(5, 9, 4, getHeight() - 18, 4, 4);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // La bordure est peinte par paintComponent (forme arrondie).
        }
    }

    // ================================================================
    // PASTILLE D'ICÔNE (logo)
    // ================================================================
    private static class IconBadge extends JPanel {
        private final Icon icon;
        private final Color color;

        IconBadge(Icon icon, Color color) {
            this.icon = icon;
            this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(38, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();

            if (icon != null) {
                int x = (getWidth() - icon.getIconWidth()) / 2;
                int y = (getHeight() - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g, x, y);
            }
        }
    }
}
