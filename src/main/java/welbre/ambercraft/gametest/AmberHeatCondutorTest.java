package welbre.ambercraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.heat.HeatBE;
import welbre.ambercraft.blockentity.heat.HeatFurnaceBE;
import welbre.ambercraft.blockentity.heat.HeatSinkBE;
import welbre.ambercraft.module.heat.HeatModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@GameTestHolder(AmberCraft.MOD_ID)
public class AmberHeatCondutorTest {

    public static final List<BlockPos> heat_conductor_breaking_test_conductor_list = List.of(
            new BlockPos(3,1,0),
            new BlockPos(2,1,0),
            new BlockPos(1,1,0),
            new BlockPos(0,1,0),
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
                    "ambercraft:heat_source_and_heat_sink",
                    Rotation.NONE,
                    200,
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
            if (Math.abs(heatSinkBE.getHeatModule().getHeatNode().getTemperature()) > 10_000)
                helper.fail("The temperature is too high!");
            else if (heatSinkBE.getHeatModule().getHeatNode().getTemperature() < temperature * 0.95f)
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
                    "ambercraft:heat_source_and_heat_sink",
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
        HeatFurnaceBE furnace = helper.getBlockEntity(new BlockPos(4,1,0));
        final double temperature = heatSinkBE.getHeatModule().getHeatNode().getTemperature();

        helper.setBlock(pos, Blocks.AIR);//remove the conductor

        //check each tick if the temperature is 95% of the original temperature. place father golden heat conductor.
        helper.onEachTick(() -> {
            if (heatSinkBE.getHeatModule().getHeatNode().getTemperature() < temperature * 0.95f)
                if (reachCoolDown.get() == false)
                {
                    reachCoolDown.set(true);
                    helper.setBlock(pos, AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get());
                    HeatBE conductorBE = helper.getBlockEntity(pos);
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
            if (Math.abs(heatSinkBE.getHeatModule().getHeatNode().getTemperature()) > 10_000)
                helper.fail("The temperature is too high!");
            else if (reachCoolDown.get() && heatSinkBE.getHeatModule().getHeatNode().getTemperature() >= temperature * 0.95)
                helper.succeed();

            throw new GameTestAssertException("");
        });
    }


    private static final List<BlockPos> heat_conductor_short_cyclical_connection_list = List.of(
            new BlockPos(0,1,0),new BlockPos(1,1,0),new BlockPos(1,1,1),new BlockPos(0,1,1)
    );
    private static final List<BlockPos> heat_conductor_long_cyclical_connection_list = List.of(
            new BlockPos(0,1,0),new BlockPos(1,1,0), new BlockPos(2,1,0),new BlockPos(2,1,1),new BlockPos(2,1,2),new BlockPos(1,1,2), new BlockPos(0,1,2), new BlockPos(0,1,1)
    );
    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty")
    public static void heat_conductor_short_cyclical_connection(GameTestHelper helper)
    {
        for (int i = 0; i < 30; i++)
            helper.runAfterDelay(i * 3, () -> testCyclicalConnection(helper, heat_conductor_short_cyclical_connection_list, 8));

        helper.runAfterDelay(30*3+1, () -> setCyclicalConnectionBlocks(helper, heat_conductor_short_cyclical_connection_list, AmberCraft.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get()) );
        helper.runAfterDelay(30*3+2, helper::succeed);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty")
    public static void heat_conductor_long_cyclical_connection(GameTestHelper helper)
    {
        for (int i = 0; i < 30; i++)
            helper.runAfterDelay(i * 3, () -> testCyclicalConnection(helper, heat_conductor_long_cyclical_connection_list,16));

        helper.runAfterDelay(30*3+1, () -> setCyclicalConnectionBlocks(helper, heat_conductor_long_cyclical_connection_list, AmberCraft.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get()) );
        helper.runAfterDelay(30*3+2, helper::succeed);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty")
    public static void heat_conductor_4x5x5_cyclical_connection(GameTestHelper helper)
    {
        ArrayList<BlockPos> list = new ArrayList<>(25);
        for (int x = 0; x < 5; x++)
            for (int y = 1; y < 5; y++)
                for (int z = 0; z < 5; z++)
                    list.add(new BlockPos(x,y,z));

        for (int i = 0; i < 30; i++)
            helper.runAfterDelay(i * 3, () -> testCyclicalConnection(helper,list,470));

        helper.runAfterDelay(30*3+1, () -> setCyclicalConnectionBlocks(helper, list, AmberCraft.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get()) );
        helper.runAfterDelay(30*3+2, helper::succeed);
    }

    private static void testCyclicalConnection(GameTestHelper helper, List<BlockPos> posList, int expected){
        setCyclicalConnectionBlocks(helper, posList, AmberCraft.Blocks.CREATIVE_HEAT_CONDUCTOR_BLOCK.get());
        HeatModule root = null;
        for (BlockPos pos : posList)
        {
            HeatBE conductor = helper.getBlockEntity(pos);
            HeatModule module = conductor.getHeatModule();

            var exceptions = module.checkInconsistencies();
            if (exceptions != null)
                for (Exception e : exceptions)
                    helper.fail(e.getMessage());

            if (root != null)
            {
                if (root != module.getRoot())
                    helper.fail("The nodes don't agree about the root!");
            }
            else
                root = (HeatModule) module.getRoot();
        }

        assert root != null;
        //compute the connection amount.
        int connections = 0;
        for (var m : root)
            connections += m.getNeighbors().length;

        if (connections != expected)
            helper.fail("Expecting %d connections, but got %d!".formatted(expected,connections));

        helper.runAfterDelay(2, () -> {
            for (BlockPos pos : posList)
            {
                HeatBE conductor = helper.getBlockEntity(pos);//check if the block entity still there.
                var error = conductor.getHeatModule().checkInconsistencies();
                if (error != null)
                    for (Exception e : error)
                        helper.fail(e.getMessage(), conductor.getBlockPos());
            }

            setCyclicalConnectionBlocks(helper, posList, Blocks.AIR);
        });
    }

    private static void setCyclicalConnectionBlocks(GameTestHelper helper, List<BlockPos> posList, Block block)
    {
        ArrayList<BlockPos> list = new ArrayList<>(posList);

        while (!list.isEmpty())
        {
            var next = list.remove(new Random().nextInt(list.size()));
            helper.setBlock(next, block);
        }
    }
}
