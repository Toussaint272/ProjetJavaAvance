package com.gestion.client.components;

import com.gestion.client.theme.ThemeConstants;

import javax.swing.*;
import java.awt.*;

public class ModernTextField extends JTextField {
    public ModernTextField() {
        this(20);
    }

    public ModernTextField(int columns) {
        super(columns);
        setFont(ThemeConstants.FONT_BODY);
        setBackground(Color.WHITE);
        setForeground(ThemeConstants.TEXT_PRIMARY);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        setPreferredSize(new Dimension(200, 38));
    }

    public ModernTextField(String text) {
        this();
        setText(text);
    }
}
