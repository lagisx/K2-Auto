package com.autoatelier.controller.client;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.SceneManager;
import com.autoatelier.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ClientDashboardController extends BaseController {

    @FXML private Label greetingLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label completedOrdersLabel;
    @FXML private VBox recentOrdersBox;

    @Override
    protected void onInit() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        greetingLabel.setText("Здравствуйте, " + name + "!");
        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                List<Order> orders = OrderService.getInstance().getMyOrders();
                long total     = orders.size();
                long active    = orders.stream().filter(o -> Order.Status.IN_PROGRESS.key.equals(o.getStatus())).count();
                long completed = orders.stream().filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus())).count();

                Platform.runLater(() -> {
                    totalOrdersLabel.setText(String.valueOf(total));
                    activeOrdersLabel.setText(String.valueOf(active));
                    completedOrdersLabel.setText(String.valueOf(completed));
                    renderRecent(orders.stream().limit(3).toList());
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void renderRecent(List<Order> orders) {
        recentOrdersBox.getChildren().clear();
        if (orders.isEmpty()) {
            Label empty = new Label("Заявок пока нет — создайте первую в каталоге!");
            empty.getStyleClass().add("page-subtitle");
            recentOrdersBox.getChildren().add(empty);
            return;
        }
        for (Order order : orders) {
            VBox card = new VBox(0);
            card.getStyleClass().add("order-card");
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> SceneManager.navigate("client-orders"));

            Order.Status st = order.getStatusEnum();
            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER_LEFT);

            Rectangle bar = new Rectangle(4, 64);
            bar.setStyle("-fx-fill: " + st.color + ";");
            bar.setArcWidth(4); bar.setArcHeight(4);

            VBox body = new VBox(6);
            body.getStyleClass().add("order-card-body");
            HBox.setHgrow(body, Priority.ALWAYS);

            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            Label svc = new Label(order.getService() != null ? order.getService().getName() : "Услуга #" + order.getServiceId());
            svc.getStyleClass().add("order-card-service");
            HBox.setHgrow(svc, Priority.ALWAYS);
            Label badge = new Label(st.display);
            badge.getStyleClass().addAll("order-card-status-badge", "badge-" + order.getStatus());
            top.getChildren().addAll(svc, badge);

            Label car = new Label("🚗  " + order.getCarInfo());
            car.getStyleClass().add("order-card-meta");

            body.getChildren().addAll(top, car);
            row.getChildren().addAll(bar, body);
            card.getChildren().add(row);
            recentOrdersBox.getChildren().add(card);
        }
    }

    @FXML private void goToProfile()  { SceneManager.navigate("client-profile"); }
    @FXML private void goToDashboard()  {  }
    @FXML private void goToCatalog()    { SceneManager.navigate("client-catalog"); }
    @FXML private void goToOrders()     { SceneManager.navigate("client-orders"); }
    @FXML private void goToNewOrder()   { SceneManager.navigate("client-new-order"); }
}
