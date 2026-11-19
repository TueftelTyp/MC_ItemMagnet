package de.tuefteltyp.itemmagnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Writes vanilla-compatible 26.3 crafting recipes as a datapack.
 * Editing the config alone does not reload the running world's recipe registry.
 */
public final class RecipePackExporter {
    private RecipePackExporter() {}

    public static String[][] sanitize(String[][] values, String[][] defaults) {
        if (values == null || values.length != 3) return copy(defaults);
        String[][] result = new String[3][3];
        for (int r = 0; r < 3; r++) {
            if (values[r] == null || values[r].length != 3) return copy(defaults);
            for (int c = 0; c < 3; c++) {
                String value = values[r][c] == null ? "" : values[r][c].trim().toLowerCase(java.util.Locale.ROOT);
                if (!value.isEmpty() && !value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
                    value = defaults[r][c];
                }
                result[r][c] = value;
            }
        }
        boolean any = false;
        for (String[] row : result) for (String cell : row) any |= !cell.isEmpty();
        return any ? result : copy(defaults);
    }

    private static String[][] copy(String[][] source) {
        String[][] result = new String[3][3];
        for (int r = 0; r < 3; r++) System.arraycopy(source[r], 0, result[r], 0, 3);
        return result;
    }

    public static String[] parseRow(String text, String[] previous) {
        String[] pieces = text.split(",", -1);
        if (pieces.length != 3) return previous.clone();
        String[] result = new String[3];
        for (int i = 0; i < 3; i++) result[i] = pieces[i].trim();
        return result;
    }

    private static JsonObject recipe(String[][] input, String output) {
        JsonObject result = new JsonObject();
        result.addProperty("type", "minecraft:crafting_shaped");
        result.addProperty("category", "equipment");
        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        Map<String, Character> mapping = new HashMap<>();
        char next = 'A';
        for (String[] row : input) {
            StringBuilder line = new StringBuilder();
            for (String ingredient : row) {
                if (ingredient.isEmpty()) { line.append(' '); continue; }
                Character symbol = mapping.get(ingredient);
                if (symbol == null) {
                    symbol = next++;
                    mapping.put(ingredient, symbol);
                    key.addProperty(symbol.toString(), ingredient);
                }
                line.append(symbol);
            }
            pattern.add(line.toString());
        }
        result.add("pattern", pattern);
        result.add("key", key);
        JsonObject item = new JsonObject();
        item.addProperty("id", "itemmagnet:" + output);
        item.addProperty("count", 1);
        result.add("result", item);
        return result;
    }

    public static void export(MagnetConfig config, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        // Write to a temporary ZIP first to avoid corrupting a prior export on error.
        Path temp = destination.resolveSibling(destination.getFileName() + ".tmp");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(temp))) {
            add(out, "pack.mcmeta", "{\"pack\":{\"description\":\"ItemMagnet custom recipes\",\"min_format\":[121,0],\"max_format\":[121,0]}}");
            add(out, "data/itemmagnet/recipe/weak_magnet.json", recipe(config.weakRecipe, "weak_magnet").toString());
            add(out, "data/itemmagnet/recipe/mid_magnet.json", recipe(config.midRecipe, "mid_magnet").toString());
            add(out, "data/itemmagnet/recipe/strong_magnet.json", recipe(config.strongRecipe, "strong_magnet").toString());
        }
        Files.move(temp, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static void add(ZipOutputStream out, String path, String content) throws IOException {
        out.putNextEntry(new ZipEntry(path));
        out.write(content.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }
}
