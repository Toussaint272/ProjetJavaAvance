package com.gestion.client.view;

import com.gestion.client.service.ApiService;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Panneau de statistiques — histogramme des affectations par lieu.
 * (Seule la présentation a été revue, la logique métier est inchangée :
 *  les données proviennent toujours de apiService.getStatistics().)
 */
public class StatisticsPanel extends JPanel {

    private final ApiService apiService;
    private final HistogramPanel histogramPanel;
    private final JLabel lblTotal;
    private final JLabel lblLieux;

    // ==================== COULEURS ====================
    private static final Color COLOR_PRIMARY        = new Color(41, 128, 185);
    private static final Color COLOR_PRIMARY_HOVER  = new Color(33, 103, 151);
    private static final Color COLOR_HEADER         = new Color(44, 62, 80);
    private static final Color COLOR_NEUTRAL        = new Color(116, 125, 140);
    private static final Color COLOR_NEUTRAL_HOVER  = new Color(92, 100, 114);
    private static final Color COLOR_BACKGROUND     = new Color(236, 240, 241);
    private static final Color COLOR_WHITE          = Color.WHITE;
    private static final Color COLOR_TEXT           = new Color(44, 62, 80);
    private static final Color COLOR_TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color COLOR_BORDER         = new Color(213, 219, 224);

    public StatisticsPanel(ApiService apiService) {
        this.apiService = apiService;
        this.histogramPanel = new HistogramPanel();
        this.lblTotal = new JLabel("0");
        this.lblLieux = new JLabel("0");

        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // ================================================================
        // EN-TÊTE : TITRE + BOUTON RAFRAÎCHIR
        // ================================================================
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COLOR_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(14, 20, 14, 20)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(COLOR_WHITE);

        JLabel iconLabel = new JLabel(icon(FontAwesomeSolid.CHART_BAR, 22, COLOR_PRIMARY));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Statistiques des Affectations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TEXT);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        JButton btnRefresh = new RoundedButton("Rafraîchir", COLOR_PRIMARY, COLOR_PRIMARY_HOVER);
        btnRefresh.setIcon(icon(FontAwesomeSolid.SYNC, 14, COLOR_WHITE));
        btnRefresh.setToolTipText("Recharger les statistiques depuis le serveur");
        btnRefresh.addActionListener(e -> loadData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ================================================================
        // ZONE CENTRALE : HISTOGRAMME
        // ================================================================
        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(COLOR_WHITE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(15, 15, 15, 15)));

        chartCard.add(histogramPanel, BorderLayout.CENTER);
        add(chartCard, BorderLayout.CENTER);

