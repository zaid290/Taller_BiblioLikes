import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.time.Year;
import java.util.ArrayList;

/**
 * Ventana principal de BiblioLikes, hecha a mano con Swing (sin el diseñador de IntelliJ).
 * Aquí solo están los controles y sus eventos; las validaciones, búsquedas y
 * ordenamientos están en la clase Lista.
 */
public class Ventana {
    // Qué se está mostrando en la lista de resultados (para recalcularlo después de editar).
    private static final int SIN_BUSQUEDA = 0;
    private static final int BUSQUEDA_ISBN = 1;
    private static final int BUSQUEDA_TEXTO = 2;
    private static final int MOSTRAR_TODOS = 3;

    private JPanel principal;
    private JTabbedPane tabbedPane1;

    // ----- Pestaña Registro -----
    private JTextField txtIsbn;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JComboBox<String> cboCategoria;
    private JSpinner spiAnio;
    private JButton btnRegistrar;
    private JLabel lblTotal;

    // ----- Pestaña Búsquedas y edición -----
    private JTextField txtBuscarIsbn;
    private JButton btnBuscarIsbn;
    private JTextField txtBuscarTexto;
    private JButton btnBuscarTexto;
    private JButton btnMostrarTodos;
    private DefaultListModel<Libro> modeloResultados;
    private JList<Libro> lstResultados;
    private JLabel lblResultados;
    private JLabel lblIsbnEdicion;
    private JTextField txtTituloEdicion;
    private JTextField txtAutorEdicion;
    private JComboBox<String> cboCategoriaEdicion;
    private JSpinner spiAnioEdicion;
    private JLabel lblMeGustaEdicion;
    private JButton btnGuardar;

    private Lista biblioteca = new Lista();

    // ISBN del libro seleccionado (-1 = ninguno). Para guardar se usa el ISBN,
    // no la posición en la JList ni el título.
    private int isbnSeleccionado = -1;
    private int busquedaActiva = SIN_BUSQUEDA;
    private int isbnBuscado;             // último ISBN encontrado con la búsqueda binaria
    private String textoBuscado = "";    // última consulta de texto válida

