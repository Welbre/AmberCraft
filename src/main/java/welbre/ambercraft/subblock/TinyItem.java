package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
        ItemStack stack = context.getItemInHand();
        TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT);
        if (component == null)
        {
            if (context.getPlayer() != null)
                //if the stack hasn't a tiny block component, the stack is in a broken state, so remove it from the player.
                context.getPlayer().setItemInHand(context.getHand(), new ItemStack(Items.AIR));
            return InteractionResult.FAIL;
        }

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

        //value between 0 and 1
        Vec3 r = context.getClickLocation().subtract(new Vec3(context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()).add(context.getClickedFace().getUnitVec3()));

        final int x,y,z;
        //if some value in r == 1, then the grid algorithm will return 0 at that coordinate, so multiply by 0.999f to 0.9999f * 16 != 16
        if (context.getClickedFace().getUnitVec3().x < 0 || context.getClickedFace().getUnitVec3().y < 0 || context.getClickedFace().getUnitVec3().z < 0)
            r = r.multiply(0.999f, 0.999f, 0.999f);

        x = (int) (r.x * 16) % 16; y = (int) (r.y * 16) % 16; z = (int) (r.z * 16) % 16;

        sub.addTinyBlock(component.get(), x, y, z);
        return super.useOn(context);
    }
}
