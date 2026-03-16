package com.autoatelier.controller;

import com.autoatelier.service.AuthService;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Заполните все обязательные поля");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Пароли не совпадают");
            return;
        }
        if (password.length() < 6) {
            showError("Пароль должен быть не менее 6 символов");
            return;
        }

        registerButton.setDisable(true);

        new Thread(() -> {
            try {
                AuthService.getInstance().register(email, password, name, phone);
                Platform.runLater(() -> SceneManager.navigate("client-dashboard"));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    showError(ex.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        SceneManager.navigate("login");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
