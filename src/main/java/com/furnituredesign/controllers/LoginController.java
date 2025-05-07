package com.furnituredesign.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.furnituredesign.services.AuthService;
import java.io.IOException;
import java.net.URL;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both Username and Password");
                return;
            }

            if (authService.authenticate(username, password)) {
                try {
                    //  Main application
                    URL fxmlUrl = getClass().getClassLoader().getResource("fxml/main.fxml");
                    if (fxmlUrl == null) {
                        throw new IOException("Could not find main.fxml");
                    }

                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    Parent root = loader.load();

                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    Scene scene = new Scene(root);

                    // Load designing parts
                    URL cssUrl = getClass().getClassLoader().getResource("styles/main.css");
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }

                    stage.setScene(scene);
                    stage.setTitle("Furniture Designer - Main");
                } catch (IOException e) {
                    errorLabel.setText("Error loading main view: " + e.getMessage());
                    System.err.println("Error loading FXML: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            errorLabel.setText("An unexpected error occurred: " + e.getMessage());
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