        // ================================================================
        // BAS : RÉSUMÉ (total + nombre de lieux)
        // ================================================================
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        summaryPanel.setBackground(COLOR_WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 20, 12, 20)));

        summaryPanel.add(createSummaryItem(FontAwesomeSolid.CLIPBOARD_LIST,
                "Total affectations", lblTotal, COLOR_PRIMARY));
        summaryPanel.add(createSummaryItem(FontAwesomeSolid.MAP_MARKER_ALT,
                "Lieux concernés", lblLieux, COLOR_HEADER));

        add(summaryPanel, BorderLayout.SOUTH);
    }

    /** Petit bloc « icône + valeur + libellé » pour le résumé du bas. */
    private JPanel createSummaryItem(Ikon ikon, String label, JLabel valueLabel, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(COLOR_WHITE);

        JLabel iconLabel = new JLabel(icon(ikon, 18, color));
        panel.add(iconLabel);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);
        panel.add(valueLabel);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelLabel.setForeground(COLOR_TEXT_LIGHT);
        panel.add(labelLabel);

        return panel;
    }

    private void loadData() {
        try {
            Map<String, Long> stats = apiService.getStatistics();

            // Préserver l'ordre d'insertion (trié par nombre décroissant pour un rendu lisible)
            Map<String, Long> sorted = new LinkedHashMap<>();
            if (stats != null) {
                stats.entrySet().stream()
                        .sorted((a, b) -> Long.compare(toLong(b.getValue()), toLong(a.getValue())))
                        .forEach(e -> sorted.put(e.getKey(), toLong(e.getValue())));
            }

            histogramPanel.setData(sorted);

            long total = sorted.values().stream().mapToLong(Long::longValue).sum();
            lblTotal.setText(String.valueOf(total));
            lblLieux.setText(String.valueOf(sorted.size()));
        } catch (Exception e) {
            histogramPanel.setData(null);
            histogramPanel.setErrorMessage(e.getMessage());
            lblTotal.setText("—");
            lblLieux.setText("—");
        }
    }

    /** Convertit n'importe quel nombre (Integer, Long, Double...) en long, sans ClassCastException. */
    private static long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private static Icon icon(Ikon ikon, int size, Color color) {
        return FontIcon.of(ikon, size, color);
    }

    // ================================================================
    // HISTOGRAMME (dessin personnalisé)
    // ================================================================
    private static class HistogramPanel extends JPanel {

        private Map<String, Long> data = new LinkedHashMap<>();
        private String errorMessage = null;

        HistogramPanel() {
            setBackground(COLOR_WHITE);
        }

        void setData(Map<String, Long> data) {
            // Normalise les valeurs : convertit Integer/Long/etc. en Long
            // pour éviter tout ClassCastException lors du dessin.
            LinkedHashMap<String, Long> safe = new LinkedHashMap<>();
            if (data != null) {
                for (Map.Entry<String, Long> e : data.entrySet()) {
                    safe.put(e.getKey(), toLong(e.getValue()));
                }
            }
            this.data = safe;
            this.errorMessage = null;
            repaint();
        }

        void setErrorMessage(String message) {
            this.errorMessage = message;
            this.data = new LinkedHashMap<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Message d'erreur
            if (errorMessage != null) {
                drawCenteredText(g2, "Erreur de chargement : " + errorMessage,
                        new Font("Segoe UI", Font.PLAIN, 14), COLOR_DANGER_TEXT);
                g2.dispose();
                return;
            }

            // Aucune donnée
            if (data.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.setColor(COLOR_TEXT_LIGHT);
                String msg = "Aucune affectation enregistrée pour le moment.";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                g2.drawString(msg, x, y);
                g2.dispose();
                return;
            }

            int padLeft = 50;
            int padRight = 30;
            int padTop = 40;
            int padBottom = 60;

            int chartWidth = Math.max(10, getWidth() - padLeft - padRight);
            int chartHeight = Math.max(10, getHeight() - padTop - padBottom);

            long maxValue = 1;
            for (Long v : data.values()) {
                if (v != null && v > maxValue) maxValue = v;
            }

            int n = data.size();
            double slot = (double) chartWidth / n;
            double barWidth = Math.min(90, slot * 0.55);
            barWidth = Math.max(12, barWidth);

            // Ligne de base (axe X)
            int baseY = padTop + chartHeight;
            g2.setColor(new Color(189, 195, 199));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(padLeft, baseY, padLeft + chartWidth, baseY);

            int i = 0;
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                long value = (entry.getValue() == null) ? 0 : entry.getValue();
                String key = (entry.getKey() == null) ? "?" : entry.getKey();

                double barHeight = (value / (double) maxValue) * chartHeight;
                int barX = (int) (padLeft + slot * i + (slot - barWidth) / 2);
                int barY = (int) (baseY - barHeight);
                int barW = (int) barWidth;
                int barH = (int) barHeight;

                // Barre avec dégradé vertical
                GradientPaint gradient = new GradientPaint(
                        0, barY, COLOR_PRIMARY,
                        0, baseY, new Color(63, 158, 219));
                g2.setPaint(gradient);
                g2.fillRoundRect(barX, barY, barW, barH, 10, 10);

                // Valeur au-dessus de la barre
                g2.setColor(COLOR_TEXT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String valueText = String.valueOf(value);
                int vx = barX + (barW - fm.stringWidth(valueText)) / 2;
                int vy = Math.max(fm.getAscent() + 4, barY - 6);
                g2.drawString(valueText, vx, vy);

                // Libellé (code du lieu) sous la barre, tronqué si trop long
                g2.setColor(COLOR_TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics kfm = g2.getFontMetrics();
                String label = key;
                while (kfm.stringWidth(label) > slot - 6 && label.length() > 1) {
                    label = label.substring(0, label.length() - 1);
                }
                if (!label.equals(key)) {
                    label = label.substring(0, Math.max(1, label.length() - 3)) + "…";
                }
                int lx = (int) (padLeft + slot * i + (slot - kfm.stringWidth(label)) / 2);
                g2.drawString(label, lx, baseY + 20);

                i++;
            }

            g2.dispose();
        }

        private void drawCenteredText(Graphics2D g2, String text, Font font, Color color) {
            g2.setFont(font);
            g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2;
            g2.drawString(text, x, y);
        }

        private static final Color COLOR_DANGER_TEXT = new Color(192, 57, 43);
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

            setFont(new Font("Segoe UI", Font.BOLD, 13));
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