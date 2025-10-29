package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.Capacitor;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.tools.Tools;
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
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.client.screen.AmberValueModifierScreen;
import welbre.ambercraft.network.AmberValueModifierPayload;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

import java.util.UUID;

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
            if (element.getElement() instanceof Capacitor capacitor)
            {
                if (!level.isClientSide)
                {
                    UpdateAmberSecureKeyPayload.ADD_NEW_KEY(UUID.randomUUID(), pos, DirectionalElectricalBE.class, (ServerPlayer) player);
                    var buf = AmberValueModifierScreen.GET_BUFFER(pos, AmberValueModifierPayload.Type.CAPACITANCE, capacitor.getCapacitance(), "ambercraft.measures.capacitance", "F");
                    AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.AMBER_VALUE_MODIFIER, buf, (ServerPlayer) player);
                }

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

}
