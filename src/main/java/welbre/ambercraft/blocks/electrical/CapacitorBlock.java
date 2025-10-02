package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.Capacitor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;

public class CapacitorBlock extends DirectionalElectricalBlock {
    public CapacitorBlock(Properties p) {
        super(p);
        factory.setConstructor(
                (module, entity, factory, level, pos) -> {
                    module.setElement(new Capacitor(10e-6));//1uF
                }
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = super.useWithoutItem(state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        if (level.getBlockEntity(pos) instanceof ElectricalBE element && player.getMainHandItem().is(Items.AIR))
        {
            if (!level.isClientSide)
            {
                if (element.getElement() instanceof Capacitor capacitor)
                {
                    final double capacitance = capacitor.getCapacitance() * (player.isShiftKeyDown() ? 0.5 : 2);
                    capacitor.setCapacitance(Math.max(1e-6, capacitance));//1uF of min capacitance
                    element.setChanged();
                    element.getElectricalModule().dirtMaster();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                    ((ServerPlayer) player).sendSystemMessage(Component.literal("Capacitance set to: " + capacitance).withColor(DyeColor.ORANGE.getTextColor()));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

}
