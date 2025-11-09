package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.Inductor;
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
import welbre.ambercraft.module.electrical.ElectricalElementModule;
import welbre.ambercraft.network.AmberValueModifierPayload;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

public class InductorBlock extends DirectionalElectricalBlock {
    public InductorBlock(Properties p) {
        super(p);
        elementConstructor.push(ElectricalElementModule.SET_ELEMENT_IN_THE_WORLD(new Inductor(10e-6)));//1uH
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        var result = super.useWithoutItem(state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        if (level.getBlockEntity(pos) instanceof ElectricalBE element && player.getMainHandItem().is(Items.AIR))
        {
            if (element.getElement() instanceof Inductor inductor)
            {
                if (!level.isClientSide)
                {
                    UpdateAmberSecureKeyPayload.ADD_NEW_KEY(pos, DirectionalElectricalBE.class, (ServerPlayer) player);
                    var buf = AmberValueModifierScreen.GET_BUFFER(pos, AmberValueModifierPayload.Type.INDUCTANCE, inductor.getInductance(), "ambercraft.measures.inductance", "H");
                    AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.AMBER_VALUE_MODIFIER, buf, (ServerPlayer) player);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
