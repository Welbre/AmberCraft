package welbre.ambercraft.blocks.electrical;


import kuse.welbre.sim.electrical.elements.Resistor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.client.screen.AmberValueModifierScreen;
import welbre.ambercraft.network.AmberValueModifierPayload.Type;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

import java.util.UUID;

public class ResistorBlock extends DirectionalElectricalBlock {
    public ResistorBlock(Properties p) {
        super(p);
        factory.setConstructor(
                (module, entity, factory, level, pos) -> {
                    module.setElement(new Resistor(1));
                }
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        var result = super.useWithoutItem(state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        if (level.getBlockEntity(pos) instanceof ElectricalBE element && player.getMainHandItem().is(Items.AIR))
        {
            if (element.getElement() instanceof Resistor resistor)
            {
                if (!level.isClientSide)
                {
                    UpdateAmberSecureKeyPayload.ADD_NEW_KEY(pos, DirectionalElectricalBE.class, (ServerPlayer) player);
                    var buf = AmberValueModifierScreen.GET_BUFFER(pos, Type.RESISTANCE, resistor.getResistance(), "ambercraft.measures.resistance", "Ω");
                    AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.AMBER_VALUE_MODIFIER, buf, (ServerPlayer) player);
                }

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
