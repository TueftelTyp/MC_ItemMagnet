package de.tuefteltyp.itemmagnet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MagnetConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("itemmagnet.json");
    public static MagnetConfig INSTANCE = new MagnetConfig();

    public boolean enabled = true;
    // When false, a magnet resets to OFF after it is unequipped or the player rejoins.
    public boolean rememberSwitchState = false;
    public boolean attractExperience = true;
    public boolean showParticles = true;
    public int particleIntervalTicks = 8;
    public boolean pullSounds = false;
    public int soundIntervalTicks = 40;
    public double soundVolume = 0.12;
    public double weakRadius = 6.0;
    public double midRadius = 12.0;
    public double strongRadius = 20.0;
    public double pullStrength = 0.23;
    public double maxSpeed = 0.9;
    public int updateIntervalTicks = 2;

    // Each row contains three Minecraft item IDs; empty strings represent empty slots.
    // The center ingredient of Mid/Strong defaults to the preceding magnet tier.
    public String[][] weakRecipe = {
        {"minecraft:iron_ingot", "minecraft:copper_block", "minecraft:iron_ingot"},
        {"minecraft:redstone", "minecraft:redstone_block", "minecraft:redstone"},
        {"minecraft:iron_ingot", "minecraft:copper_block", "minecraft:iron_ingot"}
    };
    public String[][] midRecipe = {
        {"minecraft:gold_block", "minecraft:lapis_block", "minecraft:gold_block"},
        {"minecraft:amethyst_block", "itemmagnet:weak_magnet", "minecraft:amethyst_block"},
        {"minecraft:gold_block", "minecraft:lapis_block", "minecraft:gold_block"}
    };
    public String[][] strongRecipe = {
        {"minecraft:diamond_block", "minecraft:emerald_block", "minecraft:diamond_block"},
        {"minecraft:ancient_debris", "itemmagnet:mid_magnet", "minecraft:ancient_debris"},
        {"minecraft:diamond_block", "minecraft:emerald_block", "minecraft:diamond_block"}
    };

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                MagnetConfig cfg = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), MagnetConfig.class);
                if (cfg != null) INSTANCE = cfg;
            }
            save();
        } catch (Exception ex) {
            ItemMagnet.LOGGER.error("Could not load itemmagnet.json; using defaults", ex);
            INSTANCE = new MagnetConfig();
        }
    }
    public static void save() {
        try {
            INSTANCE.validate();
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
            RecipePackExporter.export(INSTANCE, FILE.getParent().resolve("itemmagnet-recipes.zip"));
        } catch (Exception ex) {
            ItemMagnet.LOGGER.error("Could not save itemmagnet.json", ex);
        }
    }
    private void validate() {
        weakRadius = clamp(weakRadius, 0, 64);
        midRadius = clamp(midRadius, 0, 64);
        strongRadius = clamp(strongRadius, 0, 64);
        pullStrength = clamp(pullStrength, .01, 2);
        maxSpeed = clamp(maxSpeed, .05, 4);
        updateIntervalTicks = Math.max(1, Math.min(20, updateIntervalTicks));
        particleIntervalTicks = Math.max(1, Math.min(100, particleIntervalTicks));
        soundIntervalTicks = Math.max(5, Math.min(200, soundIntervalTicks));
        soundVolume = clamp(soundVolume, 0, 1);
        weakRecipe = RecipePackExporter.sanitize(weakRecipe, new MagnetConfig().weakRecipe);
        midRecipe = RecipePackExporter.sanitize(midRecipe, new MagnetConfig().midRecipe);
        strongRecipe = RecipePackExporter.sanitize(strongRecipe, new MagnetConfig().strongRecipe);
    }
    private static double clamp(double x, double min, double max) {
        return Double.isFinite(x) ? Math.max(min, Math.min(max, x)) : min;
    }
}
