import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.time.Year;

/**
 * Ventana principal de BiblioLikes, hecha a mano con Swing (sin el diseñador de IntelliJ).
 * Aquí solo están los controles y sus eventos; las validaciones, búsquedas y
 * ordenamientos están en la clase Lista.
 */
public class Ventana {
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

    private Lista biblioteca = new Lista();

    public Ventana() {
        // R1: los 20 libros se cargan una sola vez, al crear la ventana.
        // Cambiar de pestaña no vuelve a pasar por aquí, así que no se duplican.
        biblioteca.cargarDatosIniciales();

        tabbedPane1 = new JTabbedPane();
        tabbedPane1.addTab("Registro", crearPestanaRegistro());

        principal = new JPanel(new BorderLayout());
        principal.add(tabbedPane1, BorderLayout.CENTER);

        actualizarTotal();

        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarLibro();
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

    // ===================== Mensajes =====================

    private void mostrarMensaje(String texto) {
        JOptionPane.showMessageDialog(principal, texto, "BiblioLikes", JOptionPane.INFORMATION_MESSAGE);
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
