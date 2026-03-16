package com.autoatelier.controller.manager;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.Map;

public class ManagerDashboardController extends BaseController {

    @FXML private Label totalLabel;
    @FXML private Label newLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label completedLabel;
    @FXML private VBox urgentOrdersBox;

    @Override
    protected void onInit() {
        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                Map<String, Long> stats = OrderService.getInstance().getStats();
                List<Order> newOrders   = OrderService.getInstance().getOrdersByStatus(Order.Status.NEW.key);
                long total = stats.values().stream().mapToLong(Long::longValue).sum();

                Platform.runLater(() -> {
                    totalLabel.setText(String.valueOf(total));
                    newLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.NEW.key, 0L)));
                    inProgressLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.IN_PROGRESS.key, 0L)));
                    completedLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.COMPLETED.key, 0L)));
                    renderUrgent(newOrders.stream().limit(5).toList());
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void renderUrgent(List<Order> orders) {
        urgentOrdersBox.getChildren().clear();
        if (orders.isEmpty()) {
            Label lbl = new Label("Новых заявок нет — всё обработано ✅");
            lbl.getStyleClass().add("page-subtitle");
            urgentOrdersBox.getChildren().add(lbl);
            return;
        }
        for (Order o : orders) {
            VBox card = new VBox(0);
            card.getStyleClass().add("order-card");
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> SceneManager.navigate("manager-orders"));

            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER_LEFT);
            Rectangle bar = new Rectangle(4, 68);
            bar.setStyle("-fx-fill: " + Order.Status.NEW.color + ";");
            bar.setArcWidth(4); bar.setArcHeight(4);

            VBox body = new VBox(6);
            body.getStyleClass().add("order-card-body");
            HBox.setHgrow(body, Priority.ALWAYS);

            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            String svcText = o.getService() != null ? o.getService().getName() : "Услуга #" + o.getServiceId();
            Label svc = new Label(svcText);
            svc.getStyleClass().add("order-card-service");
            HBox.setHgrow(svc, Priority.ALWAYS);
            Label badge = new Label("Новый");
            badge.getStyleClass().addAll("order-card-status-badge", "badge-new");
            top.getChildren().addAll(svc, badge);

            String clientName = o.getClient() != null ? o.getClient().getFullName() : "Клиент";
            Label meta = new Label("👤  " + clientName + "   🚗  " + o.getCarInfo());
            meta.getStyleClass().add("order-card-meta");

            body.getChildren().addAll(top, meta);
            row.getChildren().addAll(bar, body);
            card.getChildren().add(row);
            urgentOrdersBox.getChildren().add(card);
        }
    }

    @FXML private void goToDashboard()  {  }
    @FXML private void goToOrders()     { SceneManager.navigate("manager-orders"); }
    @FXML private void goToCatalog()     { SceneManager.navigate("manager-catalog"); }
    @FXML private void goToProfile()     { SceneManager.navigate("manager-profile"); }
}
