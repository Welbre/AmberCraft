package welbre.ambercraft.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.network.NetworkViewerScreenPayLoad;

public class NetworkTool extends Item {
    public NetworkTool(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ModulesHolder holder)
        {
            //should send it from the server because the network viewer needs the connections between networkModules, and the client skips this step!
            if (!context.getLevel().isClientSide)
                PacketDistributor.sendToPlayer((ServerPlayer) context.getPlayer(), new NetworkViewerScreenPayLoad(holder));
            else
                return InteractionResult.SUCCESS;

            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }
}
