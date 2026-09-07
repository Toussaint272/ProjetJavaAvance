package com.gestion.client.view;

import com.gestion.client.service.ApiService;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Panneau d'accueil (tableau de bord) — interface professionnelle.
 * Cartes compteurs + histogramme des affectations par lieu.
 * (Seule la présentation a été revue, la logique métier est inchangée.)
 * Icônes vectorielles Ikonli (Font Awesome 5).
 */
public class AccueilPanel extends JPanel {

    private final ApiService apiService;
    private JLabel lblEmployes, lblLieus, lblAffectations, lblDateTime;
    private Timer timer;
    private HistogramPanel histogramPanel;

    // ==================== COULEURS ====================
    private static final Color COLOR_PRIMARY       = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS       = new Color(39, 174, 96);
    private static final Color COLOR_DANGER        = new Color(231, 76, 60);
    private static final Color COLOR_HEADER        = new Color(44, 62, 80);
    private static final Color COLOR_BACKGROUND    = new Color(236, 240, 241);
    private static final Color COLOR_WHITE         = Color.WHITE;
    private static final Color COLOR_TEXT          = new Color(44, 62, 80);
    private static final Color COLOR_TEXT_LIGHT    = new Color(127, 140, 141);
    private static final Color COLOR_BORDER        = new Color(213, 219, 224);

    public AccueilPanel(ApiService apiService) {
        this.apiService = apiService;
        initUI();
        loadStatistics();
        startClock();

        // ✅ Rafraîchit automatiquement les compteurs + l'histogramme
        // dès que le panneau redevient visible (retour sur l'accueil).
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshStats();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // ================================================================
        // EN-TÊTE : TITRE + DATE/HEURE
        // ================================================================
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COLOR_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 25, 16, 25)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(COLOR_WHITE);

        JLabel iconLabel = new JLabel(icon(FontAwesomeSolid.CHART_BAR, 24, COLOR_PRIMARY));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Tableau de Bord");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        lblDateTime = new JLabel();
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDateTime.setForeground(COLOR_TEXT_LIGHT);
        lblDateTime.setIcon(icon(FontAwesomeSolid.CLOCK, 14, COLOR_TEXT_LIGHT));
        headerPanel.add(lblDateTime, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ================================================================
        // CONTENU CENTRAL : CARTES + HISTOGRAMME
        // ================================================================
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // --- Cartes compteurs ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(COLOR_BACKGROUND);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        cardsPanel.setPreferredSize(new Dimension(0, 140));

        lblEmployes = new JLabel("0");
        lblLieus = new JLabel("0");
        lblAffectations = new JLabel("0");

        cardsPanel.add(createStatCard(FontAwesomeSolid.USER, "Employés", COLOR_PRIMARY, lblEmployes));
        cardsPanel.add(createStatCard(FontAwesomeSolid.MAP_MARKER_ALT, "Lieux", COLOR_SUCCESS, lblLieus));
        cardsPanel.add(createStatCard(FontAwesomeSolid.CLIPBOARD_LIST, "Affectations", COLOR_DANGER, lblAffectations));

        cardsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(cardsPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // --- Carte histogramme ---
        JPanel chartCard = new JPanel(new BorderLayout(0, 10));
        chartCard.setBackground(COLOR_WHITE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(15, 18, 15, 18)));
        chartCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel chartHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chartHeader.setBackground(COLOR_WHITE);

        JLabel chartIcon = new JLabel(icon(FontAwesomeSolid.CHART_PIE, 16, COLOR_PRIMARY));
        chartHeader.add(chartIcon);

        JLabel chartTitle = new JLabel("Affectations par lieu");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        chartTitle.setForeground(COLOR_TEXT);
        chartHeader.add(chartTitle);

        chartCard.add(chartHeader, BorderLayout.NORTH);

        histogramPanel = new HistogramPanel();
        chartCard.add(histogramPanel, BorderLayout.CENTER);

        centerPanel.add(chartCard);

        add(centerPanel, BorderLayout.CENTER);

        // ================================================================
        // PIED DE PAGE
        // ================================================================
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(COLOR_BACKGROUND);

        JLabel footerLabel = new JLabel("© 2026 - Système de Gestion des Affectations v2.0");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(COLOR_TEXT_LIGHT);
        footerPanel.add(footerLabel);

        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Crée une carte statistique professionnelle :
     * pastille d'icône colorée + grande valeur colorée + titre.
     */
    private JPanel createStatCard(Ikon ikon, String title, Color color, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(20, 24, 20, 24)));

