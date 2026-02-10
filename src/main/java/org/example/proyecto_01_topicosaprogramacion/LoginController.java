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
import java.util.Properties;

public class LoginController {
    // --- CONTROLES VINCULADOS AL FXML ---
    @FXML
    private TextField textUser;      // ID de tu campo de usuario
    @FXML
    private PasswordField passUser;  // ID de tu campo de contraseña
    @FXML
    private Button buttonLogin;      // ID de tu botón

    // --- INICIALIZACIÓN ---
    @FXML
    public void initialize() {
        // Asignamos la acción al botón (ya que no tenía onAction en el FXML)
        buttonLogin.setOnAction(event -> procesarLogin());

        // Un detalle pro: Que al dar ENTER en la contraseña también inicie
        passUser.setOnAction(event -> procesarLogin());
    }

    // --- LÓGICA PRINCIPAL ---
    private void procesarLogin() {
        String usuario = textUser.getText();
        String password = passUser.getText();

        // 1. Validar que no estén vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor ingresa tu correo y contraseña.");
            return;
        }

        // 2. Feedback visual (Deshabilitar botón)
        buttonLogin.setDisable(true);
        buttonLogin.setText("Conectando...");

        // 3. Proceso en segundo plano (Hilo secundario)
        new Thread(() -> {
            boolean exito = validarConGmail(usuario, password);

            // Regresamos al hilo de la interfaz gráfica
            Platform.runLater(() -> {
                if (exito) {
                    // A. Guardar datos en la sesión
                    SesionUsuario.setCorreo(usuario);
                    SesionUsuario.setPassword(password);

                    System.out.println("Login correcto. Cargando email.fxml...");

                    // B. Cambiar de pantalla
                    cambiarPantallaPrincipal();
                } else {
                    // Login fallido
                    mostrarAlerta("Error de acceso", "No se pudo conectar.\n\nVerifica:\n1. Tu correo y Contraseña de Aplicación (16 letras).\n2. Tu conexión a internet.");
                    buttonLogin.setDisable(false);
                    buttonLogin.setText("Iniciar Sesión");
                }
            });
        }).start();
    }

    // --- CONEXIÓN CON GMAIL (IMAP) ---
    private boolean validarConGmail(String user, String pass) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            // Aquí es donde se prueba la contraseña real
            store.connect("imap.gmail.com", user, pass);
            store.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error exacto en la consola
            return false;
        }
    }

    // --- CAMBIO DE PANTALLA ---
    private void cambiarPantallaPrincipal() {
        try {
            // AQUÍ ESTÁ EL CAMBIO: Cargamos "email.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("email.fxml"));
            Parent root = loader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) buttonLogin.getScene().getWindow();

            Scene scene = new Scene(root);
            // Opcional: Cargar estilos si los necesitas en la nueva ventana
            // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Bandeja de Entrada - " + SesionUsuario.getCorreo());
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error crítico", "No se encontró el archivo 'email.fxml'.\nAsegúrate de que esté en la carpeta resources junto con tus clases.");
            buttonLogin.setDisable(false); // Reactivar botón por si acaso
        }
    }

    // --- UTILIDAD PARA ALERTAS ---
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Sistema de Correo");
        alert.setHeaderText(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
