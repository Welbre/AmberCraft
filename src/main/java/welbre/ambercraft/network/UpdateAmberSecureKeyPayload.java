package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;

import java.util.HashMap;
import java.util.UUID;

/**
 * This payload updates the client security key, a special key used in the ambercraft to ensure payload fidelity. Is used only in critical cases.<br>
 * This only modifies the {@link UpdateAmberSecureKeyPayload#CLIENT_KEY} field.
 * Is crucial to {@link ModifyFieldsPayLoad}, any client that send this payload can be able to modify and BlockEntity in the level, or crash the server.<br>
 * @param uuid the string of the uuid to be updated.
 */
public record UpdateAmberSecureKeyPayload(String uuid) implements CustomPacketPayload {
    public static final HashMap<String, UUID> SECURE_KEY_MAP = new HashMap<>();
    public static final HashMap<String, BlockPos> BLOCK_POS_MAP = new HashMap<>();
    public static final HashMap<String, Class<?>> BLOCK_ENTITY_CLASS_MAP = new HashMap<>();


    public static UUID CLIENT_KEY = null;

    public void handleOnClient(IPayloadContext ignoredContext)
    {
        CLIENT_KEY = UUID.fromString(uuid);
    }

    /// Assigns an uuid to a player at a block of blockEntityClass.
    public static void ADD_NEW_KEY(UUID uuid, BlockPos pos, Class<? extends BlockEntity> aClass, ServerPlayer player) {
        var name = player.getName().getString();
        SECURE_KEY_MAP.put(name, uuid);
        BLOCK_POS_MAP.put(name, pos);
        BLOCK_ENTITY_CLASS_MAP.put(name, aClass);
        PacketDistributor.sendToPlayer(player, new UpdateAmberSecureKeyPayload(uuid.toString()));
    }

    public static @Nullable BlockPos validadeKey(String playerName, Class<?> beClass, LevelAccessor level)
    {
        if (playerName == null || beClass == null || level == null)
            return null;

        UUID uuid = SECURE_KEY_MAP.get(playerName);
        if (uuid == null)//check if the server registers a UUID key in the map, to avoid hacker use this package to modify BlockEntity from the client.
            return null;
        SECURE_KEY_MAP.remove(playerName);

        BlockPos pos = BLOCK_POS_MAP.get(playerName);
        if (pos == null)
            return null;
        BLOCK_POS_MAP.remove(playerName);

        Class<?> entityClass = BLOCK_ENTITY_CLASS_MAP.get(playerName);
        if (entityClass == null || entityClass != beClass)
            return null;
        BLOCK_ENTITY_CLASS_MAP.remove(playerName);

        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null || entity.getClass() != beClass)
            return null;

        return pos;
    }

    public static final CustomPacketPayload.Type<UpdateAmberSecureKeyPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "update_amber_secure_key"));

    public static final StreamCodec<ByteBuf, UpdateAmberSecureKeyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UpdateAmberSecureKeyPayload::uuid,
            UpdateAmberSecureKeyPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
