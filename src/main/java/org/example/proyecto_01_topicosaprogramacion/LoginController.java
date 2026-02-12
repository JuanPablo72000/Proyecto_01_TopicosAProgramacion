package org.example.proyecto_01_topicosaprogramacion;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import java.awt.Desktop;
import java.net.URI;


public class LoginController {
    @FXML
    private TextField textUser;
    @FXML
    private PasswordField passUser;
    @FXML
    private Button buttonLogin;

    @FXML
    public void initialize() throws URISyntaxException, IOException {
        buttonLogin.setOnAction(event -> procesarLogin());
        passUser.setOnAction(event -> procesarLogin());
    }

    private void procesarLogin() {
        String usuario = textUser.getText();
        String password = passUser.getText();

        // 1. Validar que no estén vacios
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor ingresa tu correo y contraseña.");
            return;
        }

        // 2. Feedback visual (Deshabilitar boton)
        buttonLogin.setDisable(true);
        buttonLogin.setText("Conectando...");

        // 3. Proceso en segundo plano (Hilo secundario)
        new Thread(() -> {
            boolean exito = validarConGmail(usuario, password);

            Platform.runLater(() -> {
                if (exito) {
                    SesionUsuario.setCorreo(usuario);
                    SesionUsuario.setPassword(password);

                    System.out.println("Login correcto. Cargando email.fxml...");

                    cambiarPantallaPrincipal();
                } else {
                    mostrarAlerta("Error de acceso", "No se pudo conectar.\n\nVerifica:\n1. Tu correo y Contraseña de Aplicación (16 letras).\n2. Tu conexión a internet.");
                    buttonLogin.setDisable(false);
                    buttonLogin.setText("Iniciar Sesión");
                }
            });
        }).start();
    }

    private boolean validarConGmail(String user, String pass) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", user, pass);
            store.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void cambiarPantallaPrincipal() {
        try {
            // 1. Cargamos el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("email.fxml"));
            Parent root = loader.load();

            // 2. Obtener el controlador de Email e inyectar los datos
            EmailController emailController = loader.getController();

            // Sacamos los datos de la clase SesionUsuario
            String correo = SesionUsuario.getCorreo();
            String password = SesionUsuario.getPassword();

            emailController.setCredenciales(correo, password);

            // 3. Mostrar la ventana en la misma pantalla
            Stage stage = (Stage) buttonLogin.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Nuevo Mensaje - " + correo);

            stage.show();

            Platform.runLater(() -> {
                stage.setMaximized(false);
                stage.setMaximized(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error crítico", "No se encontró el archivo FXML");
            buttonLogin.setDisable(false);
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Sistema de Correo");
        alert.setHeaderText(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
