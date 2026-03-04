package org.example.proyecto_01_topicosaprogramacion;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.application.Platform;

public class EmailController {
    @FXML private TextField labelRemitente;
    @FXML private TextField labelDestinatario;
    @FXML private TextField labelAsunto;
    @FXML private TextArea textMensaje;
    @FXML private StackPane paneArrastrar;
    @FXML private ListView<File> listAdjuntos;
    @FXML private Button botonEnviar;
    @FXML private Button botonEliminar;

    // 2. Variables de lógica
    private ObservableList<File> archivosObservable = FXCollections.observableArrayList();
    private String passwordUsuario;
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;

    // Extensiones permitidas
    private final List<String> extensionesValidas = Arrays.asList(
            "txt", "jav", "java", "doc", "docx", "jpg", "jpeg", "mp4", "flv", "zip", "rar"
    );

    // 3. Metodo para recibir credenciales desde la ventana anterior
    public void setCredenciales(String correo, String password) {
        this.passwordUsuario = password;
        this.labelRemitente.setText(correo);
        this.labelRemitente.setEditable(false);
    }

    // 4. Initialize: Configuración inicial
    @FXML
    public void initialize() {
        listAdjuntos.setItems(archivosObservable);

        listAdjuntos.setCellFactory(listView -> new ListCell<File>() {
            private ImageView imageView = new ImageView();

            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);

                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(file.getName());

                    Image icono = obtenerIconoParaArchivo(file);
                    if (icono != null) {
                        imageView.setImage(icono);
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(24);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        botonEnviar.setOnAction(event -> enviarCorreo());

        configurarDragAndDrop();
        configurarClickArchivos();

        botonEliminar.setOnAction(event -> {
            File archivoSeleccionado = listAdjuntos.getSelectionModel().getSelectedItem();

            if (archivoSeleccionado != null) {
                archivosObservable.remove(archivoSeleccionado);
            } else {
                mostrarAlerta("Aviso", "Primero selecciona un archivo de la lista para eliminarlo.", Alert.AlertType.INFORMATION);
            }
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) paneArrastrar.getScene().getWindow();

            stage.setMinWidth(620);
            stage.setMinHeight(750);

            stage.sizeToScene();
        });
    }

    private Image obtenerIconoParaArchivo(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        String rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/default.png";

        if (nombre.endsWith(".pdf")) {
            rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/pdf.png";
        } else if (nombre.endsWith(".doc") || nombre.endsWith(".docx")) {
            rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/word.png";
        } else if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png")) {
            rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/imagen.png";
        } else if (nombre.endsWith(".java") || nombre.endsWith(".jav")) {
            rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/java.png";
        } else if (nombre.endsWith(".zip") || nombre.endsWith(".rar")) {
            rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/zip.png";
        } else if (nombre.endsWith(".mp4") || nombre.endsWith(".flv")) {
            rutaImagen = "/org/example/proyecto_01_topicosaprogramacion/icons/mp4.png";
        }

        try {
            return new Image(getClass().getResourceAsStream(rutaImagen));
        } catch (Exception e) {
            return null;
        }
    }

