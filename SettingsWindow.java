package com.javachat.ui;

import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsWindow {

    private final Preferences preferences =
            Preferences.userRoot().node("JavaChat");

    public void show(Stage ownerStage) {

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(30));
        root.setPrefSize(450, 450);

        Label title = new Label("⚙ Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Customize your JavaChat experience"
        );
        subtitle.setStyle("-fx-text-fill: #9ca3af;");

        Separator separator = new Separator();

        CheckBox notifications =
                new CheckBox("Enable Notifications");
        notifications.setSelected(
                preferences.getBoolean("notifications", true)
        );

        CheckBox messageSound =
                new CheckBox("Message Sound");
        messageSound.setSelected(
                preferences.getBoolean("messageSound", true)
        );

        CheckBox enterToSend =
                new CheckBox("Press Enter to Send");
        enterToSend.setSelected(
                preferences.getBoolean("enterToSend", true)
        );

        CheckBox darkMode =
                new CheckBox("Dark Mode");
        darkMode.setSelected(
                preferences.getBoolean("darkMode", false)
        );

        Label status = new Label();
        status.setStyle("-fx-font-size: 13px;");

        Button saveButton =
                new Button("SAVE SETTINGS");
        saveButton.setPrefWidth(180);

        Button closeButton =
                new Button("CLOSE");
        closeButton.setPrefWidth(180);

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, closeButton);

        root.getChildren().addAll(
                title, subtitle, separator,
                notifications, messageSound,
                enterToSend, darkMode,
                status, buttonBox
        );

        Scene scene = new Scene(root, 450, 450);

        var lightCss = getClass().getResource("/css/chat.css");
        if (lightCss != null) {
            scene.getStylesheets().add(lightCss.toExternalForm());
        }

        applyDarkMode(scene, darkMode.isSelected());

        Stage settingsStage = new Stage();
        settingsStage.setTitle("JavaChat - Settings");
        settingsStage.setScene(scene);
        settingsStage.setResizable(false);

        if (ownerStage != null) {
            settingsStage.setX(ownerStage.getX() + 100);
            settingsStage.setY(ownerStage.getY() + 100);
        }

        saveButton.setOnAction(event -> {
            preferences.putBoolean(
                    "notifications", notifications.isSelected()
            );
            preferences.putBoolean(
                    "messageSound", messageSound.isSelected()
            );
            preferences.putBoolean(
                    "enterToSend", enterToSend.isSelected()
            );
            preferences.putBoolean(
                    "darkMode", darkMode.isSelected()
            );

            applyDarkMode(scene, darkMode.isSelected());
            applyDarkModeToMain(ownerStage, darkMode.isSelected());

            status.setText("✓ Settings saved successfully.");
            status.setStyle("-fx-text-fill: #22c55e;");
        });

        darkMode.setOnAction(event -> {
            preferences.putBoolean(
                    "darkMode", darkMode.isSelected()
            );
            applyDarkMode(scene, darkMode.isSelected());
            applyDarkModeToMain(ownerStage, darkMode.isSelected());
        });

        closeButton.setOnAction(event -> settingsStage.close());

        settingsStage.show();
    }

    private void applyDarkMode(Scene scene, boolean enabled) {
        if (scene == null) return;

        var darkCss = getClass().getResource("/css/dark.css");
        if (darkCss == null) return;

        String darkUrl = darkCss.toExternalForm();

        if (enabled) {
            if (!scene.getStylesheets().contains(darkUrl)) {
                scene.getStylesheets().add(darkUrl);
            }
        } else {
            scene.getStylesheets().remove(darkUrl);
        }
    }

    private void applyDarkModeToMain(
            Stage ownerStage,
            boolean enabled) {

        if (ownerStage == null || ownerStage.getScene() == null) {
            return;
        }

        applyDarkMode(
                ownerStage.getScene(),
                enabled
        );
    }
}
