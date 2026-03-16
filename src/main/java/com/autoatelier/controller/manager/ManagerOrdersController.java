package com.autoatelier.controller.manager;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ManagerOrdersController extends BaseController {

    @FXML private VBox ordersListBox;
    @FXML private VBox detailPane;
    @FXML private Label detailClient;
    @FXML private Label detailService;
    @FXML private Label detailCar;
    @FXML private Label detailDescription;
    @FXML private Label detailPrice;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea commentArea;
    @FXML private TextField priceField;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> filterCombo;

    private Order selectedOrder;

    @Override
    protected void onInit() {
        setupFilter();
        setupStatusCombo();
        detailPane.setVisible(false);
        detailPane.setManaged(false);
        loadOrders(null);
    }

    private void setupFilter() {
        filterCombo.getItems().addAll("Все", "Новые", "В работе", "Завершённые", "Отменённые");
        filterCombo.setValue("Все");
        filterCombo.setOnAction(e -> {
            String key = switch (filterCombo.getValue()) {
                case "Новые"       -> Order.Status.NEW.key;
                case "В работе"    -> Order.Status.IN_PROGRESS.key;
                case "Завершённые" -> Order.Status.COMPLETED.key;
                case "Отменённые"  -> Order.Status.CANCELLED.key;
                default -> null;
            };
            loadOrders(key);
        });
    }

    private void setupStatusCombo() {
        statusCombo.getItems().addAll(
            Order.Status.NEW.display, Order.Status.IN_PROGRESS.display,
            Order.Status.COMPLETED.display, Order.Status.CANCELLED.display
        );
    }

    private void loadOrders(String statusKey) {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                List<Order> orders = statusKey == null
                        ? OrderService.getInstance().getAllOrders()
                        : OrderService.getInstance().getOrdersByStatus(statusKey);
                Platform.runLater(() -> {
                    ordersListBox.getChildren().clear();
                    statusLabel.setText("Заказов: " + orders.size());
                    if (orders.isEmpty()) {
                        Label empty = new Label("Нет заказов");
                        empty.getStyleClass().add("page-subtitle");
                        ordersListBox.getChildren().add(empty);
                    } else {
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
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> showDetail(order));

        Order.Status st = order.getStatusEnum();

        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);

        Rectangle bar = new Rectangle(4, 80);
        bar.setStyle("-fx-fill: " + st.color + ";");
        bar.setArcWidth(4); bar.setArcHeight(4);

        VBox body = new VBox(8);
        body.getStyleClass().add("order-card-body");
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        String svcText = order.getService() != null ? order.getService().getName() : "Услуга #" + order.getServiceId();
        Label svc = new Label(svcText);
        svc.getStyleClass().add("order-card-service");
        HBox.setHgrow(svc, Priority.ALWAYS);
        svc.setWrapText(true);
        Label badge = new Label(st.display);
        badge.getStyleClass().addAll("order-card-status-badge", "badge-" + order.getStatus());
        top.getChildren().addAll(svc, badge);

        String clientName = order.getClient() != null ? order.getClient().getFullName() : "Клиент";
        Label clientLbl = new Label("👤  " + clientName + "  ·  🚗  " + order.getCarInfo());
        clientLbl.getStyleClass().add("order-card-meta");

        HBox bot = new HBox(0);
        bot.setAlignment(Pos.CENTER_LEFT);
        Label price = new Label(order.getPriceFormatted());
        price.getStyleClass().add("order-card-price");
        HBox.setHgrow(price, Priority.ALWAYS);
        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().substring(0, 10) : "";
        Label date = new Label(dateStr);
        date.getStyleClass().add("order-card-date");
        bot.getChildren().addAll(price, date);

        body.getChildren().addAll(top, clientLbl, bot);
        row.getChildren().addAll(bar, body);
        card.getChildren().add(row);
        return card;
    }

    private void showDetail(Order order) {
        selectedOrder = order;
        detailPane.setVisible(true);
        detailPane.setManaged(true);
        detailClient.setText(order.getClient() != null ? order.getClient().getFullName() : "—");
        detailService.setText(order.getService() != null ? order.getService().getName() : "—");
        detailCar.setText(order.getCarInfo());
        detailDescription.setText(order.getDescription() != null ? order.getDescription() : "—");
        detailPrice.setText(order.getPriceFormatted());
        statusCombo.setValue(order.getStatusEnum().display);
        commentArea.setText(order.getManagerComment() != null ? order.getManagerComment() : "");
        priceField.setText(order.getTotalPrice() != null ? String.valueOf(order.getTotalPrice().intValue()) : "");
    }

    @FXML
    private void handleSave() {
        if (selectedOrder == null) return;
        String statusDisplay = statusCombo.getValue();
        String statusKey = Order.Status.NEW.display.equals(statusDisplay) ? Order.Status.NEW.key
                : Order.Status.IN_PROGRESS.display.equals(statusDisplay) ? Order.Status.IN_PROGRESS.key
                : Order.Status.COMPLETED.display.equals(statusDisplay) ? Order.Status.COMPLETED.key
                : Order.Status.CANCELLED.key;
        String comment = commentArea.getText().trim();
        String priceText = priceField.getText().trim();
        saveButton.setDisable(true);
        new Thread(() -> {
            try {
                OrderService.getInstance().updateStatus(selectedOrder.getId(), statusKey,
                        comment.isBlank() ? null : comment);
                if (!priceText.isBlank())
                    OrderService.getInstance().updatePrice(selectedOrder.getId(), Double.parseDouble(priceText));
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    AlertUtil.info("Сохранено", "Заказ #" + selectedOrder.getId() + " обновлён");
                    detailPane.setVisible(false);
                    detailPane.setManaged(false);
                    selectedOrder = null;
                    loadOrders(null);
                });
            } catch (Exception e) {
                Platform.runLater(() -> { saveButton.setDisable(false); AlertUtil.error("Ошибка", e.getMessage()); });
            }
        }).start();
    }

    @FXML private void handleCancel() {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
        selectedOrder = null;
    }
    @FXML private void refresh() { loadOrders(null); filterCombo.setValue("Все"); }
    @FXML private void goToDashboard()  { SceneManager.navigate("manager-dashboard"); }
    @FXML private void goToProfile()    { SceneManager.navigate("manager-profile"); }
    @FXML private void goToCatalog()     { SceneManager.navigate("manager-catalog"); }
}
