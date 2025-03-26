package orebfuscator;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkObfuscator
{
	// Information about the presence of a section. Sections are blocks of size 16×16×16, arranged on top of each other
	// A chunk consists of 16 sections, the chunk size is 16x16x256
	// public boolean[] listLSB = new boolean[16];
	// public boolean[] listMSB = new boolean[16];

	// offsetsLSB[] - contains a list of sections where blocks of size 16x16x16 start,
	// containing the first 8 bits of the BlockID
	// Section size: 16*16*16 = 4096 bytes
	public int[] offsetsLSB = new int[16];

	// Start of the NibbleArrays containing ExtendedBlockStorage.blockMetadataArray
	// Array size: (16*16*16)/2 = 2048 (divided in half because one byte stores two values of 4 bits each)
	public int[] offsetsMetadata = new int[16];

	// Start of the NibbleArray containing ExtendedBlockStorage.blocklightArray
	public int[] offsetsBlocklight = new int[16];

	// NibbleArray for ExtendedBlockStorage.blockMSBArray
	public int[] offsetsMSB = new int[16];

	public int startX;
	public int startZ;

	// Maximum index+1 of the section
	public int len;
	public byte[] data;

	public int obfuscate(World world, int chunkX, int chunkZ, boolean hasSky, int sectionLSB, int sectionMSB, byte[] data, int pos)
	{
		this.startX = chunkX << 4;
		this.startZ = chunkZ << 4;

		this.data = data;
		int countLSB = 0;
		len = 0;
		int l, i;
		for (i = 0; i < 16; ++i) {
			l = sectionLSB >> i & 1;
			if (l == 1) {
				offsetsLSB[i] = pos;
				pos += 4096;

				countLSB++;
				len = i + 1;
			}
			else
				offsetsLSB[i] = -1;
		}

		for (i = 0; i < len; i++) {
			if (offsetsLSB[i] > -1) {
				offsetsMetadata[i] = pos;
				pos += 2048;
			}
		}

		for (i = 0; i < len; i++) {
			if (offsetsLSB[i] > -1) {
				offsetsBlocklight[i] = pos;
				pos += 2048;
			}
		}


		//if (!world.provider.hasNoSky)
		if (hasSky) {
			// If there is sky, then the buffer will contain the ExtendedBlockStorage.skylightArray array
			pos += countLSB * 2048;
		}

		for (i = 0; i < len; i++) {
			l = sectionMSB >> i & 1;
			if (l == 1) {
				offsetsMSB[i] = pos;
				pos += 2048;
			}
			else
				offsetsMSB[i] = -1;
		}

		// biome info
		pos += 256;

		for (i = 0; i < len; i++)
		{
			if (offsetsLSB[i] > -1)
			{
				l = i << 4;
				for (int x = 0; x < 16; x++)
					for (int y = 0; y < 16; y++)
						for (int z = 0; z < 16; z++)
							if (needObfuscate(world, chunkX, chunkZ, x, l | y, z))
								setBlockID(x, l | y, z, Options.worldOptions.getRandomID());
			}
		}

		return pos;
	}

	/**
	 * Returns the block corresponding to the given coordinates inside a chunk.
	 */
	public int getBlockID(int x, int y, int z) {
		int sectionIndex = y >> 4; // Index de la section
		if (sectionIndex >= offsetsLSB.length || offsetsLSB[sectionIndex] == -1) {
			return 0;
		}
		int yLocal = y & 15;
		int xLocal = x & 15;
		int zLocal = z & 15;
		int blockIndex = (yLocal << 8) | (zLocal << 4) | xLocal;

		int lsbIndex = offsetsLSB[sectionIndex] + blockIndex;
		int lsb = data[lsbIndex] & 0xFF;

		int msb = 0;
		if (offsetsMSB[sectionIndex] > -1) {
			int msbByteIndex = offsetsMSB[sectionIndex] + (blockIndex >> 1);
			int msbByte = data[msbByteIndex] & 0xFF;
			if ((blockIndex & 1) == 0)
				msb = msbByte & 0x0F;
			else
				msb = (msbByte >> 4) & 0x0F;
		}

		return (msb << 8) | lsb;
	}

	public void setBlockID(int x, int y, int z, int blockID) {
		int section = y >> 4;
		if (this.offsetsLSB[section] > -1) {
			y = y & 15;

			this.data[this.offsetsLSB[section] + (y << 8 | z << 4 | x)] = (byte) (blockID & 255);
			if (this.offsetsMSB[section] > -1) {
				int l = (y << 8) | (z << 4) | x;
				int i1 = l >> 1;
				int j1 = l & 1;
				int pos = this.offsetsMSB[section] + i1;
				if (j1 == 0)
					this.data[pos] = (byte)(this.data[pos] & 240 | blockID & 15);
				else
					this.data[pos] = (byte)(this.data[pos] & 15 | (blockID & 15) << 4);
			}
		}
	}

	public boolean isTransparent(World world, int chunkX, int chunkZ, int x, int y, int z) {
		if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15)
			return true;

		int blockID = getBlockID(x, y, z);
		Block block = Block.getBlockById(blockID);
		return Options.isTransparent(block);
	}

	public boolean needObfuscate(World world, int chunkX, int chunkZ, int x, int y, int z) {
		if (y > Options.worldOptions.maxObfuscateHeight)
			return false;

		if (!Options.isObfuscated(getBlockID(x, y, z)))
			return false;

		return !(
				isTransparent(world, chunkX, chunkZ, x - 1, y, z) ||
				isTransparent(world, chunkX, chunkZ,x + 1, y, z) ||
				isTransparent(world, chunkX, chunkZ, x, y - 1, z) ||
				isTransparent(world, chunkX, chunkZ, x, y + 1, z) ||
				isTransparent(world, chunkX, chunkZ, x, y, z - 1) ||
				isTransparent(world, chunkX, chunkZ, x, y, z + 1)
		);
	}
}