    // 5. Lógica de Drag & Drop (Arrastrar y Soltar)
    private void configurarDragAndDrop() {
        paneArrastrar.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean exito = false;

            if (db.hasFiles()) {
                for (File archivo : db.getFiles()) {
                    // Validar peso acumulado
                    long pesoActual = calcularPesoTotalActual();
                    if (pesoActual + archivo.length() > MAX_FILE_SIZE) {
                        mostrarAlerta("Límite excedido",
                                "El archivo '" + archivo.getName() + "' haría que superes los 30MB totales.",
                                Alert.AlertType.WARNING);
                        continue;
                    }

                    if (esExtensionValida(archivo.getName()) && !archivosObservable.contains(archivo)) {
                        archivosObservable.add(archivo);
                    }
                }
                exito = true;
            }
            event.setDropCompleted(exito);
            event.consume();
        });

        paneArrastrar.setOnDragOver(event -> {
            if (event.getGestureSource() != paneArrastrar && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
    }

    // Logica para abrir explorador de archivos al hacer clic
    private void configurarClickArchivos() {
        paneArrastrar.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivos adjuntos");

            // Filtros para que el explorador de Windows solo muestre tus extensiones válidas
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Todos los permitidos", "*.txt", "*.jav", "*.java", "*.doc", "*.docx", "*.jpg", "*.jpeg", "*.mp4", "*.flv", "*.zip", "*.rar", "*.pdf"),
                    new FileChooser.ExtensionFilter("Documentos (*.txt, *.doc, *.docx)", "*.txt", "*.doc", "*.docx", "*.pdf"),
                    new FileChooser.ExtensionFilter("Código Java (*.java, *.jav)", "*.java", "*.jav"),
                    new FileChooser.ExtensionFilter("Imágenes (*.jpg, *.jpeg)", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("Videos (*.mp4, *.flv)", "*.mp4", "*.flv"),
                    new FileChooser.ExtensionFilter("Comprimidos (*.zip, *.rar)", "*.zip", "*.rar")
            );

            // Obtener la ventana actual para mostrar el dialogo
            Stage stage = (Stage) paneArrastrar.getScene().getWindow();

            // Abrir el explorador permitiendo selección multiple
            List<File> archivosSeleccionados = fileChooser.showOpenMultipleDialog(stage);

            if (archivosSeleccionados != null) {
                for (File archivo : archivosSeleccionados) {
                    long pesoAcumulado = calcularPesoTotalActual();

                    if (pesoAcumulado + archivo.length() > MAX_FILE_SIZE) {
                        mostrarAlerta("Límite de tamaño",
                                "No se puede agregar '" + archivo.getName() + "' porque excede el límite total de 30MB.",
                                Alert.AlertType.WARNING);
                    } else if (!archivosObservable.contains(archivo)) {
                        archivosObservable.add(archivo);
                    }
                }
            }
        });
    }

    // Metodo auxiliar para validar extensiones
    private boolean esExtensionValida(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();
        int ultimoPunto = nombre.lastIndexOf('.');
        if (ultimoPunto > 0) {
            String extension = nombre.substring(ultimoPunto + 1);
            return extensionesValidas.contains(extension);
        }
        return false;
    }

    // 6. Logica de Envío de Correo
    private void enviarCorreo() {
        String remitente = labelRemitente.getText();
        String destinatario = labelDestinatario.getText();
        String asunto = labelAsunto.getText();
        String cuerpo = textMensaje.getText();

        if (destinatario.isEmpty() || passwordUsuario == null) {
            mostrarAlerta("Error", "Faltan datos o no se ha iniciado sesión correctamente.", Alert.AlertType.ERROR);
            return;
        }

        if (destinatario.isEmpty() || passwordUsuario == null) {
            mostrarAlerta("Error", "Faltan datos o no se ha iniciado sesión correctamente.", Alert.AlertType.ERROR);
            return;
        }

        // NUEVO: Validación final de seguridad
        if (calcularPesoTotalActual() > MAX_FILE_SIZE) {
            mostrarAlerta("Error de envío", "El total de los archivos adjuntos supera los 30MB permitidos.", Alert.AlertType.ERROR);
            return;
        }

        botonEnviar.setDisable(true);
        botonEnviar.setText("Enviando...");

        Task<Void> tareaEnvio = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                enviarEmailTecnico(remitente, passwordUsuario, destinatario, asunto, cuerpo);
                return null;
            }
        };

        tareaEnvio.setOnSucceeded(e -> {
            mostrarAlerta("Éxito", "Correo enviado correctamente.", Alert.AlertType.INFORMATION);
            botonEnviar.setDisable(false);
            botonEnviar.setText("Enviar");
            limpiarFormulario();
        });

        tareaEnvio.setOnFailed(e -> {
            Throwable error = tareaEnvio.getException();
            mostrarAlerta("Error al enviar", error.getMessage(), Alert.AlertType.ERROR);
            error.printStackTrace(); // Ver error en consola
            botonEnviar.setDisable(false);
            botonEnviar.setText("Enviar");
        });

        new Thread(tareaEnvio).start();
    }

    // Logica pura de JavaMail
    private void enviarEmailTecnico(String remitente, String password, String destinatario, String asunto, String cuerpo) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // Cambiar esto si usas Outlook/Hotmail
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

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(cuerpo);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        for (File archivo : archivosObservable) {
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

    private long calcularPesoTotalActual() {
        return archivosObservable.stream()
                .mapToLong(File::length)
                .sum();
    }
}
