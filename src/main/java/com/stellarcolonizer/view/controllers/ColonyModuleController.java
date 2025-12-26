package com.stellarcolonizer.view.controllers;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.colony.enums.PopType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public class ColonyModuleController {
    // FXML绑定的列表
    @FXML
    private ListView<Colony> colonyListView;

    // 详情面板的标签（原显示N/A的位置）
    @FXML
    private Label colonyNameLabel;
    @FXML
    private Label planetNameLabel;
    @FXML
    private Label populationLabel;
    @FXML
    private Label happinessLabel;
    @FXML
    private Label stabilityLabel;
    @FXML
    private Label developmentLabel;

    // 初始化方法（FXML加载后执行）
    @FXML
    public void initialize() {
        colonyListView.setCellFactory(param -> new ColonyListCell());

        colonyListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldColony, newColony) -> updateColonyDetails(newColony)
        );

        clearColonyDetails();
    }

    // ========== 核心：更新详情面板（替换N/A为具体值） ==========
    private void updateColonyDetails(Colony colony) {
        if (colony == null) {
            // 未选中任何殖民地：清空（替代N/A）
            clearColonyDetails();
            return;
        }

        // 1. 殖民地名称+行星
        colonyNameLabel.setText(colony.getName());
        planetNameLabel.setText(colony.getPlanet().getName());

        // 2. 人口（格式化显示）
        populationLabel.setText(formatPopulation(colony.getPopulationByType()));

        // 3. 幸福度（保留1位小数+百分比）
        happinessLabel.setText(String.format("%.1f%%", colony.getHappiness()));

        // 4. 稳定度（保留1位小数+百分比）
        stabilityLabel.setText(String.format("%.1f%%", colony.getStability()));

        // 5. 发展度（保留1位小数+百分比）
        developmentLabel.setText(String.format("%.1f%%", colony.getDevelopment()));
    }

    // ========== 辅助方法：格式化人口显示 ==========
    private String formatPopulation(Map<PopType, Integer> populationMap) {
        if (populationMap == null || populationMap.isEmpty()) {
            return "0"; // 替代N/A
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<PopType, Integer> entry : populationMap.entrySet()) {
            sb.append(entry.getKey().getDisplayName())
                    .append(":")
                    .append(entry.getValue())
                    .append(" ");
        }
        // 移除最后一个空格
        return sb.toString().trim();
    }

    // ========== 辅助方法：清空详情面板（初始状态） ==========
    private void clearColonyDetails() {
        colonyNameLabel.setText("");
        planetNameLabel.setText("");
        populationLabel.setText("");
        happinessLabel.setText("");
        stabilityLabel.setText("");
        developmentLabel.setText("");
    }

    // ========== 对外提供：设置殖民地数据（主控制器调用） ==========
    public void setColonies(ObservableList<Colony> colonies) {
        colonyListView.setItems(colonies);
        // 如果有数据，默认选中第一个
        if (!colonies.isEmpty()) {
            colonyListView.getSelectionModel().selectFirst();
        }
    }

    // ========== 自定义列表单元格（列表项显示关键信息） ==========
    private class ColonyListCell extends ListCell<Colony> {
        @Override
        protected void updateItem(Colony colony, boolean empty) {
            super.updateItem(colony, empty);
            if (empty || colony == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            // 列表项显示：殖民地名称 + 行星 + 核心状态（简化版）
            VBox cellBox = new VBox(2);
            cellBox.setPadding(new javafx.geometry.Insets(5));

            // 名称+行星
            Label nameLabel = new Label(colony.getName() + "（" + colony.getPlanet().getName() + "）");
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

            // 简要状态（幸福度+稳定度）
            HBox statusBox = new HBox(10);
            Label happyLabel = new Label("幸福度：" + String.format("%.1f%%", colony.getHappiness()));
            Label stableLabel = new Label("稳定度：" + String.format("%.1f%%", colony.getStability()));
            happyLabel.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 12;");
            stableLabel.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 12;");
            statusBox.getChildren().addAll(happyLabel, stableLabel);

            cellBox.getChildren().addAll(nameLabel, statusBox);
            cellBox.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555; -fx-border-width: 0 0 1 0;");

            setGraphic(cellBox);
        }
    }
}