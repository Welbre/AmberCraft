package welbre.ambercraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.CopperHeatConductorConductorBE;
import welbre.ambercraft.blockentity.HeatFurnaceBE;
import welbre.ambercraft.blocks.HeatFurnaceBlock;

import java.util.concurrent.atomic.AtomicReference;

@GameTestHolder(Main.MOD_ID)
public class AmberCondutorTest {

    @PrefixGameTestTemplate(false)
    @GameTest()
    public static void heat_conductor_connection_test(GameTestHelper helper)
    {
        helper.setBlock(
                0,1,0,
                Main.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                1,1,0,
                Main.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                2,1,0,
                Main.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                3,1,0,
                Main.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                4,1,0,
                Main.Blocks.HEAT_FURNACE_BLOCK.get().defaultBlockState().setValue(HeatFurnaceBlock.FACING, Direction.NORTH)
        );
        HeatFurnaceBE furnace = helper.getBlockEntity(new BlockPos(4,1,0));
        furnace.ignite();
        furnace.addPower();
        furnace.addPower();
        furnace.addPower();
        furnace.addPower();
        furnace.addPower();

        CopperHeatConductorConductorBE conductor = helper.getBlockEntity(new BlockPos(0,1,0));
        AtomicReference<Double> temperature = new AtomicReference<>(-1.0);
        helper.runAfterDelay(1,() -> {
            temperature.set(conductor.getHeatModule().getHeatNode().getTemperature());
        });
        helper.succeedWhen(() -> {
            if (conductor.getHeatModule().getHeatNode().getTemperature() - temperature.get() > 100)
                helper.succeed();
            throw new GameTestAssertException("");
        });
    }
}
