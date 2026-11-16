import java.time.Year;
import java.util.ArrayList;

/**
 * Catálogo de libros.
 * La colección principal (biblioteca) siempre queda ordenada por ISBN ascendente,
 * porque la búsqueda binaria solo funciona sobre una lista ordenada.
 */
public class Lista {
    // Categorías permitidas: son las mismas que muestra el JComboBox.
    public static final String[] CATEGORIAS = {
            "Programación", "Bases de datos", "Redes", "Seguridad",
            "Sistemas", "Software", "IA", "Gestión"
    };

    private ArrayList<Libro> biblioteca;
    private boolean datosCargados;   // evita cargar los 20 libros más de una vez

    public Lista(){
        biblioteca=new ArrayList<>();
        datosCargados = false;
    }

    // R1: carga los 20 libros de demostración UNA sola vez por ejecución.
    // Llegan desordenados a propósito, así que se ordenan por ISBN con burbuja.
    public void cargarDatosIniciales() {
        if (datosCargados) {
            return;
        }
        ArrayList<Libro> iniciales = DatosIniciales.cargar();
        for (Libro libro : iniciales) {
            biblioteca.add(libro);
        }
        ordenarPorCodigoBurbuja();
        datosCargados = true;
    }

    // Cantidad actual de libros del catálogo.
    public int getCantidad() {
        return biblioteca.size();
    }

    // R2: valida TODOS los datos y recién ahí agrega el libro.
    // Si algo falla lanza una excepción y el catálogo queda igual que antes.
    public void registrar(Libro libro) {
        if (libro.getIsbn() <= 0) {
            throw new IllegalArgumentException("El ISBN debe ser un número entero positivo.");
        }
        validarDatos(libro.getTitulo(), libro.getAutor(), libro.getCategoria(), libro.getAnioPublicacion());
        if (libro.getMeGusta() != 0) {
            throw new IllegalArgumentException("Un libro nuevo debe empezar con 0 me gusta.");
        }
        if (buscarIndicePorCodigo(libro.getIsbn()) != -1) {
            throw new IllegalArgumentException("Ya existe un libro con el ISBN " + libro.getIsbn() + ".");
        }
        biblioteca.add(libro);
        // El ISBN nuevo puede ser menor que los que ya existen (por ejemplo 999),
        // por eso se vuelve a ordenar: así la búsqueda binaria sigue funcionando.
        ordenarPorCodigoBurbuja();
    }

