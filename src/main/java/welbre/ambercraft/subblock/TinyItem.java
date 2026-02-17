package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

/**
 * A class with methods to help any item to beable to place TinyBlock in the world.
 */
public class TinyItem extends Item
{

    public TinyItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        Level level = context.getLevel();
        SubBlockBE sub = null;

        //check if the clicked block is a tiny block
        if (level.getBlockEntity(context.getClickedPos()) instanceof SubBlockBE subBlockBE)
        {
            //fixme, Se o jogador clicar em um tiny block dentro do sub block que esteja na borda do bloco, então haverá problemas, pois o correto será criar outro tiny block na direção da face clicada
            //fixme porem como está implementado agora o proprio bloco clicado será retornado!
            sub = subBlockBE;
        }
        else
        {
            final BlockPos relative = context.getClickedPos().relative(context.getClickedFace());
            //if isn't, check if the block facing the clicked direction is a sub block
            if (level.getBlockEntity(relative) instanceof SubBlockBE subBlockBE)
            {
                //if the block in the clicked direction is a sub block, then get the BE
                sub = subBlockBE;
            }
            else
            {
                //if isn't create one
                level.setBlockAndUpdate(relative, AmberCraft.Blocks.SUB_BLOCK.get().defaultBlockState());
                sub = (SubBlockBE) level.getBlockEntity(relative);
            }
        }
        if (sub == null) {
            AmberCraft.LOGGER.warn("TinyItem can't find or create the sub block!");
            return InteractionResult.FAIL;
        }


        //fixme, adding a black wool instead of the currect tiny block in the TinyItem data
        //sub.addTinyBlock(TinyBlockRegister.BLACK_WOOL.getHolder().get(), 0, 0, 0);
        System.out.println(context.getClickedPos().toShortString());
        System.out.println(context.getClickLocation().toString());//this method returns the world location that the player clicked!
        context.getClickedPos()
        return super.useOn(context);
    }
}
