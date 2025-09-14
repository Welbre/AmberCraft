package welbre.ambercraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;

public class ThermometerItem extends Item {
    public ThermometerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        final boolean client =  context.getLevel().isClientSide;

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ModulesHolder holder)
        {
            boolean find = false;

            // loop into the modules and try to find a heat module to send the temperature to the client.
            for (Module module : holder.getModule(context.getClickedFace()))
            {
                if (module instanceof HeatModule heat)
                {
                    if (client)
                        //only return in the case that the holder contains a heat module, and is the client side
                        //why? the client doesn't have the head node, only the server has, so, just return to play the animation in the client,
                        //when the server reaches this part of the code, they will send a system message to the player with the temperatures.
                        return InteractionResult.SUCCESS;
                    else
                    {
                        sendTemperature((ServerPlayer) context.getPlayer(), heat);
                        find = true;
                    }
                }
            }
            //in the case that holder is null, or don't have a heat module, this will pass.
            //notice that only the server side controls this var
            //if the holder has a heat module, and this function is called by the client; they will never reach this, duo the preview return
            if (!find)
                return InteractionResult.PASS;
            else
                return InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }

    public static void sendTemperature(ServerPlayer player, HeatModule module)
    {
        if (player != null && module != null)
            player.sendSystemMessage(Component.literal(module.getMultimeterString()).withColor(DyeColor.ORANGE.getTextColor()));
    }
}
