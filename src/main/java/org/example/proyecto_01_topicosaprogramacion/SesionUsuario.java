package org.example.proyecto_01_topicosaprogramacion;

public class SesionUsuario {
    private static String correo;
    private static String password; // La contraseña de aplicación de 16 letras

    // Getters y Setters
    public static String getCorreo() {
        return correo;
    }

    public static void setCorreo(String correo) {
        SesionUsuario.correo = correo;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        SesionUsuario.password = password;
    }

    // Método para borrar datos al salir
    public static void cerrarSesion() {
        correo = null;
        password = null;
    }
}
