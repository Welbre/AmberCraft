package welbre.ambercraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.CopperHeatConductorBE;
import welbre.ambercraft.blockentity.HeatConductorBE;
import welbre.ambercraft.blockentity.HeatFurnaceBE;
import welbre.ambercraft.blockentity.HeatSinkBE;
import welbre.ambercraft.blocks.HeatFurnaceBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@GameTestHolder(AmberCraft.MOD_ID)
public class AmberHeatCondutorTest {

    @PrefixGameTestTemplate(false)
    @GameTest()
    public static void heat_conductor_connection_test(GameTestHelper helper)
    {
        helper.setBlock(
                0,1,0,
                AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                1,1,0,
                AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                2,1,0,
                AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                3,1,0,
                AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get()
        );
        helper.setBlock(
                4,1,0,
                AmberCraft.Blocks.HEAT_FURNACE_BLOCK.get().defaultBlockState().setValue(HeatFurnaceBlock.FACING, Direction.NORTH)
        );
        HeatFurnaceBE furnace = helper.getBlockEntity(new BlockPos(4,1,0));
        furnace.ignite();
        furnace.ignite();
        furnace.ignite();
        furnace.addPower();
        furnace.addPower();
        furnace.addPower();
        furnace.addPower();
        furnace.addPower();

        CopperHeatConductorBE conductor = helper.getBlockEntity(new BlockPos(0,1,0));
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

    public static final List<BlockPos> heat_conductor_breaking_test_conductor_list = List.of(
            new BlockPos(4,1,0),
            new BlockPos(3,1,0),
            new BlockPos(2,1,0),
            new BlockPos(1,1,0),
            new BlockPos(0,1,1),
            new BlockPos(0,1,2),
            new BlockPos(1,1,2),
            new BlockPos(2,1,2),
            new BlockPos(2,1,3)
    );

    @PrefixGameTestTemplate(false)
    @GameTestGenerator
    public static Collection<TestFunction> heat_conductor_breaking_test_generator() {
        ArrayList<TestFunction> list = new ArrayList<>(heat_conductor_breaking_test_conductor_list.size());
        int index = 0;
        for (BlockPos breakPos : heat_conductor_breaking_test_conductor_list)
        {
            var fun = new TestFunction(
                    "defaultBatch",
                    "heat_conductor_breaking_test"+index++,
                    "ambercraft:heat_conductor_breaking_test",
                    Rotation.NONE,
                    100,
                    0,
                    true,
                    false,
                    1,
                    1,
                    false,
                    (helper) -> AmberHeatCondutorTest.heat_conductor_breaking_test(helper,breakPos)
            );
            list.add(fun);
        }

        return list;
    }

    public static void heat_conductor_breaking_test(GameTestHelper helper, BlockPos breakPos)
    {
        HeatSinkBE heatSinkBE = helper.getBlockEntity(new BlockPos(2,2,3));
        final double temperature = heatSinkBE.getHeatModule().getHeatNode().getTemperature();
        helper.setBlock(breakPos, Blocks.AIR);
        helper.succeedWhen(() -> {
            if (heatSinkBE.getHeatModule().getHeatNode().getTemperature() < temperature * 0.95f)
                helper.succeed();
            throw new GameTestAssertException("");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTestGenerator
    public static Collection<TestFunction> heat_conductor_replace_test_generator()
    {
        ArrayList<TestFunction> list = new ArrayList<>(heat_conductor_breaking_test_conductor_list.size());
        int index = 0;
        for (BlockPos breakPos : heat_conductor_breaking_test_conductor_list)
        {
            var fun = new TestFunction(
                    "defaultBatch",
                    "heat_conductor_replace_test"+index++,
                    "ambercraft:heat_conductor_breaking_test",
                    Rotation.NONE,
                    250,
                    0,
                    true,
                    false,
                    1,
                    1,
                    false,
                    (helper) -> AmberHeatCondutorTest.heat_conductor_replace_test(helper, breakPos)
            );
            list.add(fun);
        }

        return list;
    }

    public static void heat_conductor_replace_test(GameTestHelper helper, BlockPos pos)
    {
        AtomicReference<Boolean> reachCoolDown = new AtomicReference<>(false);

        HeatSinkBE heatSinkBE = helper.getBlockEntity(new BlockPos(2,2,3));
        HeatFurnaceBE furnace = helper.getBlockEntity(new BlockPos(4,2,0));
        final double temperature = heatSinkBE.getHeatModule().getHeatNode().getTemperature();

        helper.setBlock(pos, Blocks.AIR);//remove the conductor

        //check each tick if the temperature is 95% of the original temperature. place a golden heat conductor.
        helper.onEachTick(() -> {
            if (heatSinkBE.getHeatModule().getHeatNode().getTemperature() < temperature * 0.95f)
                if (reachCoolDown.get() == false)
                {
                    reachCoolDown.set(true);
                    helper.setBlock(pos, AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get());
                    HeatConductorBE conductorBE = helper.getBlockEntity(pos);
                    conductorBE.getHeatModule().getHeatNode().setTemperature(temperature);
                    furnace.addPower();
                    furnace.addPower();
                    furnace.addPower();
                    furnace.addPower();
                    furnace.addPower();
                    furnace.addPower();
                    furnace.ignite();
                }
        });

        //if the temperature gets at 95% of the original and reach the 100% again, success.
        helper.succeedWhen(() -> {
            System.out.println(heatSinkBE.getHeatModule().getHeatNode().getTemperature());
            if (reachCoolDown.get() && heatSinkBE.getHeatModule().getHeatNode().getTemperature() >= temperature * 0.95)
                helper.succeed();
            throw new GameTestAssertException("");
        });
    }

}
