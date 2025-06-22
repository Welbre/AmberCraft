package welbre.ambercraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import welbre.ambercraft.blockentity.*;
import welbre.ambercraft.blockitem.FacedCableBlockItem;
import welbre.ambercraft.blocks.*;
import welbre.ambercraft.blocks.parent.AmberFreeBlock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "ambercraft";

    public Main(IEventBus modBus, ModContainer container) {
        Blocks.REGISTER.register(modBus);
        Items.REGISTER.register(modBus);
        Tiles.REGISTER.register(modBus);
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

        public static final DeferredItem<FacedCableBlockItem> FACED_CABLE_BLOCK_ITEM = REGISTER.registerItem("faced_cable", FacedCableBlockItem::new);
    }

    public static final class Tiles {
        public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

        public static final Supplier<BlockEntityType<HeatFurnaceTile>> HEAT_FURNACE_TILE = REGISTER.register("heat_furnace_tile",() -> new BlockEntityType<>(HeatFurnaceTile::new, Blocks.HEAT_FURNACE_BLOCK.get()));
        public static final Supplier<BlockEntityType<CopperHeatConductorTile>> COPPER_HEAT_CONDUCTOR_TILE = REGISTER.register("copper_heat_conductor", () -> new BlockEntityType<>(CopperHeatConductorTile::new, Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()));
        public static final Supplier<BlockEntityType<IronHeatConductorTile>> IRON_HEAT_CONDUCTOR_TILE = REGISTER.register("iron_heat_conductor", () -> new BlockEntityType<>(IronHeatConductorTile::new, Blocks.IRON_HEAT_CONDUCTOR_BLOCK.get()));
        public static final Supplier<BlockEntityType<GoldHeatConductorBlockEntitty>> GOLD_HEAT_CONDUCTOR_TILE = REGISTER.register("gold_heat_conductor", () -> new BlockEntityType<>(GoldHeatConductorBlockEntitty::new, Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get()));

        public static final Supplier<BlockEntityType<HeatSinkBlockEntity>> HEAT_SINK_BLOCK_ENTITY = REGISTER.register("heat_sink", () -> new BlockEntityType<>(HeatSinkBlockEntity::new,Blocks.HEAT_SINK_BLOCK.get()));
        public static final Supplier<BlockEntityType<FacedCableBlockEntity>> FACED_CABLE_BLOCK_ENTITY = REGISTER.register("faced_cable", () -> new BlockEntityType<>(FacedCableBlockEntity::new,Blocks.ABSTRACT_FACED_CABLE_BLOCK.get()));
    }

    public static final class TABS {
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
                                    itemList.add(item);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    itemList.sort(Comparator.comparing(item -> item.getName().getString()));//sort using name
                    for (Item item : itemList)
                        output.accept(item);
                })
                .build()
        );
    }
}
