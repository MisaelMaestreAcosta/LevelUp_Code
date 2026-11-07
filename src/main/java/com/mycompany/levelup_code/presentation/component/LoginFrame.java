package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.service.UserService;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.presentation.controller.AuthController;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ventana de acceso y registro en JavaFX + CSS ({@code /styles/levelup.css})
 * con vista interactiva de código comparativo (Java, Python, C++) y autenticación
 * conectada a Oracle 18c XE mediante {@link AppContext}.
 */
public class LoginFrame {

    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private final Stage stage;
    private final AuthController authController;
    private final boolean oracleConnected;

    private boolean registerMode = false;
    private VBox usernameRow;
    private TextField txtUsername;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private Label lblFeedback;
    private Label lblFormTitle;
    private Label lblFormSubtitle;
    private Button btnSubmit;
    private Button btnTabLogin;
    private Button btnTabRegister;

    // Componentes del comparador interactivo en el panel izquierdo
    private TextArea txtCodePreview;
    private Label lblPreviewNote;
    private Button btnLangJava;
    private Button btnLangPython;
    private Button btnLangCpp;

    public LoginFrame() {
        this(new Stage());
    }

    public LoginFrame(Stage targetStage) {
        this.stage = targetStage != null ? targetStage : new Stage();
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

    public Stage getStage() {
        return stage;
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
        } else {
            stage.close();
        }
    }

    private void initComponents() {
        stage.setTitle("LevelUp Code — Plataforma de Aprendizaje Comparativo (JavaFX + CSS)");
        stage.setMinWidth(980);
        stage.setMinHeight(640);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");

        root.setLeft(buildLeftShowcasePane());
        root.setCenter(buildRightAuthPane());

        Scene scene = new Scene(root, 1100, 690);
        Theme.applyCss(scene);
        stage.setScene(scene);

        selectLanguagePreview("JAVA");
    }

