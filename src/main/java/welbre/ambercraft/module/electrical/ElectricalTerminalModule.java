package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

/**
 * <h5>This class is a wrapper to the {@link ElectricalElementModule}!</h5>
 * Only used to handle a connection in an electrical element.
 * In your BlockEntity that contains an ElectricalModule,
 * you should return an instance of this class in {@link ModulesHolder#getModule(Direction)} method.
 * Don't store or serialize this class,
 * it is only used in the connection / disconnection process to create the electrical elements.<br>
 */
public class ElectricalTerminalModule extends NetworkModule {
    protected final ElectricalModule electrical;
    protected Circuit.Pin[] terminal = {new Circuit.Pin()};

    public ElectricalTerminalModule(ElectricalModule electrical) {
        this.electrical = electrical;
    }

    @Override
    public boolean connect(NetworkModule target) {
        if (target instanceof ElectricalTerminalModule terminalModule)//pin pin connection
        {
            if (this.electrical.connect(terminalModule.electrical))
            {
                terminalModule.terminal = this.terminal;
                return true;
            }
            return false;
        }
        else
            return super.connect(target);
    }

    public ElectricalModule getElectrical() {
        return electrical;
    }

    public Circuit.Pin[] getTerminal() {
        return terminal;
    }

    @Override
    public void onLoad(ModulesHolder entity) {

    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);//used only to avoid to crash
    }

    @Override
    public void tick(ModulesHolder entity) {
        throw new UnsupportedOperationException("ElectricalPinModule is not tickable!");
    }

    @Override
    public ModuleType<?> getType() {
        return AmberCraft.ModuleTypes.ELECTRICAL_TERMINAL_MODULE_TYPE.get();
    }
}
