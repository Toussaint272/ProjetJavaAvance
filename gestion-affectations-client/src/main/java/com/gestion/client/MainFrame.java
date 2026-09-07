package com.gestion.client;

import com.gestion.client.service.ApiService;
import com.gestion.client.view.*;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Fenêtre principale de l'application — interface professionnelle.
 * (Seule la présentation a été revue, la logique de navigation est inchangée.)
 */
public class MainFrame extends JFrame {

    private final ApiService apiService;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SideMenu sideMenu;
    private AccueilPanel accueilPanel;

    // Couleurs cohérentes avec les panneaux
    private static final Color COLOR_BACKGROUND = new Color(236, 240, 241);
    private static final Color COLOR_PRIMARY    = new Color(41, 128, 185);

    // Ordre des panneaux (doit correspondre à l'index du menu latéral)
    private static final String[] PANELS = {"Accueil", "Employés", "Lieux", "Affectations", "Statistiques"};

    public MainFrame() {
        this.apiService = new ApiService();
        initUI();
    }

    private void initUI() {
        setTitle("Gestion des Affectations des Employés");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BACKGROUND);

        // Icône de la fenêtre (icône vectorielle Ikonli)
        setAppIcon();

        // Menu latéral
        sideMenu = new SideMenu(this::switchPanel);
        add(sideMenu, BorderLayout.WEST);

        // Panneau de contenu
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ajouter les panels
        accueilPanel = new AccueilPanel(apiService);
        contentPanel.add(accueilPanel, PANELS[0]);
        contentPanel.add(new EmployeePanel(apiService), PANELS[1]);
        contentPanel.add(new LieuPanel(apiService), PANELS[2]);
        contentPanel.add(new AffectationPanel(apiService), PANELS[3]);
        contentPanel.add(new StatisticsPanel(apiService), PANELS[4]);

        add(contentPanel, BorderLayout.CENTER);

        // Afficher l'accueil par défaut
        cardLayout.show(contentPanel, PANELS[0]);
    }

    /** Change le panneau affiché selon la sélection du menu latéral. */
    private void switchPanel() {
        int index = sideMenu.getSelectedIndex();
        if (index >= 0 && index < PANELS.length) {
            cardLayout.show(contentPanel, PANELS[index]);

            // ✅ Rafraîchit les compteurs à chaque retour sur l'accueil
            if (index == 0) {
                accueilPanel.refreshStats();
            }
        }
    }

    /** Définit l'icône de la fenêtre à partir d'une icône vectorielle Ikonli. */
    private void setAppIcon() {
        try {
            Icon icon = FontIcon.of(FontAwesomeSolid.BUILDING, 48, COLOR_PRIMARY);
            BufferedImage image = new BufferedImage(
                    icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            icon.paintIcon(this, g, 0, 0);
            g.dispose();
            setIconImage(image);
        } catch (Exception ignored) {
            // Si Ikonli n'est pas disponible, on garde l'icône par défaut
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MainFrame().setVisible(true);
        });
    }
}
