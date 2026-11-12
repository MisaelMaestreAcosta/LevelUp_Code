package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleGoalRepository;
import com.mycompany.levelup_code.domain.model.LearningGoal;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class LearningGoalsPane extends VBox {
    private final OracleGoalRepository repository=new OracleGoalRepository();
    private final Label status=new Label();
    private final VBox controls=new VBox(12);
    private final VBox cards=new VBox(12);
    private final int userId;
    private boolean busy;

    public LearningGoalsPane(int userId,boolean oracle) {
        super(14); this.userId=userId;
        Label heading=new Label("Mis metas"); heading.getStyleClass().add("page-title");
        status.setWrapText(true);
        ComboBox<LearningGoal.Type> type=new ComboBox<>();
        type.getItems().setAll(LearningGoal.Type.values()); type.getSelectionModel().selectFirst();
        Spinner<Integer> target=new Spinner<>(1,10000,3); target.setAccessibleText("Objetivo de la meta");
        ComboBox<String> duration=new ComboBox<>(); duration.getItems().setAll("Hoy","7 días");
        duration.getSelectionModel().selectLast();
        Button create=new Button("Crear meta"); create.getStyleClass().add("btn-primary");
        create.setOnAction(e -> {
            var goalType=type.getValue(); int amount=target.getValue();
            int days=duration.getSelectionModel().getSelectedIndex()==0?1:7;
            run(() -> { repository.create(userId,goalType,amount,days); return repository.load(userId); });
        });
        Button refresh=new Button("Actualizar metas"); refresh.setOnAction(e -> run(() -> repository.load(userId)));
        Label help=new Label("El progreso incluye la actividad guardada desde el inicio del día. "
                + "Cada reto cuenta una vez dentro del periodo. Las metas no dan XP adicional."); help.setWrapText(true);
        controls.getChildren().addAll(help,new FlowPane(10,10,type,target,duration,create,refresh),cards);
        getChildren().addAll(heading,status,controls);
        if(oracle) run(() -> repository.load(userId));
        else { controls.setDisable(true); status.setText("Conecta tu cuenta a Oracle para crear metas y seguir su avance."); }
    }
    private void run(Callable<List<LearningGoal>> action) {
        if(busy) return; busy=true; controls.setDisable(true); status.setText("Actualizando tus metas…");
        Task<List<LearningGoal>> task=new Task<>() { protected List<LearningGoal> call() throws Exception { return action.call(); } };
        task.setOnSucceeded(e -> {
            busy=false; controls.setDisable(false); cards.getChildren().clear();
            for(var goal:task.getValue()) {
                Label title=new Label(goal.type()+": "+Math.min(goal.current(),goal.target())+" de "+goal.target());
                title.getStyleClass().add("card-title");
                Label dates=new Label(goal.start()+" → "+goal.end());
                Label result=new Label(goal.completed()?"¡Meta cumplida!"
                        : goal.end().isBefore(java.time.LocalDate.now()) ? "Periodo terminado. Puedes crear una nueva meta."
                        : "Sigue avanzando. El progreso se actualiza al consultar.");
                result.setWrapText(true);
                ProgressBar progress=new ProgressBar(goal.fraction()); progress.setMaxWidth(Double.MAX_VALUE);
                Button remove=new Button("Eliminar meta");
                remove.setOnAction(event -> {
                    Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"¿Eliminar esta meta?",ButtonType.OK,ButtonType.CANCEL);
                    confirm.initOwner(getScene().getWindow());
                    if(confirm.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK)
                        run(() -> { repository.delete(userId,goal.id()); return repository.load(userId); });
                });
                VBox card=new VBox(8,title,dates,progress,result,remove); card.getStyleClass().add("surface-card");
                cards.getChildren().add(card);
            }
            status.setText(task.getValue().isEmpty()?"Crea una meta pequeña para comenzar.":"Metas actualizadas con tu actividad guardada.");
        });
        task.setOnFailed(e -> { busy=false; controls.setDisable(false); status.setText(task.getException() instanceof IllegalArgumentException
                ?task.getException().getMessage():"No se pudo acceder a las metas. Revisa la conexión y la instalación de la actualización de metas."); });
        Thread thread=new Thread(task,"learning-goals"); thread.setDaemon(true); thread.start();
    }
}
