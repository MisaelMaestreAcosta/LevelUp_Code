package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.service.UserService;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.presentation.controller.AuthController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Ventana de acceso y registro con vista interactiva de código comparativo
 * y autenticación conectada a Oracle 18c XE mediante {@link AppContext}.
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AuthController authController;
    private final boolean oracleConnected;

    private boolean registerMode = false;
    private JPanel usernameRow;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JLabel lblFeedback;
    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private JButton btnSubmit;
    private Theme.ModernButton btnTabLogin;
    private Theme.ModernButton btnTabRegister;

    // Componentes del comparador interactivo en el panel izquierdo
    private JTextArea txtCodePreview;
    private JLabel lblPreviewNote;
    private Theme.ModernButton btnLangJava;
    private Theme.ModernButton btnLangPython;
    private Theme.ModernButton btnLangCpp;

    public LoginFrame() {
        AppContext ctx = AppContext.getInstance();
        this.oracleConnected = ctx.isOracleConnected();

        if (this.oracleConnected) {
            this.authController = new AuthController(ctx.getUserService());
        } else {
            Role studentRole = new Role(1, "Estudiante", "Acceso a cursos, retos y seguimiento de progreso");
            Role adminRole = new Role(2, "Administrador", "Gestión integral de lenguajes, módulos, retos y logros");

            InMemoryAuthRepository repository = new InMemoryAuthRepository();
            UserService fallbackService = new UserService(repository, studentRole);
            this.authController = new AuthController(fallbackService);

            User demoStudent = fallbackService.register("Estudiante", "estudiante@levelup.edu.co", "estudiante123");
            demoStudent.setExperience(1450);
            demoStudent.setLevel(5);
            demoStudent.setCurrentStreak(7);
            demoStudent.setMaxStreak(12);

            User demoAdmin = fallbackService.register("Admin", "admin@levelup.edu.co", "admin12345");
            demoAdmin.setRole(adminRole);
            demoAdmin.setExperience(4200);
            demoAdmin.setLevel(10);
            demoAdmin.setCurrentStreak(21);
            demoAdmin.setMaxStreak(30);
        }

        initComponents();
    }

    private void initComponents() {
        setTitle("LevelUp Code — Plataforma de Aprendizaje Comparativo");
        setSize(1060, 670);
        setMinimumSize(new Dimension(940, 610));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);

        root.add(buildLeftShowcasePane(), BorderLayout.WEST);
        root.add(buildRightAuthPane(), BorderLayout.CENTER);

        setContentPane(root);
        selectLanguagePreview("JAVA");
    }

    private JPanel buildLeftShowcasePane() {
        JPanel leftPane = new JPanel(new BorderLayout(0, 16));
        leftPane.setPreferredSize(new Dimension(480, 0));
        leftPane.setBackground(Theme.BG_SIDEBAR);
        leftPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(28, 28, 24, 28)
        ));

        // Encabezado de marca
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandRow.setOpaque(false);
        JLabel lblBrand = new JLabel("  LevelUp Code");
        lblBrand.setIcon(Theme.createVectorIcon(Theme.IconKind.TERMINAL, 22, Theme.ACCENT_INDIGO));
        lblBrand.setFont(Theme.FONT_TITLE);
        lblBrand.setForeground(Theme.TEXT_PRIMARY);
        brandRow.add(lblBrand);
        brandRow.add(Box.createHorizontalStrut(12));
        brandRow.add(Theme.createPillBadge("UPC · ING. SISTEMAS", Theme.ACCENT_INDIGO));
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHero1 = new JLabel("Domina la lógica algorítmica");
        lblHero1.setFont(Theme.FONT_HERO);
        lblHero1.setForeground(Theme.TEXT_PRIMARY);
        lblHero1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHero2 = new JLabel("más allá de la sintaxis.");
        lblHero2.setFont(Theme.FONT_HERO);
        lblHero2.setForeground(Theme.ACCENT_INDIGO);
        lblHero2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><body style='width:325px;color:#334155;'>"
                + "Compara estructuras de datos, herencia y algoritmos en tiempo real entre "
                + "<b>Java</b>, <b>Python</b> y <b>C++</b> con validación dinámica contra <b>Casos_Prueba</b>."
                + "</body></html>");
        lblDesc.setFont(Theme.FONT_REGULAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSection.add(brandRow);
        topSection.add(Box.createVerticalStrut(18));
        topSection.add(lblHero1);
        topSection.add(Box.createVerticalStrut(2));
        topSection.add(lblHero2);
        topSection.add(Box.createVerticalStrut(10));
        topSection.add(lblDesc);

        leftPane.add(topSection, BorderLayout.NORTH);

        // Tarjeta interactiva de comparación de código en vivo con identidad cromática por lenguaje
        Theme.RoundedPanel codeCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        codeCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel codeHeader = new JPanel(new BorderLayout(8, 0));
        codeHeader.setOpaque(false);

        JLabel lblSnippetTitle = new JLabel("BÚSQUEDA BINARIA · O(log n)");
        lblSnippetTitle.setFont(Theme.FONT_SECTION);
        lblSnippetTitle.setForeground(Theme.TEXT_MUTED);
        codeHeader.add(lblSnippetTitle, BorderLayout.CENTER);

        JPanel langTabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        langTabs.setOpaque(false);
        btnLangJava = new Theme.ModernButton("Java", Theme.LANG_JAVA, Theme.darken(Theme.LANG_JAVA, 0.12f), Color.WHITE, null);
        btnLangPython = new Theme.ModernButton("Python", Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        btnLangCpp = new Theme.ModernButton("C++", Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);

        Insets tabInsets = new Insets(4, 10, 4, 10);
        btnLangJava.setMargin(tabInsets);
        btnLangPython.setMargin(tabInsets);
        btnLangCpp.setMargin(tabInsets);
        btnLangJava.setFont(Theme.FONT_SMALL);
        btnLangPython.setFont(Theme.FONT_SMALL);
        btnLangCpp.setFont(Theme.FONT_SMALL);

        btnLangJava.addActionListener(e -> selectLanguagePreview("JAVA"));
        btnLangPython.addActionListener(e -> selectLanguagePreview("PYTHON"));
        btnLangCpp.addActionListener(e -> selectLanguagePreview("CPP"));

        langTabs.add(btnLangJava);
        langTabs.add(btnLangPython);
        langTabs.add(btnLangCpp);
        codeHeader.add(langTabs, BorderLayout.EAST);

        txtCodePreview = new JTextArea();
        txtCodePreview.setEditable(false);
        txtCodePreview.setFont(Theme.FONT_CODE);
        txtCodePreview.setBackground(Theme.EDITOR_BG);
        txtCodePreview.setForeground(Theme.TEXT_PRIMARY);
        txtCodePreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        lblPreviewNote = new JLabel();
        lblPreviewNote.setFont(Theme.FONT_SMALL);
        lblPreviewNote.setForeground(Theme.ACCENT_GREEN);
        lblPreviewNote.setIcon(Theme.createVectorIcon(Theme.IconKind.CODE, 14, Theme.ACCENT_GREEN));

        JPanel codeBody = new JPanel(new BorderLayout(0, 10));
        codeBody.setOpaque(false);
        codeBody.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        codeBody.add(txtCodePreview, BorderLayout.CENTER);
        codeBody.add(lblPreviewNote, BorderLayout.SOUTH);

        codeCard.add(codeHeader, BorderLayout.NORTH);
        codeCard.add(codeBody, BorderLayout.CENTER);

        leftPane.add(codeCard, BorderLayout.CENTER);

        // Pie de estado de base de datos Oracle y seguridad PBKDF2
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        JLabel lblSecurity = new JLabel("Hash PBKDF2-HMAC-SHA256");
        lblSecurity.setFont(Theme.FONT_SMALL);
        lblSecurity.setForeground(Theme.TEXT_MUTED);
        lblSecurity.setIcon(Theme.createVectorIcon(Theme.IconKind.SHIELD, 14, Theme.ACCENT_GREEN));
        footer.add(lblSecurity, BorderLayout.WEST);

        String dbBadgeText = oracleConnected ? "ORACLE 18C XE · CONECTADO" : "MODO LOCAL EN MEMORIA";
        Color dbBadgeColor = oracleConnected ? Theme.ACCENT_GREEN : Theme.ACCENT_AMBER;
        footer.add(Theme.createPillBadge(dbBadgeText, dbBadgeColor), BorderLayout.EAST);

        leftPane.add(footer, BorderLayout.SOUTH);
        return leftPane;
    }

    private JPanel buildRightAuthPane() {
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(Theme.BG_DARK);
        rightWrapper.setBorder(BorderFactory.createEmptyBorder(32, 44, 28, 44));

        JPanel formColumn = new JPanel();
        formColumn.setLayout(new BoxLayout(formColumn, BoxLayout.Y_AXIS));
        formColumn.setOpaque(false);

        // Selector de modo: Iniciar Sesión / Crear Cuenta
        JPanel modeSwitch = new JPanel(new GridLayout(1, 2, 8, 0));
        modeSwitch.setOpaque(false);
        modeSwitch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        modeSwitch.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnTabLogin = new Theme.ModernButton(
                "Iniciar Sesión", Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
        btnTabRegister = new Theme.ModernButton(
                "Crear Cuenta", Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);

        btnTabLogin.addActionListener(e -> setAuthMode(false));
        btnTabRegister.addActionListener(e -> setAuthMode(true));

        modeSwitch.add(btnTabLogin);
        modeSwitch.add(btnTabRegister);

        lblFormTitle = new JLabel("Bienvenido de nuevo");
        lblFormTitle.setFont(Theme.FONT_HERO);
        lblFormTitle.setForeground(Theme.TEXT_PRIMARY);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFormSubtitle = new JLabel("Ingresa tus credenciales para retomar tu racha y módulos activos.");
        lblFormSubtitle.setFont(Theme.FONT_REGULAR);
        lblFormSubtitle.setForeground(Theme.TEXT_MUTED);
        lblFormSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fila condicional de Nombre (solo visible en modo Registro)
        usernameRow = new JPanel();
        usernameRow.setLayout(new BoxLayout(usernameRow, BoxLayout.Y_AXIS));
        usernameRow.setOpaque(false);
        usernameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameRow.setVisible(false);

        JLabel lblUsername = new JLabel("NOMBRE COMPLETO O ALIAS");
        lblUsername.setFont(Theme.FONT_SECTION);
        lblUsername.setForeground(Theme.TEXT_SECONDARY);
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = Theme.createStyledTextField("Ej. Estudiante Nuevo");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameRow.add(lblUsername);
        usernameRow.add(Box.createVerticalStrut(6));
        usernameRow.add(txtUsername);
        usernameRow.add(Box.createVerticalStrut(14));

        // Campo de correo
        JLabel lblEmail = new JLabel("CORREO ELECTRÓNICO");
        lblEmail.setFont(Theme.FONT_SECTION);
        lblEmail.setForeground(Theme.TEXT_SECONDARY);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtEmail = Theme.createStyledTextField("estudiante@levelup.edu.co");
        txtEmail.setText("estudiante@levelup.edu.co");
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campo de contraseña
        JLabel lblPassword = new JLabel("CONTRASEÑA");
        lblPassword.setFont(Theme.FONT_SECTION);
        lblPassword.setForeground(Theme.TEXT_SECONDARY);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = Theme.createStyledPasswordField();
        txtPassword.setText("estudiante123");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.addActionListener(e -> handleAuthSubmit());

        // Etiqueta de retroalimentación en línea
        lblFeedback = new JLabel(" ");
        lblFeedback.setFont(Theme.FONT_SMALL);
        lblFeedback.setForeground(Theme.ACCENT_ROSE);
        lblFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Botón principal
        btnSubmit = Theme.createPrimaryButton("Ingresar a la Plataforma", Theme.ACCENT_INDIGO);
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubmit.addActionListener(e -> handleAuthSubmit());

        // Sección de perfiles de acceso rápido
        JPanel quickHeader = Theme.createSectionHeader("Perfiles de Acceso Rápido (Oracle XEPDB1)", "1 clic");
        quickHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        quickHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel demoCardsGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        demoCardsGrid.setOpaque(false);
        demoCardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        demoCardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        demoCardsGrid.add(createQuickProfileCard(
                "Estudiante",
                "Estudiante · Nivel 5 (1,450 XP)",
                "ESTUDIANTE",
                Theme.ACCENT_INDIGO,
                "estudiante@levelup.edu.co",
                "estudiante123"
        ));
        demoCardsGrid.add(createQuickProfileCard(
                "Admin",
                "Administrador · Gestión CRUD",
                "ADMIN",
                Theme.ACCENT_GREEN,
                "admin@levelup.edu.co",
                "admin12345"
        ));

        formColumn.add(modeSwitch);
        formColumn.add(Box.createVerticalStrut(22));
        formColumn.add(lblFormTitle);
        formColumn.add(Box.createVerticalStrut(4));
        formColumn.add(lblFormSubtitle);
        formColumn.add(Box.createVerticalStrut(20));
        formColumn.add(usernameRow);
        formColumn.add(lblEmail);
        formColumn.add(Box.createVerticalStrut(6));
        formColumn.add(txtEmail);
        formColumn.add(Box.createVerticalStrut(14));
        formColumn.add(lblPassword);
        formColumn.add(Box.createVerticalStrut(6));
        formColumn.add(txtPassword);
        formColumn.add(Box.createVerticalStrut(10));
        formColumn.add(lblFeedback);
        formColumn.add(Box.createVerticalStrut(10));
        formColumn.add(btnSubmit);
        formColumn.add(Box.createVerticalStrut(26));
        formColumn.add(quickHeader);
        formColumn.add(Box.createVerticalStrut(12));
        formColumn.add(demoCardsGrid);

        rightWrapper.add(formColumn, BorderLayout.CENTER);
        return rightWrapper;
    }

    private JPanel createQuickProfileCard(
            String name, String meta, String badgeText, Color accent, String email, String password) {
        Theme.RoundedPanel card = new Theme.RoundedPanel(10, Theme.SURFACE, Theme.BORDER_SUBTLE, accent);
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 12));
        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);
        JLabel lblName = new JLabel(name);
        lblName.setFont(Theme.FONT_SUBTITLE);
        lblName.setForeground(Theme.TEXT_PRIMARY);
        topRow.add(lblName, BorderLayout.CENTER);
        topRow.add(Theme.createPillBadge(badgeText, accent), BorderLayout.EAST);

        JLabel lblMeta = new JLabel(meta);
        lblMeta.setFont(Theme.FONT_SMALL);
        lblMeta.setForeground(Theme.TEXT_MUTED);

        JLabel lblAction = new JLabel("Entrar con este perfil →");
        lblAction.setFont(Theme.FONT_SMALL);
        lblAction.setForeground(accent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAction.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(topRow);
        content.add(Box.createVerticalStrut(4));
        content.add(lblMeta);
        content.add(Box.createVerticalStrut(4));
        content.add(lblAction);

        card.add(content, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setFillColor(Theme.SURFACE_ELEVATED);
                card.setBorderColor(accent);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setFillColor(Theme.SURFACE);
                card.setBorderColor(Theme.BORDER_SUBTLE);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                setAuthMode(false);
                txtEmail.setText(email);
                txtPassword.setText(password);
                handleAuthSubmit();
            }
        });

        return card;
    }

    private void selectLanguagePreview(String lang) {
        if ("JAVA".equals(lang)) {
            btnLangJava.setPalette(Theme.LANG_JAVA, Theme.darken(Theme.LANG_JAVA, 0.12f), Color.WHITE, null);
            btnLangPython.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            btnLangCpp.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            txtCodePreview.setText(
                    "public class BinarySearch {\n"
                    + "    static int search(int[] a, int target) {\n"
                    + "        int low = 0, high = a.length - 1;\n"
                    + "        while (low <= high) {\n"
                    + "            int mid = low + (high - low) / 2;\n"
                    + "            if (a[mid] == target) return mid;\n"
                    + "            if (a[mid] < target) low = mid + 1;\n"
                    + "            else high = mid - 1;\n"
                    + "        }\n"
                    + "        return -1;\n"
                    + "    }\n"
                    + "}"
            );
            lblPreviewNote.setForeground(Theme.LANG_JAVA);
            lblPreviewNote.setText("Java 21 · Tipado estático y aritmética segura (#E76F00).");
        } else if ("PYTHON".equals(lang)) {
            btnLangPython.setPalette(Theme.LANG_PYTHON, Theme.darken(Theme.LANG_PYTHON, 0.12f), Color.WHITE, null);
            btnLangJava.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            btnLangCpp.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            txtCodePreview.setText(
                    "def binary_search(a: list[int], target: int) -> int:\n"
                    + "    low, high = 0, len(a) - 1\n"
                    + "    while low <= high:\n"
                    + "        mid = (low + high) // 2\n"
                    + "        if a[mid] == target:\n"
                    + "            return mid\n"
                    + "        elif a[mid] < target:\n"
                    + "            low = mid + 1\n"
                    + "        else:\n"
                    + "            high = mid - 1\n"
                    + "    return -1"
            );
            lblPreviewNote.setForeground(Theme.LANG_PYTHON);
            lblPreviewNote.setText("Python 3.12 · División entera // y sintaxis limpia (#3776AB).");
        } else {
            btnLangCpp.setPalette(Theme.LANG_CPP, Theme.darken(Theme.LANG_CPP, 0.12f), Color.WHITE, null);
            btnLangJava.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            btnLangPython.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            txtCodePreview.setText(
                    "#include <vector>\n\n"
                    + "int binarySearch(const std::vector<int>& a, int x) {\n"
                    + "    int low = 0, high = (int)a.size() - 1;\n"
                    + "    while (low <= high) {\n"
                    + "        int mid = low + (high - low) / 2;\n"
                    + "        if (a[mid] == x) return mid;\n"
                    + "        if (a[mid] < x) low = mid + 1;\n"
                    + "        else high = mid - 1;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}"
            );
            lblPreviewNote.setForeground(Theme.LANG_CPP);
            lblPreviewNote.setText("C++ 20 · Referencia constante sin copia en memoria (#00599C).");
        }
    }

    private void setAuthMode(boolean register) {
        this.registerMode = register;
        usernameRow.setVisible(register);
        lblFeedback.setText(" ");

        if (register) {
            btnTabRegister.setPalette(Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
            btnTabLogin.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            lblFormTitle.setText("Crear nueva cuenta de estudiante");
            lblFormSubtitle.setText("Se registrará en la tabla Usuarios con contraseña cifrada en PBKDF2.");
            btnSubmit.setText("Registrar en Oracle y Entrar");
            txtEmail.setText("");
            txtPassword.setText("");
            txtUsername.requestFocusInWindow();
        } else {
            btnTabLogin.setPalette(Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
            btnTabRegister.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            lblFormTitle.setText("Bienvenido de nuevo");
            lblFormSubtitle.setText("Ingresa tus credenciales para retomar tu racha y módulos activos.");
            btnSubmit.setText("Ingresar a la Plataforma");
        }
        revalidate();
        repaint();
    }

    private void handleAuthSubmit() {
        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String password = new String(txtPassword.getPassword());

        try {
            if (registerMode) {
                String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
                User created = authController.register(username, email, password);
                openDashboard(created);
            } else {
                Optional<User> authResult = authController.login(email, password);
                if (authResult.isPresent()) {
                    openDashboard(authResult.get());
                } else {
                    lblFeedback.setForeground(Theme.ACCENT_ROSE);
                    lblFeedback.setText("Credenciales incorrectas. Verifica tu correo o usa un perfil rápido.");
                }
            }
        } catch (Exception ex) {
            lblFeedback.setForeground(Theme.ACCENT_ROSE);
            lblFeedback.setText(ex.getMessage());
        }
    }

    private void openDashboard(User authenticatedUser) {
        DashboardFrame dashboard = new DashboardFrame(authenticatedUser);
        dashboard.setVisible(true);
        dispose();
    }

    /**
     * Repositorio en memoria de respaldo en caso de que el servicio local de Oracle esté detenido.
     */
    private static final class InMemoryAuthRepository implements UserRepository {

        private final Map<String, User> usersByEmail = new HashMap<>();
        private int sequence = 1;

        @Override
        public Optional<User> findByEmail(String email) {
            if (email == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(usersByEmail.get(email.trim().toLowerCase(Locale.ROOT)));
        }

        @Override
        public User save(User user) {
            if (user.getId() <= 0) {
                user.setId(sequence++);
            }
            usersByEmail.put(user.getEmail().trim().toLowerCase(Locale.ROOT), user);
            return user;
        }

        @Override
        public User update(User user) {
            usersByEmail.put(user.getEmail().trim().toLowerCase(Locale.ROOT), user);
            return user;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
