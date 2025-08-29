package welbre.ambercraft.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import welbre.ambercraft.blockentity.HeatSourceBE;
import welbre.ambercraft.network.HeatSourceSetterPayload;


import static welbre.ambercraft.blockentity.HeatSourceBE.Mode;

@OnlyIn(Dist.CLIENT)
public class HeatSourceScreen extends Screen{
    public EditBox input;
    public CycleButton<Mode> modeButton;
    public Button doneButton;
    public StringWidget invalid;

    public Mode mode;
    public final HeatSourceBE entity;

    public HeatSourceScreen(FriendlyByteBuf buf) {
        super(Component.literal("Heat source"));
        this.entity = (HeatSourceBE) Minecraft.getInstance().level.getBlockEntity(buf.readBlockPos());
        this.mode = entity.mode;
    }

    @Override
    protected void init() {
        super.init();
        this.invalid = new StringWidget(this.width / 2 -100, 80, 100, 20, Component.literal("Invalid number!"), this.font);


        this.input = new EditBox(this.font, this.width / 2 - 100, 100, 200, 20, Component.literal("Temperature"));
        this.input.setMaxLength(10);
        //this.input.setFilter();
        this.input.setValue(entity.mode == Mode.TEMPERATURE ? String.valueOf(entity.temperature) : String.valueOf(entity.heat));
        this.input.setTextColor(DyeColor.LIME.getTextColor());
        this.input.setResponder(this::dynamicResponder);
        this.modeButton = CycleButton.builder(this::cycleButton).withValues(Mode.values()).withInitialValue(mode).create(this.width / 2 - 100, 130, 200, 20, Component.literal("Mode"), this::cycleButtonClick);
        this.doneButton = Button.builder(Component.literal("ok"), this::done).bounds(this.width/2 + 100, 100, 20, 20).build();

        this.addRenderableWidget(input);
        this.addRenderableWidget(modeButton);
        this.addRenderableWidget(doneButton);
    }

    private void done(Button button)
    {
        try {
            double value = Double.parseDouble(this.input.getValue());
            switch (mode)
            {
                case TEMPERATURE -> this.entity.temperature = value;
                case HEAT -> this.entity.heat = value;
            }
            getMinecraft().setScreen(null);
            PacketDistributor.sendToServer(new HeatSourceSetterPayload(this.entity));
        } catch (Exception e)
        {
            invalid.setColor(DyeColor.RED.getTextColor());
        }

    }

    private void dynamicResponder(String s) {
        try {
            double value = Double.parseDouble(s);
            this.input.setTextColor(DyeColor.LIME.getTextColor());
            this.removeWidget(invalid);

        } catch (Exception e)
        {
            this.input.setTextColor(DyeColor.RED.getTextColor());
            if (!this.renderables.contains(invalid))
                this.addRenderableWidget(invalid);
        }
    }

    private Component cycleButton(Mode mode)
    {
        return switch (mode){
            case TEMPERATURE -> Component.literal("Temperature");
            case HEAT -> Component.literal("Heat");
        };
    }

    private void cycleButtonClick(CycleButton<Mode> cycleButton,Mode value){
        this.removeWidget(invalid);
        this.mode = value;
        this.entity.mode = value;
        switch (value)
        {
            case TEMPERATURE -> this.input.setValue(String.valueOf(this.entity.temperature));
            case HEAT -> this.input.setValue(String.valueOf(this.entity.heat));
        }
    }

    @Override
    public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        super.render(p_281549_, p_281550_, p_282878_, p_282465_);

    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
