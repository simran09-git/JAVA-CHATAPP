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

public class LoginWindow {

    public void show(Stage stage) {

        VBox root = new VBox(15);

        root.setAlignment(Pos.CENTER);
        root.setPrefSize(500, 600);

        root.getStyleClass().add("login-root");


        // Logo

        Label logo =
                new Label("💬");

        logo.getStyleClass().add(
                "login-logo"
        );


        // Title

        Label title =
                new Label("JavaChat");

        title.getStyleClass().add(
                "login-title"
        );


        // Subtitle

        Label subtitle =
                new Label(
                        "Connect. Chat. Share."
                );

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


        // Login button

        Button loginButton =
                new Button("LOGIN");

        loginButton.getStyleClass().add(
                "login-button"
        );


        // Status

        Label status =
                new Label();

        status.getStyleClass().add(
                "login-status"
        );


        // Register text

        Label registerText =
                new Label(
                        "Don't have an account?"
                );

        registerText.getStyleClass().add(
                "register-text"
        );


        // Register button

        Button registerButton =
                new Button(
                        "Create Account"
                );

        registerButton.getStyleClass().add(
                "register-button"
        );


        // =====================================================
        // LOGIN
        // =====================================================

        loginButton.setOnAction(event -> {

            String user =
                    username.getText().trim();

            String pass =
                    password.getText();


            if (user.isEmpty()
                    || pass.isEmpty()) {

                status.setText(
                        "Please enter username and password."
                );

                return;
            }


            boolean valid =
                    UserDAO.login(
                            user,
                            pass
                    );


            if (valid) {

                status.setText(
                        "Login successful!"
                );


                ChatWindow chatWindow =
                        new ChatWindow(user);

                chatWindow.show(stage);

            } else {

                status.setText(
                        "Invalid username or password."
                );
            }
        });


        // =====================================================
        // REGISTER
        // =====================================================

        registerButton.setOnAction(event -> {

            RegisterWindow registerWindow =
                    new RegisterWindow();

            registerWindow.show(stage);
        });


        // =====================================================
        // ROOT CONTENT
        // =====================================================

        VBox registerBox =
                new VBox(5);

        registerBox.setAlignment(
                Pos.CENTER
        );

        registerBox.getChildren().addAll(

                registerText,
                registerButton
        );


        root.getChildren().addAll(

                logo,
                title,
                subtitle,

                username,
                password,

                loginButton,

                status,

                registerBox
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
                "JavaChat - Login"
        );

        stage.setScene(scene);

        stage.show();
    }
}