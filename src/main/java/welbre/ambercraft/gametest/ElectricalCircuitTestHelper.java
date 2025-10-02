package welbre.ambercraft.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Function;
import java.util.function.Supplier;

public record ElectricalCircuitTestHelper<T>(
        long delayToRun,
        Supplier<T> getter,
        Function<T, Boolean> test,
        Function<T, String> crasher
)
{
    public void scheduler(GameTestHelper helper, int index, long placeDelay)
    {
        helper.runAfterDelay(
                placeDelay + delayToRun,
                () -> {
                    T obj = getter.get();

                    final boolean _success = test.apply(obj);

                    if (!_success)
                        helper.fail("Fail in %s while running the %dth of %d, %s".formatted(helper.testInfo.getTestName(), index +1 , CircuitTest.NUMBER_OF_RUNS, crasher.apply(obj)));
                }
        );
    }
}
