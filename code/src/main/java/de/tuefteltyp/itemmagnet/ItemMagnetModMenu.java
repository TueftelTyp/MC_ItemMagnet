package de.tuefteltyp.itemmagnet;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public final class ItemMagnetModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MagnetConfig c = MagnetConfig.INSTANCE;
            ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.translatable("config.itemmagnet.title"));
            builder.setSavingRunnable(MagnetConfig::save);
            ConfigCategory cat = builder.getOrCreateCategory(Component.translatable("config.itemmagnet.general"));
            var e = builder.entryBuilder();
            cat.addEntry(e.startBooleanToggle(Component.translatable("config.itemmagnet.enabled"), c.enabled).setDefaultValue(true).setSaveConsumer(v -> c.enabled = v).build());
            cat.addEntry(e.startBooleanToggle(Component.translatable("config.itemmagnet.remember_switch_state"), c.rememberSwitchState).setDefaultValue(false).setSaveConsumer(v -> c.rememberSwitchState = v).build());
            cat.addEntry(e.startBooleanToggle(Component.translatable("config.itemmagnet.xp"), c.attractExperience).setDefaultValue(true).setSaveConsumer(v -> c.attractExperience = v).build());
            cat.addEntry(e.startDoubleField(Component.translatable("config.itemmagnet.weak"), c.weakRadius).setDefaultValue(6.0).setMin(0).setMax(64).setSaveConsumer(v -> c.weakRadius = v).build());
            cat.addEntry(e.startDoubleField(Component.translatable("config.itemmagnet.mid"), c.midRadius).setDefaultValue(12.0).setMin(0).setMax(64).setSaveConsumer(v -> c.midRadius = v).build());
            cat.addEntry(e.startDoubleField(Component.translatable("config.itemmagnet.strong"), c.strongRadius).setDefaultValue(20.0).setMin(0).setMax(64).setSaveConsumer(v -> c.strongRadius = v).build());
            cat.addEntry(e.startDoubleField(Component.translatable("config.itemmagnet.strength"), c.pullStrength).setDefaultValue(.23).setMin(.01).setMax(2).setSaveConsumer(v -> c.pullStrength = v).build());
            cat.addEntry(e.startDoubleField(Component.translatable("config.itemmagnet.speed"), c.maxSpeed).setDefaultValue(.9).setMin(.05).setMax(4).setSaveConsumer(v -> c.maxSpeed = v).build());
            cat.addEntry(e.startIntField(Component.translatable("config.itemmagnet.interval"), c.updateIntervalTicks).setDefaultValue(2).setMin(1).setMax(20).setSaveConsumer(v -> c.updateIntervalTicks = v).build());
            ConfigCategory effects = builder.getOrCreateCategory(Component.translatable("config.itemmagnet.effects"));
            effects.addEntry(e.startBooleanToggle(Component.translatable("config.itemmagnet.particles"), c.showParticles).setDefaultValue(true).setSaveConsumer(v -> c.showParticles = v).build());
            effects.addEntry(e.startIntField(Component.translatable("config.itemmagnet.particle_interval"), c.particleIntervalTicks).setDefaultValue(8).setMin(1).setMax(100).setSaveConsumer(v -> c.particleIntervalTicks = v).build());
            effects.addEntry(e.startBooleanToggle(Component.translatable("config.itemmagnet.sounds"), c.pullSounds).setDefaultValue(false).setSaveConsumer(v -> c.pullSounds = v).build());
            effects.addEntry(e.startIntField(Component.translatable("config.itemmagnet.sound_interval"), c.soundIntervalTicks).setDefaultValue(40).setMin(5).setMax(200).setSaveConsumer(v -> c.soundIntervalTicks = v).build());
            effects.addEntry(e.startDoubleField(Component.translatable("config.itemmagnet.sound_volume"), c.soundVolume).setDefaultValue(.12).setMin(0).setMax(1).setSaveConsumer(v -> c.soundVolume = v).build());
            addRecipeCategory(builder, e, "weak", c.weakRecipe);
            addRecipeCategory(builder, e, "mid", c.midRecipe);
            addRecipeCategory(builder, e, "strong", c.strongRecipe);
            return builder.build();
        };
    }
    private static void addRecipeCategory(ConfigBuilder builder, ConfigEntryBuilder entries, String tier, String[][] grid) {
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.itemmagnet.recipe." + tier));
        for (int row = 0; row < 3; row++) {
            final int index = row;
            String original = String.join(", ", grid[row]);
            category.addEntry(entries.startStrField(
                Component.translatable("config.itemmagnet.recipe.row", row + 1), original)
                .setSaveConsumer(value -> grid[index] = RecipePackExporter.parseRow(value, grid[index]))
                .build());
        }
    }
}

