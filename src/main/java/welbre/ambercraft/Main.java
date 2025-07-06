package welbre.ambercraft;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.*;
import welbre.ambercraft.blockentity.*;
import welbre.ambercraft.item.FacedCableBlockItem;
import welbre.ambercraft.blocks.*;
import welbre.ambercraft.blocks.parent.AmberFreeBlock;
import welbre.ambercraft.cables.CableType;
import welbre.ambercraft.cables.AmberFCableComponent;
import welbre.ambercraft.cables.TestCableType;
import welbre.ambercraft.module.HeatModuleType;
import welbre.ambercraft.module.ModuleType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "ambercraft";

    public Main(IEventBus modBus, ModContainer container) {
        modBus.addListener(AmberRegisters::registerRegistries);
        ServerEvents.register();


        Modules.REGISTER.register(modBus);
        CableTypes.REGISTER.register(modBus);

        Blocks.REGISTER.register(modBus);
        Items.REGISTER.register(modBus);
        BlockEntity.REGISTER.register(modBus);
        Components.REGISTER.register(modBus);

        TABS.REGISTER.register(modBus);
    }

    public static final class Blocks {
        public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(MOD_ID);
        public static final DeferredHolder<Block, Block> IRON_MACHINE_CASE_BLOCK = REGISTER.registerSimpleBlock("iron_machine_case_block");
        public static final DeferredHolder<Block, VoltageSourceBlockAmberBasic> VOLTAGE_SOURCE_BLOCK = REGISTER.registerBlock("voltage_source_block", VoltageSourceBlockAmberBasic::new);
        public static final DeferredHolder<Block, ResistorBlock> RESISTOR_BLOCK = REGISTER.registerBlock("resistor_block", ResistorBlock::new);
        public static final DeferredHolder<Block, Ground> GROUND_BLOCK = REGISTER.registerBlock("ground_block", Ground::new);
        public static final DeferredHolder<Block, HeatFurnace> HEAT_FURNACE_BLOCK = REGISTER.registerBlock("heat_furnace", HeatFurnace::new);
        public static final DeferredHolder<Block, AmberFreeBlock> CREATIVE_HEAT_FURNACE_BLOCK = REGISTER.registerBlock("creative_heat_furnace", AmberFreeBlock::new);

        public static final DeferredHolder<Block, CopperHeatConductorBlock> COPPER_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("copper_heat_conductor", CopperHeatConductorBlock::new);
        public static final DeferredHolder<Block, IronHeatConductorBlock> IRON_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("iron_heat_conductor", IronHeatConductorBlock::new);
        public static final DeferredHolder<Block, GoldHeatConductorBlock> GOLD_HEAT_CONDUCTOR_BLOCK = REGISTER.registerBlock("gold_heat_conductor", GoldHeatConductorBlock::new);

        public static final DeferredHolder<Block, HeatSinkBlock> HEAT_SINK_BLOCK = REGISTER.registerBlock("heat_sink", HeatSinkBlock::new);

        public static final DeferredHolder<Block, FacedCableBlock> ABSTRACT_FACED_CABLE_BLOCK = REGISTER.registerBlock("faced_cable", FacedCableBlock::new);
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

        public static final DeferredItem<BlockItem> HEAT_SINK_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.HEAT_SINK_BLOCK);

        @TABS.SKIP
        public static final DeferredItem<FacedCableBlockItem> FACED_CABLE_BLOCK_ITEM = REGISTER.registerItem("faced_cable", FacedCableBlockItem::new);
    }

    public static final class BlockEntity {
        public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

        public static final Supplier<BlockEntityType<HeatFurnaceBE>> HEAT_FURNACE_BE = REGISTER.register("heat_furnace_tile",() -> new BlockEntityType<>(HeatFurnaceBE::new, Blocks.HEAT_FURNACE_BLOCK.get()));
        public static final Supplier<BlockEntityType<CopperHeatConductorConductorBE>> COPPER_HEAT_CONDUCTOR_BE = REGISTER.register("copper_heat_conductor", () -> new BlockEntityType<>(CopperHeatConductorConductorBE::new, Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()));
        public static final Supplier<BlockEntityType<IronHeatConductorConductorBE>> IRON_HEAT_CONDUCTOR_BE = REGISTER.register("iron_heat_conductor", () -> new BlockEntityType<>(IronHeatConductorConductorBE::new, Blocks.IRON_HEAT_CONDUCTOR_BLOCK.get()));
        public static final Supplier<BlockEntityType<GoldHeatConductorConductorBE>> GOLD_HEAT_CONDUCTOR_BE = REGISTER.register("gold_heat_conductor", () -> new BlockEntityType<>(GoldHeatConductorConductorBE::new, Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get()));

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

        public static final Supplier<DataComponentType<AmberFCableComponent>> CABLE_DATA_COMPONENT = REGISTER.registerComponentType(
                "cable_data",
                builder -> builder.persistent(AmberFCableComponent.CODEC).networkSynchronized(AmberFCableComponent.STREAM_CODEC)
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
                        new AmberFCableComponent(CableTypes.TEST_CABLE_TYPE.get(), color.getTextureDiffuseColor()));
                list.add(stack);
            }
            return list;
        }
    }
}
