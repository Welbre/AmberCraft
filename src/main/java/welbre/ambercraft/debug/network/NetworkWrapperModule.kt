package welbre.ambercraft.debug.network

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import welbre.ambercraft.module.ModulesHolder
import welbre.ambercraft.module.heat.HeatModule
import welbre.ambercraft.module.network.NetworkModule
import java.io.Serializable

class NetworkWrapperModule<T>(entity: T): Serializable where T : ModulesHolder
{
    val module: Array<NetworkModule> = entity.modules.filterIsInstance<NetworkModule>().toTypedArray();
    val posX: Int = entity.blockPos.x;
    val posY: Int = entity.blockPos.y;
    val posZ: Int = entity.blockPos.z;
    
    private fun findBlockEntity(visited: MutableList<BlockEntity>, level: Level, where: BlockPos, entity: BlockEntity, target: NetworkModule) : BlockEntity?
    {
        visited.add(entity);
        if (entity is ModulesHolder)
        for (module in entity.modules)
            if (module is HeatModule)
                if (module.id == target.ID)
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
    
    fun findBlockEntity(targetModule: NetworkModule): BlockEntity? {
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