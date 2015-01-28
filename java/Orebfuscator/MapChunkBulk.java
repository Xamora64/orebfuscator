package Orebfuscator;

import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class MapChunkBulk 
{
	public int offsetLSB;
	public int offsetMSB;
	public int offsetMetadata;
	public int offsetBlocklight;
	public int len;
	public int lenLSB;
	public int lenMSB;
	public int[] bufLSB;
	public int[] bufMSB;
	public byte[][] dataArray;
	public byte[] data;
	
	public MapChunkBulk(S26PacketMapChunkBulk packet, World world)
	{
        int k = 0;
        int l;

        bufLSB = (int[]) Fields.getValue(packet, "field_149265_c"); // 1110011 - ����� ������ (ExtendedBlockStorage.blockLSBArray) �� ������ (1 - ���� ����, 0 - ��� �����)
        bufMSB = (int[]) Fields.getValue(packet, "field_149262_d"); // 0001110 - ����� ������ (ExtendedBlockStorage.blockMSBArray) �� ������ (1 - ���� ����, 0 - ��� �����)
        dataArray = (byte[][]) Fields.getValue(packet, "field_149260_f"); // -- ������ ������

        int lsb;
        int msb;
        int pos;
        int len;
        
        for (int i = 0; i < bufLSB.length; i++)
        {
        	lenLSB = 0;
        	lenMSB = 0;
        	len = 0;
	        for (int j = 0; j < 16; ++j)
	        {
	            l = bufLSB[i] >> j & 1;
	            
	            if (l == 1)
	            {
	            	lenLSB++;
	            	len = j + 1;
		            l = bufMSB[i] >> j & 1;
		            lenMSB++;
	            }
	        }
	        
	        offsetLSB = 0; // � ���� ������� ���������� ����� �������� 16x16x16 ���������� ������ 8 ��� BlockID
	        				// ������ ����� 16*16*16 = 4096 ����
           	offsetMetadata = offsetLSB + lenLSB * 4096; // ������ NibbleArrays ���������� ExtendedBlockStorage.blockMetadataArray
           									// ������ ������� (16*16*16)/2 = 2048 (����� �������, �.�. � ����� ����� ���������� ��� �������� �� 4 ����)  
           	offsetBlocklight = offsetMetadata + lenLSB * 2048; // ������ NibbleArray ����������� ExtendedBlockStorage.blocklightArray
           	
           	// NibbleArray ExtendedBlockStorage.blockMSBArray
            if (world.provider.hasNoSky)
            {
            	offsetMSB = offsetBlocklight + lenLSB * 2048;
            }
            else
            {
            	offsetMSB = offsetBlocklight + 2 * lenLSB * 2048;  // ���� ���� ����, �� � ������ ����� ������ ExtendedBlockStorage.skylightArray
            }
            
            pos = 0;
            data = dataArray[i];
	        for (int j = 0; j < 16; ++j)
	        {
	            l = bufLSB[i] >> j & 1;
	            
	            if (l == 1)
	            {
	            	for (int x = 0; x < 16; x++)
	            	{
	            		for (int y = 0; y < 16; y++)
	            		{
	            			for (int z = 0; z < 16; z++)
	            			{
	            				if (!Orebfuscator.isBlockTransparent(getBlockID(x, y, z)))
	            				{
	            					//data[pos + n] = 1;
	            				}
	            			}
	            		}
	            	}
	            	pos += 4096;
	            }
	        }
        }
	}
	
	public int getBlockID(int x, int y, int z)
	{
        if (y >> 4 < this.lenLSB)
        {
        	y = y & 15;
        	/*
            int l = this.blockLSBArray[y << 8 | z << 4 | x] & 255;

            if (this.blockMSBArray != null)
            {
                l |= this.blockMSBArray.get(p_150819_1_, p_150819_2_, p_150819_3_) << 8;
            }

            return Block.getBlockById(l);
            */
        }
		
		return 0;
	}
}
