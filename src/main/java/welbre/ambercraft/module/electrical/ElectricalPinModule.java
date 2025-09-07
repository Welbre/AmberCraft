package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.Tools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * This module is used to create an electrical access point to a {@link Element}.<br>
 */
public class ElectricalPinModule extends NetworkModule implements DebugToolInfo {
    public final Element[] elementPointer;
    private transient final Function<Element[], Circuit.Pin> pinGetter;
    public final String name;

    public ElectricalPinModule(Element[] pointer, Function<Element[], Circuit.Pin> pinGetter, String name) {
        this.elementPointer = pointer;
        this.pinGetter = pinGetter;
        this.name = name;
    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);
    }

    @Override
    public void tick(ModulesHolder entity) {

    }

    @Override
    public ModuleType<?> getType() {
        return AmberCraft.ModuleTypes.ELECTRICAL_MODULE_TYPE.get();
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        tag.putString("name", name);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        tag.putString("name", name);
    }

    public void alloc() {

    }

    public void free() {

    }

    public List<Element> getElements()
    {
        if (elementPointer != null)
            return Arrays.stream(elementPointer).filter(Objects::nonNull).toList();
        else return List.of();
    }

    public Circuit.Pin getPin(){
        if (elementPointer != null)
            return pinGetter.apply(elementPointer);
        else
            throw new NullPointerException("Try to get a pin in a non initiated !" + getClass().getSimpleName() + " instance!");
    }

    @Override
    public List<Component> getInfo() {
        var elements = getElements();
        if (elements.isEmpty())
            return List.of(Component.literal("Element is null!"));

        List<Component> list = new ArrayList<>();
        for (Element e : elements)
        {
            list.add(
                    Component.empty()
                            .append(e.getClass().getSimpleName()).withColor(DyeColor.PURPLE.getTextColor())//element name
                            .append(Component.literal(":"+name).withColor(DyeColor.LIGHT_GRAY.getTextColor())) //pin name
                            .append(Component.literal("(%s)".formatted(Tools.proprietyToSi(e.getProperties(), e.getPropertiesSymbols(), 2))).withColor(DyeColor.GRAY.getTextColor())) // properties
                            .append(Component.literal("[%s,%s]: ".formatted(e.getPinA() == null ? "gnd" : e.getPinA().address+1, e.getPinB() == null ? "gnd" : e.getPinB().address+1)).withColor(DyeColor.LIGHT_GRAY.getTextColor())) // mna pins
                            .append(Component.literal("%.2fv, %.2fA, %.2fW".formatted(e.getVoltageDifference(),e.getCurrent(),e.getPower())).withColor(DyeColor.LIME.getTextColor())) // electrical quantity
            );
        }

        return list;
    }
}
