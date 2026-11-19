package de.tuefteltyp.itemmagnet;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class MagnetItem extends Item {
    private static final String STATE_KEY = "itemmagnet_enabled";

    public MagnetItem(Properties properties) { super(properties); }

    // No explicit state means OFF: a player must activate each magnet with right-click.
    public static boolean isActive(ItemStack stack) {
        if (!(stack.getItem() instanceof MagnetItem)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(STATE_KEY, false);
    }

    // Reset only the toggle field; keep any other custom item data intact.
    public static void resetSwitchState(ItemStack stack) {
        if (!(stack.getItem() instanceof MagnetItem)) return;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().contains(STATE_KEY)) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(STATE_KEY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            boolean next = !isActive(stack);
            CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putBoolean(STATE_KEY, next));
            // Intentionally no actionbar or chat text. The status icon is the indicator.
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        MagnetConfig c = MagnetConfig.INSTANCE;
        double range = stack.is(ItemMagnet.STRONG) ? c.strongRadius :
                       stack.is(ItemMagnet.MID) ? c.midRadius : c.weakRadius;
        tooltip.accept(Component.translatable("tooltip.itemmagnet.state",
                Component.translatable(isActive(stack) ? "tooltip.itemmagnet.on" : "tooltip.itemmagnet.off"))
            .withStyle(isActive(stack) ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.accept(Component.translatable("tooltip.itemmagnet.radius", range).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.itemmagnet.speed", c.maxSpeed).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.itemmagnet.strength", c.pullStrength).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.itemmagnet.xp",
                Component.translatable(c.attractExperience ? "tooltip.itemmagnet.yes" : "tooltip.itemmagnet.no"))
            .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.itemmagnet.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
