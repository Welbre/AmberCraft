package welbre.ambercraft.blockentity;

import kuse.welbre.sim.electrical.abstractt.Element;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalPinModule;

import java.io.*;

/**
 * A generic {@link welbre.ambercraft.AmberCraft.BlockEntity} used in simple electrical machines or as template.<br>
 * A {@link ElectricalBE#elementPointer} is an array of {@link Element} used as a pointer in the {@link ElectricalBE#pinA} and {@link ElectricalBE#pinA}.<br>
 * The element pinA is by default obtained in the North, and pinB by the South.<br>
 * <code color=#F0D500>The {@link ElectricalBE#elementPointer} can't be null!, and can't have a null element!<code>
 */
public class ElectricalBE extends ModulesHolder {
    public Element[] elementPointer;
    public ElectricalPinModule pinA;
    public ElectricalPinModule pinB;

    public ElectricalBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        setElement(null);
    }

    public ElectricalBE(BlockPos pos, BlockState state)
    {
        this(AmberCraft.BlockEntity.ELECTRICAL_BE.get(), pos, state);
        setElement(null);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (elementPointer[0] != null)
        {
            try
            {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream data = new DataOutputStream(stream);
                elementPointer[0].serialize(data);
                stream.close();
                data.close();
                tag.putByteArray("element", stream.toByteArray());
            } catch (Exception e)
            {
                AmberCraft.LOGGER.error("Failed to save element in the block entity!", e);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("element"))
        {
            byte[] data = tag.getByteArray("element");
            try
            {
                ByteArrayInputStream stream = new ByteArrayInputStream(data);
                elementPointer[0].unSerialize(new DataInputStream(stream));
                stream.close();
            } catch (Exception e)
            {
                AmberCraft.LOGGER.error("Failed to save element in the block entity!", e);
            }
        }
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{pinA, pinB};
    }

    @Override
    public @NotNull Module[] getModule(Direction dir) {
        if (dir == Direction.NORTH)
            return new Module[]{pinA};
        else if (dir == Direction.SOUTH)
            return new Module[]{pinB};
        else
            return new Module[0];
    }

    @Override
    public @NotNull Module[] getModule(Object object) {
        if (object instanceof Direction dir)
            return getModule(dir);
        return new Module[0];
    }

    public Element getElement() {
        return elementPointer[0];
    }

    public void setElement(Element element) {
        elementPointer = new Element[]{element};
        pinA = new ElectricalPinModule(elementPointer, element == null ? null : p -> p[0].getPinA(), "pinA");
        pinB = new ElectricalPinModule(elementPointer, element == null ? null : p -> p[0].getPinB(), "pinB");
    }

    public ElectricalPinModule getPinA() {
        return pinA;
    }

    public void setPinA(ElectricalPinModule pinA) {
        this.pinA = pinA;
    }

    public ElectricalPinModule getPinB() {
        return pinB;
    }

    public void setPinB(ElectricalPinModule pinB) {
        this.pinB = pinB;
    }
}
