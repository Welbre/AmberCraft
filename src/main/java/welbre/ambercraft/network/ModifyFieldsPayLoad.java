package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static welbre.ambercraft.network.UpdateAmberSecureKeyPayload.*;

public record ModifyFieldsPayLoad(UUID secureKey, Class<?> beClass, List<Field> fields, List<Object> values) implements CustomPacketPayload {

    public void handleOnServer(IPayloadContext context) {
        if (secureKey == null || beClass == null || fields == null || values == null)
            return;

        if (!context.player().hasPermissions(2))
            return;

        String name = context.player().getName().getString();
        UUID uuid = SECURE_KEY_MAP.get(name);
        if (uuid == null)//check if the server registers a UUID key in the map, to avoid hacker use this package to modify BlockEntity from the client.
            return;
        SECURE_KEY_MAP.remove(name);

        BlockPos pos = BLOCK_POS_MAP.get(name);
        if (pos == null)
            return;
        BLOCK_POS_MAP.remove(name);

        Class<?> entityClass = BLOCK_ENTITY_CLASS_MAP.get(name);
        if (entityClass == null || entityClass != beClass)
            return;
        BLOCK_ENTITY_CLASS_MAP.remove(name);

        BlockEntity entity = context.player().level().getBlockEntity(pos);
        if (entity == null || entity.getClass() != beClass)
            return;

        if (fields.size() != values.size())
            return;

        try
        {
            final int size = fields.size();
            for (int i = 0; i < size; i++)
            {
                Field field = fields.get(i);
                Object object = values.get(i);

                if (field != null && object != null)
                {
                    if (!field.getType().isPrimitive())
                        return;
                    field.set(entity, object);
                }
            }
            entity.setChanged();
            context.player().level().sendBlockUpdated(pos, entity.getBlockState(), entity.getBlockState(), 3);
        } catch (Exception ignored) {}
    }

    public static final StreamCodec<ByteBuf, ModifyFieldsPayLoad> STREAM_CODEC = StreamCodec.ofMember(ModifyFieldsPayLoad::encode, ModifyFieldsPayLoad::decode);

    //Only runs in the server, so the MAPS are available for use.
    private static @NotNull ModifyFieldsPayLoad decode(ByteBuf byteBuf)
    {
        try
        {
            UUID secureKey = UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(byteBuf));
            Class<?> entityClass = Class.forName(ByteBufCodecs.STRING_UTF8.decode(byteBuf));

            final int fieldsSize = byteBuf.readInt();
            if (fieldsSize <= 0 || fieldsSize > 50)
                throw new IllegalArgumentException("Invalid field size!");
            List<Field> fields = new ArrayList<>(fieldsSize);
            for (int i = 0; i < fieldsSize; i++)
                fields.add(entityClass.getField(ByteBufCodecs.STRING_UTF8.decode(byteBuf)));

            final int valuesSize = byteBuf.readInt();
            if (valuesSize <= 0 || valuesSize > 50)
                throw  new IllegalArgumentException("Invalid values size!");
            List<Object> values = new ArrayList<>(valuesSize);
            for (int i = 0; i < valuesSize; i++)
                values.add(parser(fields.get(i).getType(), ByteBufCodecs.STRING_UTF8.decode(byteBuf)));

            return new ModifyFieldsPayLoad(secureKey, entityClass, fields, values);
        } catch (Exception ignored) {}//avoid prints in the stderr.
        return new ModifyFieldsPayLoad(null, null, null, null);
    }

    private void encode(ByteBuf byteBuf) {
        ByteBufCodecs.STRING_UTF8.encode(byteBuf, secureKey.toString());
        ByteBufCodecs.STRING_UTF8.encode(byteBuf, beClass.getName());

        byteBuf.writeInt(fields.size());
        for (Field field : fields)
            ByteBufCodecs.STRING_UTF8.encode(byteBuf, field.getName());

        byteBuf.writeInt(values.size());
        for (Object value : values)
            ByteBufCodecs.STRING_UTF8.encode(byteBuf, value.toString());
    }

    private static Object parser(Class<?> type, String s) {
        if (type == double.class)
            return Double.parseDouble(s);
        else if (type == float.class)
            return Float.parseFloat(s);
        else if (type == int.class)
            return Integer.parseInt(s);
        else if (type == long.class)
            return Long.parseLong(s);
        else if (type == short.class)
            return Short.parseShort(s);
        else if (type == byte.class)
            return Byte.parseByte(s);
        else if (type == boolean.class)
            return Boolean.parseBoolean(s);
        else if (type == char.class)
            return s.charAt(0);
        else
            return null;
    }

    public static final Type<ModifyFieldsPayLoad> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "modify_fields_payload"));
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
