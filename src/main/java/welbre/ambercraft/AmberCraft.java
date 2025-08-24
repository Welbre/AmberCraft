package welbre.ambercraft;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;
import welbre.ambercraft.blockentity.*;
import welbre.ambercraft.blocks.*;
import welbre.ambercraft.blocks.heat.*;
import welbre.ambercraft.cables.FacedCableComponent;
import welbre.ambercraft.cables.CableType;
import welbre.ambercraft.cables.TestCableType;
import welbre.ambercraft.commands.Event;
import welbre.ambercraft.item.FacedCableBlockItem;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.heat.HeatModuleType;
import welbre.ambercraft.network.PayLoadRegister;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Mod(AmberCraft.MOD_ID)
public class AmberCraft {
    public static final String MOD_ID = "ambercraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AmberCraft(IEventBus modBus, ModContainer container) {
        modBus.addListener(AmberRegisters::registerRegistries);
        Event.register();


        Modules.REGISTER.register(modBus);
        CableTypes.REGISTER.register(modBus);

        Blocks.REGISTER.register(modBus);
        Items.REGISTER.register(modBus);
        BlockEntity.REGISTER.register(modBus);
        Components.REGISTER.register(modBus);

        modBus.addListener(PayLoadRegister::registerPayLoads);

        TABS.REGISTER.register(modBus);
    }

    public static final class Blocks {
        public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(MOD_ID);

        public static final DeferredHolder<Block, Block> IRON_MACHINE_CASE_BLOCK = REGISTER.registerSimpleBlock("iron_machine_case_block");
        public static final DeferredHolder<Block, VoltageSourceBlock> VOLTAGE_SOURCE_BLOCK = REGISTER.registerBlock("voltage_source_block", VoltageSourceBlock::new);
        public static final DeferredHolder<Block, ResistorBlock> RESISTOR_BLOCK = REGISTER.registerBlock("resistor_block", ResistorBlock::new);
        public static final DeferredHolder<Block, Ground> GROUND_BLOCK = REGISTER.registerBlock("ground_block", Ground::new);

        public static final DeferredHolder<Block, HeatPumpBlock> HEAT_PUMP_BLOCK = REGISTER.registerBlock("heat_pump", HeatPumpBlock::new);
        public static final DeferredHolder<Block, HeatSourceBlock> HEAT_SOURCE_BLOCK = REGISTER.registerBlock("heat_source", HeatSourceBlock::new);
        public static final DeferredHolder<Block, HeatFurnaceBlock> HEAT_FURNACE_BLOCK = REGISTER.registerBlock("heat_furnace", HeatFurnaceBlock::new);
        public static final DeferredHolder<Block, CreativeHeatFurnaceBlock> CREATIVE_HEAT_FURNACE_BLOCK = REGISTER.registerBlock("creative_heat_furnace", CreativeHeatFurnaceBlock::new);

        public static final DeferredHolder<Block, CopperHeatConductorBlock> COPPER_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("copper_heat_conductor", CopperHeatConductorBlock::new);
        public static final DeferredHolder<Block, IronHeatConductorBlock> IRON_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("iron_heat_conductor", IronHeatConductorBlock::new);
        public static final DeferredHolder<Block, GoldHeatConductorBlock> GOLD_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("gold_heat_conductor", GoldHeatConductorBlock::new);
        public static final DeferredHolder<Block, CreativeHeatConductorBlock> CREATIVE_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("creative_heat_conductor", CreativeHeatConductorBlock::new);

        public static final DeferredHolder<Block, HeatSinkBlock> HEAT_SINK_BLOCK = REGISTER.registerBlock("heat_sink", HeatSinkBlock::new);

        public static final DeferredHolder<Block, FacedCableBlock> ABSTRACT_FACED_CABLE_BLOCK = REGISTER.registerBlock("faced_cable", FacedCableBlock::new);

        /// contains all blocks that can use {@link HeatBE}
        public static final List<DeferredHolder<Block, ? extends HeatBlock>> HEAT_BE_USES = new ArrayList<>(List.of(
                COPPER_HEAT_CONDUCTOR_BLOCK,IRON_HEAT_CONDUCTOR_BLOCK,GOLD_HEAT_CONDUCTOR_BLOCK,CREATIVE_HEAT_CONDUCTOR_BLOCK, HEAT_SOURCE_BLOCK
        ));
    }

    public static final class Items{
        public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(MOD_ID);
        public static final DeferredItem<BlockItem> IRON_MACHINE_CASE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.IRON_MACHINE_CASE_BLOCK);
        public static final DeferredItem<BlockItem> VOLTAGE_SOURCE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.VOLTAGE_SOURCE_BLOCK);
        public static final DeferredItem<BlockItem> RESISTOR_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.RESISTOR_BLOCK);
        public static final DeferredItem<BlockItem> GROUND_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.GROUND_BLOCK);
        public static final DeferredItem<BlockItem> HEAT_FURNACE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.HEAT_FURNACE_BLOCK);
        public static final DeferredItem<BlockItem> CREATIVE_HEAT_FURNACE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.CREATIVE_HEAT_FURNACE_BLOCK);
        public static final DeferredItem<Item> MULTIMETER = REGISTER.registerItem("multimeter", Item::new, new Item.Properties());

        public static final DeferredItem<BlockItem> COPPER_HEAT_CONDUCTOR_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.COPPER_HEAT_CONDUCTOR_BLOCK);
        public static final DeferredItem<BlockItem> IRON_HEAT_CONDUCTOR_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.IRON_HEAT_CONDUCTOR_BLOCK);
        public static final DeferredItem<BlockItem> GOLD_HEAT_CONDUCTOR_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.GOLD_HEAT_CONDUCTOR_BLOCK);
        public static final DeferredItem<BlockItem> CREATIVE_HEAT_CONDUCTOR_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.CREATIVE_HEAT_CONDUCTOR_BLOCK);
        public static final DeferredItem<BlockItem> HEAT_SOURCE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.HEAT_SOURCE_BLOCK);
        public static final DeferredItem<BlockItem> HEAT_PUMP_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.HEAT_PUMP_BLOCK);

        public static final DeferredItem<BlockItem> HEAT_SINK_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.HEAT_SINK_BLOCK);

        @TABS.SKIP
        public static final DeferredItem<FacedCableBlockItem> FACED_CABLE_BLOCK_ITEM = REGISTER.registerItem("faced_cable", FacedCableBlockItem::new);
    }

    public static final class BlockEntity {
        public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

        public static final Supplier<BlockEntityType<HeatFurnaceBE>> HEAT_FURNACE_BE = REGISTER.register("heat_furnace_tile",() -> new BlockEntityType<>(HeatFurnaceBE::new, Blocks.HEAT_FURNACE_BLOCK.get()));
        public static final Supplier<BlockEntityType<HeatBE>> HEAT_CONDUCTOR_BE = REGISTER.register("heat_conductor", () -> new BlockEntityType<>(HeatBE::new, Blocks.HEAT_BE_USES.stream().map(DeferredHolder::get).toArray(Block[]::new)));
        public static final Supplier<BlockEntityType<HeatSourceBE>> HEAT_SOURCE_BE = REGISTER.register("heat_source", () -> new BlockEntityType<>(HeatSourceBE::new,Blocks.HEAT_SOURCE_BLOCK.get()));
        public static final Supplier<BlockEntityType<HeatPumpBE>> HEAT_PUMP_BE = REGISTER.register("heat_pump", () -> new BlockEntityType<>(HeatPumpBE::new, Blocks.HEAT_PUMP_BLOCK.get()));
        public static final Supplier<BlockEntityType<CreativeHeatFurnaceBE>> CREATIVE_HEAT_FURNACE_BE = REGISTER.register("creative_heat_furnace", () -> new BlockEntityType<>(CreativeHeatFurnaceBE::new,Blocks.CREATIVE_HEAT_FURNACE_BLOCK.get()));

        public static final Supplier<BlockEntityType<HeatSinkBE>> HEAT_SINK_BLOCK_BE = REGISTER.register("heat_sink", () -> new BlockEntityType<>(HeatSinkBE::new,Blocks.HEAT_SINK_BLOCK.get()));
        public static final Supplier<BlockEntityType<FacedCableBE>> FACED_CABLE_BLOCK_BE = REGISTER.register("faced_cable", () -> new BlockEntityType<>(FacedCableBE::new,Blocks.ABSTRACT_FACED_CABLE_BLOCK.get()));
    }

    public static final class AmberRegisters {
        private static final ResourceKey<Registry<CableType>> CABLE_TYPE_REGISTER_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cable_type"));
        private static final ResourceKey<Registry<ModuleType<?>>> MODULE_TYPE_REGISTER_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "module_type"));

        public static final Registry<CableType>  CABLE_TYPE_REGISTRY = new RegistryBuilder<>(CABLE_TYPE_REGISTER_KEY).create();
        public static final Registry<ModuleType<?>>  MODULE_TYPE_REGISTRY = new RegistryBuilder<>(MODULE_TYPE_REGISTER_KEY).create();

        public static void registerRegistries(NewRegistryEvent event) {
            event.register(CABLE_TYPE_REGISTRY);
            event.register(MODULE_TYPE_REGISTRY);
        }
    }

    public static final class CableTypes {
        public static final DeferredRegister<CableType> REGISTER = DeferredRegister.create(AmberRegisters.CABLE_TYPE_REGISTRY, "cable_type");

        public static final Supplier<TestCableType> TEST_CABLE_TYPE = REGISTER.register("test_cable_type", TestCableType::new);
    }

    public static final class Modules {
        public static final DeferredRegister<ModuleType<?>> REGISTER = DeferredRegister.create(AmberRegisters.MODULE_TYPE_REGISTRY, "module_type");

        public static final DeferredHolder<ModuleType<?>, HeatModuleType> HEAT_MODULE_TYPE = REGISTER.register("heat_module_type", HeatModuleType::new);
    }

    public static final class Components {
        public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,MOD_ID);

        public static final Supplier<DataComponentType<FacedCableComponent>> CABLE_DATA_COMPONENT = REGISTER.registerComponentType(
                "cable_data",
                builder -> builder.persistent(FacedCableComponent.CODEC).networkSynchronized(FacedCableComponent.STREAM_CODEC)
        );
    }

    public static final class TABS {
        /**
         * Skips the automatic tab insertion.
         */
        @Retention(RetentionPolicy.RUNTIME)
        public @interface SKIP {}


        public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = REGISTER.register("ambercraft_tab", () -> CreativeModeTab.builder()
                .title(Component.literal("AmberCraft"))
                .icon(Items.MULTIMETER::toStack)
                .displayItems((parameters, output) -> {
                    List<Item> itemList = new ArrayList<>();
                    try {
                        for (Field field : Items.class.getDeclaredFields()) {
                            if (field.get(null) instanceof DeferredItem<?> register)
                                if (register.get() instanceof Item item)
                                    if (!field.isAnnotationPresent(SKIP.class))
                                        itemList.add(item);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    itemList.sort(Comparator.comparing(item -> item.getName().getString()));//sort using name


                    output.acceptAll(FACED_CABLES());
                    for (Item item : itemList)
                        output.accept(item);
                })
                .build()
        );

        public static List<ItemStack> FACED_CABLES(){
            var list = new ArrayList<ItemStack>();
            for (DyeColor color : DyeColor.values())
            {
                var stack = new ItemStack(Items.FACED_CABLE_BLOCK_ITEM.get());
                stack.set(Components.CABLE_DATA_COMPONENT.get(),
                        new FacedCableComponent(CableTypes.TEST_CABLE_TYPE.get(), color.getTextureDiffuseColor()));
                list.add(stack);
            }
            return list;
        }
    }
}
