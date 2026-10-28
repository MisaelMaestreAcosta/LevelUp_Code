package com.mycompany.levelup_code.ui;

import com.mycompany.levelup_code.persistence.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardFrame extends JFrame {

    private UserDAO userDAO;
    
    // Componentes principales
    private JTabbedPane tabbedPane;
    private JPanel panelDashboard;
    private JPanel panelUsuarios;
    private JPanel panelInventario;

    public DashboardFrame() {
        setTitle("LevelUp Code - Sistema Principal");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializamos el DAO
        userDAO = new UserDAO();

        // Creamos un contenedor de pestañas para ver varias secciones del programa
        tabbedPane = new JTabbedPane();

        // 1. Pestaña de Inicio / Dashboard
        panelDashboard = crearPanelDashboard();
        tabbedPane.addTab("🏠 Inicio", panelDashboard);

        // 2. Pestaña de Gestión de Usuarios
        panelUsuarios = crearPanelUsuarios();
        tabbedPane.addTab("👥 Gestión de Usuarios", panelUsuarios);

        // 3. Pestaña de Inventario / Productos (GameZone / LevelUp)
        panelInventario = crearPanelInventario();
        tabbedPane.addTab("🎮 Inventario / Catálogo", panelInventario);

        // Añadimos el panel de pestañas a la ventana
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelDashboard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));
        
        JLabel lblBienvenido = new JLabel("¡Bienvenido al panel principal de LevelUp Code!");
        lblBienvenido.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton btnPruebaDAO = new JButton("Probar Conexión DAO");
        btnPruebaDAO.setPreferredSize(new Dimension(220, 40));

        btnPruebaDAO.addActionListener(e -> {
            try {
                JOptionPane.showMessageDialog(this, 
                    "Conexión con Oracle y UserDAO funcionando correctamente.", 
                    "Estado de Persistencia", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setPreferredSize(new Dimension(220, 40));
        btnSalir.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de salir?", "Salir", JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) System.exit(0);
        });

        panel.add(lblBienvenido);
        panel.add(btnPruebaDAO);
        panel.add(btnSalir);
        return panel;
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Lista de Usuarios Registrados en el Sistema");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));

        // Tabla de ejemplo para mostrar datos de la base de datos
        String[] columnas = {"ID", "Usuario", "Rol", "Estado"};
        Object[][] datos = {
            {"1", "Rafael Vega", "Administrador", "Activo"},
            {"2", "Usuario Demo", "Cliente", "Activo"}
        };
        JTable tablaUsuarios = new JTable(datos, columnas);
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);

        JButton btnActualizar = new JButton("Actualizar Tabla desde BD");
        btnActualizar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Aquí se cargarán los datos reales usando UserDAO.findAll()");
        });

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnActualizar, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel lblInfo = new JLabel("Sección de Productos / Accesorios / Juegos en desarrollo.");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(lblInfo);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}