package com.furnituredesign.models;

import java.util.List;
import java.util.ArrayList;

public class Design {
    private Room room;
    private List<Furniture> furniture;

    public Design(Room room) {
        this.room = room;
        this.furniture = new ArrayList<>();
    }

    public Design(Room room, List<Furniture> furniture) {
        this.room = room;
        this.furniture = new ArrayList<>(furniture);
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public List<Furniture> getFurniture() {
        return furniture;
    }

    public void setFurniture(List<Furniture> furniture) {
        this.furniture = furniture;
    }

    public void addFurniture(Furniture item) {
        furniture.add(item);
    }

    public void removeFurniture(Furniture item) {
        furniture.remove(item);
    }

    @Override
    public String toString() {
        return String.format("Design: %s with %d furniture items", room, furniture.size());
    }
}