    public Ventana() {
        // R1: los 20 libros se cargan una sola vez, al crear la ventana.
        // Cambiar de pestaña no vuelve a pasar por aquí, así que no se duplican.
        biblioteca.cargarDatosIniciales();

        tabbedPane1 = new JTabbedPane();
        tabbedPane1.addTab("Registro", crearPestanaRegistro());
        tabbedPane1.addTab("Búsquedas y edición", crearPestanaBusquedas());

        principal = new JPanel(new BorderLayout());
        principal.add(tabbedPane1, BorderLayout.CENTER);

        actualizarTotal();
        limpiarFormularioEdicion();

        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarLibro();
            }
        });
        btnBuscarIsbn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarPorIsbn();
            }
        });
        btnBuscarTexto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarPorTexto();
            }
        });
        btnMostrarTodos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarTodos();
            }
        });
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCambios();
            }
        });
        // Al seleccionar un libro de la lista SOLO se muestra su detalle; no se cambia ningún dato.
        lstResultados.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    mostrarSeleccionado();
                }
            }
        });
    }

    // ===================== Pestaña Registro (R1 y R2) =====================

    private JPanel crearPestanaRegistro() {
        txtIsbn = new JTextField(22);
        txtTitulo = new JTextField(22);
        txtAutor = new JTextField(22);
        cboCategoria = new JComboBox<>(Lista.CATEGORIAS);
        spiAnio = crearSpinnerAnio();
        btnRegistrar = new JButton("Registrar");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 8));
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Nuevo libro"),
                BorderFactory.createEmptyBorder(8, 10, 10, 10)));
        formulario.add(new JLabel("ISBN:"));
        formulario.add(txtIsbn);
        formulario.add(new JLabel("Título:"));
        formulario.add(txtTitulo);
        formulario.add(new JLabel("Autor:"));
        formulario.add(txtAutor);
        formulario.add(new JLabel("Categoría:"));
        formulario.add(cboCategoria);
        formulario.add(new JLabel("Año de publicación:"));
        formulario.add(spiAnio);
        formulario.add(new JLabel("Me gusta: empieza en 0"));
        formulario.add(btnRegistrar);

        // El FlowLayout deja el formulario con su tamaño natural (no lo estira).
        JPanel contenedorFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contenedorFormulario.add(formulario);

        lblTotal = new JLabel();
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTotal.add(lblTotal);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(contenedorFormulario, BorderLayout.NORTH);
        panel.add(panelTotal, BorderLayout.CENTER);
        return panel;
    }

    // Spinner de año: desde 1 hasta el año actual, sin separador de miles (2026 y no 2.026).
    private JSpinner crearSpinnerAnio() {
        int anioActual = Year.now().getValue();
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(anioActual, 1, anioActual, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        return spinner;
    }

    // R2: lee el formulario y registra el libro. Si algo falla, avisa y no cambia nada;
    // lo que se escribió se queda en el formulario para poder corregirlo.
    private void registrarLibro() {
        int isbn;
        try {
            isbn = Integer.parseInt(txtIsbn.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarError("El ISBN debe ser un número entero positivo.");
            return;
        }
        int anio;
        try {
            spiAnio.commitEdit();   // toma el año aunque se haya escrito a mano
            anio = ((Number) spiAnio.getValue()).intValue();
        } catch (ParseException ex) {
            mostrarError("El año debe estar entre 1 y " + Year.now().getValue() + ".");
            return;
        }
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        String categoria = (String) cboCategoria.getSelectedItem();

        try {
            // Constructor de 5 datos: el libro nuevo empieza con 0 me gusta.
            biblioteca.registrar(new Libro(isbn, titulo, autor, categoria, anio));
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
            return;
        }
        actualizarTotal();
        refrescarResultados();   // si la búsqueda activa incluye al libro nuevo, aparece
        mostrarMensaje("Libro registrado correctamente.\nTotal de libros: " + biblioteca.getCantidad());
        limpiarFormularioRegistro();
    }

    private void limpiarFormularioRegistro() {
        txtIsbn.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        cboCategoria.setSelectedIndex(0);
        spiAnio.setValue(Year.now().getValue());
        txtIsbn.requestFocusInWindow();
    }

    private void actualizarTotal() {
        lblTotal.setText("Total de libros en el catálogo: " + biblioteca.getCantidad());
    }

    // ===================== Pestaña Búsquedas y edición (R3 y R4) =====================

    private JPanel crearPestanaBusquedas() {
        // --- Zona de búsqueda ---
        txtBuscarIsbn = new JTextField(10);
        btnBuscarIsbn = new JButton("Buscar por ISBN");
        txtBuscarTexto = new JTextField(20);
        btnBuscarTexto = new JButton("Buscar coincidencias");
        btnMostrarTodos = new JButton("Mostrar todos");

        JPanel filaIsbn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaIsbn.add(new JLabel("ISBN:"));
        filaIsbn.add(txtBuscarIsbn);
        filaIsbn.add(btnBuscarIsbn);

        JPanel filaTexto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaTexto.add(new JLabel("Título o autor:"));
        filaTexto.add(txtBuscarTexto);
        filaTexto.add(btnBuscarTexto);
        filaTexto.add(btnMostrarTodos);

        JPanel panelBusqueda = new JPanel(new GridLayout(2, 1));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Buscar"));
        panelBusqueda.add(filaIsbn);
        panelBusqueda.add(filaTexto);

        // --- Resultados ---
        modeloResultados = new DefaultListModel<>();
        lstResultados = new JList<>(modeloResultados);
        lstResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lblResultados = new JLabel("Busque por ISBN, por texto o pulse Mostrar todos.");

        JPanel panelResultados = new JPanel(new BorderLayout(5, 5));
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));
        panelResultados.add(lblResultados, BorderLayout.NORTH);
        panelResultados.add(new JScrollPane(lstResultados), BorderLayout.CENTER);

        // --- Formulario de edición ---
        lblIsbnEdicion = new JLabel();
        txtTituloEdicion = new JTextField(18);
        txtAutorEdicion = new JTextField(18);
        cboCategoriaEdicion = new JComboBox<>(Lista.CATEGORIAS);
        spiAnioEdicion = crearSpinnerAnio();
        lblMeGustaEdicion = new JLabel();
        lblMeGustaEdicion.setFont(lblMeGustaEdicion.getFont().deriveFont(Font.BOLD, 14f));
        btnGuardar = new JButton("Guardar cambios");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("ISBN (no se edita):"));
        formulario.add(lblIsbnEdicion);
        formulario.add(new JLabel("Título:"));
        formulario.add(txtTituloEdicion);
        formulario.add(new JLabel("Autor:"));
        formulario.add(txtAutorEdicion);
        formulario.add(new JLabel("Categoría:"));
        formulario.add(cboCategoriaEdicion);
        formulario.add(new JLabel("Año de publicación:"));
        formulario.add(spiAnioEdicion);
        formulario.add(new JLabel("Me gusta:"));
        formulario.add(lblMeGustaEdicion);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnGuardar);

        JPanel contenidoEdicion = new JPanel(new BorderLayout(0, 10));
        contenidoEdicion.add(formulario, BorderLayout.NORTH);
        contenidoEdicion.add(botones, BorderLayout.CENTER);

        JPanel panelEdicion = new JPanel(new BorderLayout());
        panelEdicion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Libro seleccionado"),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        panelEdicion.add(contenidoEdicion, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(panelResultados, BorderLayout.CENTER);
        panel.add(panelEdicion, BorderLayout.EAST);
        return panel;
    }

    // R4: busca por ISBN con la búsqueda binaria de Lista.
    private void buscarPorIsbn() {
        int isbn;
        try {
            isbn = Integer.parseInt(txtBuscarIsbn.getText().trim());
        } catch (NumberFormatException ex) {
            limpiarResultados();
            mostrarError("El ISBN debe ser un número entero positivo.");
            return;
        }
        int indice;
        try {
            indice = biblioteca.buscarIndicePorCodigo(isbn);
        } catch (IllegalArgumentException ex) {   // ISBN cero o negativo
            limpiarResultados();
            mostrarError(ex.getMessage());
            return;
        }
        if (indice == -1) {
            // Se borra la consulta anterior: así no se puede editar ni dar me gusta
            // al libro que se había buscado antes.
            limpiarResultados();
            mostrarAviso("No se encontró ningún libro con el ISBN " + isbn + ".");
            return;
        }
        busquedaActiva = BUSQUEDA_ISBN;
        isbnBuscado = isbn;
        ArrayList<Libro> encontrado = new ArrayList<>();
        encontrado.add(biblioteca.obtenerLibro(indice));
        mostrarResultados(encontrado, descripcionIsbn(indice));
        lstResultados.setSelectedIndex(0);   // queda seleccionado y se muestra en el formulario
    }

    // R3: búsqueda lineal de coincidencias en título o autor.
    private void buscarPorTexto() {
        String texto = txtBuscarTexto.getText();
        ArrayList<Libro> resultados;
        try {
            resultados = biblioteca.buscarCoincidencias(texto);
        } catch (IllegalArgumentException ex) {   // consulta vacía o solo con espacios
            limpiarResultados();
            mostrarError(ex.getMessage());
            return;
        }
        busquedaActiva = BUSQUEDA_TEXTO;
        textoBuscado = texto.trim();
        mostrarResultados(resultados, descripcionTexto(resultados.size()));
        if (resultados.isEmpty()) {
            mostrarAviso("No hay coincidencias para \"" + textoBuscado + "\".");
        }
    }

    // Muestra el catálogo completo en el orden de la colección principal (por ISBN).
    private void mostrarTodos() {
        busquedaActiva = MOSTRAR_TODOS;
        ArrayList<Libro> todos = biblioteca.obtener();
        mostrarResultados(todos, "Catálogo completo: " + todos.size() + " libros, ordenados por ISBN.");
    }

    private String descripcionIsbn(int indice) {
        return "ISBN " + isbnBuscado + " encontrado con búsqueda binaria (índice " + indice + ").";
    }

    private String descripcionTexto(int cantidad) {
        return cantidad + (cantidad == 1 ? " coincidencia" : " coincidencias")
                + " para \"" + textoBuscado + "\" en título o autor.";
    }

    // Llena la JList con los libros recibidos. La selección anterior se borra.
    private void mostrarResultados(ArrayList<Libro> libros, String descripcion) {
        lstResultados.clearSelection();
        modeloResultados.clear();
        for (Libro libro : libros) {
            modeloResultados.addElement(libro);
        }
        lblResultados.setText(descripcion);
        limpiarFormularioEdicion();
    }

    private void limpiarResultados() {
        busquedaActiva = SIN_BUSQUEDA;
        lstResultados.clearSelection();
        modeloResultados.clear();
        lblResultados.setText("Sin resultados.");
        limpiarFormularioEdicion();
    }

    // Se llama cuando cambia la selección de la JList: solo muestra el detalle.
    private void mostrarSeleccionado() {
        Libro libro = lstResultados.getSelectedValue();
        if (libro == null) {
            limpiarFormularioEdicion();
            return;
        }
        isbnSeleccionado = libro.getIsbn();
        lblIsbnEdicion.setText(String.valueOf(libro.getIsbn()));
        txtTituloEdicion.setText(libro.getTitulo());
        txtAutorEdicion.setText(libro.getAutor());
        cboCategoriaEdicion.setSelectedItem(libro.getCategoria());
        spiAnioEdicion.setValue(libro.getAnioPublicacion());
        lblMeGustaEdicion.setText(String.valueOf(libro.getMeGusta()));
        habilitarEdicion(true);
    }

    private void limpiarFormularioEdicion() {
        isbnSeleccionado = -1;
        lblIsbnEdicion.setText("(ninguno seleccionado)");
        txtTituloEdicion.setText("");
        txtAutorEdicion.setText("");
        cboCategoriaEdicion.setSelectedIndex(0);
        spiAnioEdicion.setValue(Year.now().getValue());
        lblMeGustaEdicion.setText("-");
        habilitarEdicion(false);
    }

    // Los campos solo se pueden escribir cuando hay un libro seleccionado.
    // Los botones quedan activos para poder avisar si no hay selección.
    private void habilitarEdicion(boolean habilitar) {
        txtTituloEdicion.setEditable(habilitar);
        txtAutorEdicion.setEditable(habilitar);
        cboCategoriaEdicion.setEnabled(habilitar);
        spiAnioEdicion.setEnabled(habilitar);
    }

    // R4: guarda la edición del libro seleccionado, identificándolo por su ISBN.
    private void guardarCambios() {
        if (isbnSeleccionado == -1) {
            mostrarAviso("Primero busque y seleccione un libro.");
            return;
        }
        int anio;
        try {
            spiAnioEdicion.commitEdit();
            anio = ((Number) spiAnioEdicion.getValue()).intValue();
        } catch (ParseException ex) {
            mostrarError("El año debe estar entre 1 y " + Year.now().getValue() + ".");
            return;
        }
        try {
            biblioteca.editarLibro(isbnSeleccionado, txtTituloEdicion.getText(), txtAutorEdicion.getText(),
                    (String) cboCategoriaEdicion.getSelectedItem(), anio);
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());   // no se cambió ningún dato del libro
            return;
        }
        int isbnEditado = isbnSeleccionado;
        refrescarResultados();
        if (isbnSeleccionado == isbnEditado) {
            mostrarMensaje("Cambios guardados.");
        } else {
            mostrarMensaje("Cambios guardados.\nEl libro ya no coincide con \"" + textoBuscado
                    + "\", así que se retiró de los resultados.");
        }
    }

    // Vuelve a calcular la búsqueda activa (después de registrar o editar) y, si el libro
    // que estaba seleccionado sigue en los resultados, lo vuelve a seleccionar.
    private void refrescarResultados() {
        int isbnAnterior = isbnSeleccionado;
        if (busquedaActiva == BUSQUEDA_TEXTO) {
            ArrayList<Libro> resultados = biblioteca.buscarCoincidencias(textoBuscado);
            mostrarResultados(resultados, descripcionTexto(resultados.size()));
        } else if (busquedaActiva == MOSTRAR_TODOS) {
            mostrarTodos();
        } else if (busquedaActiva == BUSQUEDA_ISBN) {
            int indice = biblioteca.buscarIndicePorCodigo(isbnBuscado);
            ArrayList<Libro> encontrado = new ArrayList<>();
            encontrado.add(biblioteca.obtenerLibro(indice));
            mostrarResultados(encontrado, descripcionIsbn(indice));
        } else {
            return;
        }
        seleccionarPorIsbn(isbnAnterior);
    }

    private void seleccionarPorIsbn(int isbn) {
        for (int i = 0; i < modeloResultados.size(); i++) {
            if (modeloResultados.get(i).getIsbn() == isbn) {
                lstResultados.setSelectedIndex(i);
                lstResultados.ensureIndexIsVisible(i);
                return;
            }
        }
        // Si ya no está en los resultados, la selección queda vacía.
    }

    // ===================== Mensajes =====================

    private void mostrarMensaje(String texto) {
        JOptionPane.showMessageDialog(principal, texto, "BiblioLikes", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAviso(String texto) {
        JOptionPane.showMessageDialog(principal, texto, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String texto) {
        JOptionPane.showMessageDialog(principal, texto, "Dato no válido", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("BiblioLikes - Catálogo de libros");
                frame.setContentPane(new Ventana().principal);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(950, 600);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
