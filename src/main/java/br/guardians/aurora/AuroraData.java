package br.guardians.aurora;

import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuroraData {
    public static class Info {
        public String faction = "", team = "", god = "";
        public int level = 1;
    }

    static class Save {
        Map<String, Info> players = new HashMap<>();
        Map<String, Integer> scores = new HashMap<>();
    }

    static final Gson GSON = new Gson();
    static Save s = new Save();
    static Path file;

    public static Info get(UUID id) {
        return s.players.computeIfAbsent(id.toString(), k -> new Info());
    }

    public static void addScore(String key, int n) { s.scores.merge(key, n, Integer::sum); }
    public static Map<String, Integer> scores() { return s.scores; }

    public static void load(MinecraftServer srv) {
        file = srv.getSavePath(WorldSavePath.ROOT).resolve("aurora.json");
        s = new Save();
        try {
            if (Files.exists(file)) {
                Save r = GSON.fromJson(Files.readString(file), Save.class);
                if (r != null && r.players != null && r.scores != null) s = r;
            }
        } catch (Exception ignored) {}
    }

    public static void save(MinecraftServer srv) {
        try {
            if (file == null) file = srv.getSavePath(WorldSavePath.ROOT).resolve("aurora.json");
            Files.writeString(file, GSON.toJson(s));
        } catch (Exception ignored) {}
    }
}
