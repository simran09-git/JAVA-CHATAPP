package com.javachat.ui;

import com.javachat.database.UserDAO;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterWindow {

    public void show(Stage stage) {

        VBox root = new VBox(15);

        root.setAlignment(Pos.CENTER);
        root.setPrefSize(500, 600);

        root.getStyleClass().add("login-root");

        // Logo
        Label logo = new Label("💬");
        logo.getStyleClass().add("login-logo");

        // Title
        Label title = new Label("Create Account");
        title.getStyleClass().add("login-title");

        // Subtitle
        Label subtitle =
                new Label("Join JavaChat today");

        subtitle.getStyleClass().add(
                "login-subtitle"
        );

        // Username
        TextField username =
                new TextField();

        username.setPromptText(
                "Username"
        );

        username.getStyleClass().add(
                "login-input"
        );

        // Password
        PasswordField password =
                new PasswordField();

        password.setPromptText(
                "Password"
        );

        password.getStyleClass().add(
                "login-input"
        );

        // Confirm password
        PasswordField confirmPassword =
                new PasswordField();

        confirmPassword.setPromptText(
                "Confirm Password"
        );

        confirmPassword.getStyleClass().add(
                "login-input"
        );

        // Create account button
        Button registerButton =
                new Button("CREATE ACCOUNT");

        registerButton.getStyleClass().add(
                "login-button"
        );

        // Status
        Label status =
                new Label();

        status.getStyleClass().add(
                "login-status"
        );

        // Back to login
        Button loginButton =
                new Button("Back to Login");

        loginButton.getStyleClass().add(
                "register-button"
        );


        // =====================================================
        // REGISTER ACTION
        // =====================================================

        registerButton.setOnAction(event -> {

            String user =
                    username.getText().trim();

            String pass =
                    password.getText();

            String confirm =
                    confirmPassword.getText();


            // Empty fields

            if (user.isEmpty()
                    || pass.isEmpty()
                    || confirm.isEmpty()) {

                status.setText(
                        "Please fill all fields."
                );

                return;
            }


            // Username validation

            if (user.length() < 3) {

                status.setText(
                        "Username must be at least 3 characters."
                );

                return;
            }


            // Password validation

            if (pass.length() < 4) {

                status.setText(
                        "Password must be at least 4 characters."
                );

                return;
            }


            // Password confirmation

            if (!pass.equals(confirm)) {

                status.setText(
                        "Passwords do not match."
                );

                return;
            }


            // Database registration

            boolean registered =
                    UserDAO.register(
                            user,
                            pass
                    );


            if (registered) {

                status.setText(
                        "Account created successfully!"
                );

                username.clear();
                password.clear();
                confirmPassword.clear();

            } else {

                status.setText(
                        "Username already exists or registration failed."
                );
            }
        });


        // =====================================================
        // BACK TO LOGIN
        // =====================================================

        loginButton.setOnAction(event -> {

            LoginWindow loginWindow =
                    new LoginWindow();

            loginWindow.show(stage);
        });


        // =====================================================
        // ADD CONTROLS
        // =====================================================

        root.getChildren().addAll(

                logo,
                title,
                subtitle,

                username,
                password,
                confirmPassword,

                registerButton,

                status,

                loginButton
        );


        // =====================================================
        // SCENE
        // =====================================================

        Scene scene =
                new Scene(
                        root,
                        500,
                        600
                );


        // Load CSS

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

        stage.setTitle(
                "JavaChat - Create Account"
        );

        stage.setScene(scene);

        stage.show();
    }
}