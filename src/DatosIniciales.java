import java.util.ArrayList;

/** Datos ficticios. Invocar cargar() una sola vez, al iniciar la sesión. */
public final class DatosIniciales {
    private DatosIniciales() { }

    public static ArrayList<Libro> cargar() {
        ArrayList<Libro> libros = new ArrayList<>();
        // Se entregan desordenados: aplicar burbuja por código antes de buscar binariamente.
        libros.add(new Libro(1016, "Inteligencia artificial", "Nicolás López", "IA", 2025, 7));
        libros.add(new Libro(1003, "Java para principiantes", "Carla Mora", "Programación", 2022, 5));
        libros.add(new Libro(1010, "Bases de datos relacionales", "Marco Vera", "Bases de datos", 2020, 2));
        libros.add(new Libro(1001, "Fundamentos de Java", "Ana Torres", "Programación", 2021, 5));
        libros.add(new Libro(1018, "Aplicaciones con Java", "Camila Vega", "Programación", 2023, 2));
        libros.add(new Libro(1008, "Desarrollo de interfaces Swing", "Pablo Soto", "Programación", 2024, 7));
        libros.add(new Libro(1005, "Algoritmos y complejidad", "Sofía Paz", "Programación", 2023, 7));
        libros.add(new Libro(1013, "Ingeniería de software", "Gabriela Ríos", "Software", 2023, 4));
        libros.add(new Libro(1002, "Estructuras de datos", "Luis Vega", "Programación", 2020, 3));
        libros.add(new Libro(1019, "Arquitectura de software", "Tomás León", "Software", 2024, 3));
        libros.add(new Libro(1006, "Programación orientada a objetos", "Diego Ruiz", "Programación", 2021, 3));
        libros.add(new Libro(1015, "Modelado con UML", "Valeria Sol", "Software", 2024, 4));
        libros.add(new Libro(1004, "Diseño de bases de datos", "Pedro León", "Bases de datos", 2019, 0));
        libros.add(new Libro(1020, "Ciberseguridad aplicada", "Isabel Rojas", "Seguridad", 2025, 1));
        libros.add(new Libro(1011, "Java y colecciones", "Daniela Cruz", "Programación", 2025, 5));
        libros.add(new Libro(1007, "Introducción a redes", "Elena Ramos", "Redes", 2020, 1));
        libros.add(new Libro(1017, "Gestión de proyectos", "Paula Reyes", "Gestión", 2021, 0));
        libros.add(new Libro(1009, "Seguridad informática", "Laura Díaz", "Seguridad", 2022, 0));
        libros.add(new Libro(1014, "Pruebas de software", "Mateo Peña", "Software", 2022, 1));
        libros.add(new Libro(1012, "Sistemas operativos", "Andrés Luna", "Sistemas", 2021, 2));
        return libros;
    }
}
