package orebfuscator;

import java.lang.reflect.Field;

import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.world.World;

public class MapChunkBulkObfuscator 
{
    public static Field fieldChunkX;
    public static Field fieldChunkZ;
	
	public static Field fieldHasSky;
    
	// 1110011 - ����� ������ (ExtendedBlockStorage.blockLSBArray) �� ������ (1 - ���� ����, 0 - ��� �����)
	public static Field fieldStatusLSB = null;
	
	// 0001110 - ����� ������ (ExtendedBlockStorage.blockMSBArray) �� ������ (1 - ���� ����, 0 - ��� �����)
	public static Field fieldStatusMSB;
	
	// -- ������ ������
	public static Field fieldData;
	public static Field field_149268_i;
	
	public static ChunkObfuscator info;
	
	public static S26PacketMapChunkBulk obfuscate(World world, S26PacketMapChunkBulk packet)
	{
		if (fieldStatusLSB == null)
		{
            fieldChunkX = Fields.getField(packet, "field_149266_a");
            fieldChunkZ = Fields.getField(packet, "field_149264_b");

        	fieldHasSky = Fields.getField(packet, "field_149267_h");

            fieldStatusLSB = Fields.getField(packet, "field_149265_c");
			fieldStatusMSB = Fields.getField(packet, "field_149262_d");
			fieldData = Fields.getField(packet, "field_149260_f");
			field_149268_i = Fields.getField(packet, "field_149268_i");

        	byte[] data = (byte[]) Fields.getValue(packet, field_149268_i);
        	Options.isBuildCraft = data.length > 0; // Forge keep this array empty
			
			info = new ChunkObfuscator();
		}
		

		int[] chunkX = (int[]) Fields.getValue(packet, fieldChunkX);
		int[] chunkZ = (int[]) Fields.getValue(packet, fieldChunkZ);
		boolean hasSky = (Boolean) Fields.getValue(packet, fieldHasSky);
        int[] statusLSB = (int[]) Fields.getValue(packet, fieldStatusLSB);
        int[] statusMSB = (int[]) Fields.getValue(packet, fieldStatusMSB);
        
        Options.worldOptions = Options.getWorldOptions(world);

        if (Options.isBuildCraft)
        {
        	byte[] data = (byte[]) Fields.getValue(packet, field_149268_i);
        	int pos = 0;
            for (int i = 0; i < statusLSB.length; i++)
            {
            	pos = info.obfuscate(world, chunkX[i], chunkZ[i], hasSky, statusLSB[i], statusMSB[i], data, pos);
            }
        }
        else
        {
        	byte[][] dataArray = (byte[][]) Fields.getValue(packet, fieldData);
            for (int i = 0; i < statusLSB.length; i++)
            {
            	info.obfuscate(world, chunkX[i], chunkZ[i], hasSky, statusLSB[i], statusMSB[i], dataArray[i], 0);
            }
        }
        
        return packet;
	}
}