    private VBox buildLeftShowcasePane() {
        VBox leftPane = new VBox(16);
        leftPane.getStyleClass().add("sidebar");
        leftPane.setPrefWidth(480);
        leftPane.setPadding(new Insets(28, 28, 24, 28));

        // Encabezado de marca
        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        Label lblBrand = new Label("LevelUp Code");
        lblBrand.getStyleClass().add("page-title");
        lblBrand.setGraphic(Theme.createIcon(Theme.IconKind.TERMINAL, 20, Theme.HEX_INDIGO));
        lblBrand.setGraphicTextGap(8);
        Label badgeUpc = Theme.createPillBadge("UPC · ING. SISTEMAS", "badge-indigo");
        brandRow.getChildren().addAll(lblBrand, badgeUpc);

        VBox heroBox = new VBox(2);
        Label lblHero1 = new Label("Domina la lógica algorítmica");
        lblHero1.getStyleClass().add("hero-title");
        Label lblHero2 = new Label("más allá de la sintaxis.");
        lblHero2.getStyleClass().add("hero-accent");
        heroBox.getChildren().addAll(lblHero1, lblHero2);

        Label lblDesc = new Label(
                "Compara estructuras de datos, herencia y algoritmos en tiempo real entre "
                + "Java, Python y C++ con validación dinámica contra Casos_Prueba."
        );
        lblDesc.getStyleClass().add("body-text");
        lblDesc.setWrapText(true);

        // Tarjeta interactiva de comparación de código en vivo
        VBox codeCard = new VBox(10);
        codeCard.getStyleClass().add("surface-card");
        VBox.setVgrow(codeCard, Priority.ALWAYS);

        HBox codeHeader = new HBox(8);
        codeHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblSnippetTitle = new Label("BÚSQUEDA BINARIA · O(log n)");
        lblSnippetTitle.getStyleClass().add("section-caption");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnLangJava = new Button("Java");
        btnLangPython = new Button("Python");
        btnLangCpp = new Button("C++");

        btnLangJava.setOnAction(e -> selectLanguagePreview("JAVA"));
        btnLangPython.setOnAction(e -> selectLanguagePreview("PYTHON"));
        btnLangCpp.setOnAction(e -> selectLanguagePreview("CPP"));

        codeHeader.getChildren().addAll(lblSnippetTitle, spacer, btnLangJava, btnLangPython, btnLangCpp);

        txtCodePreview = new TextArea();
        txtCodePreview.setEditable(false);
        txtCodePreview.getStyleClass().add("code-editor");
        VBox.setVgrow(txtCodePreview, Priority.ALWAYS);

        lblPreviewNote = new Label();
        lblPreviewNote.getStyleClass().add("text-success");
        lblPreviewNote.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 14, Theme.HEX_GREEN));
        lblPreviewNote.setGraphicTextGap(6);
        lblPreviewNote.setWrapText(true);

        codeCard.getChildren().addAll(codeHeader, txtCodePreview, lblPreviewNote);

        // Pie de estado de base de datos Oracle y seguridad PBKDF2
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lblSecurity = new Label("Hash PBKDF2-HMAC-SHA256");
        lblSecurity.getStyleClass().add("muted-text");
        lblSecurity.setGraphic(Theme.createIcon(Theme.IconKind.SHIELD, 14, Theme.HEX_GREEN));
        lblSecurity.setGraphicTextGap(6);

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        String dbBadgeText = oracleConnected ? "ORACLE 18C XE · CONECTADO" : "MODO LOCAL EN MEMORIA";
        String dbBadgeClass = oracleConnected ? "badge-green" : "badge-amber";
        Label badgeDb = Theme.createPillBadge(dbBadgeText, dbBadgeClass);

        footer.getChildren().addAll(lblSecurity, fSpacer, badgeDb);

        leftPane.getChildren().addAll(brandRow, heroBox, lblDesc, codeCard, footer);
        return leftPane;
    }

    private VBox buildRightAuthPane() {
        VBox formColumn = new VBox(14);
        formColumn.setPadding(new Insets(34, 48, 28, 48));
        formColumn.setAlignment(Pos.CENTER_LEFT);

        // Selector de modo: Iniciar Sesión / Crear Cuenta
        HBox modeSwitch = new HBox(10);
        btnTabLogin = new Button("Iniciar Sesión");
        btnTabLogin.getStyleClass().add("btn-primary");
        btnTabLogin.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTabLogin, Priority.ALWAYS);

        btnTabRegister = new Button("Crear Cuenta");
        btnTabRegister.getStyleClass().add("btn-secondary");
        btnTabRegister.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTabRegister, Priority.ALWAYS);

        btnTabLogin.setOnAction(e -> setAuthMode(false));
        btnTabRegister.setOnAction(e -> setAuthMode(true));
        modeSwitch.getChildren().addAll(btnTabLogin, btnTabRegister);

        lblFormTitle = new Label("Bienvenido de nuevo");
        lblFormTitle.getStyleClass().add("hero-title");

        lblFormSubtitle = new Label("Ingresa tus credenciales para retomar tu racha y módulos activos.");
        lblFormSubtitle.getStyleClass().add("muted-text");

        // Fila condicional de Nombre (solo visible en modo Registro)
        usernameRow = new VBox(6);
        usernameRow.setVisible(false);
        usernameRow.setManaged(false);

        Label lblUsername = new Label("NOMBRE COMPLETO O ALIAS");
        lblUsername.getStyleClass().add("section-caption");

        txtUsername = new TextField();
        txtUsername.setPromptText("Ej. Estudiante Nuevo");
        txtUsername.getStyleClass().add("input-field");
        usernameRow.getChildren().addAll(lblUsername, txtUsername);

        // Campo de correo
        VBox emailBox = new VBox(6);
        Label lblEmail = new Label("CORREO ELECTRÓNICO");
        lblEmail.getStyleClass().add("section-caption");
        txtEmail = new TextField(oracleConnected ? "" : "estudiante@levelup.edu.co");
        txtEmail.setPromptText("usuario@levelup.edu.co");
        txtEmail.getStyleClass().add("input-field");
        emailBox.getChildren().addAll(lblEmail, txtEmail);

        // Campo de contraseña
        VBox passBox = new VBox(6);
        Label lblPassword = new Label("CONTRASEÑA");
        lblPassword.getStyleClass().add("section-caption");
        txtPassword = new PasswordField();
        txtPassword.setText(oracleConnected ? "" : "estudiante123");
        txtPassword.setPromptText("Mínimo 8 caracteres");
        txtPassword.getStyleClass().add("input-field");
        txtPassword.setOnAction(e -> handlePrimaryAction());
        passBox.getChildren().addAll(lblPassword, txtPassword);

        // Mensajes de validación o error
        lblFeedback = new Label(" ");
        lblFeedback.getStyleClass().add("muted-text");
        lblFeedback.setWrapText(true);

        // Botón principal de acción
        btnSubmit = new Button("Ingresar al Espacio de Trabajo");
        btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_WHITE));
        btnSubmit.setGraphicTextGap(8);
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setOnAction(e -> handlePrimaryAction());

        formColumn.getChildren().addAll(
                modeSwitch, lblFormTitle, lblFormSubtitle,
                usernameRow, emailBox, passBox,
                lblFeedback, btnSubmit
        );

        // El acceso rápido de demostración se restringe estrictamente al modo sin Oracle
        if (!oracleConnected) {
            VBox quickCard = new VBox(10);
            quickCard.getStyleClass().add("surface-card");
            VBox.setMargin(quickCard, new Insets(8, 0, 0, 0));

            HBox quickHeader = Theme.createSectionHeader(
                    "Acceso Rápido con 1 Clic",
                    "Solo Modo Demo Local",
                    "badge-amber"
            );

            HBox quickButtons = new HBox(10);
            Button btnQuickStudent = new Button("Estudiante Demo · Nivel 5");
            btnQuickStudent.getStyleClass().add("btn-secondary");
            btnQuickStudent.setGraphic(Theme.createIcon(Theme.IconKind.USER, 14, Theme.HEX_INDIGO));
            btnQuickStudent.setGraphicTextGap(6);
            btnQuickStudent.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnQuickStudent, Priority.ALWAYS);
            btnQuickStudent.setOnAction(e -> quickLoginAs("estudiante@levelup.edu.co", "estudiante123"));

            Button btnQuickAdmin = new Button("Administrador Demo · Local");
            btnQuickAdmin.getStyleClass().add("btn-secondary");
            btnQuickAdmin.setGraphic(Theme.createIcon(Theme.IconKind.SHIELD, 14, Theme.HEX_GREEN));
            btnQuickAdmin.setGraphicTextGap(6);
            btnQuickAdmin.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnQuickAdmin, Priority.ALWAYS);
            btnQuickAdmin.setOnAction(e -> quickLoginAs("admin@levelup.edu.co", "admin12345"));

            quickButtons.getChildren().addAll(btnQuickStudent, btnQuickAdmin);
            quickCard.getChildren().addAll(quickHeader, quickButtons);
            formColumn.getChildren().add(quickCard);
        }
        return formColumn;
    }

    private void setAuthMode(boolean register) {
        this.registerMode = register;
        this.usernameRow.setVisible(register);
        this.usernameRow.setManaged(register);
        this.lblFeedback.setText(" ");

        btnTabLogin.getStyleClass().setAll(!register ? "btn-primary" : "btn-secondary");
        btnTabRegister.getStyleClass().setAll(register ? "btn-primary" : "btn-secondary");

        if (register) {
            lblFormTitle.setText("Crea tu cuenta de estudiante");
            lblFormSubtitle.setText("Regístrate en la tabla Usuarios con rol Estudiante y comienza en Nivel 1.");
            btnSubmit.setText("Registrar Cuenta e Iniciar");
            txtUsername.setText("");
            txtEmail.setText("");
            txtPassword.setText("");
            txtUsername.requestFocus();
        } else {
            lblFormTitle.setText("Bienvenido de nuevo");
            lblFormSubtitle.setText("Ingresa tus credenciales para retomar tu racha y módulos activos.");
            btnSubmit.setText("Ingresar al Espacio de Trabajo");
            if (!oracleConnected) {
                txtEmail.setText("estudiante@levelup.edu.co");
                txtPassword.setText("estudiante123");
            } else {
                txtPassword.setText("");
            }
        }
    }

    private void selectLanguagePreview(String lang) {
        if ("JAVA".equals(lang)) {
            btnLangJava.getStyleClass().setAll("btn-java");
            btnLangPython.getStyleClass().setAll("btn-secondary");
            btnLangCpp.getStyleClass().setAll("btn-secondary");
            txtCodePreview.setText(
                    "// Java 21 LTS — Tipado estático y prevención de overflow\n"
                    + "public class BinarySearch {\n"
                    + "    public static int search(int[] arr, int target) {\n"
                    + "        int low = 0, high = arr.length - 1;\n"
                    + "        while (low <= high) {\n"
                    + "            int mid = low + (high - low) / 2;\n"
                    + "            if (arr[mid] == target) return mid;\n"
                    + "            if (arr[mid] < target) low = mid + 1;\n"
                    + "            else high = mid - 1;\n"
                    + "        }\n"
                    + "        return -1;\n"
                    + "    }\n"
                    + "}"
            );
            lblPreviewNote.setText("Java 21: Arreglos tipados en memoria contigua y verificación estricta en compilación.");
        } else if ("PYTHON".equals(lang)) {
            btnLangJava.getStyleClass().setAll("btn-secondary");
            btnLangPython.getStyleClass().setAll("btn-python");
            btnLangCpp.getStyleClass().setAll("btn-secondary");
            txtCodePreview.setText(
                    "# Python 3.12 — Expresividad algorítmica y type hints\n"
                    + "def binary_search(arr: list[int], target: int) -> int:\n"
                    + "    low, high = 0, len(arr) - 1\n"
                    + "    while low <= high:\n"
                    + "        mid = (low + high) // 2\n"
                    + "        if arr[mid] == target:\n"
                    + "            return mid\n"
                    + "        elif arr[mid] < target:\n"
                    + "            low = mid + 1\n"
                    + "        else:\n"
                    + "            high = mid - 1\n"
                    + "    return -1"
            );
            lblPreviewNote.setText("Python 3.12: Enteros de precisión arbitraria y operador // de división entera.");
        } else {
            btnLangJava.getStyleClass().setAll("btn-secondary");
            btnLangPython.getStyleClass().setAll("btn-secondary");
            btnLangCpp.getStyleClass().setAll("btn-cpp");
            txtCodePreview.setText(
                    "// ISO C++ 20 — Referencia constante sin copia (Zero-Overhead)\n"
                    + "#include <vector>\n\n"
                    + "int binarySearch(const std::vector<int>& arr, int target) {\n"
                    + "    int low = 0;\n"
                    + "    int high = static_cast<int>(arr.size()) - 1;\n"
                    + "    while (low <= high) {\n"
                    + "        int mid = low + (high - low) / 2;\n"
                    + "        if (arr[mid] == target) return mid;\n"
                    + "        if (arr[mid] < target) low = mid + 1;\n"
                    + "        else high = mid - 1;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}"
            );
            lblPreviewNote.setText("C++ 20: Paso por referencia constante (const std::vector<int>&) con latencia mínima.");
        }
    }

    private void handlePrimaryAction() {
        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText() : "";

        try {
            if (registerMode) {
                String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
                User created = authController.register(username, email, password);
                showFeedback("¡Cuenta registrada exitosamente! Abriendo panel...", false);
                openDashboard(created);
            } else {
                Optional<User> authResult = authController.login(email, password);
                if (authResult.isPresent()) {
                    showFeedback("¡Sesión verificada! Cargando cursos y telemetría...", false);
                    openDashboard(authResult.get());
                } else {
                    String hint = oracleConnected
                            ? "Credenciales incorrectas. Verifica tu correo electrónico y contraseña."
                            : "Credenciales incorrectas. Verifica tu correo o usa un perfil de demostración local.";
                    showFeedback(hint, true);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error durante la autenticación o registro de usuario", ex);
            showFeedback(ex.getMessage(), true);
        }
    }

    private void quickLoginAs(String email, String password) {
        if (oracleConnected) {
            showFeedback("El acceso rápido está deshabilitado con Oracle activo. Inicia sesión con credenciales verificadas.", true);
            return;
        }
        setAuthMode(false);
        txtEmail.setText(email);
        txtPassword.setText(password);
        try {
            Optional<User> res = authController.login(email, password);
            if (res.isPresent()) {
                openDashboard(res.get());
            } else {
                showFeedback("No se pudo autenticar la cuenta de demostración solicitada.", true);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Fallo en inicio rápido de demostración local", ex);
            showFeedback("Error al iniciar sesión local: " + ex.getMessage(), true);
        }
    }

    private void showFeedback(String message, boolean isError) {
        lblFeedback.getStyleClass().setAll(isError ? "text-danger" : "text-success");
        lblFeedback.setText(message);
    }

    private void openDashboard(User authenticatedUser) {
        DashboardFrame dashboard = new DashboardFrame(stage, authenticatedUser);
        dashboard.show();
    }

    /**
     * Repositorio en memoria de respaldo cuando la base de datos local no está activa.
     */
    private static final class InMemoryAuthRepository implements UserRepository {

        private final Map<String, User> byEmail = new HashMap<>();
        private final Map<Integer, User> byId = new HashMap<>();
        private int sequence = 1;

        @Override
        public synchronized Optional<User> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public synchronized Optional<User> findByEmail(String email) {
            if (email == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(byEmail.get(email.trim().toLowerCase(Locale.ROOT)));
        }

        @Override
        public synchronized User save(User user) {
            if (user.getId() <= 0) {
                user.setId(sequence++);
            }
            byId.put(user.getId(), user);
            if (user.getEmail() != null) {
                byEmail.put(user.getEmail().trim().toLowerCase(Locale.ROOT), user);
            }
            return user;
        }

        @Override
        public synchronized User update(User user) {
            if (user != null) {
                return save(user);
            }
            return null;
        }
    }
}
