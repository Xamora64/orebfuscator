package orebfuscator;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class BlockHelper 
{
	private static int getBlockExtId(final ExtendedBlockStorage extendedblockstorage, final int x, final int y, final int z)
	{
        if (extendedblockstorage != null)
        {
            int l = extendedblockstorage.getBlockLSBArray()[y << 8 | z << 4 | x] & 255;

            if (extendedblockstorage.getBlockMSBArray() != null)
                l |= extendedblockstorage.getBlockMSBArray().get(x, y, z) << 8;

            return l;
        }
        return 0;
	}
	
	public static int getBlockID(final Chunk chunk, final int x, final int y, final int z)
	{
        ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();	                
        if (y >> 4 < storageArrays.length)
            return getBlockExtId(storageArrays[y >> 4], x, y & 15, z);
        return 0;
	}
}
