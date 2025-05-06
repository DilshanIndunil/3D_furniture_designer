package com.furnituredesign.services;

import com.furnituredesign.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.List;

public class DesignService {
    private final Gson gson;

    public DesignService() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public void saveDesign(Room room, List<Furniture> furniture, File file) {
        try {
            Design design = new Design(room, furniture);
            String json = gson.toJson(design);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Design loadDesign(File file) {
        try {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, Design.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}