package com.gestion.client.components;

import com.gestion.client.theme.ThemeConstants;

import javax.swing.*;
import java.awt.*;

public class ModernDialog extends JDialog {
    private JPanel contentPanel;

    public ModernDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeConstants.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void addField(String label, JComponent component, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(ThemeConstants.FONT_BODY);
        labelComp.setForeground(ThemeConstants.TEXT_PRIMARY);
        contentPanel.add(labelComp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(component, gbc);
    }

    public void addButtons(JButton... buttons) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        for (JButton btn : buttons) {
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }
}
