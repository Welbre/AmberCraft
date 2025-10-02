package welbre.ambercraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.FaceBrain;
import welbre.ambercraft.module.network.NetworkModule;

@GameTestHolder(AmberCraft.MOD_ID)
public class FacedCableGameTest
{

    @PrefixGameTestTemplate(false)
    @GameTest(template = "faced_anchor")
    public static void faced_anchor_breaking(GameTestHelper helper)
    {
        if (helper.getBlockState(new BlockPos(0,1,0)).isAir())
            helper.fail("The faced block has been destroyed before the test started!");

        FacedCableBE be = helper.getBlockEntity(new BlockPos(0,1,0));
        if (be.getState().getFaceStatus(Direction.EAST) == null)
            helper.fail( "The east cable face is broken!" );
        if (be.getState().getFaceStatus(Direction.SOUTH) == null)
            helper.fail( "The south cable face is broken!" );

        //remove first anchor
        helper.setBlock(new BlockPos(0,1,1), Blocks.AIR);

        if (helper.getBlockState(new BlockPos(0,1,0)).isAir())
            helper.fail("The faced block has been destroyed after only one anchor was broken!");

        if (be.getState().getFaceStatus(Direction.SOUTH) != null)
            helper.fail( "The south anchor is broken and the cable still there!" );

        //remove last anchor
        helper.setBlock(new BlockPos(1,1,0), Blocks.AIR);

        if (!helper.getBlockState(new BlockPos(0,1,0)).isAir())
            helper.fail("The faced block still there after all anchors were broken!");
        if (helper.getEntities(EntityType.ITEM).size() != 2)
            helper.fail("Can't find the cable's itens after all anchors were broken!");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "faced_color_connection")
    public static void faced_color_connection(GameTestHelper helper)
    {
        helper.runAfterDelay(1, () ->
        {
            //check the yellow / purple
            TEST_SECTION(helper, new BlockPos(0,1,0), 10);
            //check the white
            TEST_SECTION(helper, new BlockPos(2,1,0), 5);
            //check the yellow / green
            TEST_SECTION(helper, new BlockPos(3,1,0), 10);

            //connect the 3 part.
            ((FacedCableBE) helper.getBlockEntity(new BlockPos(2,1,0))).toggleIgnoreColor(Direction.DOWN);

            //check if all the 3 part are connected
            TEST_SECTION(helper, new BlockPos(2,1,0), 25);

            helper.succeed();
        });
    }


    private static void TEST_SECTION(GameTestHelper helper, BlockPos pos, int expectedSize)
    {
        FacedCableBE cable = helper.getBlockEntity(pos);
        FaceBrain brain = cable.getBrain().getFaceBrain(Direction.DOWN);
        if (brain == null)
            helper.fail("The cable has no brain!");

        if (brain.modules().length == 0)
            helper.fail("The cable has no modules!");

        if (brain.modules()[0] instanceof NetworkModule networkModule)
        {
            int size = 0;
            for (var ignored : networkModule)
                size++;
            if (size != expectedSize)
                helper.fail("Expecting %d modules, but got %d!".formatted(expectedSize,size));
        }
        else
            helper.fail("The first module isn't a networkModule!");
    }
}
