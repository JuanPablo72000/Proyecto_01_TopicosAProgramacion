module org.example.proyecto_01_topicosaprogramacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;


    opens org.example.proyecto_01_topicosaprogramacion to javafx.fxml;
    exports org.example.proyecto_01_topicosaprogramacion;
}