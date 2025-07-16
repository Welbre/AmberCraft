package welbre.ambercraft.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.Main;

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
    }
}
