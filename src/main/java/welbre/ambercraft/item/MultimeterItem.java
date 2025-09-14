package welbre.ambercraft.item;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.tools.Tools;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.item.components.MultimeterComponent;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalCableModule;
import welbre.ambercraft.module.electrical.ElectricalElementModule;
import welbre.ambercraft.module.electrical.ElectricalModule;
import welbre.ambercraft.module.electrical.ElectricalTerminalModule;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static welbre.ambercraft.AmberCraft.Components.MULTIMETER_CACHE_DATA_COMPONENT;

public class MultimeterItem extends Item {
    public static final HashMap<UUID, Circuit.Pin> pinMap = new HashMap<>();

    public MultimeterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        final boolean client =  context.getLevel().isClientSide;
        if (context.getPlayer() == null)
            return InteractionResult.PASS;

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ModulesHolder holder)
        {
            List<NetworkModule> modules = filterModules(holder.getModule(context.getClickedFace()));
            if (!modules.isEmpty())
            {
                if (!client)
                {
                    for (NetworkModule module : modules)
                    {
                        handle(context.getItemInHand(), (ServerPlayer) context.getPlayer(), module);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    /// Server side only!
    public @NotNull InteractionResult handle(ItemStack stack, ServerPlayer player, NetworkModule module)
    {
        MultimeterComponent component = stack.getComponents().get(MULTIMETER_CACHE_DATA_COMPONENT.get());
        boolean hasFirst = false;
        if (component != null)
            if (pinMap.containsKey(component.id()))
                hasFirst = true;

        if (hasFirst)
        {
            switch (module)
            {
                case ElectricalCableModule ecm ->
                {
                    final var term = ecm.getTerminal()[0];
                    Circuit.Pin first = pinMap.remove(component.id());
                    stack.remove(MULTIMETER_CACHE_DATA_COMPONENT.get());
                    sendVdiff(player, first, term);

                    return InteractionResult.SUCCESS;
                }
                case ElectricalTerminalModule etm ->
                {
                    final var term = etm.getTerminal()[0];
                    Circuit.Pin first = pinMap.remove(component.id());
                    stack.remove(MULTIMETER_CACHE_DATA_COMPONENT.get());
                    sendVdiff(player, first, term);

                    return InteractionResult.SUCCESS;
                }
                case ElectricalElementModule eem ->
                {
                    sendVdiff(player, eem.getTerminalA().getTerminal()[0], eem.getTerminalB().getTerminal()[0]);
                    pinMap.remove(component.id());
                    stack.remove(MULTIMETER_CACHE_DATA_COMPONENT.get());

                    return InteractionResult.SUCCESS;
                }
                case null, default ->
                {
                    return InteractionResult.FAIL;
                }
            }
        } else
        {
            switch (module)
            {
                case ElectricalCableModule ecm ->
                {
                    putComponent(stack, ecm.getTerminal()[0]);
                    player.sendSystemMessage(Component.literal("First connection done").withColor(DyeColor.LIGHT_GRAY.getTextColor()));
                    return InteractionResult.SUCCESS;
                }
                case ElectricalTerminalModule etm ->
                {
                    putComponent(stack, etm.getTerminal()[0]);
                    player.sendSystemMessage(Component.literal("First connection done").withColor(DyeColor.LIGHT_GRAY.getTextColor()));
                    return InteractionResult.SUCCESS;
                }
                case ElectricalElementModule eem ->
                {
                    sendVdiff(player, eem.getTerminalA().getTerminal()[0], eem.getTerminalB().getTerminal()[0]);
                    player.sendSystemMessage(Component.literal("First connection done").withColor(DyeColor.LIGHT_GRAY.getTextColor()));
                    return InteractionResult.SUCCESS;
                }
                case null, default ->
                {
                    return InteractionResult.FAIL;
                }
            }
        }
    }

    private static void putComponent(ItemStack stack, Circuit.Pin pin)
    {
        UUID uuid = UUID.randomUUID();
        stack.set(MULTIMETER_CACHE_DATA_COMPONENT.get(), new MultimeterComponent(uuid));
        pinMap.put(uuid, pin);
    }

    private void sendVdiff(ServerPlayer player, Circuit.Pin a, Circuit.Pin b)
    {
        if (a == b)
        {
            player.sendSystemMessage(Component.literal("You are measuring the voltage difference in the same point, it will always be zero!")
                    .withColor(DyeColor.RED.getTextColor()));
            return;
        }
        double va = 0, vb = 0;
        if (a != null && a.P_voltage != null)
            va = a.P_voltage[0];
        if (b != null && b.P_voltage != null)
            vb = b.P_voltage[0];

        player.sendSystemMessage(
                Component.literal("Voltage: %s".formatted(
                        Tools.proprietyToSi(va - vb, "V")
                ))
        );
    }

    private List<NetworkModule> filterModules(Module[] modules)
    {
        return Arrays.stream(modules)
                .filter(a -> a instanceof NetworkModule)
                .map(a -> (NetworkModule) a)
                .filter(a -> (a instanceof ElectricalTerminalModule) || (a instanceof ElectricalModule)).toList();
    }
}
