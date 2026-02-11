package org.example.proyecto_01_topicosaprogramacion;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EmailController {
    // --- 1. Definición de componentes con tus nuevos IDs ---
    @FXML private TextField labelRemitente;     // Antes txtRemitente
    @FXML private TextField labelDestinatario;  // Antes txtDestinatario
    @FXML private TextField labelAsunto;        // Antes txtAsunto
    @FXML private TextArea textMensaje;         // Antes txtCuerpo
    @FXML private StackPane paneArrastrar;      // Antes dropZone
    @FXML private ListView<File> listAdjuntos;  // Antes listaAdjuntos
    @FXML private Button botonEnviar;           // Antes btnEnviar

    // --- 2. Variables de lógica ---
    private ObservableList<File> archivosObservable = FXCollections.observableArrayList();
    private String passwordUsuario; // Aquí guardaremos la contraseña que viene del Login

    // Extensiones permitidas (normalizadas en minúsculas)
    private final List<String> extensionesValidas = Arrays.asList(
            "txt", "jav", "java", "doc", "docx", "jpg", "jpeg", "mp4", "flv", "zip", "rar"
    );

    // --- 3. Método para recibir credenciales desde la ventana anterior ---
    public void setCredenciales(String correo, String password) {
        this.passwordUsuario = password;
        this.labelRemitente.setText(correo);
        this.labelRemitente.setEditable(false); // Bloqueamos para que no cambien el remitente
    }

    // --- 4. Initialize: Configuración inicial ---
    @FXML
    public void initialize() {
        // Vincular la lista visual con los datos
        listAdjuntos.setItems(archivosObservable);

        // Configurar el evento del botón enviar
        botonEnviar.setOnAction(event -> enviarCorreo());

        // Configurar la zona de arrastrar y soltar
        configurarDragAndDrop();

        configurarClickArchivos();
    }

    // --- 5. Lógica de Drag & Drop (Arrastrar y Soltar) ---
    private void configurarDragAndDrop() {
        // Evento: Cuando arrastras algo sobre el StackPane
        paneArrastrar.setOnDragOver(event -> {
            if (event.getGestureSource() != paneArrastrar && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // Evento: Cuando sueltas los archivos
        paneArrastrar.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean exito = false;

            if (db.hasFiles()) {
                for (File archivo : db.getFiles()) {
                    if (esExtensionValida(archivo.getName()) && !archivosObservable.contains(archivo)) {
                        archivosObservable.add(archivo);
                    } else {
                        System.out.println("Archivo rechazado o duplicado: " + archivo.getName());
                    }
                }
                exito = true;
            }
            event.setDropCompleted(exito);
            event.consume();
        });
    }

    // --- Lógica para abrir explorador de archivos al hacer clic ---
    private void configurarClickArchivos() {
        paneArrastrar.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivos adjuntos");

            // Filtros para que el explorador de Windows solo muestre tus extensiones válidas
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Todos los permitidos", "*.txt", "*.jav", "*.java", "*.doc", "*.docx", "*.jpg", "*.jpeg", "*.mp4", "*.flv", "*.zip", "*.rar"),
                    new FileChooser.ExtensionFilter("Documentos (*.txt, *.doc, *.docx)", "*.txt", "*.doc", "*.docx"),
                    new FileChooser.ExtensionFilter("Código Java (*.java, *.jav)", "*.java", "*.jav"),
                    new FileChooser.ExtensionFilter("Imágenes (*.jpg, *.jpeg)", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("Videos (*.mp4, *.flv)", "*.mp4", "*.flv"),
                    new FileChooser.ExtensionFilter("Comprimidos (*.zip, *.rar)", "*.zip", "*.rar")
            );

            // Obtener la ventana actual para mostrar el diálogo
            Stage stage = (Stage) paneArrastrar.getScene().getWindow();

            // Abrir el explorador permitiendo selección múltiple
            List<File> archivosSeleccionados = fileChooser.showOpenMultipleDialog(stage);

            // Si el usuario seleccionó archivos (y no le dio a "Cancelar")
            if (archivosSeleccionados != null) {
                for (File archivo : archivosSeleccionados) {
                    // Evitar duplicados en la lista visual
                    if (!archivosObservable.contains(archivo)) {
                        archivosObservable.add(archivo);
                    }
                }
            }
        });
    }

    // Método auxiliar para validar extensiones
    private boolean esExtensionValida(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();
        int ultimoPunto = nombre.lastIndexOf('.');
        if (ultimoPunto > 0) {
            String extension = nombre.substring(ultimoPunto + 1);
            return extensionesValidas.contains(extension);
        }
        return false;
    }

    // --- 6. Lógica de Envío de Correo (En segundo plano) ---
    private void enviarCorreo() {
        // Recolección de datos
        String remitente = labelRemitente.getText();
        String destinatario = labelDestinatario.getText();
        String asunto = labelAsunto.getText();
        String cuerpo = textMensaje.getText();

        if (destinatario.isEmpty() || passwordUsuario == null) {
            mostrarAlerta("Error", "Faltan datos o no se ha iniciado sesión correctamente.", Alert.AlertType.ERROR);
            return;
        }

        // Interfaz visual de "Cargando"
        botonEnviar.setDisable(true);
        botonEnviar.setText("Enviando...");

        // Tarea en segundo plano para no congelar la interfaz
        Task<Void> tareaEnvio = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                enviarEmailTecnico(remitente, passwordUsuario, destinatario, asunto, cuerpo);
                return null;
            }
        };

        // Si sale bien
        tareaEnvio.setOnSucceeded(e -> {
            mostrarAlerta("Éxito", "Correo enviado correctamente.", Alert.AlertType.INFORMATION);
            botonEnviar.setDisable(false);
            botonEnviar.setText("Enviar");
            limpiarFormulario();
        });

        // Si falla
        tareaEnvio.setOnFailed(e -> {
            Throwable error = tareaEnvio.getException();
            mostrarAlerta("Error al enviar", error.getMessage(), Alert.AlertType.ERROR);
            error.printStackTrace(); // Ver error en consola
            botonEnviar.setDisable(false);
            botonEnviar.setText("Enviar");
        });

        new Thread(tareaEnvio).start();
    }

    // Lógica pura de JavaMail
    private void enviarEmailTecnico(String remitente, String password, String destinatario, String asunto, String cuerpo) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // Cambia esto si usas Outlook/Hotmail
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(remitente));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(asunto);

        // Parte 1: Texto
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(cuerpo);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Parte 2: Adjuntos
        for (File archivo : archivosObservable) {
            // Declaramos explícitamente un MimeBodyPart para el adjunto
            MimeBodyPart parteAdjunto = new MimeBodyPart();
            parteAdjunto.attachFile(archivo);
            multipart.addBodyPart(parteAdjunto);
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    private void limpiarFormulario() {
        labelDestinatario.clear();
        labelAsunto.clear();
        textMensaje.clear();
        archivosObservable.clear();
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
