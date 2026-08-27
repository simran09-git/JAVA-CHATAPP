package com.javachat.ui;

import com.javachat.database.UserDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProfileWindow {

    private final String username;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProfileWindow(String username) {

        this.username = username;
    }


    // =========================================================
    // SHOW PROFILE
    // =========================================================

    public void show(Stage ownerStage) {

        VBox root =
                new VBox(15);

        root.setAlignment(
                Pos.CENTER
        );

        root.setPadding(
                new Insets(30)
        );

        root.setPrefSize(
                450,
                600
        );

        root.getStyleClass().add(
                "login-root"
        );


        // =====================================================
        // TITLE
        // =====================================================

        Label icon =
                new Label("👤");

        icon.setStyle(
                "-fx-font-size: 55px;"
        );


        Label title =
                new Label(
                        "My Profile"
                );

        title.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
        );


        Label usernameLabel =
                new Label(
                        "Username: "
                                + username
                );

        usernameLabel.setStyle(
                "-fx-font-size: 16px;"
        );


        Label statusLabel =
                new Label(
                        "● Account Active"
                );

        statusLabel.setStyle(
                "-fx-text-fill: #22c55e;"
                        + "-fx-font-weight: bold;"
        );


        Separator separator =
                new Separator();


        // =====================================================
        // CHANGE PASSWORD
        // =====================================================

        Label passwordTitle =
                new Label(
                        "Change Password"
                );

        passwordTitle.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
        );


        PasswordField oldPassword =
                new PasswordField();

        oldPassword.setPromptText(
                "Current password"
        );

        oldPassword.getStyleClass().add(
                "login-input"
        );


        PasswordField newPassword =
                new PasswordField();

        newPassword.setPromptText(
                "New password"
        );

        newPassword.getStyleClass().add(
                "login-input"
        );


        PasswordField confirmPassword =
                new PasswordField();

        confirmPassword.setPromptText(
                "Confirm new password"
        );

        confirmPassword.getStyleClass().add(
                "login-input"
        );


        Button changePasswordButton =
                new Button(
                        "CHANGE PASSWORD"
                );

        changePasswordButton.getStyleClass().add(
                "login-button"
        );


        Label passwordStatus =
                new Label();

        passwordStatus.getStyleClass().add(
                "login-status"
        );


        // =====================================================
        // CHANGE PASSWORD ACTION
        // =====================================================

        changePasswordButton.setOnAction(
                event -> {

                    String oldPass =
                            oldPassword
                                    .getText();

                    String newPass =
                            newPassword
                                    .getText();

                    String confirmPass =
                            confirmPassword
                                    .getText();


                    if (oldPass.isEmpty()
                            || newPass.isEmpty()
                            || confirmPass.isEmpty()) {

                        passwordStatus.setText(
                                "Please fill all fields."
                        );

                        return;
                    }


                    if (!newPass.equals(
                            confirmPass
                    )) {

                        passwordStatus.setText(
                                "New passwords do not match."
                        );

                        return;
                    }


                    if (newPass.length() < 4) {

                        passwordStatus.setText(
                                "Password must contain at least 4 characters."
                        );

                        return;
                    }


                    boolean changed =
                            UserDAO.changePassword(
                                    username,
                                    oldPass,
                                    newPass
                            );


                    if (changed) {

                        passwordStatus.setText(
                                "Password changed successfully."
                        );

                        oldPassword.clear();
                        newPassword.clear();
                        confirmPassword.clear();

                    } else {

                        passwordStatus.setText(
                                "Current password is incorrect."
                        );
                    }
                }
        );


        // =====================================================
        // CLOSE BUTTON
        // =====================================================

        Button closeButton =
                new Button(
                        "CLOSE"
                );

        closeButton.getStyleClass().add(
                "register-button"
        );

        closeButton.setOnAction(
                event -> ownerStage.close()
        );


        // =====================================================
        // ROOT
        // =====================================================

        root.getChildren().addAll(

                icon,
                title,

                usernameLabel,
                statusLabel,

                separator,

                passwordTitle,

                oldPassword,
                newPassword,
                confirmPassword,

                changePasswordButton,
                passwordStatus,

                closeButton
        );


        // =====================================================
        // SCENE
        // =====================================================

        Scene scene =
                new Scene(
                        root,
                        450,
                        600
                );


        var css =
                getClass()
                        .getResource(
                                "/css/chat.css"
                        );


        if (css != null) {

            scene.getStylesheets().add(
                    css.toExternalForm()
            );
        }


        // =====================================================
        // STAGE
        // =====================================================

        Stage profileStage =
                new Stage();

        profileStage.setTitle(
                "JavaChat - Profile"
        );

        profileStage.setScene(
                scene
        );

        profileStage.setResizable(
                false
        );

        profileStage.initOwner(
                ownerStage
        );

        profileStage.show();
    }
}