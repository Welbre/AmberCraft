package welbre.ambercraft.client.screen;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.network.ModifyFieldsPayLoad;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ModifyFieldsScreen extends Screen {
    final BlockEntity entity;
    final ArrayList<Field> fieldList;
    final ArrayList<Class<?>> types;
    final ArrayList<EditBox> boxes;

    public ModifyFieldsScreen(FriendlyByteBuf buf) {
        super(Component.literal("AmberCraft BE Modify"));
        entity = Minecraft.getInstance().level.getBlockEntity(buf.readBlockPos());
        assert entity != null;

        final int size = buf.readInt();
        fieldList = new ArrayList<>(size);
        types = new ArrayList<>(size);
        boxes = new ArrayList<>(size);
        
        for (int i = 0; i < size; i++)
        {
            final int length = buf.readInt();
            final byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            String name = new String(bytes);
            try
            {
                Field field = entity.getClass().getField(name);
                if (!field.getType().isPrimitive())
                    throw new IllegalArgumentException("Field %s, %s isn't a primitive type!".formatted(field.getName(), field.getClass().getName()));
                else
                    types.add(field.getType());
                fieldList.add(field);
            } catch (NoSuchFieldException e)
            {
                AmberCraft.LOGGER.error("Fail while loading a field!", e);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < fieldList.size(); i++)
        {
            Field field = fieldList.get(i);
            var box = createBox(0,i*25, field);
            this.addRenderableWidget(box);
            this.boxes.add(box);
        }
    }

    @Override
    public void onClose() {
        final ArrayList<Object> values = new ArrayList<>(fieldList.size());
        for (int i = 0; i < fieldList.size(); i++)
        {
            Class<?> type = types.get(i);
            Object apply = getParser(type).apply(boxes.get(i).getValue());
            if (apply == null)
                throw new IllegalArgumentException("Fail while trying to parse a value!");
            values.add(apply);
        }
        PacketDistributor.sendToServer(new ModifyFieldsPayLoad(UpdateAmberSecureKeyPayload.CLIENT_KEY, entity.getClass(), fieldList, values));
        super.onClose();
    }

    public static FriendlyByteBuf GET_BUFFER(BlockPos pos, String... fields)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);

        buf.writeInt(fields.length);
        for (String field : fields)
        {
            buf.writeInt(field.length());
            buf.writeBytes(field.getBytes());
        }
        return buf;
    }
    private BiConsumer<EditBox, String> getReponder(Class<?> type){
        return (box,string) -> {
            try
            {
                getParser(type).apply(string);
                box.setTextColor(DyeColor.LIME.getTextColor());
            } catch (Exception e)
            {
                box.setTextColor(DyeColor.RED.getTextColor());
            }
        };
    }

    private Function<String, ?> getParser(Class<?> type) {
        if (type == double.class)
            return Double::parseDouble;
        else if (type == float.class)
            return Float::parseFloat;
        else if (type == int.class)
            return Integer::parseInt;
        else if (type == long.class)
            return Long::parseLong;
        else if (type == short.class)
            return Short::parseShort;
        else if (type == byte.class)
            return Byte::parseByte;
        else if (type == boolean.class)
            return Boolean::parseBoolean;
        else if (type == char.class)
            return s -> s.charAt(0);
        else
            return a -> null;
    }

    private void setEditBoxValue(EditBox box, Field field) {
        try
        {
            box.setValue(String.valueOf(field.get(entity)));
        } catch (Exception e)
        {
            AmberCraft.LOGGER.error("Fail while trying to set a field value!", e);
        }
    }


    private EditBox createBox(int x, int y, Field field) {
        var box = new EditBox(this.font, x, y, 200, 20, Component.empty());
        box.setFocused(false);
        BiConsumer<EditBox, String> reponder = getReponder(field.getType());
        box.setResponder(string -> reponder.accept(box, string));
        setEditBoxValue(box, field);
        return box;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);
        System.out.println("focus changed!");
    }
}
