package com.autoatelier.controller.admin;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.service.StorageService;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminStatisticsController extends BaseController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label avgOrderLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalClientsLabel;

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> revenueBarChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis barYAxis;

    @Override
    protected void onInit() {
        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                List<Order> orders = OrderService.getInstance().getAllOrders();
                Map<String, Long> statusStats = OrderService.getInstance().getStats();
                List<com.autoatelier.model.User> users = StorageService.getInstance().getAllUsers();

                long clients = users.stream().filter(u -> "client".equals(u.getRole())).count();

                double totalRevenue = orders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus()))
                        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                        .sum();

                long completedCount = statusStats.getOrDefault(Order.Status.COMPLETED.key, 0L);
                double avgOrder = completedCount > 0 ? totalRevenue / completedCount : 0;

                Map<String, Double> revenueByService = orders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus())
                                && o.getService() != null && o.getTotalPrice() != null)
                        .collect(Collectors.groupingBy(
                                o -> o.getService().getName(),
                                Collectors.summingDouble(Order::getTotalPrice)
                        ));

                Platform.runLater(() -> {
                    totalRevenueLabel.setText(String.format("%.0f ₽", totalRevenue));
                    avgOrderLabel.setText(String.format("%.0f ₽", avgOrder));
                    totalOrdersLabel.setText(String.valueOf(orders.size()));
                    totalClientsLabel.setText(String.valueOf(clients));

                    buildPieChart(statusStats);
                    buildBarChart(revenueByService);
                });
            } catch (Exception e) {
            }
        }).start();
    }

    private void buildPieChart(Map<String, Long> stats) {
        statusPieChart.getData().clear();
        for (Order.Status status : Order.Status.values()) {
            long count = stats.getOrDefault(status.key, 0L);
            if (count > 0) {
                PieChart.Data slice = new PieChart.Data(status.display + " (" + count + ")", count);
                statusPieChart.getData().add(slice);
            }
        }
        statusPieChart.setTitle("Статусы заказов");
        statusPieChart.setLegendVisible(true);
        statusPieChart.setLabelsVisible(true);
    }

    private void buildBarChart(Map<String, Double> revenueByService) {
        revenueBarChart.getData().clear();
        barXAxis.setLabel("Услуга");
        barYAxis.setLabel("Выручка (₽)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Выручка по услугам");

        revenueByService.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(8)
                .forEach(e -> {
                    String svcName = e.getKey().length() > 20
                            ? e.getKey().substring(0, 20) + "…"
                            : e.getKey();
                    series.getData().add(new XYChart.Data<>(svcName, e.getValue()));
                });

        revenueBarChart.getData().add(series);
    }

    @FXML private void goToDashboard() { SceneManager.navigate("admin-dashboard"); }
    @FXML private void goToProfile()   { SceneManager.navigate("admin-profile"); }
    @FXML private void goToUsers() { SceneManager.navigate("admin-users"); }
    @FXML private void goToServices() { SceneManager.navigate("admin-services"); }
}
