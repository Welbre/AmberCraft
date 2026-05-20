package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.Diode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.electrical.ElectricalElementModule;

public class DiodeBlock extends DirectionalElectricalBlock {
    public DiodeBlock(Properties p) {
        super(p);
        elementConstructor.push(ElectricalElementModule.SET_ELEMENT_IN_THE_WORLD(new Diode()));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
