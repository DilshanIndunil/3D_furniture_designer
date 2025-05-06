package com.furnituredesign.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import com.furnituredesign.models.*;
import com.furnituredesign.services.DesignService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML
    private TextField roomWidthField;
    @FXML
    private TextField roomLengthField;
    @FXML
    private TextField roomHeightField;
    @FXML
    private ColorPicker wallColorPicker;
    @FXML
    private ColorPicker floorColorPicker;
    @FXML
    private ComboBox<String> furnitureTypeCombo;
    @FXML
    private ListView<Furniture> furnitureListView;
    @FXML
    private Canvas designCanvas;
    @FXML
    private ColorPicker furnitureColorPicker;

    private final DesignService designService = new DesignService();
    private Room currentRoom;
    private List<Furniture> furnitureList = new ArrayList<>();
    private boolean is3DView = false;
    private Furniture selectedFurniture = null;
    private double dragOffsetX, dragOffsetY;

    @FXML
    public void initialize() {
        // Initialize furniture types
        furnitureTypeCombo.getItems().addAll(
                "Chair", "Table", "Sofa", "Bed", "Cabinet", "Bookshelf");

        // Set default colors
        wallColorPicker.setValue(Color.WHITE);
        floorColorPicker.setValue(Color.LIGHTGRAY);

        // Set up ListView
        furnitureListView.setMaxHeight(Double.MAX_VALUE);
        furnitureListView.setMaxWidth(Double.MAX_VALUE);

        // Initialize canvas
        designCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        designCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redraw());

        // Mouse events for dragging furniture
        designCanvas.setOnMousePressed(e -> {
            double mouseX = e.getX();
            double mouseY = e.getY();
            selectedFurniture = null;
            for (Furniture furniture : furnitureList) {
                double fx = furniture.getX();
                double fy = furniture.getY();
                double fw = 50, fh = 50; // pixel size for drawing
                if (mouseX >= fx && mouseX <= fx + fw && mouseY >= fy && mouseY <= fy + fh) {
                    selectedFurniture = furniture;
                    dragOffsetX = mouseX - fx;
                    dragOffsetY = mouseY - fy;
                    break;
                }
            }
        });
        designCanvas.setOnMouseDragged(e -> {
            if (selectedFurniture != null) {
                double mouseX = e.getX();
                double mouseY = e.getY();
                // Room bounds in pixels
                double minX = 50, minY = 50;
                double maxX = designCanvas.getWidth() - 100;
                double maxY = designCanvas.getHeight() - 100;
                double newX = mouseX - dragOffsetX;
                double newY = mouseY - dragOffsetY;
                // Clamp to room
                newX = Math.max(minX, Math.min(newX, minX + maxX - 50));
                newY = Math.max(minY, Math.min(newY, minY + maxY - 50));
                selectedFurniture.setX(newX);
                selectedFurniture.setY(newY);
                redraw();
            }
        });
        designCanvas.setOnMouseReleased(e -> selectedFurniture = null);
    }

    @FXML
    private void handleCreateRoom() {
        try {
            // Validate input fields
            if (roomWidthField.getText().isEmpty() ||
                    roomLengthField.getText().isEmpty() ||
                    roomHeightField.getText().isEmpty()) {
                showError("Please enter all room dimensions");
                return;
            }

            double width = Double.parseDouble(roomWidthField.getText());
            double length = Double.parseDouble(roomLengthField.getText());
            double height = Double.parseDouble(roomHeightField.getText());

            // Validate dimensions
            if (width <= 0 || length <= 0 || height <= 0) {
                showError("Room dimensions must be greater than 0");
                return;
            }

            // Create new room
            currentRoom = new Room(width, length, height);
            furnitureList.clear();
            furnitureListView.getItems().clear();

            // Apply colors
            currentRoom.setWallColor(wallColorPicker.getValue().toString());
            currentRoom.setFloorColor(floorColorPicker.getValue().toString());

            // Redraw the canvas
            redraw();

            // Show success message
            showSuccess("Room created successfully!");
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for room dimensions");
        }
    }

    @FXML
    private void handleAddFurniture() {
        if (currentRoom == null) {
            showError("Please create a room first");
            return;
        }
        String type = furnitureTypeCombo.getValue();
        if (type == null) {
            showError("Please select a furniture type");
            return;
        }
        Furniture furniture = new Furniture(type);
        // Place in center of room area (in pixels)
        furniture.setX(50 + (designCanvas.getWidth() - 100) / 2 - 25);
        furniture.setY(50 + (designCanvas.getHeight() - 100) / 2 - 25);
        // Set color from color picker
        if (furnitureColorPicker != null && furnitureColorPicker.getValue() != null) {
            furniture.setColor(furnitureColorPicker.getValue().toString());
        }
        furnitureList.add(furniture);
        furnitureListView.getItems().add(furniture);
        redraw();
    }

    @FXML
    private void handleRemoveFurniture() {
        Furniture selected = furnitureListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            furnitureList.remove(selected);
            furnitureListView.getItems().remove(selected);
            redraw();
        }
    }

    @FXML
    private void handleSaveDesign() {
        if (currentRoom == null) {
            showError("No design to save");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Design");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Design Files", "*.json"));

        File file = fileChooser.showSaveDialog(designCanvas.getScene().getWindow());
        if (file != null) {
            designService.saveDesign(currentRoom, furnitureList, file);
        }
    }

    @FXML
    private void handleLoadDesign() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Design");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Design Files", "*.json"));

        File file = fileChooser.showOpenDialog(designCanvas.getScene().getWindow());
        if (file != null) {
            Design design = designService.loadDesign(file);
            if (design != null) {
                currentRoom = design.getRoom();
                furnitureList = design.getFurniture();
                furnitureListView.getItems().setAll(furnitureList);
                redraw();
            }
        }
    }

    @FXML
    private void handle2DView() {
        is3DView = false;
        redraw();
    }

    @FXML
    private void handle3DView() {
        is3DView = true;
        redraw();
    }

    @FXML
    private void handleApplyShading() {
        redraw();
    }

    @FXML
    private void handleResetView() {
        redraw();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleNewDesign() {
        // Clear the current room and furniture
        currentRoom = null;
        furnitureList.clear();
        furnitureListView.getItems().clear();
        roomWidthField.clear();
        roomLengthField.clear();
        roomHeightField.clear();
        wallColorPicker.setValue(Color.WHITE);
        floorColorPicker.setValue(Color.LIGHTGRAY);
        redraw();
    }

    private void redraw() {
        if (currentRoom == null)
            return;

        GraphicsContext gc = designCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, designCanvas.getWidth(), designCanvas.getHeight());

        if (is3DView) {
            draw3DView(gc);
        } else {
            draw2DView(gc);
        }
    }

    private void draw2DView(GraphicsContext gc) {
        // Fill background with white
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, designCanvas.getWidth(), designCanvas.getHeight());

        // Fill room with wall color if a room exists
        if (currentRoom != null) {
            Color wallColor = Color.WHITE;
            try {
                wallColor = Color.web(currentRoom.getWallColor());
            } catch (Exception ignored) {
            }
            gc.setFill(wallColor);
            gc.fillRect(50, 50, designCanvas.getWidth() - 100, designCanvas.getHeight() - 100);
        }

        // Draw room outline
        gc.setStroke(Color.BLACK);
        gc.strokeRect(50, 50, designCanvas.getWidth() - 100, designCanvas.getHeight() - 100);

        // Draw furniture at their (x, y)
        for (Furniture furniture : furnitureList) {
            Color color = Color.GRAY;
            try {
                color = Color.web(furniture.getColor());
            } catch (Exception ignored) {
            }
            gc.setFill(color);
            gc.fillRect(furniture.getX(), furniture.getY(), 50, 50);
        }
    }

    private void draw3DView(GraphicsContext gc) {
        // Basic 3D representation using perspective projection
        // This is a simplified version - in a real application, you'd use JavaFX 3D or
        // JMonkeyEngine
        gc.setFill(wallColorPicker.getValue());
        gc.fillRect(50, 50, designCanvas.getWidth() - 100, designCanvas.getHeight() - 100);

        // Draw furniture in 3D
        for (Furniture furniture : furnitureList) {
            // Simplified 3D furniture representation
            gc.setFill(Color.GRAY);
            gc.fillRect(100, 100, 50, 50);
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}