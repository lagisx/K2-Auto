package com.autoatelier.controller.client;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ClientMyOrdersController extends BaseController {

    @FXML private VBox ordersListBox;
    @FXML private VBox detailPane;
    @FXML private Label detailService;
    @FXML private Label detailCar;
    @FXML private Label detailStatus;
    @FXML private Label detailPrice;
    @FXML private Label detailComment;
    @FXML private Label detailDescription;
    @FXML private Label statusLabel;

    @Override
    protected void onInit() {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
        loadOrders();
    }

    private void loadOrders() {
        statusLabel.setText("Загрузка заявок...");
        new Thread(() -> {
            try {
                List<Order> orders = OrderService.getInstance().getMyOrders();
                Platform.runLater(() -> {
                    ordersListBox.getChildren().clear();
                    if (orders.isEmpty()) {
                        statusLabel.setText("У вас пока нет заявок — создайте первую!");
                    } else {
                        statusLabel.setText("Заявок: " + orders.size());
                        orders.forEach(o -> ordersListBox.getChildren().add(buildOrderCard(o)));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private VBox buildOrderCard(Order order) {
        VBox card = new VBox(0);
        card.getStyleClass().add("order-card");
        card.setOnMouseClicked(e -> showDetail(order));
        card.setCursor(javafx.scene.Cursor.HAND);

        Order.Status st = order.getStatusEnum();

        HBox row = new HBox(0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Rectangle bar = new Rectangle(4, 72);
        bar.setStyle("-fx-fill: " + st.color + ";");
        bar.setArcWidth(4); bar.setArcHeight(4);

        VBox body = new VBox(8);
        body.getStyleClass().add("order-card-body");
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox top = new HBox(10);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label svcName = new Label(order.getService() != null
                ? order.getService().getName() : "Услуга #" + order.getServiceId());
        svcName.getStyleClass().add("order-card-service");
        HBox.setHgrow(svcName, Priority.ALWAYS);
        svcName.setWrapText(true);

        Label badge = new Label(st.display);
        badge.getStyleClass().addAll("order-card-status-badge", "badge-" + order.getStatus());
        top.getChildren().addAll(svcName, badge);

        Label carLbl = new Label("🚗  " + order.getCarInfo());
        carLbl.getStyleClass().add("order-card-meta");

        HBox bot = new HBox(0);
        bot.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(order.getPriceFormatted());
        price.getStyleClass().add("order-card-price");
        HBox.setHgrow(price, Priority.ALWAYS);
        String dateStr = order.getCreatedAt() != null
                ? order.getCreatedAt().substring(0, 10) : "";
        Label date = new Label(dateStr);
        date.getStyleClass().add("order-card-date");
        bot.getChildren().addAll(price, date);

        body.getChildren().addAll(top, carLbl, bot);

        if (order.getManagerComment() != null && !order.getManagerComment().isBlank()) {
            Label cmt = new Label("💬  " + order.getManagerComment());
            cmt.getStyleClass().add("order-card-comment");
            cmt.setWrapText(true);
            body.getChildren().add(cmt);
        }

        row.getChildren().addAll(bar, body);
        card.getChildren().add(row);
        return card;
    }

    private void showDetail(Order order) {
        detailPane.setVisible(true);
        detailPane.setManaged(true);
        detailService.setText(order.getService() != null ? order.getService().getName() : "—");
        detailCar.setText(order.getCarInfo());
        detailStatus.setText(order.getStatusDisplay());
        detailStatus.setStyle("-fx-text-fill: " + order.getStatusEnum().color + ";");
        detailPrice.setText(order.getPriceFormatted());
        detailDescription.setText(order.getDescription() != null ? order.getDescription() : "—");
        detailComment.setText(order.getManagerComment() != null
                ? order.getManagerComment() : "Комментарий не добавлен");
    }

    @FXML private void refresh() { loadOrders(); }
    @FXML private void goToProfile()  { SceneManager.navigate("client-profile"); }
    @FXML private void goToDashboard()  { SceneManager.navigate("client-dashboard"); }
    @FXML private void goToCatalog()    { SceneManager.navigate("client-catalog"); }
    @FXML private void goToNewOrder()   { SceneManager.navigate("client-new-order"); }
}
