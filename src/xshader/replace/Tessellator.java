package xshader.replace;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.PriorityQueue;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.client.util.QuadComparator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import xshader.Globals;
import xshader.tessellator.ITessellator;

public class Tessellator implements ITessellator {
	
	private static int nativeBufferSize = 0x200000;
	@SuppressWarnings("unused")
	private static int trivertsInBuffer = (nativeBufferSize / 48) * 6;
	public static boolean renderingWorldRenderer = false;
	public boolean defaultTexture = false;
	private int rawBufferSize = 0;
	public int textureID = 0;
	
	/** The byte buffer used for GL allocation. */
	private static ByteBuffer byteBuffer = GLAllocation.createDirectByteBuffer(nativeBufferSize * 4);
	/** The same memory as byteBuffer, but referenced as an integer buffer. */
	private static IntBuffer intBuffer = byteBuffer.asIntBuffer();
	/** The same memory as byteBuffer, but referenced as an float buffer. */
	private static FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
	/** The same memory as byteBuffer, but referenced as an short buffer. */
	private static ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
	/** Raw integer array. */
	private int[] rawBuffer;
	/**
	 * The number of vertices to be drawn in the next draw call. Reset to 0
	 * between draw calls.
	 */
	private int vertexCount;
	/** The first coordinate to be used for the texture. */
	private double textureU;
	/** The second coordinate to be used for the texture. */
	private double textureV;
	private int brightness;
	/** The color (RGBA) value to be used for the following draw call. */
	private int color;
	/**
	 * Whether the current draw object for this tessellator has color values.
	 */
	private boolean hasColor;
	/**
	 * Whether the current draw object for this tessellator has texture
	 * coordinates.
	 */
	private boolean hasTexture;
	private boolean hasBrightness;
	/**
	 * Whether the current draw object for this tessellator has normal values.
	 */
	private boolean hasNormals;
	/** The index into the raw buffer to be used for the next data. */
	private int rawBufferIndex;
	/**
	 * The number of vertices manually added to the given draw call. This
	 * differs from vertexCount because it adds extra
	 * vertices when converting quads to triangles.
	 */
	@SuppressWarnings("unused")
	private int addedVertices;
	/** Disables all color information for the following draw call. */
	private boolean isColorDisabled;
	/** The draw mode currently being used by the tessellator. */
	private int drawMode;
	/**
	 * An offset to be applied along the x-axis for all vertices in this draw
	 * call.
	 */
	private double xOffset;
	/**
	 * An offset to be applied along the y-axis for all vertices in this draw
	 * call.
	 */
	private double yOffset;
	/**
	 * An offset to be applied along the z-axis for all vertices in this draw
	 * call.
	 */
	private double zOffset;
	/** The normal to be applied to the face being drawn. */
	private int normal;
	/** The static instance of the Tessellator. */
	public static final Tessellator instance = new Tessellator(2097152);
	/** Whether this tessellator is currently in draw mode. */
	private boolean isDrawing;
	/** The size of the buffers used (in integers). */
	@SuppressWarnings("unused")
	private int bufferSize;
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00000960";
	
	private static final int RAW_BUFFER_ENTRY_SIZE = 9;
	
	private static final int RAW_BUFFER_ENTRY_SIZE_4 = RAW_BUFFER_ENTRY_SIZE * 4;
	
	private int materialID;
	
	private boolean hasMaterialID;
	
	@SuppressWarnings("unused")
	private Tessellator(int par1) {
		//
	}
	
	public Tessellator() {}
	
	static {
		instance.defaultTexture = true;
	}
	
