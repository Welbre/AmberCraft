package welbre.ambercraft.subblock;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

/**
 * A class with methods to help any item to beable to place TinyBlock in the world.
 */
public class TinyBlockItem extends Item
{

    public TinyBlockItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        ItemStack stack = context.getItemInHand();
        TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT);
        if (component == null)
        {
            if (context.getPlayer() != null)
                //if the stack hasn't a tiny block component, the stack is in a broken state, so remove it from the player.
                context.getPlayer().setItemInHand(context.getHand(), new ItemStack(Items.AIR));
            return InteractionResult.FAIL;
        }

        TinyBlock tinyBlock = component.get();
        Level level = context.getLevel();
        Grid16Context grid = new Grid16Context(component.get(), level, context.getClickedPos(), context.getClickLocation(), context.getClickedFace());

        SubBlockBE sub;
        //get a tiny block or create one
        if (level.getBlockEntity(grid.anchor()) instanceof SubBlockBE subBlockBE)
            sub = subBlockBE;
        else
        {
            level.setBlock(grid.anchor(), AmberCraft.Blocks.SUB_BLOCK.get().defaultBlockState(), 3);
            sub = (SubBlockBE) level.getBlockEntity(grid.anchor());
        }

        if (sub == null)
            return InteractionResult.FAIL;

        //Place Tiny Item
        if (!sub.addTinyBlock(tinyBlock, grid))
            return InteractionResult.FAIL;

        //Play sound
        SoundType soundtype = tinyBlock.getSoundType(sub.tinyBS.getLast(), level, sub.getBlockPos(), context.getPlayer());

        level.playSound(
                context.getPlayer(),
                sub.getBlockPos(),
                soundtype.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F
        );

        //consume amount
        stack.shrink(1);

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT);
        if (component == null)
            return Component.literal("invalid tiny block");
        else
            return component.get().getTinyItemName();
    }
}
