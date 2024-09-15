package Orebfuscator;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class ChunkObfuscator
{
	// ?????????? ? ??????? ??????. ?????? - ??? ????? ???????? 16?16?16, ?????? ????????????? ???? ??? ??????
	// Chunk ??????? ?? 16 ??????, ?????? ????? 16x16x256
	//public boolean[] listLSB = new boolean[16];
	//public boolean[] listMSB = new boolean[16];

	// offsetsLSB[] - ???????? ?????? ??????, ? ??????? ?????????? ????? ???????? 16x16x16 ?????????? ?????? 8 ??? BlockID
	// ?????? ?????? 16*16*16 = 4096 ????
	public int[] offsetsLSB = new int[16];

	// ?????? NibbleArrays ?????????? ExtendedBlockStorage.blockMetadataArray
	// ?????? ??????? (16*16*16)/2 = 2048 (????? ???????, ?.?. ? ????? ????? ?????????? ??? ???????? ?? 4 ????)
	public int[] offsetsMetadata = new int[16];

	// ?????? NibbleArray ??????????? ExtendedBlockStorage.blocklightArray
	public int[] offsetsBlocklight = new int[16];

	// NibbleArray ExtendedBlockStorage.blockMSBArray
	public int[] offsetsMSB = new int[16];

	public int startX;
	public int startZ;

	// ???????????? ??????+1 ??????
	public int len;
	public byte[] data;

	public int obfuscate(World world, int chunkX, int chunkZ, boolean hasSky, int sectionLSB, int sectionMSB, byte[] data, int pos)
	{
		this.startX = chunkX << 4;
		this.startZ = chunkZ << 4;

		this.data = data;
		int countLSB = 0;
		len = 0;
		int l;
		int i;
		for (i = 0; i < 16; ++i)
		{
			l = sectionLSB >> i & 1;
			if (l == 1)
			{
				offsetsLSB[i] = pos;
				pos += 4096;

				countLSB++;
				len = i + 1;
			}
			else
			{
				offsetsLSB[i] = -1;
			}
		}

		for (i = 0; i < len; i++)
		{
			if (offsetsLSB[i] > -1)
			{
				offsetsMetadata[i] = pos;
				pos += 2048;
			}
		}

		for (i = 0; i < len; i++)
		{
			if (offsetsLSB[i] > -1)
			{
				offsetsBlocklight[i] = pos;
				pos += 2048;
			}
		}


		//if (!world.provider.hasNoSky)
		if (hasSky)
		{
			// ???? ???? ????, ?? ? ?????? ????? ?????? ExtendedBlockStorage.skylightArray
			pos += countLSB * 2048;
		}

		for (i = 0; i < len; i++)
		{
			l = sectionMSB >> i & 1;
			if (l == 1)
			{
				offsetsMSB[i] = pos;
				pos += 2048;
			}
			else
			{
				offsetsMSB[i] = -1;
			}
		}

		// biome info
		pos += 256;

		for (i = 0; i < len; i++)
		{
			if (offsetsLSB[i] > -1)
			{
				l = i << 4;
				for (int x = 0; x < 16; x++)
				{
					for (int y = 0; y < 16; y++)
					{
						for (int z = 0; z < 16; z++)
						{
							if (neetObfuscate(x, l | y, z))
							{
								setBlockID(x, l | y, z, Options.worldOptions.getRandomID());
							}
						}
					}
				}
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
			if ((blockIndex & 1) == 0) {
				msb = msbByte & 0x0F;
			} else {
				msb = (msbByte >> 4) & 0x0F;
			}
		}

		return (msb << 8) | lsb;
	}

	public void setBlockID(int x, int y, int z, int blockID)
	{
		int section = y >> 4;
		if (this.offsetsLSB[section] > -1)
		{
			y = y & 15;

			this.data[this.offsetsLSB[section] + (y << 8 | z << 4 | x)] = (byte) (blockID & 255);
			if (this.offsetsMSB[section] > -1)
			{
				int l = y << 4 | z << 4 | x;
				int i1 = l >> 1;
				int j1 = l & 1;
				int pos = this.offsetsMSB[section] + i1;
				if (j1 == 0)
				{
					this.data[pos] = (byte)(this.data[pos] & 240 | blockID & 15);
				}
				else
				{
					this.data[pos] = (byte)(this.data[pos] & 15 | (blockID & 15) << 4);
				}
			}
		}
	}

	public boolean isTransparent(int x, int y, int z) {
		if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15) {
			return true;
		}
		int blockID = getBlockID(x, y, z);
		return Options.isTransparent(Block.getBlockById(blockID));
	}

	public boolean neetObfuscate(int x, int y, int z) {
		if (y > Options.worldOptions.maxObfuscateHeight)
			return false;

		int blockID = getBlockID(x, y, z);
		if (!Options.isObfuscated(blockID)) {
			return false;
		}

		return !(
				isTransparent(x - 1, y, z) ||
						isTransparent(x + 1, y, z) ||
						isTransparent(x, y - 1, z) ||
						isTransparent(x, y + 1, z) ||
						isTransparent(x, y, z - 1) ||
						isTransparent(x, y, z + 1)
		);
	}
}
