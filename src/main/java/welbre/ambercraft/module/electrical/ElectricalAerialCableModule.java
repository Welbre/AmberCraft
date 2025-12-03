package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.tools.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.List;

/// A version of {@link ElectricalCableModule} but when a ElectricalAerialCableModule -> ElectricalAerialCableModule connection happens, the resistor resistence changes based in the distance.
public class ElectricalAerialCableModule extends ElectricalCableModule
{
    ///the resistance_per_block used to calculate the aerialResistence, in ohm/m
    public double aerialResistence = 1.0;
    public transient BlockPos pos;

    public ElectricalAerialCableModule() {
        super();
    }

    /**
     *
     * @param resistence the resistence between a connection
     * @param aerialResistence the resistance_per_block used to calculate the aerialResistence
     */
    public ElectricalAerialCableModule(double resistence, double aerialResistence, BlockPos pos) {
        super(resistence);
        this.aerialResistence = aerialResistence;
        this.pos = pos;
    }

    @Override
    public boolean connect(NetworkModule target) {
        if (super.connect(target) && target instanceof ElectricalAerialCableModule other)
        {
            //update the last resistor and terminalEnd to the new resistor
            //why ?, ElectricalAerialCableModule is an instance of ElectricalCableModule, and the super method created a resistor if receive ElectricalCableModule,
            //but with the wrong resistence.
            final double length = pos.getCenter().distanceTo(other.pos.getCenter());
            resistors[resistors.length-1] = new Resistor(length * (aerialResistence + other.aerialResistence) /2.0);
        }
        return super.connect(target);
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        tag.putLong("pos", pos.asLong());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        pos = BlockPos.of(tag.getLong("pos"));
    }

    @Override
    public List<Component> getInfo() {
        List<Component> info = super.getInfo();
        info.add(2, Component.literal("Aerial resistence: " + Tools.proprietyToSi(aerialResistence, "Ω/m")));
        return info;
    }
}
