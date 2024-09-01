package com.mosadie.effectmc.core.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class EffectExporter {
    public static void main(String[] args) {
        System.out.println("Exporting effects...");
        EffectMCCore core = new EffectMCCore(null, null, null);

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Effect.class, new EffectSerializer());
        Gson gson = builder.create();

        String file = "effects.json";

        if (args.length > 0) {
            file = args[0];
        }

        File outputFile = new File(file);

        if (outputFile.exists()) {
            System.out.println("Output file already exists, overwriting...");
        }


        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write(gson.toJson(core.getEffects().toArray(new Effect[0])));
            writer.close();
            System.out.println("Effects exported to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to export effects: " + e.getMessage());
        }
    }
}
