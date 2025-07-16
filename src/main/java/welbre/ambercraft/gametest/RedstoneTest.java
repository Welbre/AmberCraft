package welbre.ambercraft.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import welbre.ambercraft.Main;

@GameTestHolder(Main.MOD_ID)
public class RedstoneTest {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "custom_redstone_lamp_template")
    public static void customRedstoneLampTest(GameTestHelper helper)
    {

    }
}