        // Pastille circulaire colorée avec l'icône
        JPanel iconBadge = new CircleBadge(color, icon(ikon, 26, COLOR_WHITE));
        card.add(iconBadge, BorderLayout.WEST);

        // Bloc central : valeur + titre
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(COLOR_WHITE);
        textPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(COLOR_TEXT_LIGHT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(titleLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void loadStatistics() {
        // 1) Compteurs globaux
        try {
            lblEmployes.setText(String.valueOf(apiService.getAllEmployees().size()));
        } catch (Exception e) {
            lblEmployes.setText("—");
        }
        try {
            lblLieus.setText(String.valueOf(apiService.getAllLieus().size()));
        } catch (Exception e) {
            lblLieus.setText("—");
        }
        try {
            lblAffectations.setText(String.valueOf(apiService.getAllAffectations().size()));
        } catch (Exception e) {
            lblAffectations.setText("—");
        }

        // 2) Histogramme des affectations par lieu
        try {
            Map<String, Long> stats = apiService.getStatistics();

            // Normalise + trie par valeur décroissante
            Map<String, Long> sorted = new LinkedHashMap<>();
            if (stats != null) {
                stats.entrySet().stream()
                        .sorted((a, b) -> Long.compare(toLong(b.getValue()), toLong(a.getValue())))
                        .forEach(e -> sorted.put(e.getKey(), toLong(e.getValue())));
            }
            histogramPanel.setData(sorted);
        } catch (Exception e) {
            histogramPanel.setErrorMessage(e.getMessage());
        }
    }

    /** Convertit n'importe quel nombre (Integer, Long...) en long, sans ClassCastException. */
    private static long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void startClock() {
        timer = new Timer(1000, e -> {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            lblDateTime.setText(now);
        });
        timer.start();
    }

    public void refreshStats() {
        loadStatistics();
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private static Icon icon(Ikon ikon, int size, Color color) {
        return FontIcon.of(ikon, size, color);
    }

    /** Pastille circulaire colorée contenant une icône. */
    private static class CircleBadge extends JPanel {
        private final Color color;
        private final Icon icon;

        CircleBadge(Color color, Icon icon) {
            this.color = color;
            this.icon = icon;
            setOpaque(false);
            setPreferredSize(new Dimension(58, 58));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.dispose();

            if (icon != null) {
                int x = (getWidth() - icon.getIconWidth()) / 2;
                int y = (getHeight() - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g, x, y);
            }
        }
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
            // Normalise : Integer/Long -> Long (évite tout ClassCastException)
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

            if (errorMessage != null) {
                drawCenteredText(g2, "Erreur de chargement : " + errorMessage,
                        new Font("Segoe UI", Font.PLAIN, 14), new Color(192, 57, 43));
                g2.dispose();
                return;
            }

            if (data.isEmpty()) {
                drawCenteredText(g2, "Aucune affectation enregistrée pour le moment.",
                        new Font("Segoe UI", Font.PLAIN, 15), COLOR_TEXT_LIGHT);
                g2.dispose();
                return;
            }

            int padLeft = 50;
            int padRight = 30;
            int padTop = 35;
            int padBottom = 55;

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

                // Libellé sous la barre (tronqué si nécessaire)
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
    }
}
