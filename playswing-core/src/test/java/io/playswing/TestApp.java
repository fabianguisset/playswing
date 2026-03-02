package io.playswing;

import javax.swing.*;
import java.awt.*;

/**
 * A simple Swing application used as the target for PlaySwing tests.
 */
public class TestApp {

    private JFrame frame;
    private JLabel resultLabel;

    public void start() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("PlaySwing Test App");
            frame.setLayout(new FlowLayout());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField(15);
            nameField.setName("nameField");
            nameLabel.setLabelFor(nameField);

            JLabel cityLabel = new JLabel("City:");
            JComboBox<String> cityCombo = new JComboBox<>(
                    new String[]{"Berlin", "London", "Paris", "Tokyo"});
            cityCombo.setName("cityCombo");

            JCheckBox agreeCheck = new JCheckBox("I agree");
            agreeCheck.setName("agreeCheck");

            JButton submitBtn = new JButton("Submit");
            submitBtn.setName("submitBtn");

            resultLabel = new JLabel("Result: none");
            resultLabel.setName("resultLabel");

            submitBtn.addActionListener(e ->
                    resultLabel.setText("Result: " + nameField.getText()
                            + " from " + cityCombo.getSelectedItem()
                            + (agreeCheck.isSelected() ? " (agreed)" : "")));

            frame.add(nameLabel);
            frame.add(nameField);
            frame.add(cityLabel);
            frame.add(cityCombo);
            frame.add(agreeCheck);
            frame.add(submitBtn);
            frame.add(resultLabel);

            frame.setSize(400, 200);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new TestApp().start();
    }
}
