package welbre.ambercraft.gametest;

import kuse.welbre.sim.electrical.abstractt.Element;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.Blocks.*;

@GameTestHolder(AmberCraft.MOD_ID)
public class CircuitTest
{
    public static final int NUMBER_OF_RUNS = 30;
    public static boolean SHOULD_DISPLAY_SCHEDULER = false;
    public static final Block STRUCTURE_BLOCK = QUARTZ_BRICKS;
    public static final List<Block> TEST_BLOCKS = List.of(REDSTONE_BLOCK, EMERALD_BLOCK, DIAMOND_BLOCK, COPPER_BLOCK, IRON_BLOCK, LAPIS_BLOCK, AMETHYST_BLOCK, NETHERITE_BLOCK);

    @PrefixGameTestTemplate(false)
    @GameTest(template = "electrical/voltage_source")
    public static void electrical_vs(GameTestHelper helper)
    {
        RUN_ELEMENT_TEST(
                GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                CURRENT_ABS_CHECK(20),
                CURRENT_ABS_CRASHER(20),
                helper);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "electrical/voltage_source_and_resistor")
    public static void electrical_vs_and_resistor(GameTestHelper helper)
    {
        RUN_ELEMENT_TEST(
                GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                CURRENT_ABS_CHECK(5),
                CURRENT_ABS_CRASHER(5),
                helper);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "electrical/voltage_source_and_capacitor", timeoutTicks = 300)
    public static void electrical_vs_and_capacitor(GameTestHelper helper)
    {
        RUN_ELEMENT_TEST(6, helper,
                new ElectricalCircuitTestHelper<>(
                        0,
                        GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                        CURRENT_ABS_BIGGER_THAT_CHECK(20),
                        CURRENT_ABS_BIGGER_THAT_CRASHER(20)
                        ),
                new ElectricalCircuitTestHelper<>(
                        5,
                        GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                        CURRENT_ABS_CHECK(0),
                        CURRENT_ABS_CRASHER(0)
                    ),
                new ElectricalCircuitTestHelper<>(
                        3,
                        GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                        CURRENT_ABS_BETWEEN_CHECK(0, 20),
                        CURRENT_ABS_BETWEEN_CRASHER(0, 20)
                )
                );
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "electrical/voltage_source_and_inductor", timeoutTicks = 300)
    public static void electrical_vs_and_indutor(GameTestHelper helper)
    {
        RUN_ELEMENT_TEST(6, helper,
                new ElectricalCircuitTestHelper<>(
                        0,
                        GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                        CURRENT_ABS_BIGGER_THAT_CHECK(0),
                        CURRENT_ABS_BIGGER_THAT_CRASHER(0)
                ),
                new ElectricalCircuitTestHelper<>(
                        5,
                        GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                        CURRENT_ABS_CHECK(25),
                        CURRENT_ABS_CRASHER(25)
                ),
                new ElectricalCircuitTestHelper<>(
                        3,
                        GET_SUPPLIER_FROM_BE(helper, new BlockPos(1,1,1)),
                        CURRENT_ABS_BETWEEN_CHECK(0, 25),
                        CURRENT_ABS_BETWEEN_CRASHER(0, 25)
                )
        );
    }

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------utils--------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    public static <T extends Element> void RUN_ELEMENT_TEST(Supplier<T> getter, Function<T, Boolean> test, Function<T, String> crasher, GameTestHelper helper)
    {
        RUN_ELEMENT_TEST(1, helper, new ElectricalCircuitTestHelper<>(0, getter, test, crasher));
    }

    public static <T extends Element> void RUN_ELEMENT_TEST(int testDuration, Supplier<T> getter, Function<T, Boolean> test, Function<T, String> crasher, GameTestHelper helper)
    {
        RUN_ELEMENT_TEST(testDuration, helper, new ElectricalCircuitTestHelper<>(0, getter, test, crasher));
    }

    /**
     * @param tests A collection of tests that will be executed.
     * @param testDuration The time that the code will wait after the structure be placed.
     * @param helper The game helper.
     * @param <T> A generic type that can be anything
     */
    public static <T> void RUN_ELEMENT_TEST(final long testDuration, GameTestHelper helper, ElectricalCircuitTestHelper<?> ...tests)
    {
        //argument checking, (test duration + 1 "from the set-structure phase") * (Number_of_runs + 1 "wait to test it to complete in the last run") + 5 "only for safety"
        final long successAfter = (testDuration + 1) * (NUMBER_OF_RUNS+1) + 5;
        if (helper.testInfo.getTestFunction().maxTicks() < successAfter)
            helper.fail("The test function has not enough time to complete! Min time " + successAfter);
        if (testDuration < 1)
            helper.fail("The test duration must be at least 1 ticks!");
        List<Exception> exceptions = new ArrayList<>();
        for (int i = 0; i < tests.length; i++)
            if (tests[i].delayToRun() >= testDuration)
                exceptions.add(new IllegalArgumentException("The test #%d delay must be less that %d ticks!".formatted(i, testDuration)));
        if (!exceptions.isEmpty())
        {
            StringBuilder msg = new StringBuilder("The following tests have invalid delay:\n");
            exceptions.forEach(e -> msg.append(e.getMessage()).append("\n"));
            helper.fail(msg.toString());
        }

        //the test
        for (int i = 0; i < NUMBER_OF_RUNS; i++)
        {
            HANDLE_SCHEDULER_DRAW(i, testDuration, helper, tests);
            final int _i = (int) ( (testDuration +1) * i + 1);
            final int finalI = i;
            //schedule one tick later to avoid the game test place the structure and this part of the code replace it in the same tick.
            helper.runAfterDelay(_i, () -> replaceStructure(helper));
            Stream.of(tests).forEach(s -> s.scheduler(helper, finalI, _i+1 ));
        }
        helper.runAfterDelay(successAfter, helper::succeed);
    }

