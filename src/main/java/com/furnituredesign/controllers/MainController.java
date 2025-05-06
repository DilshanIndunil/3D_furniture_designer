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
import javafx.scene.SubScene;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;

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
    @FXML
    private SubScene room3DSubScene;

    private final DesignService designService = new DesignService();
    private Room currentRoom;
    private List<Furniture> furnitureList = new ArrayList<>();
    private boolean is3DView = false;
    private Furniture selectedFurniture = null;
    private double dragOffsetX, dragOffsetY;
    private PerspectiveCamera camera3D;
    private double anchorX, anchorY;
    private double anchorAngleX = -20, anchorAngleY = -20;
    private double cameraAngleX = -20, cameraAngleY = -20;
    private double cameraPanX = 0, cameraPanY = 0;
    private boolean panning = false;
    private Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);

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

        // Zoom for 3D view
        room3DSubScene.setOnScroll(e -> {
            if (is3DView && camera3D != null) {
                double delta = e.getDeltaY();
                double newZ = camera3D.getTranslateZ() + (delta > 0 ? 50 : -50);
                newZ = Math.max(-10000, Math.min(-200, newZ));
                camera3D.setTranslateZ(newZ);
            }
        });
        // Mouse drag for rotation and panning
        room3DSubScene.setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
            anchorAngleX = cameraAngleX;
            anchorAngleY = cameraAngleY;
            panning = e.isSecondaryButtonDown() || e.isShiftDown();
        });
        room3DSubScene.setOnMouseDragged(e -> {
            if (is3DView && camera3D != null) {
                double dx = e.getSceneX() - anchorX;
                double dy = e.getSceneY() - anchorY;
                if (panning) {
                    cameraPanX += dx * 0.5;
                    cameraPanY += dy * 0.5;
                    camera3D.setTranslateX(cameraPanX);
                    camera3D.setTranslateY(
                            -cameraPanY - (currentRoom != null ? (currentRoom.getHeight() * 100) / 4 : 0));
                    anchorX = e.getSceneX();
                    anchorY = e.getSceneY();
                } else {
                    cameraAngleY = anchorAngleY + dx * 0.3;
                    cameraAngleX = anchorAngleX - dy * 0.3;
                    rotateY.setAngle(cameraAngleY);
                    rotateX.setAngle(cameraAngleX);
                }
            }
        });

        // Sync color changes to 3D
        wallColorPicker.setOnAction(e -> {
            if (is3DView)
                build3DRoomScene();
        });
        floorColorPicker.setOnAction(e -> {
            if (is3DView)
                build3DRoomScene();
        });
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
        designCanvas.setVisible(true);
        room3DSubScene.setVisible(false);
        redraw();
    }

    @FXML
    private void handle3DView() {
        is3DView = true;
        designCanvas.setVisible(false);
        room3DSubScene.setVisible(true);
        build3DRoomScene();
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

        // Draw furniture at their (x, y) with specific shapes
        for (Furniture furniture : furnitureList) {
            Color color = Color.GRAY;
            try {
                color = Color.web(furniture.getColor());
            } catch (Exception ignored) {
            }
            gc.setFill(color);
            String type = furniture.getType().toLowerCase();
            double x = furniture.getX();
            double y = furniture.getY();
            switch (type) {
                case "chair":
                    // Small square 30x30
                    gc.fillRect(x, y, 30, 30);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x, y, 30, 30);
                    break;
                case "table":
                    // Standard rectangle 50x30
                    gc.fillRect(x, y, 50, 30);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x, y, 50, 30);
                    break;
                case "sofa":
                    // Rounded rectangle 60x30, arc 15
                    gc.fillRoundRect(x, y, 60, 30, 15, 15);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRoundRect(x, y, 60, 30, 15, 15);
                    break;
                case "bed":
                    // Large rectangle 70x40
                    gc.fillRect(x, y, 70, 40);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x, y, 70, 40);
                    break;
                case "cabinet":
                    // Tall rectangle 30x50
                    gc.fillRect(x, y, 30, 50);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x, y, 30, 50);
                    break;
                case "bookshelf":
                    // Thin tall rectangle 15x60
                    gc.fillRect(x, y, 15, 60);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x, y, 15, 60);
                    break;
                default:
                    // Default square 40x40
                    gc.fillRect(x, y, 40, 40);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x, y, 40, 40);
            }
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

    private void build3DRoomScene() {
        if (currentRoom == null)
            return;
        double width = currentRoom.getWidth();
        double length = currentRoom.getLength();
        double height = currentRoom.getHeight();
        double scale = 100;
        double roomW = width * scale;
        double roomL = length * scale;
        double roomH = height * scale;

        Group root3D = new Group();
        // Floor
        Box floor = new Box(roomW, 5, roomL);
        PhongMaterial floorMat = new PhongMaterial();
        try {
            floorMat.setDiffuseColor(Color.web(currentRoom.getFloorColor()));
        } catch (Exception e) {
            floorMat.setDiffuseColor(Color.LIGHTGRAY);
        }
        floor.setMaterial(floorMat);
        floor.setTranslateY(roomH / 2);
        root3D.getChildren().add(floor);
        // Walls (all four)
        PhongMaterial wallMat = new PhongMaterial();
        try {
            wallMat.setDiffuseColor(Color.web(currentRoom.getWallColor()));
        } catch (Exception e) {
            wallMat.setDiffuseColor(Color.WHITE);
        }
        // Back wall
        Box backWall = new Box(roomW, roomH, 5);
        backWall.setMaterial(wallMat);
        backWall.setTranslateZ(-roomL / 2);
        root3D.getChildren().add(backWall);
        // Front wall
        Box frontWall = new Box(roomW, roomH, 5);
        frontWall.setMaterial(wallMat);
        frontWall.setTranslateZ(roomL / 2);
        root3D.getChildren().add(frontWall);
        // Left wall
        Box leftWall = new Box(5, roomH, roomL);
        leftWall.setMaterial(wallMat);
        leftWall.setTranslateX(-roomW / 2);
        root3D.getChildren().add(leftWall);
        // Right wall
        Box rightWall = new Box(5, roomH, roomL);
        rightWall.setMaterial(wallMat);
        rightWall.setTranslateX(roomW / 2);
        root3D.getChildren().add(rightWall);
        // Optionally, add ceiling (commented out for dollhouse effect)
        // Box ceiling = new Box(roomW, 5, roomL);
        // ceiling.setMaterial(floorMat);
        // ceiling.setTranslateY(-roomH / 2);
        // root3D.getChildren().add(ceiling);
        // Furniture
        for (Furniture furniture : furnitureList) {
            Box fBox;
            String type = furniture.getType().toLowerCase();
            double fw = 40, fl = 40, fh = 40;
            switch (type) {
                case "chair":
                    fw = fl = 30;
                    fh = 30;
                    break;
                case "table":
                    fw = 50;
                    fl = 30;
                    fh = 25;
                    break;
                case "sofa":
                    fw = 60;
                    fl = 30;
                    fh = 25;
                    break;
                case "bed":
                    fw = 70;
                    fl = 40;
                    fh = 20;
                    break;
                case "cabinet":
                    fw = 30;
                    fl = 20;
                    fh = 50;
                    break;
                case "bookshelf":
                    fw = 15;
                    fl = 20;
                    fh = 60;
                    break;
            }
            fBox = new Box(fw, fh, fl);
            PhongMaterial mat = new PhongMaterial();
            try {
                mat.setDiffuseColor(Color.web(furniture.getColor()));
            } catch (Exception e) {
                mat.setDiffuseColor(Color.GRAY);
            }
            fBox.setMaterial(mat);
            // Place furniture in the room (map 2D x/y to 3D x/z, y is floor)
            double px = furniture.getX() - 50 - (roomW / 2) + fw / 2;
            double pz = furniture.getY() - 50 - (roomL / 2) + fl / 2;
            fBox.setTranslateX(px);
            fBox.setTranslateY(roomH / 2 - fh / 2);
            fBox.setTranslateZ(pz);
            root3D.getChildren().add(fBox);
        }
        // Camera
        camera3D = new PerspectiveCamera(true);
        camera3D.setTranslateZ(-roomL);
        camera3D.setTranslateY(-roomH / 4);
        camera3D.setNearClip(0.1);
        camera3D.setFarClip(10000.0);
        camera3D.setFieldOfView(35);
        // Reset rotation and pan
        cameraAngleX = -20;
        cameraAngleY = -20;
        cameraPanX = 0;
        cameraPanY = 0;
        rotateX = new Rotate(cameraAngleX, Rotate.X_AXIS);
        rotateY = new Rotate(cameraAngleY, Rotate.Y_AXIS);
        camera3D.getTransforms().setAll(rotateY, rotateX);
        // Light
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(-roomH / 2);
        light.setTranslateZ(-roomL / 2);
        root3D.getChildren().add(light);
        // Set up SubScene
        room3DSubScene.setRoot(root3D);
        room3DSubScene.setCamera(camera3D);
        room3DSubScene.setFill(Color.LIGHTGRAY);
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