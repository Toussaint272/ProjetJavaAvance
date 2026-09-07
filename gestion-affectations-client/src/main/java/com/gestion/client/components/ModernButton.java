package com.gestion.client.components;

import com.gestion.client.theme.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private boolean isRounded = true;

    public ModernButton(String text) {
        this(text, ThemeConstants.PRIMARY);
    }

    public ModernButton(String text, Color color) {
        super(text);
        this.backgroundColor = color;
        this.hoverColor = color.brighter();
        this.pressedColor = color.darker();

        setFont(ThemeConstants.FONT_BODY);
        setForeground(Color.WHITE);
        setBackground(backgroundColor);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(backgroundColor);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressedColor);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(hoverColor);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isRounded) {
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConstants.BORDER_RADIUS, ThemeConstants.BORDER_RADIUS);
        } else {
            super.paintComponent(g);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    public void setRounded(boolean rounded) {
        this.isRounded = rounded;
        repaint();
    }
}
