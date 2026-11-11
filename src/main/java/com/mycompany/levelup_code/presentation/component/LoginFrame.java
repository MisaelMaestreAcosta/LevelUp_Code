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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
    private TextField txtVisiblePassword;
    private CheckBox chkShowPassword;
    private Label lblUsernameError;
    private Label lblEmailError;
    private Label lblPasswordError;
    private Label lblPasswordHint;
    private ProgressIndicator authProgress;
    private VBox quickCard;
    private boolean authenticating;
    private Task<Optional<User>> authTask;
    private Scene loginScene;
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
        txtEmail.requestFocus();
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
        } else {
            stage.close();
        }
    }

    private void initComponents() {
        stage.setTitle("LevelUp Code · Bienvenido");
        stage.setMinWidth(360);
        stage.setMinHeight(420);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");

        VBox showcase = buildLeftShowcasePane();
        VBox form = buildRightAuthPane();
        StackPane formContainer = new StackPane(form);
        formContainer.setMinWidth(0);
        BorderPane content = new BorderPane();
        content.setMinWidth(0);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setCenter(formContainer);
        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("scroll-pane-clean");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        content.setLeft(showcase);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1100, 690);
        Theme.applyCss(scene);
        stage.setScene(scene);
        loginScene = scene;
        scroll.viewportBoundsProperty().addListener((observable, oldBounds, bounds) -> {
            boolean compact = bounds.getWidth() < 900;
            if (compact && content.getBottom() != showcase) {
                content.setLeft(null);
                content.setBottom(showcase);
            } else if (!compact && content.getLeft() != showcase) {
                content.setBottom(null);
                content.setLeft(showcase);
            }
            form.setPadding(new Insets(28, compact ? 20 : 32, 28, compact ? 20 : 32));
        });
        // Keep Tab navigation and validation focus visible even in short windows.
        scene.focusOwnerProperty().addListener((observable, oldNode, node) -> {
            if (node != null) {
                Platform.runLater(() -> revealFocusedControl(scroll, content, node));
            }
        });
        stage.showingProperty().addListener((observable, wasShowing, showing) -> {
            if (!showing && authTask != null) {
                authTask.cancel();
            }
        });

        selectLanguagePreview("JAVA");
    }

    private VBox buildLeftShowcasePane() {
        VBox leftPane = new VBox(16);
        leftPane.getStyleClass().add("sidebar");
        leftPane.setPrefWidth(430);
        leftPane.setMinWidth(0);
        leftPane.setPadding(new Insets(28, 28, 24, 28));

        // Encabezado de marca
        FlowPane brandRow = new FlowPane(10, 8);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        Label lblBrand = new Label("LevelUp Code");
        lblBrand.getStyleClass().add("page-title");
        lblBrand.setGraphic(Theme.createIcon(Theme.IconKind.TERMINAL, 20, Theme.HEX_INDIGO));
        lblBrand.setGraphicTextGap(8);
        Label badgeUpc = Theme.createPillBadge("UPC · ING. SISTEMAS", "badge-indigo");
        brandRow.getChildren().addAll(lblBrand, badgeUpc);

        VBox heroBox = new VBox(2);
        Label lblHero1 = new Label("Aprende a programar");
        lblHero1.getStyleClass().add("hero-title");
        lblHero1.setWrapText(true);
        Label lblHero2 = new Label("paso a paso.");
        lblHero2.getStyleClass().add("hero-accent");
        heroBox.getChildren().addAll(lblHero1, lblHero2);

        Label lblDesc = new Label(
                "Practica con retos y compara cómo se resuelve un mismo problema en Java, Python y C++."
        );
        lblDesc.getStyleClass().add("body-text");
        lblDesc.setWrapText(true);

        // Tarjeta interactiva de comparación de código en vivo
        VBox codeCard = new VBox(10);
        codeCard.getStyleClass().add("surface-card");
        VBox.setVgrow(codeCard, Priority.ALWAYS);

        FlowPane codeHeader = new FlowPane(8, 8);
        codeHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblSnippetTitle = new Label("UN EJEMPLO, TRES LENGUAJES");
        lblSnippetTitle.getStyleClass().add("section-caption");

        btnLangJava = new Button("Java");
        btnLangPython = new Button("Python");
        btnLangCpp = new Button("C++");

        btnLangJava.setOnAction(e -> selectLanguagePreview("JAVA"));
        btnLangPython.setOnAction(e -> selectLanguagePreview("PYTHON"));
        btnLangCpp.setOnAction(e -> selectLanguagePreview("CPP"));

        codeHeader.getChildren().addAll(btnLangJava, btnLangPython, btnLangCpp);

        txtCodePreview = new TextArea();
        txtCodePreview.setEditable(false);
        txtCodePreview.setPrefColumnCount(24);
        txtCodePreview.setPrefRowCount(12);
        txtCodePreview.setMinWidth(0);
        txtCodePreview.setAccessibleText("Ejemplo de búsqueda binaria. Selecciona un lenguaje para comparar.");
        txtCodePreview.getStyleClass().add("code-editor");
        VBox.setVgrow(txtCodePreview, Priority.ALWAYS);

        lblPreviewNote = new Label();
        lblPreviewNote.getStyleClass().add("text-success");
        lblPreviewNote.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 14, Theme.HEX_GREEN));
        lblPreviewNote.setGraphicTextGap(6);
        lblPreviewNote.setWrapText(true);

        codeCard.getChildren().addAll(lblSnippetTitle, codeHeader, txtCodePreview, lblPreviewNote);

        // Pie de estado de base de datos Oracle y seguridad PBKDF2
        FlowPane footer = new FlowPane(8, 8);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lblSecurity = new Label("Contraseña protegida");
        lblSecurity.getStyleClass().add("muted-text");
        lblSecurity.setGraphic(Theme.createIcon(Theme.IconKind.SHIELD, 14, Theme.HEX_GREEN));
        lblSecurity.setGraphicTextGap(6);

        String dbBadgeText = oracleConnected ? "EN LÍNEA" : "DEMO SIN CONEXIÓN";
        String dbBadgeClass = oracleConnected ? "badge-green" : "badge-amber";
        Label badgeDb = Theme.createPillBadge(dbBadgeText, dbBadgeClass);

        footer.getChildren().addAll(lblSecurity, badgeDb);

        leftPane.getChildren().addAll(brandRow, heroBox, lblDesc, codeCard, footer);
        return leftPane;
    }

    private VBox buildRightAuthPane() {
        VBox formColumn = new VBox(14);
        formColumn.setMinWidth(0);
        formColumn.setMaxWidth(600);
        formColumn.setPadding(new Insets(34, 48, 28, 48));
        formColumn.setAlignment(Pos.CENTER_LEFT);

        // Selector de modo: Iniciar Sesión / Crear Cuenta
        HBox modeSwitch = new HBox(10);
        btnTabLogin = new Button("Iniciar sesión");
        btnTabLogin.getStyleClass().add("btn-primary");
        btnTabLogin.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTabLogin, Priority.ALWAYS);

        btnTabRegister = new Button("Crear cuenta");
        btnTabRegister.getStyleClass().add("btn-secondary");
        btnTabRegister.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTabRegister, Priority.ALWAYS);

        btnTabLogin.setOnAction(e -> setAuthMode(false));
        btnTabRegister.setOnAction(e -> setAuthMode(true));
        modeSwitch.getChildren().addAll(btnTabLogin, btnTabRegister);

        lblFormTitle = new Label("Bienvenido de nuevo");
        lblFormTitle.getStyleClass().add("hero-title");
        lblFormTitle.setWrapText(true);

        lblFormSubtitle = new Label("Entra para continuar aprendiendo.");
        lblFormSubtitle.getStyleClass().add("muted-text");
        lblFormSubtitle.setWrapText(true);

        // Fila condicional de Nombre (solo visible en modo Registro)
        usernameRow = new VBox(6);
        usernameRow.setVisible(false);
        usernameRow.setManaged(false);

        Label lblUsername = new Label("Nombre o alias");
        lblUsername.getStyleClass().add("section-caption");

        txtUsername = new TextField();
        txtUsername.setPromptText("Ej. Estudiante Nuevo");
        txtUsername.getStyleClass().add("input-field");
        lblUsername.setLabelFor(txtUsername);
        txtUsername.setAccessibleText("Nombre o alias");
        txtUsername.setOnAction(e -> txtEmail.requestFocus());
        lblUsernameError = createFieldError();
        usernameRow.getChildren().addAll(lblUsername, txtUsername, lblUsernameError);

        // Campo de correo
        VBox emailBox = new VBox(6);
        Label lblEmail = new Label("Correo electrónico");
        lblEmail.getStyleClass().add("section-caption");
        txtEmail = new TextField(oracleConnected ? "" : "estudiante@levelup.edu.co");
        txtEmail.setPromptText("usuario@levelup.edu.co");
        txtEmail.getStyleClass().add("input-field");
        lblEmail.setLabelFor(txtEmail);
        txtEmail.setAccessibleText("Correo electrónico");
        txtEmail.setOnAction(e -> passwordControl().requestFocus());
        lblEmailError = createFieldError();
        emailBox.getChildren().addAll(lblEmail, txtEmail, lblEmailError);

        // Campo de contraseña
        VBox passBox = new VBox(6);
        Label lblPassword = new Label("Contraseña");
        lblPassword.getStyleClass().add("section-caption");
        txtPassword = new PasswordField();
        txtPassword.setText(oracleConnected ? "" : "estudiante123");
        txtPassword.setPromptText("Tu contraseña");
        txtPassword.getStyleClass().add("input-field");
        txtPassword.setAccessibleText("Contraseña");
        txtPassword.setAccessibleHelp("Usa la contraseña de tu cuenta.");
        lblPassword.setLabelFor(txtPassword);
        txtPassword.setOnAction(e -> handlePrimaryAction());
        txtVisiblePassword = new TextField();
        txtVisiblePassword.getStyleClass().add("input-field");
        txtVisiblePassword.setAccessibleText("Contraseña visible");
        txtVisiblePassword.setAccessibleHelp("Usa la contraseña de tu cuenta.");
        txtVisiblePassword.promptTextProperty().bind(txtPassword.promptTextProperty());
        txtVisiblePassword.textProperty().bindBidirectional(txtPassword.textProperty());
        txtVisiblePassword.setOnAction(e -> handlePrimaryAction());
        chkShowPassword = new CheckBox("Mostrar contraseña");
        txtVisiblePassword.visibleProperty().bind(chkShowPassword.selectedProperty());
        txtVisiblePassword.managedProperty().bind(txtVisiblePassword.visibleProperty());
        txtPassword.visibleProperty().bind(chkShowPassword.selectedProperty().not());
        txtPassword.managedProperty().bind(txtPassword.visibleProperty());
        chkShowPassword.selectedProperty().addListener((observable, oldValue, visible) -> {
            TextField previous = visible ? txtPassword : txtVisiblePassword;
            TextField current = passwordControl();
            current.selectRange(previous.getAnchor(), previous.getCaretPosition());
            lblPassword.setLabelFor(current);
        });
        StackPane passwordFields = new StackPane(txtPassword, txtVisiblePassword);
        passwordFields.setMinWidth(0);
        lblPasswordHint = new Label("Usa la contraseña de tu cuenta.");
        lblPasswordHint.getStyleClass().add("muted-text");
        lblPasswordHint.setWrapText(true);
        lblPasswordError = createFieldError();
        passBox.getChildren().addAll(lblPassword, passwordFields, chkShowPassword,
                lblPasswordHint, lblPasswordError);

        // Mensajes de validación o error
        lblFeedback = new Label();
        lblFeedback.getStyleClass().add("muted-text");
        lblFeedback.setWrapText(true);
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);
        authProgress = new ProgressIndicator();
        authProgress.setPrefSize(20, 20);
        authProgress.setMaxSize(20, 20);
        authProgress.setVisible(false);
        authProgress.setManaged(false);
        authProgress.setAccessibleText("Procesando solicitud");
        HBox feedbackRow = new HBox(8, authProgress, lblFeedback);
        feedbackRow.setAlignment(Pos.CENTER_LEFT);
        feedbackRow.visibleProperty().bind(lblFeedback.visibleProperty());
        feedbackRow.managedProperty().bind(feedbackRow.visibleProperty());
        HBox.setHgrow(lblFeedback, Priority.ALWAYS);

        // Botón principal de acción
        btnSubmit = new Button("Iniciar sesión");
        btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_WHITE));
        btnSubmit.setGraphicTextGap(8);
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setOnAction(e -> handlePrimaryAction());
        btnSubmit.setDefaultButton(true);

        formColumn.getChildren().addAll(
                modeSwitch, lblFormTitle, lblFormSubtitle,
                usernameRow, emailBox, passBox,
                feedbackRow, btnSubmit
        );

        // El acceso rápido de demostración se restringe estrictamente al modo sin Oracle
        if (!oracleConnected) {
            quickCard = new VBox(10);
            quickCard.getStyleClass().add("surface-card");
            VBox.setMargin(quickCard, new Insets(8, 0, 0, 0));

            Label quickHeader = new Label("Prueba una cuenta demo");
            quickHeader.getStyleClass().add("card-title");
            Label demoHint = new Label("Estás sin conexión. Las cuentas y los cambios de esta demo no se guardan al cerrar.");
            demoHint.getStyleClass().add("muted-text");
            demoHint.setWrapText(true);

            FlowPane quickButtons = new FlowPane(10, 10);
            Button btnQuickStudent = new Button("Estudiante demo");
            btnQuickStudent.getStyleClass().add("btn-secondary");
            btnQuickStudent.setGraphic(Theme.createIcon(Theme.IconKind.USER, 14, Theme.HEX_INDIGO));
            btnQuickStudent.setGraphicTextGap(6);
            btnQuickStudent.setMaxWidth(Double.MAX_VALUE);
            btnQuickStudent.setOnAction(e -> quickLoginAs("estudiante@levelup.edu.co", "estudiante123"));

            Button btnQuickAdmin = new Button("Administrador demo");
            btnQuickAdmin.getStyleClass().add("btn-secondary");
            btnQuickAdmin.setGraphic(Theme.createIcon(Theme.IconKind.SHIELD, 14, Theme.HEX_GREEN));
            btnQuickAdmin.setGraphicTextGap(6);
            btnQuickAdmin.setMaxWidth(Double.MAX_VALUE);
            btnQuickAdmin.setOnAction(e -> quickLoginAs("admin@levelup.edu.co", "admin12345"));

            quickButtons.getChildren().addAll(btnQuickStudent, btnQuickAdmin);
            Button reconnect = new Button("Comprobar conexión con Oracle");
            reconnect.getStyleClass().add("btn-ghost-green");
            Label reconnectStatus = new Label();
            reconnectStatus.getStyleClass().add("muted-text");
            reconnectStatus.setWrapText(true);
            reconnect.setOnAction(event -> {
                if (authenticating) return;
                reconnect.setDisable(true);
                reconnectStatus.setText("Comprobando Oracle…");
                Task<Boolean> check = new Task<>() {
                    @Override protected Boolean call() { return AppContext.reconnectOracle(); }
                };
                check.setOnSucceeded(done -> {
                    reconnect.setDisable(false);
                    if (!stage.isShowing() || stage.getScene() != loginScene) return;
                    if (check.getValue()) new LoginFrame(stage).show();
                    else reconnectStatus.setText("Oracle todavía no responde. Puedes continuar en modo demo.");
                });
                check.setOnFailed(done -> {
                    reconnect.setDisable(false);
                    LOGGER.log(Level.WARNING, "No se pudo reconectar con Oracle", check.getException());
                    reconnectStatus.setText("No pudimos comprobar Oracle. Inténtalo de nuevo.");
                });
                Thread worker = new Thread(check, "login-oracle-reconnect");
                worker.setDaemon(true);
                worker.start();
            });
            quickCard.getChildren().addAll(quickHeader, demoHint, quickButtons, reconnect, reconnectStatus);
            formColumn.getChildren().add(quickCard);
        }
        installValidation(txtUsername, lblUsernameError, this::usernameError);
        installValidation(txtEmail, lblEmailError, this::emailError);
        installValidation(txtPassword, lblPasswordError, this::passwordError);
        installValidation(txtVisiblePassword, lblPasswordError, this::passwordError);
        return formColumn;
    }

    private void setAuthMode(boolean register) {
        if (authenticating || registerMode == register) {
            return;
        }
        this.registerMode = register;
        this.usernameRow.setVisible(register);
        this.usernameRow.setManaged(register);
        clearFeedback();
        chkShowPassword.setSelected(false);
        txtPassword.clear();
        lblPasswordHint.setText(register ? "Elige una contraseña de al menos 8 caracteres." : "Usa la contraseña de tu cuenta.");
        setFieldError(txtUsername, lblUsernameError, "");
        setFieldError(txtEmail, lblEmailError, "");
        setFieldError(txtPassword, lblPasswordError, "");

        btnTabLogin.getStyleClass().setAll("button", !register ? "btn-primary" : "btn-secondary");
        btnTabRegister.getStyleClass().setAll("button", register ? "btn-primary" : "btn-secondary");
        txtPassword.setPromptText(register ? "Mínimo 8 caracteres" : "Tu contraseña");

        if (register) {
            lblFormTitle.setText("Crea tu cuenta");
            lblFormSubtitle.setText("Empieza a aprender y sigue tu progreso.");
            btnSubmit.setText("Crear cuenta");
            txtUsername.requestFocus();
        } else {
            lblFormTitle.setText("Bienvenido de nuevo");
            lblFormSubtitle.setText("Entra para continuar aprendiendo.");
            btnSubmit.setText("Iniciar sesión");
            txtEmail.requestFocus();
        }
    }

    private void selectLanguagePreview(String lang) {
        if ("JAVA".equals(lang)) {
            btnLangJava.getStyleClass().setAll("button", "btn-java");
            btnLangPython.getStyleClass().setAll("button", "btn-secondary");
            btnLangCpp.getStyleClass().setAll("button", "btn-secondary");
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
            lblPreviewNote.setText("Java: indica el tipo de cada dato antes de usarlo.");
        } else if ("PYTHON".equals(lang)) {
            btnLangJava.getStyleClass().setAll("button", "btn-secondary");
            btnLangPython.getStyleClass().setAll("button", "btn-python");
            btnLangCpp.getStyleClass().setAll("button", "btn-secondary");
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
            lblPreviewNote.setText("Python: usa la indentación para organizar los pasos.");
        } else {
            btnLangJava.getStyleClass().setAll("button", "btn-secondary");
            btnLangPython.getStyleClass().setAll("button", "btn-secondary");
            btnLangCpp.getStyleClass().setAll("button", "btn-cpp");
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
            lblPreviewNote.setText("C++: consulta la lista sin copiarla ni modificarla.");
        }
    }

    private void handlePrimaryAction() {
        if (authenticating || !validateForm()) {
            return;
        }
        // Capture input on the FX thread; the worker never reads or modifies controls.
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String username = txtUsername.getText().trim();
        boolean registering = registerMode;
        Task<Optional<User>> task = new Task<>() {
            @Override
            protected Optional<User> call() {
                return registering
                        ? Optional.of(authController.register(username, email, password))
                        : authController.login(email, password);
            }
        };
        authTask = task;
        setAuthenticating(true);
        showFeedback(registering ? "Creando tu cuenta…" : "Verificando tus datos…", false);
        lblFeedback.getStyleClass().setAll("label", "muted-text");
        task.setOnSucceeded(event -> {
            authTask = null;
            setAuthenticating(false);
            if (!stage.isShowing() || stage.getScene() != loginScene) {
                return;
            }
            Optional<User> result = task.getValue();
            if (result.isEmpty()) {
                showFeedback("El correo o la contraseña no coinciden. Revisa tus datos e intenta de nuevo.", true);
                passwordControl().requestFocus();
                passwordControl().selectAll();
                return;
            }
            txtPassword.clear();
            try {
                showFeedback("¡Listo! Abriendo tu espacio…", false);
                openDashboard(result.get());
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "No se pudo abrir el panel después de autenticar", ex);
                // A successful registration must not be repeated when navigation fails.
                if (registering) {
                    setAuthMode(false);
                }
                showFeedback("Tu cuenta está lista, pero no pudimos abrir tu espacio. Intenta iniciar sesión de nuevo.", true);
            }
        });
        task.setOnFailed(event -> {
            authTask = null;
            setAuthenticating(false);
            Throwable failure = task.getException();
            LOGGER.log(Level.WARNING, "Error durante la autenticación o registro de usuario", failure);
            if (failure instanceof IllegalArgumentException
                    && "Ya existe un usuario registrado con ese correo.".equals(failure.getMessage())) {
                setFieldError(txtEmail, lblEmailError, "Este correo ya tiene una cuenta. Inicia sesión o usa otro.");
                showFeedback("No se creó la cuenta. Revisa el correo.", true);
                txtEmail.requestFocus();
            } else {
                showFeedback("No pudimos completar la solicitud. Intenta de nuevo en un momento.", true);
                btnSubmit.requestFocus();
            }
        });
        task.setOnCancelled(event -> {
            authTask = null;
            setAuthenticating(false);
            clearFeedback();
        });
        Thread worker = new Thread(task, "levelup-login-auth");
        worker.setDaemon(true);
        worker.start();
    }

    private void quickLoginAs(String email, String password) {
        if (oracleConnected || authenticating) {
            return;
        }
        setAuthMode(false);
        txtEmail.setText(email);
        txtPassword.setText(password);
        handlePrimaryAction();
    }

    private void showFeedback(String message, boolean isError) {
        lblFeedback.getStyleClass().setAll("label", isError ? "text-danger" : "text-success");
        lblFeedback.setText(message);
        lblFeedback.setVisible(true);
        lblFeedback.setManaged(true);
    }

    private void clearFeedback() {
        lblFeedback.setText("");
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);
    }

    private TextField passwordControl() {
        return chkShowPassword.isSelected() ? txtVisiblePassword : txtPassword;
    }

    private Label createFieldError() {
        Label error = new Label();
        error.getStyleClass().add("text-danger");
        error.setWrapText(true);
        error.setMinHeight(Region.USE_PREF_SIZE);
        error.setVisible(false);
        error.setManaged(false);
        return error;
    }

    private void setFieldError(TextField field, Label label, String message) {
        label.setText(message);
        label.setVisible(!message.isEmpty());
        label.setManaged(!message.isEmpty());
        field.setAccessibleHelp(message);
        if (field == txtPassword || field == txtVisiblePassword) {
            txtPassword.setAccessibleHelp(message.isEmpty() ? lblPasswordHint.getText() : message);
            txtVisiblePassword.setAccessibleHelp(txtPassword.getAccessibleHelp());
        }
    }

    private void installValidation(TextField field, Label error, java.util.function.Supplier<String> validate) {
        field.focusedProperty().addListener((observable, wasFocused, focused) -> {
            if (!focused && !authenticating && field.isVisible() && !field.getText().isEmpty()) {
                setFieldError(field, error, validate.get());
            }
        });
        field.textProperty().addListener((observable, oldText, newText) -> {
            if (!authenticating) {
                clearFeedback();
                if (error.isVisible()) {
                    setFieldError(field, error, validate.get());
                }
            }
        });
    }

    private String usernameError() {
        if (!registerMode) {
            return "";
        }
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            return "Escribe tu nombre o un alias.";
        }
        return username.length() > 80 ? "Usa un nombre de hasta 80 caracteres." : "";
    }

    private String emailError() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            return "Escribe el correo de tu cuenta.";
        }
        // Registration mirrors UserService; login does not add new account restrictions.
        if (registerMode && (email.length() > UserService.MAX_EMAIL_LENGTH
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            return "Usa un correo válido, por ejemplo nombre@correo.com (máximo 120 caracteres).";
        }
        return "";
    }

    private String passwordError() {
        String password = txtPassword.getText();
        if (password.isEmpty()) {
            return "Escribe tu contraseña.";
        }
        return registerMode && password.length() < 8 ? "Usa al menos 8 caracteres." : "";
    }

    private boolean validateForm() {
        String nameError = usernameError();
        String mailError = emailError();
        String passError = passwordError();
        setFieldError(txtUsername, lblUsernameError, nameError);
        setFieldError(txtEmail, lblEmailError, mailError);
        setFieldError(txtPassword, lblPasswordError, passError);
        TextField firstInvalid = !nameError.isEmpty() ? txtUsername
                : !mailError.isEmpty() ? txtEmail : !passError.isEmpty() ? passwordControl() : null;
        if (firstInvalid != null) {
            showFeedback("Revisa los campos indicados para continuar.", true);
            firstInvalid.requestFocus();
            return false;
        }
        return true;
    }

    private void setAuthenticating(boolean busy) {
        authenticating = busy;
        chkShowPassword.setSelected(false);
        txtUsername.setDisable(busy);
        txtEmail.setDisable(busy);
        txtPassword.setDisable(busy);
        txtVisiblePassword.setDisable(busy);
        chkShowPassword.setDisable(busy);
        btnTabLogin.setDisable(busy);
        btnTabRegister.setDisable(busy);
        btnSubmit.setDisable(busy);
        btnSubmit.setText(busy ? "Espera un momento…" : registerMode ? "Crear cuenta" : "Iniciar sesión");
        authProgress.setVisible(busy);
        authProgress.setManaged(busy);
        if (quickCard != null) {
            quickCard.setDisable(busy);
        }
    }

    private void revealFocusedControl(ScrollPane scroll, Region content, Node node) {
        if (node.getScene() != loginScene || loginScene.getFocusOwner() != node) {
            return;
        }
        content.layout();
        Bounds bounds = content.sceneToLocal(node.localToScene(node.getBoundsInLocal()));
        double viewportHeight = scroll.getViewportBounds().getHeight();
        double overflow = content.getHeight() - viewportHeight;
        if (overflow <= 0) {
            return;
        }
        double top = scroll.getVvalue() * overflow;
        if (bounds.getMinY() < top + 12) {
            scroll.setVvalue(Math.max(0, (bounds.getMinY() - 12) / overflow));
        } else if (bounds.getMaxY() > top + viewportHeight - 12) {
            scroll.setVvalue(Math.min(1, (bounds.getMaxY() + 12 - viewportHeight) / overflow));
        }
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
