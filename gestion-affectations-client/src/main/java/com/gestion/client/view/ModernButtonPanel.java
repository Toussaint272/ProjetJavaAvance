package com.gestion.client.view;

import javax.swing.*;
import java.awt.*;

public class ModernButtonPanel extends JPanel {

    public ModernButtonPanel(JButton... buttons) {
        setLayout(new GridLayout(1, buttons.length, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setBackground(new Color(248, 249, 250));

        for (JButton btn : buttons) {
            styleButton(btn);
            add(btn);
        }
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Effet au survol
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 152, 219));
            }
        });
    }
}
