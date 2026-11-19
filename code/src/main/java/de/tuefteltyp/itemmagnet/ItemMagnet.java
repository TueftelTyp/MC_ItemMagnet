package de.tuefteltyp.itemmagnet;

import net.fabricmc.api.ModInitializer;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ItemMagnet implements ModInitializer {
    public static final String ID = "itemmagnet";
    // Weak keys ensure server player objects can be reclaimed after disconnect.
    private final Map<ServerPlayer, HeldMagnets> previousHeld = Collections.synchronizedMap(new WeakHashMap<>());
    private static final class HeldMagnets {
        final ItemStack main;
        final ItemStack off;
        HeldMagnets(ItemStack main, ItemStack off) {
            this.main = main;
            this.off = off;
        }
    }
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final Holder<MobEffect> ACTIVE_EFFECT = Registry.registerForHolder(
        BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(ID, "magnet_active"), new MagnetActiveEffect());
    public static final Item WEAK = register("weak_magnet");
    public static final Item MID = register("mid_magnet");
    public static final Item STRONG = register("strong_magnet");

    private static Item register(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, new MagnetItem(new Item.Properties().setId(key).stacksTo(1)));
    }
    @Override public void onInitialize() {
        MagnetConfig.load();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(WEAK); entries.accept(MID); entries.accept(STRONG);
        });
        ServerTickEvents.END_LEVEL_TICK.register(this::tick);
        LOGGER.info("ItemMagnet v1.4 initialized");
    }
    private void tick(ServerLevel level) {
        MagnetConfig cfg = MagnetConfig.INSTANCE;
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) {
                player.removeEffect(ACTIVE_EFFECT);
                continue;
            }
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            refreshTransientSwitchState(player, main, off, cfg);
            double radius = cfg.enabled ? Math.max(radius(main, cfg), radius(off, cfg)) : 0;
            boolean active = radius > 0;
            MobEffectInstance current = player.getEffect(ACTIVE_EFFECT);
            if (active) {
                // No effect particles, only the vanilla status icon (upper-right HUD).
                if (current == null || current.getDuration() < 35) {
                    player.addEffect(new MobEffectInstance(ACTIVE_EFFECT, 100, 0, true, false, true));
                }
            } else if (current != null) {
                player.removeEffect(ACTIVE_EFFECT);
            }
            if (!active || level.getGameTime() % cfg.updateIntervalTicks != 0) continue;
            Vec3 target = player.position().add(0, .8, 0);
            AABB area = player.getBoundingBox().inflate(radius);
            int particleBudget = cfg.showParticles && level.getGameTime() % cfg.particleIntervalTicks == 0 ? 8 : 0;
            boolean foundDrops = false;
            for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, area, Entity::isAlive)) {
                if (drop.distanceToSqr(player) > radius * radius) continue;
                pull(drop, target, cfg);
                foundDrops = true;
                if (particleBudget-- > 0) {
                    level.sendParticles(ParticleTypes.END_ROD, drop.getX(), drop.getY() + .18,
                        drop.getZ(), 1, .08, .08, .08, .005);
                }
            }
            if (cfg.attractExperience) {
                for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, area, Entity::isAlive)) {
                    if (orb.distanceToSqr(player) > radius * radius) continue;
                    pull(orb, target, cfg);
                    foundDrops = true;
                    if (particleBudget-- > 0) {
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, orb.getX(), orb.getY() + .1,
                            orb.getZ(), 1, .07, .07, .07, .002);
                    }
                }
            }
            // One low-volume feedback sound per player, never per item.
            if (foundDrops && cfg.pullSounds && level.getGameTime() % cfg.soundIntervalTicks == 0) {
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, (float)cfg.soundVolume, 1.7f);
            }
        }
    }
    // Without persistence, the ON/OFF toggle is only valid while the same
    // physical stack remains equipped in the same hand. On a new session
    // or after re-equipping, removing the toggle field resets it to OFF.
    private void refreshTransientSwitchState(ServerPlayer player, ItemStack main, ItemStack off, MagnetConfig cfg) {
        if (cfg.rememberSwitchState) {
            previousHeld.remove(player);
            return;
        }
        HeldMagnets previous = previousHeld.get(player);
        if (previous == null || previous.main != main) MagnetItem.resetSwitchState(main);
        if (previous == null || previous.off != off) MagnetItem.resetSwitchState(off);
        previousHeld.put(player, new HeldMagnets(main, off));
    }

    private static double radius(ItemStack stack, MagnetConfig cfg) {
        if (!MagnetItem.isActive(stack)) return 0;
        return radius(stack.getItem(), cfg);
    }
    private static double radius(Item item, MagnetConfig cfg) {
        if (item == STRONG) return cfg.strongRadius;
        if (item == MID) return cfg.midRadius;
        if (item == WEAK) return cfg.weakRadius;
        return 0;
    }
    private static void pull(Entity entity, Vec3 target, MagnetConfig cfg) {
        Vec3 delta = target.subtract(entity.position());
        double distance = delta.length();
        if (distance < .15) return;
        Vec3 velocity = entity.getDeltaMovement().scale(.78).add(delta.scale(cfg.pullStrength / Math.max(1, distance)));
        if (velocity.lengthSqr() > cfg.maxSpeed * cfg.maxSpeed) velocity = velocity.normalize().scale(cfg.maxSpeed);
        entity.setDeltaMovement(velocity);
    }
    private static final class MagnetActiveEffect extends MobEffect {
        private MagnetActiveEffect() { super(MobEffectCategory.BENEFICIAL, 0x27DCE0); }
    }
}
