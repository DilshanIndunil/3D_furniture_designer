package com.furnituredesign.models;

public class Room {
    private double width;
    private double length;
    private double height;
    private String wallColor;
    private String floorColor;

    public Room(double width, double length, double height) {
        this.width = width;
        this.length = length;
        this.height = height;
        this.wallColor = "#FFFFFF";
        this.floorColor = "#CCCCCC";
    }

    // Getters and setters
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

    public String getWallColor() {
        return wallColor;
    }

    public void setWallColor(String wallColor) {
        this.wallColor = wallColor;
    }

    public String getFloorColor() {
        return floorColor;
    }

    public void setFloorColor(String floorColor) {
        this.floorColor = floorColor;
    }

    @Override
    public String toString() {
        return String.format("Room (%.2fm x %.2fm x %.2fm)", width, length, height);
    }
}