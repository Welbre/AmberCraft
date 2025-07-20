package welbre.ambercraft.debug

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.network.PacketDistributor
import welbre.ambercraft.blockentity.HeatConductorBE
import welbre.ambercraft.module.ModulesHolder
import welbre.ambercraft.module.heat.HeatModule
import java.io.Serializable

class NetworkWrapperModule : Serializable
{
    val module: HeatModule;
    val posX: Int;
    val posY: Int;
    val posZ: Int;

    constructor(conductor: HeatConductorBE)
    {
        module = conductor.heatModule;
        posX = conductor.blockPos.x
        posY = conductor.blockPos.y
        posZ = conductor.blockPos.z
    }
    
    private fun findBlockEntity(visited: MutableList<BlockEntity>, level: Level, where: BlockPos, entity: BlockEntity, target: HeatModule) : BlockEntity?
    {
        visited.add(entity);
        if (entity is ModulesHolder)
        for (module in entity.modules)
            if (module is HeatModule)
                if (module.ID == target.ID)
                    return entity

        for (dir in Direction.entries)
        {
            val relative = where.relative(dir)
            val be = level.getBlockEntity(relative)
            if (be != null && be is ModulesHolder)
            {
                if (!visited.contains(be)) {
                    val found = findBlockEntity(visited, level, relative, be, target);
                    if (found != null) return found
                }
            }
        }
        return null;
    }
    
    fun findBlockEntity(targetModule: HeatModule): BlockEntity? {
        val level:Level = Minecraft.getInstance().level?: throw IllegalStateException("BlockEntity is not in father world!")
        val visited: MutableList<BlockEntity> = ArrayList()
        val pos = BlockPos(posX, posY, posZ)

        val blockEntity: BlockEntity? = level.getBlockEntity(pos)
        if (blockEntity != null)
        {
            if (blockEntity is ModulesHolder)
                return findBlockEntity(visited, level, pos, blockEntity, targetModule)
        }
        
        return null;
    }
}