package xshader.tessellator;

public class TesselatorVertexState extends net.minecraft.client.shader.TesselatorVertexState {

	private boolean hasMaterialID;
	
	public TesselatorVertexState(int[] p_i45079_1_, int p_i45079_2_, int p_i45079_3_, boolean p_i45079_4_, boolean p_i45079_5_, boolean p_i45079_6_, boolean p_i45079_7_, boolean hasMaterialID) {
		super(p_i45079_1_, p_i45079_2_, p_i45079_3_, p_i45079_4_, p_i45079_5_, p_i45079_6_, p_i45079_7_);
		this.hasMaterialID = hasMaterialID;
	}
	
	public boolean getHasMaterialID()
    {
        return this.hasMaterialID;
    }
	
}
