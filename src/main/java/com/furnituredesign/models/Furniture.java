package com.furnituredesign.models;

public class Furniture {
    private String type;
    private double x;
    private double y;
    private double z;
    private double width;
    private double length;
    private double height;
    private String color;

    public Furniture(String type) {
        this.type = type;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.color = "#808080";

        // Set default dimensions based on furniture type
        switch (type.toLowerCase()) {
            case "chair":
                this.width = 0.5;
                this.length = 0.5;
                this.height = 1.0;
                break;
            case "table":
                this.width = 1.2;
                this.length = 0.8;
                this.height = 0.75;
                break;
            case "sofa":
                this.width = 2.0;
                this.length = 0.8;
                this.height = 0.9;
                break;
            case "bed":
                this.width = 2.0;
                this.length = 1.6;
                this.height = 0.5;
                break;
            case "cabinet":
                this.width = 1.0;
                this.length = 0.6;
                this.height = 1.8;
                break;
            case "bookshelf":
                this.width = 1.0;
                this.length = 0.4;
                this.height = 2.0;
                break;
            default:
                this.width = 1.0;
                this.length = 1.0;
                this.height = 1.0;
        }
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return String.format("%s (%.2fm x %.2fm x %.2fm)", type, width, length, height);
    }
}