package welbre.ambercraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import welbre.ambercraft.blocks.ResistorBlock;
import welbre.ambercraft.blocks.VoltageSourceBlock;

import java.util.function.Function;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "ambercraft";

    public Main(IEventBus modBus, ModContainer container) {
        Blocks.REGISTER.register(modBus);
        Items.REGISTER.register(modBus);
        TABS.REGISTER.register(modBus);
    }

    public static final class Blocks {
        public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(MOD_ID);
        public static final DeferredHolder<Block, Block> IRON_MACHINE_CASE_BLOCK = REGISTER.registerSimpleBlock("iron_machine_case_block");
        public static final DeferredHolder<Block, VoltageSourceBlock> VOLTAGE_SOURCE_BLOCK = REGISTER.registerBlock("voltage_source_block", VoltageSourceBlock::new);
        public static final DeferredHolder<Block, ResistorBlock> RESISTOR_BLOCK = REGISTER.registerBlock("resistor_block", ResistorBlock::new);
    }

    public static final class Items{
        public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(MOD_ID);
        public static final DeferredItem<BlockItem> IRON_MACHINE_CASE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.IRON_MACHINE_CASE_BLOCK);
        public static final DeferredItem<BlockItem> VOLTAGE_SOURCE_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.VOLTAGE_SOURCE_BLOCK);
        public static final DeferredItem<BlockItem> RESISTOR_BLOCK_ITEM = REGISTER.registerSimpleBlockItem(Blocks.RESISTOR_BLOCK);
        public static final DeferredItem<Item> MULTIMETER = REGISTER.registerItem("multimeter", Item::new, new Item.Properties());
    }

    public static final class TABS {
        public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = REGISTER.register("ambercraft_tab", () -> CreativeModeTab.builder()
                .title(Component.literal("AmberCraft"))
                .icon(Items.MULTIMETER::toStack)
                .displayItems((parameters, output) ->{
                        output.accept(Items.IRON_MACHINE_CASE_BLOCK_ITEM.get());
                        output.accept(Items.VOLTAGE_SOURCE_BLOCK_ITEM.get());
                        output.accept(Items.RESISTOR_BLOCK_ITEM.get());
                        output.accept(Items.MULTIMETER.get());
                })
                .build()
        );
    }
}
