package welbre.ambercraft.client.screen;

import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.sun.jna.platform.KeyboardUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforgespi.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
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
            var box = createBox(this.width / 2 - 100, i * 25, field);
            this.addRenderableWidget(box);
            var name = new StringWidget(Component.literal(field.getName()).append(":").append(Component.literal(field.getType().getName()).withColor(0xFFCF8E6D)), this.font);
            name.alignLeft();
            name.setPosition(this.width / 2 - 110 - name.getWidth(), i * 25 + 5);
            var bg = new Renderable(){
                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    guiGraphics.fill(name.getX()-5, name.getY()-5, name.getX() + name.getWidth()+5, name.getY() + name.getHeight()+5, 0xcc787878);
                }
            };
            this.addRenderableOnly(bg);
            this.boxes.add(box);
            this.addRenderableOnly(name);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER)
            this.setFocused(null);
        else if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            if (this.getFocused() != null && this.getFocused() instanceof EditBox box)
            {
                int i = boxes.indexOf(box);
                if (i + 1 < boxes.size())
                    this.setFocused(boxes.get(i + 1));
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
}
