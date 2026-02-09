module org.example.proyecto_01_topicosaprogramacion {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.proyecto_01_topicosaprogramacion to javafx.fxml;
    exports org.example.proyecto_01_topicosaprogramacion;
}