    public static Supplier<Element> GET_SUPPLIER_FROM_BE(GameTestHelper helper, BlockPos pos)
    {
        return () -> ((ElectricalBE) helper.getBlockEntity(pos)).getElectricalModule().getElement();
    }

    public static Function<Element,Boolean> CURRENT_ABS_CHECK(double expected)
    {
        return element -> computeError(Math.abs(element.getCurrent()), expected) < 0.01;//1% of error
    }

    public static Function<Element,Boolean> CURRENT_ABS_BIGGER_THAT_CHECK(double expected)
    {
        return element -> Math.abs(element.getCurrent()) > expected;
    }

    public static Function<Element,Boolean> CURRENT_ABS_SMALLER_THAT_CHECK(double expected)
    {
        return element -> Math.abs(element.getCurrent()) < expected;
    }

    public static Function<Element,Boolean> CURRENT_ABS_BETWEEN_CHECK(double min, double max)
    {
        return element -> (Math.abs(element.getCurrent()) <= max) && (Math.abs(element.getCurrent()) >= min);
    }

    public static Function<Element,String> CURRENT_ABS_CRASHER(double expected)
    {
        return element -> "Expected current to be %fA, but got %fA!".formatted(expected, Math.abs(element.getCurrent()));
    }

    public static Function<Element,String> CURRENT_ABS_BIGGER_THAT_CRASHER(double expected)
    {
        return element -> "Expected current to be bigger that %fA, but got %fA!".formatted(expected, Math.abs(element.getCurrent()));
    }

    public static Function<Element,String> CURRENT_ABS_SMALLER_THAT_CRASHER(double expected)
    {
        return element -> "Expected current to be smaller that %fA, but got %fA!".formatted(expected, Math.abs(element.getCurrent()));
    }

    public static Function<Element,String> CURRENT_ABS_BETWEEN_CRASHER(double min, double max)
    {
        return element -> "Expected current to be between %fA and %fA, but got %fA!".formatted(min, max, Math.abs(element.getCurrent()));
    }

    public static double computeError(double got, double expected)
    {
        if (expected != 0)
            return Math.abs(got - expected) / expected;
        else
            return Math.abs(got);
    }

    public static void replaceStructure(GameTestHelper helper)
    {
        final BlockPos reference = helper.testInfo.getStructureBlockPos();
        AABB bounds = helper.getBounds();
        for (int x = (int) bounds.minX; x  < bounds.maxX; x++)
            for (int y = (int) bounds.minY; y  < bounds.maxY; y++)
                for (int z = (int) bounds.minZ; z  < bounds.maxZ; z++)
                    helper.destroyBlock(new BlockPos(x,y-1,z).subtract(reference));

        var structure = helper.testInfo.getStructureBlockEntity();
        structure.placeStructure(helper.getLevel());
        BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(structure);
        helper.getLevel().getBlockTicks().clearArea(boundingbox);
        helper.getLevel().clearBlockEvents(boundingbox);
    }
    public static void HANDLE_SCHEDULER_DRAW(int i, long testDuration, GameTestHelper helper, ElectricalCircuitTestHelper<?>[] tests)
    {
        if (!SHOULD_DISPLAY_SCHEDULER) return;

        final long finalDuration = testDuration +1;//add the time to the structure be placed

        //structure
        helper.runAfterDelay(finalDuration*i+1, () -> helper.setBlock(new BlockPos((int) -(finalDuration*i+1),11,0), STRUCTURE_BLOCK));
        //tasks
        for (int j = 0; j < tests.length; j++)
        {
            ElectricalCircuitTestHelper<?> s = tests[j];

            final int finalDelay = (int) (finalDuration * i + 2 + s.delayToRun());
            final int _j = j;
            final Block block = TEST_BLOCKS.get(_j % TEST_BLOCKS.size());

            helper.runAfterDelay(finalDelay, () -> helper.setBlock(new BlockPos(-finalDelay, 10 + _j+2, 0), block));
        }

        final long successAfter = finalDuration * (NUMBER_OF_RUNS+1) + 5;
        for (int j = 0; j < successAfter; j++)
        {
            helper.setBlock(new BlockPos(-j,10,0), GOLD_BLOCK);
            for (int y = 0; y < tests.length+1; y++)
            {
                helper.setBlock(new BlockPos(-j,10+y+1,0), AIR);
            }
        }
    }
}