	/**
	 * Draws the data set up in this tessellator and resets the state to prepare
	 * for new drawing.
	 */
	public int draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		}
		this.isDrawing = false;
		
		int offs = 0;
		while (offs < this.vertexCount) {
			int vtc = Math.min(this.vertexCount - offs, nativeBufferSize >> 5);
			Tessellator.intBuffer.clear();
			Tessellator.intBuffer.put(this.rawBuffer, offs * RAW_BUFFER_ENTRY_SIZE, vtc * RAW_BUFFER_ENTRY_SIZE);
			Tessellator.byteBuffer.position(0);
			Tessellator.byteBuffer.limit(vtc * RAW_BUFFER_ENTRY_SIZE_4);
			offs += vtc;
			
			if (this.hasTexture) {
				Tessellator.floatBuffer.position(3);
				GL11.glTexCoordPointer(2, RAW_BUFFER_ENTRY_SIZE_4, Tessellator.floatBuffer);
				GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			}
			
			if (this.hasBrightness) {
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
				Tessellator.shortBuffer.position(14);
				GL11.glTexCoordPointer(2, RAW_BUFFER_ENTRY_SIZE_4, Tessellator.shortBuffer);
				GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			}
			
			if (this.hasColor) {
				Tessellator.byteBuffer.position(20);
				GL11.glColorPointer(4, true, RAW_BUFFER_ENTRY_SIZE_4, Tessellator.byteBuffer);
				GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			}
			
			if (this.hasNormals) {
				Tessellator.byteBuffer.position(24);
				GL11.glNormalPointer(RAW_BUFFER_ENTRY_SIZE_4, Tessellator.byteBuffer);
				GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
			}
			
			if (Globals.gShader != null && this.hasMaterialID) {
				Tessellator.intBuffer.position(8);
				GL20.glVertexAttribPointer(Globals.gShader.getAttributePos("materialID"), 1, false, false, RAW_BUFFER_ENTRY_SIZE_4, Tessellator.intBuffer);
				GL20.glEnableVertexAttribArray(Globals.gShader.getAttributePos("materialID"));
			}
			
			Tessellator.floatBuffer.position(0);
			GL11.glVertexPointer(3, RAW_BUFFER_ENTRY_SIZE_4, Tessellator.floatBuffer);
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glDrawArrays(this.drawMode, 0, vtc);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			
			if (Globals.gShader != null && this.hasMaterialID) {
				GL20.glDisableVertexAttribArray(Globals.gShader.getAttributePos("materialID"));
			}
			
			if (this.hasTexture) {
				GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			}
			
			if (this.hasBrightness) {
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
				GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			}
			
			if (this.hasColor) {
				GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			}
			
			if (this.hasNormals) {
				GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			}
		}
		
		if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < (this.rawBufferSize << 3)) {
			this.rawBufferSize = 0x10000;
			this.rawBuffer = new int[this.rawBufferSize];
		}
		
		int i = this.rawBufferIndex * 4;
		this.reset();
		return i;
	}
	
	@SuppressWarnings("unchecked")
	public TesselatorVertexState getVertexState(float x, float y, float z) {
		int[] aint = new int[this.rawBufferIndex];
		PriorityQueue<Integer> priorityqueue = new PriorityQueue<Integer>(this.rawBufferIndex, new QuadComparator(this.rawBuffer, x + (float) this.xOffset, y + (float) this.yOffset, z + (float) this.zOffset));
		int i;
		
		for (i = 0; i < this.rawBufferIndex; i += RAW_BUFFER_ENTRY_SIZE_4) {
			priorityqueue.add(Integer.valueOf(i));
		}
		
		for (i = 0; !priorityqueue.isEmpty(); i += RAW_BUFFER_ENTRY_SIZE_4) {
			int j = priorityqueue.remove().intValue();
			
			for (int k = 0; k < RAW_BUFFER_ENTRY_SIZE_4; ++k) {
				aint[i + k] = this.rawBuffer[j + k];
			}
		}
		
		System.arraycopy(aint, 0, this.rawBuffer, 0, aint.length);
		return new xshader.tessellator.TesselatorVertexState(aint, this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor, this.hasMaterialID);
	}
	
	public void setVertexState(TesselatorVertexState vertexState) {
		while (vertexState.getRawBuffer().length > this.rawBufferSize && this.rawBufferSize > 0) {
			this.rawBufferSize <<= 1;
		}
		if (this.rawBufferSize > this.rawBuffer.length) {
			this.rawBuffer = new int[this.rawBufferSize];
		}
		System.arraycopy(vertexState.getRawBuffer(), 0, this.rawBuffer, 0, vertexState.getRawBuffer().length);
		this.rawBufferIndex = vertexState.getRawBufferIndex();
		this.vertexCount = vertexState.getVertexCount();
		this.hasTexture = vertexState.getHasTexture();
		this.hasBrightness = vertexState.getHasBrightness();
		this.hasColor = vertexState.getHasColor();
		this.hasNormals = vertexState.getHasNormals();
		this.hasMaterialID = vertexState instanceof xshader.tessellator.TesselatorVertexState ? ((xshader.tessellator.TesselatorVertexState) vertexState).getHasMaterialID() : false;
	}
	
	/**
	 * Clears the tessellator state in preparation for new drawing.
	 */
	private void reset() {
		this.vertexCount = 0;
		Tessellator.byteBuffer.clear();
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
	}
	
	/**
	 * Sets draw mode in the tessellator to draw quads.
	 */
	public void startDrawingQuads() {
		this.startDrawing(7);
	}
	
	/**
	 * Resets tessellator state and prepares for drawing (with the specified
	 * draw mode).
	 */
	@SuppressWarnings("hiding")
	public void startDrawing(int drawMode) {
		if (this.isDrawing) {
			throw new IllegalStateException("Already tesselating!");
		}
		this.isDrawing = true;
		this.reset();
		this.drawMode = drawMode;
		this.hasNormals = false;
		this.hasColor = false;
		this.hasTexture = false;
		this.hasBrightness = false;
		this.isColorDisabled = false;
		this.hasMaterialID = false;
	}
	
	/**
	 * Sets the texture coordinates.
	 */
	public void setTextureUV(double u, double v) {
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}
	
	public void setBrightness(int brightness) {
		this.hasBrightness = true;
		this.brightness = brightness;
	}
	
	/**
	 * Sets the RGB values as specified, converting from floats between 0 and 1
	 * to integers from 0-255.
	 */
	public void setColorOpaque_F(float r, float g, float b) {
		this.setColorOpaque((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F));
	}
	
	/**
	 * Sets the RGBA values for the color, converting from floats between 0 and
	 * 1 to integers from 0-255.
	 */
	public void setColorRGBA_F(float r, float g, float b, float a) {
		this.setColorRGBA((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
	}
	
	/**
	 * Sets the RGB values as specified, and sets alpha to opaque.
	 */
	public void setColorOpaque(int r, int g, int b) {
		this.setColorRGBA(r, g, b, 255);
	}
	
	/**
	 * Sets the RGBA values for the color. Also clamps them to 0-255.
	 */
	public void setColorRGBA(int r, int g, int b, int a) {
		if (!this.isColorDisabled) {
			int rr = r > 255 ? 255 : r < 0 ? 0 : r;
			int gg = g > 255 ? 255 : g < 0 ? 0 : g;
			int bb = b > 255 ? 255 : b < 0 ? 0 : b;
			int aa = a > 255 ? 255 : a < 0 ? 0 : a;
			
			this.hasColor = true;
			
			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				this.color = aa << 24 | bb << 16 | gg << 8 | rr;
			} else {
				this.color = rr << 24 | gg << 16 | bb << 8 | aa;
			}
		}
	}
	
	/**
	 * Adds a vertex specifying both x,y,z and the texture u,v for it.
	 */
	public void addVertexWithUV(double x, double y, double z, double u, double v) {
		this.setTextureUV(u, v);
		this.addVertex(x, y, z);
	}
	
	/**
	 * Adds a vertex with the specified x,y,z to the current draw call. It will
	 * trigger a draw() if the buffer gets
	 * full.
	 */
	public void addVertex(double x, double y, double z) {
		if (this.rawBufferIndex >= this.rawBufferSize - 32) {
			if (this.rawBufferSize == 0) {
				this.rawBufferSize = 0x10000;
				this.rawBuffer = new int[this.rawBufferSize];
			} else {
				this.rawBufferSize *= 2;
				this.rawBuffer = Arrays.copyOf(this.rawBuffer, this.rawBufferSize);
			}
		}
		++this.addedVertices;
		
		if (this.hasTexture) {
			this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float) this.textureU);
			this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float) this.textureV);
		}
		
		if (this.hasBrightness) {
			this.rawBuffer[this.rawBufferIndex + 7] = this.brightness;
		}
		
		if (this.hasColor) {
			this.rawBuffer[this.rawBufferIndex + 5] = this.color;
		}
		
		if (this.hasNormals) {
			this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
		}
		
		if (this.hasMaterialID) {
			this.rawBuffer[this.rawBufferIndex + 8] = this.materialID;
		}
		
		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float) (x + this.xOffset));
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float) (y + this.yOffset));
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float) (z + this.zOffset));
		this.rawBufferIndex += RAW_BUFFER_ENTRY_SIZE;
		++this.vertexCount;
	}
	
	/**
	 * Sets the color to the given opaque value (stored as byte values packed in
	 * an integer).
	 */
	public void setColorOpaque_I(int color) {
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		this.setColorOpaque(r, g, b);
	}
	
	/**
	 * Sets the color to the given color (packed as bytes in integer) and alpha
	 * values.
	 */
	public void setColorRGBA_I(int color, int a) {
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		this.setColorRGBA(r, g, b, a);
	}
	
	/**
	 * Disables colors for the current draw call.
	 */
	public void disableColor() {
		this.isColorDisabled = true;
	}
	
	/**
	 * Sets the normal for the current draw call.
	 */
	public void setNormal(float x, float y, float z) {
		this.hasNormals = true;
		byte nx = (byte) ((int) (x * 127.0F));
		byte ny = (byte) ((int) (y * 127.0F));
		byte nz = (byte) ((int) (z * 127.0F));
		this.normal = nx & 255 | (ny & 255) << 8 | (nz & 255) << 16;
	}
	
	/**
	 * Sets the translation for all vertices in the current draw call.
	 */
	public void setTranslation(double x, double y, double z) {
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
	}
	
	/**
	 * Offsets the translation for all vertices in the current draw call.
	 */
	public void addTranslation(float x, float y, float z) {
		this.xOffset += x;
		this.yOffset += y;
		this.zOffset += z;
	}
	
	@Override
	public void setMaterialID(int materialID) {
		this.hasMaterialID = true;
		this.materialID = materialID;
	}
	
}