package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.Tools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.DyeColor;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ElectricalModule extends NetworkModule implements DebugToolInfo {
    private Element element;
    private ElectricalPinModule pinA;
    private ElectricalPinModule pinB;

    public ElectricalModule() {
    }

    public ElectricalModule(Element element)
    {
        setElement(element);
    }

    public void setElement(Element e)
    {
        element = e;
        pinA = new ElectricalPinModule(this, e::getPinA, e::connectA);
        pinB = new ElectricalPinModule(this, e::getPinB, e::connectB);
    }

    public Element getElement() {
        return element;
    }

    public ElectricalPinModule getPinA() {
        return pinA;
    }

    public ElectricalPinModule getPinB() {
        return pinB;
    }

    //-------------------------------------------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------Data--------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------------------------------------------//

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        if (element != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(stream);
            try
            {

                element.serialize(data);
                data.close();
                stream.close();
                tag.putByteArray("element", stream.toByteArray());
                tag.putString("elementClass", element.getClass().getSimpleName());
            } catch (IOException e)
            {
                AmberCraft.LOGGER.error("ElectricalModule: Failed to serialize element!", e);
            }
        }
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        if (tag.contains("elementClass"))
        {
            try
            {
                Class<?> clazz = Class.forName("kuse.welbre.sim.electrical.elements." + tag.getString("elementClass"));
                Object object = clazz.getConstructor().newInstance();
                if (object instanceof Element el)
                {
                    DataInputStream data = new DataInputStream(new ByteArrayInputStream(tag.getByteArray("element")));
                    el.unSerialize(data);
                    data.close();

                    setElement(el);
                } else {
                    throw new RuntimeException("ElectricalModule: Element class is not an instance of Element! Expected %s founded %s".formatted(Element.class.getName(), object.getClass().getName()));
                }
            } catch (Exception e)
            {
                AmberCraft.LOGGER.error("ElectricalModule: Failed to deserialize element!", e);
            }
        }
    }

    public void alloc(){}

    public void free()
    {
        disconnectAll();
        element = null;
        pinA = null;
        pinB = null;
    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);
    }

    @Override
    public void tick(ModulesHolder entity) {
        if (!this.isMaster())
            return;
        Profiler.get().push("ElectricalModule tick");

        master.tick(entity);

        Profiler.get().pop();
    }

    @Override
    public List<Component> getInfo() {
        if (element == null)
            return List.of(Component.literal("Element is null!"));

        List<Component> list = new ArrayList<>();
        list.add(GET_ELEMENT_INFO(element));

        return list;
    }

    public static Component GET_ELEMENT_INFO(Element element)
    {
        if (element == null)
            return Component.literal("Element is null");
        else
            return Component.empty()
                    .append(element.getClass().getSimpleName()).withColor(DyeColor.PURPLE.getTextColor())//element name
                    .append(Component.literal("(%s)".formatted(Tools.proprietyToSi(element.getProperties(), element.getPropertiesSymbols(), 2))).withColor(DyeColor.GRAY.getTextColor())) // properties
                    .append(Component.literal("[%s,%s]: ".formatted(element.getPinA() == null ? "gnd" : element.getPinA().address+1, element.getPinB() == null ? "gnd" : element.getPinB().address+1)).withColor(DyeColor.LIGHT_GRAY.getTextColor())) // mna pins
                    .append(Component.literal("%.2fv, %.2fA, %.2fW".formatted(element.getVoltageDifference(),element.getCurrent(),element.getPower())).withColor(DyeColor.LIME.getTextColor()));// electrical quantity
    }

    @Override
    public <T extends Module> ModuleType<T> getType() {
        var type = AmberCraft.ModuleTypes.ELECTRICAL_MODULE_TYPE.get();
        return (ModuleType<T>) type;
    }
}
