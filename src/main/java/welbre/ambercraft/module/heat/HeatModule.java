package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.heat.HeatBE;
import welbre.ambercraft.item.ThermometerItem;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.Node;
import welbre.ambercraft.sim.heat.HeatNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HeatModule extends NetworkModule implements Serializable, DebugToolInfo {
    HeatNode node;

    public HeatModule() {
        super();
    }

    public HeatNode getHeatNode(){
        return node;
    }
    
    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        if (node != null)
            tag.put("node", node.toTag());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        if (tag.contains("node")){
            HeatNode n = (HeatNode) Node.fromStringClass(tag.getCompound("node").getString("class"));
            n.fromTag(tag.getCompound("node"));
            node = n;
        }
    }

    public String getMultimeterString(){
        return "%.3fºC".formatted(node.getTemperature());
    }

    @Override
    public String toString() {
        return "HeatModule{ID = @%x}".formatted(ID);
    }

    @Override
    public void tick(ModulesHolder entity){
        if (!this.isMaster())
            return;
        Profiler.get().push("HeatModuleTick");

        masterLogic.tick(entity);

        Profiler.get().pop();
    }

    /**
     * Allocates father new instance of father {@link HeatNode} and registers it within the network,
     */
    public void alloc() {
        node = new HeatNode();
    }

    /**
     * Initialize the current module in the world, using the blockEntity, the level, and the position.<br>
     *
     * Server side only!
     */
    public <T extends ModulesHolder> void init(T entity, LevelAccessor level, BlockPos pos)
    {
        refresh(entity);
        this.node.setTemperature(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos));

        if (isMaster())
            masterLogic = createMaster();
    }

    /**
     * Frees the pointer associated with this module.<br>
     * Before call this, the internal pointer will be null.
     */
    public void free() {
        node = null;
        disconnectAll();
    }

    @Override
    public Master createMaster() {
        return new HeatModuleMaster(this);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == AmberCraft.Items.THERMOMETER.get())
        {
            if (!level.isClientSide)
                ThermometerItem.sendTemperature((ServerPlayer) player, this);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide())
            return;
        if (this.getHeatNode().getTemperature() > 100)
            entity.hurtServer((ServerLevel) level, level.damageSources().inFire(), (float) (this.getHeatNode().getTemperature() / 100f));
    }

    @Override
    public List<Component> getInfo() {
        ArrayList<Component> list = new ArrayList<>();
        if (node != null)
        {
            list.add(Component.literal("Temperature: %.2f ºC".formatted(node.getTemperature())));
            list.add(Component.literal("Conductivity: %.2f W/ºC".formatted(node.getThermalConductivity())));
            list.add(Component.literal("Capacidade: %.2f J/ºC".formatted(node.getThermalMass())));
            list.add(Component.literal("Ambient temperature: %.2f ºC".formatted(node.getEnvTemperature())));
            list.add(Component.literal("Ambient conductivity: %.2f W/ºC".formatted(node.getEnvConductivity())));
        } else
            list.add(Component.literal("Heat not is null!").withColor(DyeColor.RED.getTextColor()));
        return list;
    }

    public static <T extends HeatBE> Module.Consumer<T, HeatModule> SET_THERMAL_CONDUCTIVITY_CONSUMER(double value)
    {
        return (module, entity, level, pos) -> module.getHeatNode().setThermalConductivity(value);
    }
}
