package welbre.ambercraft.blockitem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.blocks.FacedCableBlock;
import welbre.ambercraft.cables.CableDataComponent;

public class FacedCableBlockItem extends BlockItem {
    public FacedCableBlockItem(Item.Properties properties) {
        super(Main.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(),
                properties.component(Main.Components.CABLE_DATA_COMPONENT.get(), new CableDataComponent(0, (byte) 0))
        );
    }
    @Override
    public @NotNull InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        Direction clickedFace = context.getClickedFace();

        CableDataComponent component = context.getItemInHand().getComponents().get(Main.Components.CABLE_DATA_COMPONENT.get());
        component = component == null ? new CableDataComponent(0,(byte) 0) : component;

        if (be instanceof FacedCableBlockEntity faced){
            if (faced.getStatus().getFaceStatus(clickedFace.getOpposite()) == null) {
                faced.getStatus().addCenter(clickedFace.getOpposite(),component.color() ,component.type());
                faced.calculateState(level,pos);
                //todo check if it will blowup all.
                //todo fix not working while the cable is isolado de outros cabos
                level.markAndNotifyBlock(pos,level.getChunkAt(pos),level.getBlockState(pos),level.getBlockState(pos),3,512);
                return InteractionResult.SUCCESS;
            }
        }
        InteractionResult result = super.place(context);
        //put the cable if don't have on the block.
        if (result.consumesAction()) {
            if (level.getBlockEntity(pos) instanceof FacedCableBlockEntity faced)
            {
                faced.getStatus().addCenter(clickedFace.getOpposite(),component.color(),component.type());
                faced.calculateState(level,pos);
            }
        }

        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (level.getBlockState(clicked.relative(context.getClickedFace().getOpposite())).getBlock() instanceof FacedCableBlock)
            return false;
        return super.canPlace(context, state);
    }


}
