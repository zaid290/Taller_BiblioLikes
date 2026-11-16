/**
 * Libro del catálogo.
 * El ISBN es el código único del libro: es positivo, no se repite y no se puede
 * cambiar después del registro (por eso es final y no tiene setter).
 */
public class Libro {
    private final int isbn;
    private String titulo;
    private String autor;
    private String categoria;
    private int anioPublicacion;
    private int meGusta;

    // Libro nuevo (formulario de registro): siempre empieza con 0 me gusta.
    public Libro(int isbn, String titulo, String autor, String categoria, int anioPublicacion) {
        this(isbn, titulo, autor, categoria, anioPublicacion, 0);
    }

    // Constructor completo: lo usa DatosIniciales para los 20 libros de demostración.
    public Libro(int isbn, String titulo, String autor, String categoria, int anioPublicacion, int meGusta) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.anioPublicacion = anioPublicacion;
        this.meGusta = meGusta;
    }

    public int getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public int getMeGusta() {
        return meGusta;
    }

    @Override
    public String toString() {
        return isbn + " | " + titulo + " | " + autor + " | Me gusta: " + meGusta;
    }
}