    // Ordenamiento burbuja por ISBN ascendente sobre la colección principal.
    public void ordenarPorCodigoBurbuja() {
        int n = biblioteca.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (biblioteca.get(j).getIsbn() > biblioteca.get(j + 1).getIsbn()) {
                    Libro aux = biblioteca.get(j);
                    biblioteca.set(j, biblioteca.get(j + 1));
                    biblioteca.set(j + 1, aux);
                }
            }
        }
    }

    // Reglas de los datos que el usuario escribe. Si algo está mal, lanza una excepción.
    private void validarDatos(String titulo, String autor, String categoria, int anioPublicacion) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor es obligatorio.");
        }
        if (!esCategoriaValida(categoria)) {
            throw new IllegalArgumentException("Seleccione una categoría de la lista.");
        }
        int anioActual = Year.now().getValue();
        if (anioPublicacion < 1 || anioPublicacion > anioActual) {
            throw new IllegalArgumentException("El año debe estar entre 1 y " + anioActual + ".");
        }
    }

    private boolean esCategoriaValida(String categoria) {
        if (categoria == null) {
            return false;
        }
        for (String c : CATEGORIAS) {
            if (c.equals(categoria)) {
                return true;
            }
        }
        return false;
    }

    // R3: búsqueda lineal. Revisa TODOS los libros y devuelve los que tienen el texto
    // en el título O en el autor, sin importar mayúsculas o minúsculas.
    // Como recorre la colección principal en orden, los resultados salen ordenados por ISBN.
    public ArrayList<Libro> buscarCoincidencias(String texto){
        if(texto==null || texto.trim().isEmpty())
            throw new IllegalArgumentException("Escriba un texto para buscar (no puede estar vacío).");
        String consulta=texto.trim().toLowerCase();
        ArrayList<Libro> resultado=new ArrayList<>();

        for(Libro aux:biblioteca){
            String titulo=aux.getTitulo().toLowerCase();
            String autor=aux.getAutor().toLowerCase();
            // con || el libro se agrega una sola vez aunque coincida en título y autor
            if(titulo.contains(consulta) || autor.contains(consulta)){
                resultado.add(aux);
            }
        }
        return resultado;   // si no hay coincidencias queda vacío, nunca null
    }

    // R4: búsqueda binaria propia sobre la colección principal (ordenada por ISBN).
    // Devuelve el índice donde está el libro, o -1 si no existe.
    public int buscarIndicePorCodigo(int isbn){
        if(isbn<=0)
            throw new IllegalArgumentException("El ISBN debe ser un número entero positivo.");
        if(biblioteca.isEmpty())
            return -1;
        int tam=biblioteca.size()-1;
        if(isbn<biblioteca.get(0).getIsbn()
                ||isbn>biblioteca.get(tam).getIsbn())
            return -1;
        int i=0;      // límite inferior
        int s=tam;    // límite superior
        int c;        // posición central
        while(i<=s){
            c=(i+s)/2;
            int valor=biblioteca.get(c).getIsbn();
            if(isbn==valor){
                return c;
            }else if(isbn>valor){
                i=c+1;
            }else{
                s=c-1;
            }
        }
        return -1;

    }

    // Libro que está en una posición de la colección principal
    // (por ejemplo, el índice que devuelve la búsqueda binaria).
    public Libro obtenerLibro(int indice) {
        return biblioteca.get(indice);
    }

    // R4: edita el libro identificado por su ISBN (no por la posición en la JList ni por el título).
    // Primero valida TODO y solo después cambia los datos: así nunca queda un cambio a medias.
    public void editarLibro(int isbn, String titulo, String autor, String categoria, int anioPublicacion) {
        int indice = buscarIndicePorCodigo(isbn);
        if (indice == -1) {
            throw new IllegalArgumentException("No existe un libro con el ISBN " + isbn + ".");
        }
        validarDatos(titulo, autor, categoria, anioPublicacion);
        Libro libro = biblioteca.get(indice);
        libro.setTitulo(titulo.trim());
        libro.setAutor(autor.trim());
        libro.setCategoria(categoria);
        libro.setAnioPublicacion(anioPublicacion);
        // El ISBN no cambia, así que la colección sigue ordenada y no hace falta reordenar.
    }

    // R5: suma un me gusta al libro con ese ISBN (lo encuentra con la búsqueda binaria).
    public void darMeGusta(int isbn) {
        int indice = buscarIndicePorCodigo(isbn);
        if (indice == -1) {
            throw new IllegalArgumentException("No existe un libro con el ISBN " + isbn + ".");
        }
        biblioteca.get(indice).incrementarMeGusta();
    }

    // R6: ranking de popularidad. Se ordena una COPIA de la colección principal con burbuja
    // por me gusta descendente. La principal no se toca: sigue ordenada por ISBN para la binaria.
    public ArrayList<Libro> obtenerRanking() {
        ArrayList<Libro> ranking = new ArrayList<>(biblioteca);   // mismos objetos Libro, otra lista
        int n = ranking.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                // Burbuja estable: solo se intercambia si el de la izquierda tiene MENOS me gusta.
                // Si empatan no se mueven y, como la copia viene ordenada por ISBN,
                // los empatados quedan con ISBN ascendente.
                if (ranking.get(j).getMeGusta() < ranking.get(j + 1).getMeGusta()) {
                    Libro aux = ranking.get(j);
                    ranking.set(j, ranking.get(j + 1));
                    ranking.set(j + 1, aux);
                }
            }
        }
        return ranking;
    }

    // Devuelve una COPIA: desde afuera nadie puede desordenar la colección principal.
    public ArrayList<Libro> obtener(){
        return new ArrayList<>(biblioteca);
    }

}
