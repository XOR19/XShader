package xshader.replace;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseFilter;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IRenderHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import xshader.Globals;
import xshader.Utils;
import xshader.fbo.FBO;
import xshader.shader.Shader;
import xshader.tessellator.ITessellator;

public class EntityRenderer implements IResourceManagerReloadListener {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();
	private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
	public static boolean anaglyphEnable;
	/** Anaglyph field (0=R, 1=GB) */
	public static int anaglyphField;
	/** A reference to the Minecraft object. */
	Minecraft mc;
	private float farPlaneDistance;
	public final ItemRenderer itemRenderer;
	private final MapItemRenderer theMapItemRenderer;
	/** Entity renderer update count */
	private int rendererUpdateCount;
	/** Pointed entity */
	private Entity pointedEntity;
	private MouseFilter mouseFilterXAxis = new MouseFilter();
	private MouseFilter mouseFilterYAxis = new MouseFilter();
	/** Mouse filter dummy 1 */
	@SuppressWarnings("unused")
	private MouseFilter mouseFilterDummy1 = new MouseFilter();
	/** Mouse filter dummy 2 */
	@SuppressWarnings("unused")
	private MouseFilter mouseFilterDummy2 = new MouseFilter();
	/** Mouse filter dummy 3 */
	@SuppressWarnings("unused")
	private MouseFilter mouseFilterDummy3 = new MouseFilter();
	/** Mouse filter dummy 4 */
	@SuppressWarnings("unused")
	private MouseFilter mouseFilterDummy4 = new MouseFilter();
	private float thirdPersonDistance = 4.0F;
	/** Third person distance temp */
	private float thirdPersonDistanceTemp = 4.0F;
	private float debugCamYaw;
	private float prevDebugCamYaw;
	private float debugCamPitch;
	private float prevDebugCamPitch;
	/** Smooth cam yaw */
	private float smoothCamYaw;
	/** Smooth cam pitch */
	private float smoothCamPitch;
	/** Smooth cam filter X */
	private float smoothCamFilterX;
	/** Smooth cam filter Y */
	private float smoothCamFilterY;
	/** Smooth cam partial ticks */
	private float smoothCamPartialTicks;
	private float debugCamFOV;
	private float prevDebugCamFOV;
	private float camRoll;
	private float prevCamRoll;
	/**
	 * The texture id of the blocklight/skylight texture used for lighting
	 * effects
	 */
	private final DynamicTexture lightmapTexture;
	/**
	 * Colors computed in updateLightmap() and loaded into the lightmap
	 * emptyTexture
	 */
	private final int[] lightmapColors;
	private final ResourceLocation locationLightMap;
	/** FOV modifier hand */
	private float fovModifierHand;
	/** FOV modifier hand prev */
	private float fovModifierHandPrev;
	/** FOV multiplier temp */
	private float fovMultiplierTemp;
	private float bossColorModifier;
	private float bossColorModifierPrev;
	/** Cloud fog mode */
	private boolean cloudFog;
	@SuppressWarnings("unused")
	private final IResourceManager resourceManager = null;
	public ShaderGroup theShaderGroup = null;
	@SuppressWarnings("unused")
	private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[0];
	public static final int shaderCount = 0;
	@SuppressWarnings("unused")
	private int shaderIndex = 0;
	private double cameraZoom;
	private double cameraYaw;
	private double cameraPitch;
	/** Previous frame time in milliseconds */
	private long prevFrameTime;
	/** End time of last render (ns) */
	private long renderEndNanoTime;
	/**
	 * Is set, updateCameraAndRender() calls updateLightmap(); set by
	 * updateTorchFlicker()
	 */
	private boolean lightmapUpdateNeeded;
	/** Torch flicker X */
	float torchFlickerX;
	/** Torch flicker DX */
	float torchFlickerDX;
	/** Torch flicker Y */
	float torchFlickerY;
	/** Torch flicker DY */
	float torchFlickerDY;
	private Random random;
	/** Rain sound counter */
	private int rainSoundCounter;
	/** Rain X coords */
	float[] rainXCoords;
	/** Rain Y coords */
	float[] rainYCoords;
	/** Fog color buffer */
	FloatBuffer fogColorBuffer;
	/** red component of the fog color */
	float fogColorRed;
	/** green component of the fog color */
	float fogColorGreen;
	/** blue component of the fog color */
	float fogColorBlue;
	/** Fog color 2 */
	private float fogColor2;
	/** Fog color 1 */
	private float fogColor1;
	/**
	 * Debug view direction (0=OFF, 1=Front, 2=Right, 3=Back, 4=Left,
	 * 5=TiltLeft, 6=TiltRight)
	 */
	public int debugViewDirection;
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00000947";
	
	@SuppressWarnings("unused")
	public EntityRenderer(Minecraft mc, IResourceManager resourceManager) {
		this.cameraZoom = 1.0D;
		this.prevFrameTime = Minecraft.getSystemTime();
		this.random = new Random();
		this.fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
		this.mc = mc;
		this.theMapItemRenderer = new MapItemRenderer(mc.getTextureManager());
		this.itemRenderer = new ItemRenderer(mc);
		this.lightmapTexture = new DynamicTexture(16, 16);
		this.locationLightMap = mc.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
		this.lightmapColors = this.lightmapTexture.getTextureData();
	}
	
	@SuppressWarnings("static-method")
	public boolean isShaderActive() {
		return false;
	}
	
	public void deactivateShader() {
		//
	}
	
	public void activateNextShader() {
		//
	}
	
	@SuppressWarnings("hiding")
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		//
	}
	
	/**
	 * Updates the entity renderer
	 */
	public void updateRenderer() {
		this.updateFovModifierHand();
		this.updateTorchFlicker();
		this.fogColor2 = this.fogColor1;
		this.thirdPersonDistanceTemp = this.thirdPersonDistance;
		this.prevDebugCamYaw = this.debugCamYaw;
		this.prevDebugCamPitch = this.debugCamPitch;
		this.prevDebugCamFOV = this.debugCamFOV;
		this.prevCamRoll = this.camRoll;
		float f;
		float f1;
		
		if (this.mc.gameSettings.smoothCamera) {
			f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			f1 = f * f * f * 8.0F;
			this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * f1);
			this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * f1);
			this.smoothCamPartialTicks = 0.0F;
			this.smoothCamYaw = 0.0F;
			this.smoothCamPitch = 0.0F;
		}
		
		if (this.mc.renderViewEntity == null) {
			this.mc.renderViewEntity = this.mc.thePlayer;
		}
		
		f = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(this.mc.renderViewEntity.posX), MathHelper.floor_double(this.mc.renderViewEntity.posY), MathHelper.floor_double(this.mc.renderViewEntity.posZ));
		f1 = this.mc.gameSettings.renderDistanceChunks / 16;
		float f2 = f * (1.0F - f1) + f1;
		this.fogColor1 += (f2 - this.fogColor1) * 0.1F;
		++this.rendererUpdateCount;
		this.itemRenderer.updateEquippedItem();
		this.addRainParticles();
		this.bossColorModifierPrev = this.bossColorModifier;
		
		if (BossStatus.hasColorModifier) {
			this.bossColorModifier += 0.05F;
			
			if (this.bossColorModifier > 1.0F) {
				this.bossColorModifier = 1.0F;
			}
			
			BossStatus.hasColorModifier = false;
		} else if (this.bossColorModifier > 0.0F) {
			this.bossColorModifier -= 0.0125F;
		}
	}
	
	@SuppressWarnings("static-method")
	public ShaderGroup getShaderGroup() {
		return null;
	}
	
	@SuppressWarnings("unused")
	public void updateShaderGroupSize(int width, int height) {
		//
	}
	
	/**
	 * Finds what block or object the mouse is over at the specified partial
	 * tick time. Args: partialTickTime
	 */
	public void getMouseOver(float partialTickTime) {
		if (this.mc.renderViewEntity != null) {
			if (this.mc.theWorld != null) {
				this.mc.pointedEntity = null;
				double d0 = this.mc.playerController.getBlockReachDistance();
				this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(d0, partialTickTime);
				double d1 = d0;
				Vec3 vec3 = this.mc.renderViewEntity.getPosition(partialTickTime);
				
				if (this.mc.playerController.extendedReach()) {
					d0 = 6.0D;
					d1 = 6.0D;
				} else {
					if (d0 > 3.0D) {
						d1 = 3.0D;
					}
					
					d0 = d1;
				}
				
				if (this.mc.objectMouseOver != null) {
					d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
				}
				
				Vec3 vec31 = this.mc.renderViewEntity.getLook(partialTickTime);
				Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
				this.pointedEntity = null;
				Vec3 vec33 = null;
				float f1 = 1.0F;
				List<?> list = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f1, f1, f1));
				double d2 = d1;
				
				for (int i = 0; i < list.size(); ++i) {
					Entity entity = (Entity) list.get(i);
					
					if (entity.canBeCollidedWith()) {
						float f2 = entity.getCollisionBorderSize();
						AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2, f2, f2);
						MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
						
						if (axisalignedbb.isVecInside(vec3)) {
							if (0.0D < d2 || d2 == 0.0D) {
								this.pointedEntity = entity;
								vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
								d2 = 0.0D;
							}
						} else if (movingobjectposition != null) {
							double d3 = vec3.distanceTo(movingobjectposition.hitVec);
							
							if (d3 < d2 || d2 == 0.0D) {
								if (entity == this.mc.renderViewEntity.ridingEntity && !entity.canRiderInteract()) {
									if (d2 == 0.0D) {
										this.pointedEntity = entity;
										vec33 = movingobjectposition.hitVec;
									}
								} else {
									this.pointedEntity = entity;
									vec33 = movingobjectposition.hitVec;
									d2 = d3;
								}
							}
						}
					}
				}
				
				if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
					this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
					
					if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
						this.mc.pointedEntity = this.pointedEntity;
					}
				}
			}
		}
	}
	
	/**
	 * Update FOV modifier hand
	 */
	private void updateFovModifierHand() {
		if (this.mc.renderViewEntity instanceof EntityPlayerSP) {
			EntityPlayerSP entityplayersp = (EntityPlayerSP) this.mc.renderViewEntity;
			this.fovMultiplierTemp = entityplayersp.getFOVMultiplier();
		} else {
			this.fovMultiplierTemp = this.mc.thePlayer.getFOVMultiplier();
		}
		this.fovModifierHandPrev = this.fovModifierHand;
		this.fovModifierHand += (this.fovMultiplierTemp - this.fovModifierHand) * 0.5F;
		
		if (this.fovModifierHand > 1.5F) {
			this.fovModifierHand = 1.5F;
		}
		
		if (this.fovModifierHand < 0.1F) {
			this.fovModifierHand = 0.1F;
		}
	}
	
	/**
	 * Changes the field of view of the player depending on if they are
	 * underwater or not
	 */
	private float getFOVModifier(float partialTickTime, boolean par2) {
		if (this.debugViewDirection > 0) {
			return 90.0F;
		}
		EntityLivingBase entityplayer = this.mc.renderViewEntity;
		float f1 = 70.0F;
		
		if (par2) {
			f1 += this.mc.gameSettings.fovSetting * 40.0F;
			f1 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * partialTickTime;
		}
		
		if (entityplayer.getHealth() <= 0.0F) {
			float f2 = entityplayer.deathTime + partialTickTime;
			f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
		}
		
		Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entityplayer, partialTickTime);
		
		if (block.getMaterial() == Material.water) {
			f1 = f1 * 60.0F / 70.0F;
		}
		
		return f1 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * partialTickTime;
	}
	
	private void hurtCameraEffect(float partialTickTime) {
		EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
		float f1 = entitylivingbase.hurtTime - partialTickTime;
		float f2;
		
		if (entitylivingbase.getHealth() <= 0.0F) {
			f2 = entitylivingbase.deathTime + partialTickTime;
			GL11.glRotatef(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
		}
		
		if (f1 >= 0.0F) {
			f1 /= entitylivingbase.maxHurtTime;
			f1 = MathHelper.sin(f1 * f1 * f1 * f1 * (float) Math.PI);
			f2 = entitylivingbase.attackedAtYaw;
			GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
		}
	}
	
	/**
	 * Setups all the GL settings for view bobbing. Args: partialTickTime
	 */
	private void setupViewBobbing(float partialTickTime) {
		if (this.mc.renderViewEntity instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer) this.mc.renderViewEntity;
			float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
			float f2 = -(entityplayer.distanceWalkedModified + f1 * partialTickTime);
			float f3 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTickTime;
			float f4 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTickTime;
			GL11.glTranslatef(MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F);
			GL11.glRotatef(MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(f4, 1.0F, 0.0F, 0.0F);
		}
	}
	
	/**
	 * sets up player's eye (or camera in third person mode)
	 */
	private void orientCamera(float partialTickTime) {
		EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
		float f1 = entitylivingbase.yOffset - 1.62F;
		double d0 = entitylivingbase.prevPosX + (entitylivingbase.posX - entitylivingbase.prevPosX) * partialTickTime;
		double d1 = entitylivingbase.prevPosY + (entitylivingbase.posY - entitylivingbase.prevPosY) * partialTickTime - f1;
		double d2 = entitylivingbase.prevPosZ + (entitylivingbase.posZ - entitylivingbase.prevPosZ) * partialTickTime;
		GL11.glRotatef(this.prevCamRoll + (this.camRoll - this.prevCamRoll) * partialTickTime, 0.0F, 0.0F, 1.0F);
		
		if (entitylivingbase.isPlayerSleeping()) {
			f1 = (float) (f1 + 1.0D);
			GL11.glTranslatef(0.0F, 0.3F, 0.0F);
			
			if (!this.mc.gameSettings.debugCamEnable) {
				ForgeHooksClient.orientBedCamera(this.mc, entitylivingbase);
				GL11.glRotatef(entitylivingbase.prevRotationYaw + (entitylivingbase.rotationYaw - entitylivingbase.prevRotationYaw) * partialTickTime + 180.0F, 0.0F, -1.0F, 0.0F);
				GL11.glRotatef(entitylivingbase.prevRotationPitch + (entitylivingbase.rotationPitch - entitylivingbase.prevRotationPitch) * partialTickTime, -1.0F, 0.0F, 0.0F);
			}
		} else if (this.mc.gameSettings.thirdPersonView > 0) {
			double d7 = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTickTime;
			float f2;
			float f6;
			
			if (this.mc.gameSettings.debugCamEnable) {
				f6 = this.prevDebugCamYaw + (this.debugCamYaw - this.prevDebugCamYaw) * partialTickTime;
				f2 = this.prevDebugCamPitch + (this.debugCamPitch - this.prevDebugCamPitch) * partialTickTime;
				GL11.glTranslatef(0.0F, 0.0F, (float) (-d7));
				GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(f6, 0.0F, 1.0F, 0.0F);
			} else {
				f6 = entitylivingbase.rotationYaw;
				f2 = entitylivingbase.rotationPitch;
				
				if (this.mc.gameSettings.thirdPersonView == 2) {
					f2 += 180.0F;
				}
				
				double d3 = -MathHelper.sin(f6 / 180.0F * (float) Math.PI) * MathHelper.cos(f2 / 180.0F * (float) Math.PI) * d7;
				double d4 = MathHelper.cos(f6 / 180.0F * (float) Math.PI) * MathHelper.cos(f2 / 180.0F * (float) Math.PI) * d7;
				double d5 = (-MathHelper.sin(f2 / 180.0F * (float) Math.PI)) * d7;
				
				for (int k = 0; k < 8; ++k) {
					float f3 = (k & 1) * 2 - 1;
					float f4 = (k >> 1 & 1) * 2 - 1;
					float f5 = (k >> 2 & 1) * 2 - 1;
					f3 *= 0.1F;
					f4 *= 0.1F;
					f5 *= 0.1F;
					MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(this.mc.theWorld.getWorldVec3Pool().getVecFromPool(d0 + f3, d1 + f4, d2 + f5), this.mc.theWorld.getWorldVec3Pool().getVecFromPool(d0 - d3 + f3 + f5, d1 - d5 + f4, d2 - d4 + f5));
					
					if (movingobjectposition != null) {
						double d6 = movingobjectposition.hitVec.distanceTo(this.mc.theWorld.getWorldVec3Pool().getVecFromPool(d0, d1, d2));
						
						if (d6 < d7) {
							d7 = d6;
						}
					}
				}
				
				if (this.mc.gameSettings.thirdPersonView == 2) {
					GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
				}
				
				GL11.glRotatef(entitylivingbase.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(entitylivingbase.rotationYaw - f6, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, (float) (-d7));
				GL11.glRotatef(f6 - entitylivingbase.rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(f2 - entitylivingbase.rotationPitch, 1.0F, 0.0F, 0.0F);
			}
		} else {
			GL11.glTranslatef(0.0F, 0.0F, -0.1F);
		}
		
		if (!this.mc.gameSettings.debugCamEnable) {
			GL11.glRotatef(entitylivingbase.prevRotationPitch + (entitylivingbase.rotationPitch - entitylivingbase.prevRotationPitch) * partialTickTime, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(entitylivingbase.prevRotationYaw + (entitylivingbase.rotationYaw - entitylivingbase.prevRotationYaw) * partialTickTime + 180.0F, 0.0F, 1.0F, 0.0F);
		}
		
		GL11.glTranslatef(0.0F, f1, 0.0F);
		d0 = entitylivingbase.prevPosX + (entitylivingbase.posX - entitylivingbase.prevPosX) * partialTickTime;
		d1 = entitylivingbase.prevPosY + (entitylivingbase.posY - entitylivingbase.prevPosY) * partialTickTime - f1;
		d2 = entitylivingbase.prevPosZ + (entitylivingbase.posZ - entitylivingbase.prevPosZ) * partialTickTime;
		this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTickTime);
	}
	
	/**
	 * sets up projection, view effects, camera position/rotation
	 */
	private void setupCameraTransform(float partialTickTime, int eye) {
		this.farPlaneDistance = this.mc.gameSettings.renderDistanceChunks * 16;
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		float f1 = 0.07F;
		
		if (this.mc.gameSettings.anaglyph) {
			GL11.glTranslatef((-(eye * 2 - 1)) * f1, 0.0F, 0.0F);
		}
		
		if (this.cameraZoom != 1.0D) {
			GL11.glTranslatef((float) this.cameraYaw, (float) (-this.cameraPitch), 0.0F);
			GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
		}
		
		Project.gluPerspective(this.getFOVModifier(partialTickTime, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
		float f2;
		
		if (this.mc.playerController.enableEverythingIsScrewedUpMode()) {
			f2 = 0.6666667F;
			GL11.glScalef(1.0F, f2, 1.0F);
		}
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		if (this.mc.gameSettings.anaglyph) {
			GL11.glTranslatef((eye * 2 - 1) * 0.1F, 0.0F, 0.0F);
		}
		
		this.hurtCameraEffect(partialTickTime);
		
		if (this.mc.gameSettings.viewBobbing) {
			this.setupViewBobbing(partialTickTime);
		}
		
		f2 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTickTime;
		
		if (f2 > 0.0F) {
			byte b0 = 20;
			
			if (this.mc.thePlayer.isPotionActive(Potion.confusion)) {
				b0 = 7;
			}
			
			float f3 = 5.0F / (f2 * f2 + 5.0F) - f2 * 0.04F;
			f3 *= f3;
			GL11.glRotatef((this.rendererUpdateCount + partialTickTime) * b0, 0.0F, 1.0F, 1.0F);
			GL11.glScalef(1.0F / f3, 1.0F, 1.0F);
			GL11.glRotatef(-(this.rendererUpdateCount + partialTickTime) * b0, 0.0F, 1.0F, 1.0F);
		}
		
		this.orientCamera(partialTickTime);
		
		if (this.debugViewDirection > 0) {
			int j = this.debugViewDirection - 1;
			
			if (j == 1) {
				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
			}
			
			if (j == 2) {
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			}
			
			if (j == 3) {
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			}
			
			if (j == 4) {
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
			}
			
			if (j == 5) {
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
			}
		}
	}
	
	/**
	 * Render player hand
	 */
	private void renderHand(float partialTickTime, int eye) {
		if (this.debugViewDirection <= 0) {
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			float f1 = 0.07F;
			
			if (this.mc.gameSettings.anaglyph) {
				GL11.glTranslatef((-(eye * 2 - 1)) * f1, 0.0F, 0.0F);
			}
			
			if (this.cameraZoom != 1.0D) {
				GL11.glTranslatef((float) this.cameraYaw, (float) (-this.cameraPitch), 0.0F);
				GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
			}
			
			Project.gluPerspective(this.getFOVModifier(partialTickTime, false), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
			
			if (this.mc.playerController.enableEverythingIsScrewedUpMode()) {
				float f2 = 0.6666667F;
				GL11.glScalef(1.0F, f2, 1.0F);
			}
			GL11.glScalef(0.2F, 0.2f, 0.2F);
			
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			
			if (this.mc.gameSettings.anaglyph) {
				GL11.glTranslatef((eye * 2 - 1) * 0.1F, 0.0F, 0.0F);
			}
			
			GL11.glPushMatrix();
			this.hurtCameraEffect(partialTickTime);
			
			if (this.mc.gameSettings.viewBobbing) {
				this.setupViewBobbing(partialTickTime);
			}
			
			if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping() && !this.mc.gameSettings.hideGUI && !this.mc.playerController.enableEverythingIsScrewedUpMode()) {
				this.enableLightmap(partialTickTime);
				this.itemRenderer.renderItemInFirstPerson(partialTickTime);
				this.disableLightmap(partialTickTime);
			}
			
			GL11.glPopMatrix();
			
			if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping()) {
				this.itemRenderer.renderOverlays(partialTickTime);
				this.hurtCameraEffect(partialTickTime);
			}
			
			if (this.mc.gameSettings.viewBobbing) {
				this.setupViewBobbing(partialTickTime);
			}
		}
	}
	
	/**
	 * Disable secondary texture unit used by lightmap
	 */
	@SuppressWarnings({ "static-method", "unused" })
	public void disableLightmap(double partialTickTime) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	/**
	 * Enable lightmap in secondary texture unit
	 */
	@SuppressWarnings("unused")
	public void enableLightmap(double partialTickTime) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		float f = 0.00390625F;
		GL11.glScalef(f, f, f);
		GL11.glTranslatef(8.0F, 8.0F, 8.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.mc.getTextureManager().bindTexture(this.locationLightMap);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	/**
	 * Recompute a random value that is applied to block color in
	 * updateLightmap()
	 */
	private void updateTorchFlicker() {
		this.torchFlickerDX = (float) (this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.torchFlickerDY = (float) (this.torchFlickerDY + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.torchFlickerDX = (float) (this.torchFlickerDX * 0.9D);
		this.torchFlickerDY = (float) (this.torchFlickerDY * 0.9D);
		this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX) * 1.0F;
		this.torchFlickerY += (this.torchFlickerDY - this.torchFlickerY) * 1.0F;
		this.lightmapUpdateNeeded = true;
	}
	
	private void updateLightmap(float partialTickTime) {
		WorldClient worldclient = this.mc.theWorld;
		
		if (worldclient != null) {
			for (int i = 0; i < 256; ++i) {
				float f1 = worldclient.getSunBrightness(1.0F) * 0.95F + 0.05F;
				float f2 = worldclient.provider.lightBrightnessTable[i / 16] * f1;
				float f3 = worldclient.provider.lightBrightnessTable[i % 16] * (this.torchFlickerX * 0.1F + 1.5F);
				
				if (worldclient.lastLightningBolt > 0) {
					f2 = worldclient.provider.lightBrightnessTable[i / 16];
				}
				
				float f4 = f2 * (worldclient.getSunBrightness(1.0F) * 0.65F + 0.35F);
				float f5 = f2 * (worldclient.getSunBrightness(1.0F) * 0.65F + 0.35F);
				float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
				float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
				float f8 = f4 + f3;
				float f9 = f5 + f6;
				float f10 = f2 + f7;
				f8 = f8 * 0.96F + 0.03F;
				f9 = f9 * 0.96F + 0.03F;
				f10 = f10 * 0.96F + 0.03F;
				float f11;
				
				if (this.bossColorModifier > 0.0F) {
					f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTickTime;
					f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
					f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
					f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
				}
				
				if (worldclient.provider.dimensionId == 1) {
					f8 = 0.22F + f3 * 0.75F;
					f9 = 0.28F + f6 * 0.75F;
					f10 = 0.25F + f7 * 0.75F;
				}
				
				float f12;
				
				if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
					f11 = this.getNightVisionBrightness(this.mc.thePlayer, partialTickTime);
					f12 = 1.0F / f8;
					
					if (f12 > 1.0F / f9) {
						f12 = 1.0F / f9;
					}
					
					if (f12 > 1.0F / f10) {
						f12 = 1.0F / f10;
					}
					
					f8 = f8 * (1.0F - f11) + f8 * f12 * f11;
					f9 = f9 * (1.0F - f11) + f9 * f12 * f11;
					f10 = f10 * (1.0F - f11) + f10 * f12 * f11;
				}
				
				if (f8 > 1.0F) {
					f8 = 1.0F;
				}
				
				if (f9 > 1.0F) {
					f9 = 1.0F;
				}
				
				if (f10 > 1.0F) {
					f10 = 1.0F;
				}
				
				f11 = this.mc.gameSettings.gammaSetting;
				f12 = 1.0F - f8;
				float f13 = 1.0F - f9;
				float f14 = 1.0F - f10;
				f12 = 1.0F - f12 * f12 * f12 * f12;
				f13 = 1.0F - f13 * f13 * f13 * f13;
				f14 = 1.0F - f14 * f14 * f14 * f14;
				f8 = f8 * (1.0F - f11) + f12 * f11;
				f9 = f9 * (1.0F - f11) + f13 * f11;
				f10 = f10 * (1.0F - f11) + f14 * f11;
				f8 = f8 * 0.96F + 0.03F;
				f9 = f9 * 0.96F + 0.03F;
				f10 = f10 * 0.96F + 0.03F;
				
				if (f8 > 1.0F) {
					f8 = 1.0F;
				}
				
				if (f9 > 1.0F) {
					f9 = 1.0F;
				}
				
				if (f10 > 1.0F) {
					f10 = 1.0F;
				}
				
				if (f8 < 0.0F) {
					f8 = 0.0F;
				}
				
				if (f9 < 0.0F) {
					f9 = 0.0F;
				}
				
				if (f10 < 0.0F) {
					f10 = 0.0F;
				}
				
				short short1 = 255;
				int j = (int) (f8 * 255.0F);
				int k = (int) (f9 * 255.0F);
				int l = (int) (f10 * 255.0F);
				this.lightmapColors[i] = short1 << 24 | j << 16 | k << 8 | l;
			}
			
			this.lightmapTexture.updateDynamicTexture();
			this.lightmapUpdateNeeded = false;
		}
	}
	
	/**
	 * Gets the night vision brightness
	 */
	@SuppressWarnings("static-method")
	private float getNightVisionBrightness(EntityPlayer player, float partialTickTime) {
		int i = player.getActivePotionEffect(Potion.nightVision).getDuration();
		return i > 200 ? 1.0F : 0.7F + MathHelper.sin((i - partialTickTime) * (float) Math.PI * 0.2F) * 0.3F;
	}
	
	/**
	 * Will update any inputs that effect the camera angle (mouse) and then
	 * render the world and GUI
	 */
	public void updateCameraAndRender(float partialTickTime) {
		this.mc.mcProfiler.startSection("lightTex");
		
		if (this.lightmapUpdateNeeded) {
			this.updateLightmap(partialTickTime);
		}
		
		this.mc.mcProfiler.endSection();
		boolean flag = Display.isActive();
		
		if (!flag && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1))) {
			if (Minecraft.getSystemTime() - this.prevFrameTime > 500L) {
				this.mc.displayInGameMenu();
			}
		} else {
			this.prevFrameTime = Minecraft.getSystemTime();
		}
		
		this.mc.mcProfiler.startSection("mouse");
		
		if (this.mc.inGameHasFocus && flag) {
			this.mc.mouseHelper.mouseXYChange();
			float f1 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			float f2 = f1 * f1 * f1 * 8.0F;
			float f3 = this.mc.mouseHelper.deltaX * f2;
			float f4 = this.mc.mouseHelper.deltaY * f2;
			byte b0 = 1;
			
			if (this.mc.gameSettings.invertMouse) {
				b0 = -1;
			}
			
			if (this.mc.gameSettings.smoothCamera) {
				this.smoothCamYaw += f3;
				this.smoothCamPitch += f4;
				float f5 = partialTickTime - this.smoothCamPartialTicks;
				this.smoothCamPartialTicks = partialTickTime;
				f3 = this.smoothCamFilterX * f5;
				f4 = this.smoothCamFilterY * f5;
				this.mc.thePlayer.setAngles(f3, f4 * b0);
			} else {
				this.mc.thePlayer.setAngles(f3, f4 * b0);
			}
		}
		
		this.mc.mcProfiler.endSection();
		
		if (!this.mc.skipRenderWorld) {
			anaglyphEnable = this.mc.gameSettings.anaglyph;
			final ScaledResolution scaledresolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
			int i = scaledresolution.getScaledWidth();
			int j = scaledresolution.getScaledHeight();
			final int k = Mouse.getX() * i / this.mc.displayWidth;
			final int l = j - Mouse.getY() * j / this.mc.displayHeight - 1;
			int i1 = this.mc.gameSettings.limitFramerate;
			
			if (this.mc.theWorld != null) {
				this.mc.mcProfiler.startSection("level");
				
				if (this.mc.isFramerateLimitBelowMax()) {
					this.renderWorld(partialTickTime, this.renderEndNanoTime + 1000000000 / i1);
				} else {
					this.renderWorld(partialTickTime, 0L);
				}
				
				this.renderEndNanoTime = System.nanoTime();
				this.mc.mcProfiler.endStartSection("gui");
				
				if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null) {
					GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
					this.mc.ingameGUI.renderGameOverlay(partialTickTime, this.mc.currentScreen != null, k, l);
				}
				
				this.mc.mcProfiler.endSection();
			} else {
				GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				this.setupOverlayRendering();
				this.renderEndNanoTime = System.nanoTime();
			}
			
			if (this.mc.currentScreen != null) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				
				try {
					this.mc.currentScreen.drawScreen(k, l, partialTickTime);
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
					crashreportcategory.addCrashSectionCallable("Screen name", new Callable<String>() {
						
						@SuppressWarnings({ "unused", "hiding" })
						private static final String __OBFID = "CL_00000948";
						
						@Override
						public String call() {
							return EntityRenderer.this.mc.currentScreen.getClass().getCanonicalName();
						}
					});
					crashreportcategory.addCrashSectionCallable("Mouse location", new Callable<String>() {
						
						@SuppressWarnings({ "unused", "hiding" })
						private static final String __OBFID = "CL_00000950";
						
						@Override
						public String call() {
							return String.format("Scaled: (%d, %d). Absolute: (%d, %d)", new Object[] { Integer.valueOf(k), Integer.valueOf(l), Integer.valueOf(Mouse.getX()), Integer.valueOf(Mouse.getY()) });
						}
					});
					crashreportcategory.addCrashSectionCallable("Screen size", new Callable<String>() {
						
						@SuppressWarnings({ "unused", "hiding" })
						private static final String __OBFID = "CL_00000951";
						
						@Override
						public String call() {
							return String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", new Object[] { Integer.valueOf(scaledresolution.getScaledWidth()), Integer.valueOf(scaledresolution.getScaledHeight()), Integer.valueOf(EntityRenderer.this.mc.displayWidth), Integer.valueOf(EntityRenderer.this.mc.displayHeight), Integer.valueOf(scaledresolution.getScaleFactor()) });
						}
					});
					throw new ReportedException(crashreport);
				}
			}
		}
	}
	
	private void setupReal3D(int eye){
		if (this.mc.gameSettings.anaglyph) {
			anaglyphField = eye;
			if (eye == 0) {
				GL11.glColorMask(false, true, true, true);
			} else {
				GL11.glColorMask(true, false, false, true);
			}
		}
	}
	
	private boolean isReal3D(){
		return this.mc.gameSettings.anaglyph;
	}
	
	private void renderEntities(float partialTickTime, Frustrum frustrum, int pass){
		this.mc.mcProfiler.endStartSection("entities");
		RenderHelper.enableStandardItemLighting();
		ForgeHooksClient.setRenderPass(pass);
		this.mc.renderGlobal.renderEntities(this.mc.renderViewEntity, frustrum, partialTickTime);
		ForgeHooksClient.setRenderPass(-1);
		RenderHelper.disableStandardItemLighting();
	}
	
	private void drawSelectionBox(float partialTickTime){
		EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
		if(this.mc.objectMouseOver != null && entitylivingbase instanceof EntityPlayer && !this.mc.gameSettings.hideGUI && this.cameraZoom == 1.0D){
			this.mc.mcProfiler.endStartSection("outline");
			EntityPlayer player = (EntityPlayer) entitylivingbase;
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			if (!ForgeHooksClient.onDrawBlockHighlight(this.mc.renderGlobal, player, this.mc.objectMouseOver, 0, player.inventory.getCurrentItem(), partialTickTime)) {
				this.mc.renderGlobal.drawSelectionBox(player, this.mc.objectMouseOver, 0, partialTickTime);
			}
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}
	}
	
	private void updateRenderers(long time){
		this.mc.mcProfiler.endStartSection("updatechunks");
		
		while (!this.mc.renderGlobal.updateRenderers(this.mc.renderViewEntity, false) && time != 0L) {
			long k = time - System.nanoTime();
			
			if (k < 0L || k > 1000000000L) {
				break;
			}
		}
	}
	
	private void renderTerrain(float partialTickTime){
		this.mc.mcProfiler.endStartSection("prepareterrain");
		this.setupFog(0, partialTickTime);
		GL11.glEnable(GL11.GL_FOG);
		this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		RenderHelper.disableStandardItemLighting();
		this.mc.mcProfiler.endStartSection("terrain");
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		this.mc.renderGlobal.sortAndRender(this.mc.renderViewEntity, 0, partialTickTime);
		GL11.glPopMatrix();
	}
	
	private void renderLitParticles(float partialTickTime){
		this.mc.mcProfiler.endStartSection("litParticles");
		RenderHelper.enableStandardItemLighting();
		this.enableLightmap(partialTickTime);
		this.mc.effectRenderer.renderLitParticles(this.mc.renderViewEntity, partialTickTime);
		this.disableLightmap(partialTickTime);
		RenderHelper.disableStandardItemLighting();
	}
	
	private void renderParticles(float partialTickTime){
		this.mc.mcProfiler.endStartSection("particles");
		this.setupFog(0, partialTickTime);
		this.enableLightmap(partialTickTime);
		this.mc.effectRenderer.renderParticles(this.mc.renderViewEntity, partialTickTime);
		this.disableLightmap(partialTickTime);
	}
	
	private void drawBlockDamageTexture(float partialTickTime){
		this.mc.mcProfiler.endStartSection("destroyProgress");
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 1, 1, 0);
		this.mc.renderGlobal.drawBlockDamageTexture(Tessellator.instance, this.mc.renderViewEntity, partialTickTime);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void renderWeather(float partialTickTime){
		this.mc.mcProfiler.endStartSection("weather");
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_CULL_FACE);
		this.renderRainSnow(partialTickTime);
		GL11.glDepthMask(true);
	}
	
	private void renderWater(float partialTickTime){
		this.mc.mcProfiler.endStartSection("water");
		GL11.glEnable(GL11.GL_CULL_FACE);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		this.setupFog(0, partialTickTime);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(false);
		this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		
		if (this.mc.gameSettings.fancyGraphics) {
			
			if (this.mc.gameSettings.ambientOcclusion != 0) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
			}
			
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			
			this.mc.renderGlobal.sortAndRender(this.mc.renderViewEntity, 1, partialTickTime);
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glShadeModel(GL11.GL_FLAT);
		} else {
			this.mc.renderGlobal.sortAndRender(this.mc.renderViewEntity, 1, partialTickTime);
		}
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private Frustrum setupFrustrum(float partialTickTime){
		this.mc.mcProfiler.endStartSection("culling");
		EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
		double d0 = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * partialTickTime;
		double d1 = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * partialTickTime;
		double d2 = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * partialTickTime;
		Frustrum frustrum = new Frustrum();
		frustrum.setPosition(d0, d1, d2);
		this.mc.renderGlobal.clipRenderersByFrustum(frustrum, partialTickTime);
		return frustrum;
	}
	
	private void renderSky(float partialTickTime){
		if (this.mc.gameSettings.renderDistanceChunks >= 4) {
			this.mc.mcProfiler.endStartSection("sky");
			this.setupFog(-1, partialTickTime);
			this.mc.renderGlobal.renderSky(partialTickTime);
		}
	}
	
	private void renderHand2(float partialTickTime, int eye){
		this.mc.mcProfiler.endStartSection("hand");
		((ITessellator)Tessellator.instance).setMaterialID(0);
		if (!ForgeHooksClient.renderFirstPersonHand(this.mc.renderGlobal, partialTickTime, eye) && this.cameraZoom == 1.0D) {
			this.renderHand(partialTickTime, eye);
		}
	}
	
	private void renderClouds(float partialTickTime, boolean over){
		if(over){
			if (this.mc.renderViewEntity.posY >= 128.0D) {
				this.mc.mcProfiler.endStartSection("aboveClouds");
				this.renderCloudsCheck(this.mc.renderGlobal, partialTickTime);
			}
		}else{
			if (this.mc.renderViewEntity.posY < 128.0D) {
				this.renderCloudsCheck(this.mc.renderGlobal, partialTickTime);
			}
		}
	}
	
	private void forgeRenderLast(float partialTickTime){
		this.mc.mcProfiler.endStartSection("FRenderLast");
		ForgeHooksClient.dispatchRenderLast(this.mc.renderGlobal, partialTickTime);
	}
	
	private void renderClear(float partialTickTime){
		this.mc.mcProfiler.endStartSection("clear");
		GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		this.updateFogColor(partialTickTime);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	private void setupCamera(float partialTickTime, int eye){
		this.mc.mcProfiler.endStartSection("camera");
		this.setupCameraTransform(partialTickTime, eye);
		ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
	}
	
	private void setupFrustrum(){
		this.mc.mcProfiler.endStartSection("frustrum");
		ClippingHelperImpl.getInstance();
	}
	
	private void renderWorld(float partialTickTime, long time, int eye){
		renderClear(partialTickTime);
		GL11.glEnable(GL11.GL_CULL_FACE);
		setupCamera(partialTickTime, eye);
		setupFrustrum();
		
		renderSky(partialTickTime);
		
		GL11.glEnable(GL11.GL_FOG);
		this.setupFog(1, partialTickTime);
		
		if (this.mc.gameSettings.ambientOcclusion != 0) {
			GL11.glShadeModel(GL11.GL_SMOOTH);
		}
		
		Frustrum frustrum = setupFrustrum(partialTickTime);
		
		if (eye == 0) {
			updateRenderers(time);
		}
		
		renderClouds(partialTickTime, false);
		
		renderTerrain(partialTickTime);
		
		if (this.debugViewDirection == 0) {
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			renderEntities(partialTickTime, frustrum, 0);
			renderLitParticles(partialTickTime);
			renderParticles(partialTickTime);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
			
			drawSelectionBox(partialTickTime);
		}else if (!this.mc.renderViewEntity.isInsideOfMaterial(Material.water)){
			drawSelectionBox(partialTickTime);
		}
		
		drawBlockDamageTexture(partialTickTime);
		
		renderWeather(partialTickTime);

		renderWater(partialTickTime);
		
		if (this.debugViewDirection == 0){
			GL11.glEnable(GL11.GL_CULL_FACE);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			this.setupFog(0, partialTickTime);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
			renderEntities(partialTickTime, frustrum, 1);
		}
		
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_FOG);
		
		renderClouds(partialTickTime, true);
		
		forgeRenderLast(partialTickTime);
		
		renderHand2(partialTickTime, eye);
	}
	
	private static void startRenderWorld(){
		Globals.gBuffer.bind();
		Globals.gShader.bind();
		Globals.gShader.uniformInteger("Texture1", 1);
	}
	
	private static void stopRenderWorld(){
		FBO.unBind();
		Utils.mc().getFramebuffer().bindFramebuffer(false);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getDephtBuffer());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getColorBuffer(0));
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getColorBuffer(1));
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getColorBuffer(2));
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		Globals.mShader.bind();
		Globals.mShader.uniformInteger("gBuffer_Depth", 0);
		Globals.mShader.uniformInteger("gBuffer_Color", 1);
		Globals.mShader.uniformInteger("gBuffer_Normal", 2);
		Globals.mShader.uniformInteger("gBuffer_Material", 3);
		Globals.mShader.uniformFloat("textureSize", 2048);
		float width = Utils.mc().displayWidth;
		float height = Utils.mc().displayHeight;
		Globals.mShader.uniformVec2("textureScale", width/2048.0f, height/2048.0f);
		Globals.mShader.uniformFloat("depthRange_Near", 0.05F);
		Globals.mShader.uniformFloat("depthRange_Far", Utils.mc().gameSettings.renderDistanceChunks * 32);
		Globals.mShader.uniformFloat("depthRange_Diff", Utils.mc().gameSettings.renderDistanceChunks * 32 - 0.05F);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(false);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(-1);
		tessellator.addVertexWithUV(-1, -1, 0, 0, 0);
		tessellator.addVertexWithUV(1, -1, 0, 1, 0);
		tessellator.addVertexWithUV(1, 1, 0, 1, 1);
		tessellator.addVertexWithUV(-1, 1, 0, 0,1);
		tessellator.draw();
		Shader.unBind();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}
	
	public void renderWorld(float partialTickTime, long time) {
		startRenderWorld();
		this.mc.mcProfiler.startSection("lightTex");
		
		if (this.lightmapUpdateNeeded) {
			this.updateLightmap(partialTickTime);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);
		
		if (this.mc.renderViewEntity == null) {
			this.mc.renderViewEntity = this.mc.thePlayer;
		}
		
		this.mc.mcProfiler.endStartSection("pick");
		this.getMouseOver(partialTickTime);
		this.mc.mcProfiler.endStartSection("center");
		
		for (int eye = 0; eye < 2; ++eye) {
			setupReal3D(eye);
			
			renderWorld(partialTickTime, time, eye);
			
			if (!isReal3D()) {
				stopRenderWorld();
				this.mc.mcProfiler.endSection();
				return;
			}
		}
		
		GL11.glColorMask(true, true, true, false);
		stopRenderWorld();
		this.mc.mcProfiler.endSection();
	}
	
	/**
	 * Render clouds if enabled
	 */
	private void renderCloudsCheck(RenderGlobal renderGlobal, float partialTickTime) {
		if (this.mc.gameSettings.shouldRenderClouds()) {
			this.mc.mcProfiler.endStartSection("clouds");
			GL11.glPushMatrix();
			this.setupFog(0, partialTickTime);
			GL11.glEnable(GL11.GL_FOG);
			renderGlobal.renderClouds(partialTickTime);
			GL11.glDisable(GL11.GL_FOG);
			this.setupFog(1, partialTickTime);
			GL11.glPopMatrix();
		}
	}
	
	private void addRainParticles() {
		float f = this.mc.theWorld.getRainStrength(1.0F);
		
		if (!this.mc.gameSettings.fancyGraphics) {
			f /= 2.0F;
		}
		
		if (f != 0.0F) {
			this.random.setSeed(this.rendererUpdateCount * 312987231L);
			EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
			WorldClient worldclient = this.mc.theWorld;
			int i = MathHelper.floor_double(entitylivingbase.posX);
			int j = MathHelper.floor_double(entitylivingbase.posY);
			int k = MathHelper.floor_double(entitylivingbase.posZ);
			byte b0 = 10;
			double d0 = 0.0D;
			double d1 = 0.0D;
			double d2 = 0.0D;
			int l = 0;
			int i1 = (int) (100.0F * f * f);
			
			if (this.mc.gameSettings.particleSetting == 1) {
				i1 >>= 1;
			} else if (this.mc.gameSettings.particleSetting == 2) {
				i1 = 0;
			}
			
			for (int j1 = 0; j1 < i1; ++j1) {
				int k1 = i + this.random.nextInt(b0) - this.random.nextInt(b0);
				int l1 = k + this.random.nextInt(b0) - this.random.nextInt(b0);
				int i2 = worldclient.getPrecipitationHeight(k1, l1);
				Block block = worldclient.getBlock(k1, i2 - 1, l1);
				BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(k1, l1);
				
				if (i2 <= j + b0 && i2 >= j - b0 && biomegenbase.canSpawnLightningBolt() && biomegenbase.getFloatTemperature(k1, i2, l1) >= 0.15F) {
					float f1 = this.random.nextFloat();
					float f2 = this.random.nextFloat();
					
					if (block.getMaterial() == Material.lava) {
						this.mc.effectRenderer.addEffect(new EntitySmokeFX(worldclient, k1 + f1, i2 + 0.1F - block.getBlockBoundsMinY(), l1 + f2, 0.0D, 0.0D, 0.0D));
					} else if (block.getMaterial() != Material.air) {
						++l;
						
						if (this.random.nextInt(l) == 0) {
							d0 = k1 + f1;
							d1 = i2 + 0.1F - block.getBlockBoundsMinY();
							d2 = l1 + f2;
						}
						
						this.mc.effectRenderer.addEffect(new EntityRainFX(worldclient, k1 + f1, i2 + 0.1F - block.getBlockBoundsMinY(), l1 + f2));
					}
				}
			}
			
			if (l > 0 && this.random.nextInt(3) < this.rainSoundCounter++) {
				this.rainSoundCounter = 0;
				
				if (d1 > entitylivingbase.posY + 1.0D && worldclient.getPrecipitationHeight(MathHelper.floor_double(entitylivingbase.posX), MathHelper.floor_double(entitylivingbase.posZ)) > MathHelper.floor_double(entitylivingbase.posY)) {
					this.mc.theWorld.playSound(d0, d1, d2, "ambient.weather.rain", 0.1F, 0.5F, false);
				} else {
					this.mc.theWorld.playSound(d0, d1, d2, "ambient.weather.rain", 0.2F, 1.0F, false);
				}
			}
		}
	}
	
	/**
	 * Render rain and snow
	 */
	protected void renderRainSnow(float partialTickTime) {
		IRenderHandler renderer = null;
		if ((renderer = this.mc.theWorld.provider.getWeatherRenderer()) != null) {
			renderer.render(partialTickTime, this.mc.theWorld, this.mc);
			return;
		}
		
		float f1 = this.mc.theWorld.getRainStrength(partialTickTime);
		
		if (f1 > 0.0F) {
			this.enableLightmap(partialTickTime);
			
			if (this.rainXCoords == null) {
				this.rainXCoords = new float[1024];
				this.rainYCoords = new float[1024];
				
				for (int i = 0; i < 32; ++i) {
					for (int j = 0; j < 32; ++j) {
						float f2 = j - 16;
						float f3 = i - 16;
						float f4 = MathHelper.sqrt_float(f2 * f2 + f3 * f3);
						this.rainXCoords[i << 5 | j] = -f3 / f4;
						this.rainYCoords[i << 5 | j] = f2 / f4;
					}
				}
			}
			
			EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
			WorldClient worldclient = this.mc.theWorld;
			int k2 = MathHelper.floor_double(entitylivingbase.posX);
			int l2 = MathHelper.floor_double(entitylivingbase.posY);
			int i3 = MathHelper.floor_double(entitylivingbase.posZ);
			Tessellator tessellator = Tessellator.instance;
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			double d0 = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * partialTickTime;
			double d1 = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * partialTickTime;
			double d2 = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * partialTickTime;
			int k = MathHelper.floor_double(d1);
			byte b0 = 5;
			
			if (this.mc.gameSettings.fancyGraphics) {
				b0 = 10;
			}
			
			byte b1 = -1;
			float f5 = this.rendererUpdateCount + partialTickTime;
			
			if (this.mc.gameSettings.fancyGraphics) {
				b0 = 10;
			}
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			
			for (int l = i3 - b0; l <= i3 + b0; ++l) {
				for (int i1 = k2 - b0; i1 <= k2 + b0; ++i1) {
					int j1 = (l - i3 + 16) * 32 + i1 - k2 + 16;
					float f6 = this.rainXCoords[j1] * 0.5F;
					float f7 = this.rainYCoords[j1] * 0.5F;
					BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(i1, l);
					
					if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()) {
						int k1 = worldclient.getPrecipitationHeight(i1, l);
						int l1 = l2 - b0;
						int i2 = l2 + b0;
						
						if (l1 < k1) {
							l1 = k1;
						}
						
						if (i2 < k1) {
							i2 = k1;
						}
						
						float f8 = 1.0F;
						int j2 = k1;
						
						if (k1 < k) {
							j2 = k;
						}
						
						if (l1 != i2) {
							this.random.setSeed(i1 * i1 * 3121 + i1 * 45238971 ^ l * l * 418711 + l * 13761);
							float f9 = biomegenbase.getFloatTemperature(i1, l1, l);
							float f10;
							double d4;
							
							if (worldclient.getWorldChunkManager().getTemperatureAtHeight(f9, k1) >= 0.15F) {
								if (b1 != 0) {
									if (b1 >= 0) {
										tessellator.draw();
									}
									
									b1 = 0;
									this.mc.getTextureManager().bindTexture(locationRainPng);
									tessellator.startDrawingQuads();
								}
								
								f10 = ((this.rendererUpdateCount + i1 * i1 * 3121 + i1 * 45238971 + l * l * 418711 + l * 13761 & 31) + partialTickTime) / 32.0F * (3.0F + this.random.nextFloat());
								double d3 = i1 + 0.5F - entitylivingbase.posX;
								d4 = l + 0.5F - entitylivingbase.posZ;
								float f12 = MathHelper.sqrt_double(d3 * d3 + d4 * d4) / b0;
								float f13 = 1.0F;
								tessellator.setBrightness(worldclient.getLightBrightnessForSkyBlocks(i1, j2, l, 0));
								tessellator.setColorRGBA_F(f13, f13, f13, ((1.0F - f12 * f12) * 0.5F + 0.5F) * f1);
								tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
								tessellator.addVertexWithUV(i1 - f6 + 0.5D, l1, l - f7 + 0.5D, 0.0F * f8, l1 * f8 / 4.0F + f10 * f8);
								tessellator.addVertexWithUV(i1 + f6 + 0.5D, l1, l + f7 + 0.5D, 1.0F * f8, l1 * f8 / 4.0F + f10 * f8);
								tessellator.addVertexWithUV(i1 + f6 + 0.5D, i2, l + f7 + 0.5D, 1.0F * f8, i2 * f8 / 4.0F + f10 * f8);
								tessellator.addVertexWithUV(i1 - f6 + 0.5D, i2, l - f7 + 0.5D, 0.0F * f8, i2 * f8 / 4.0F + f10 * f8);
								tessellator.setTranslation(0.0D, 0.0D, 0.0D);
							} else {
								if (b1 != 1) {
									if (b1 >= 0) {
										tessellator.draw();
									}
									
									b1 = 1;
									this.mc.getTextureManager().bindTexture(locationSnowPng);
									tessellator.startDrawingQuads();
								}
								
								f10 = ((this.rendererUpdateCount & 511) + partialTickTime) / 512.0F;
								float f16 = this.random.nextFloat() + f5 * 0.01F * (float) this.random.nextGaussian();
								float f11 = this.random.nextFloat() + f5 * (float) this.random.nextGaussian() * 0.001F;
								d4 = i1 + 0.5F - entitylivingbase.posX;
								double d5 = l + 0.5F - entitylivingbase.posZ;
								float f14 = MathHelper.sqrt_double(d4 * d4 + d5 * d5) / b0;
								float f15 = 1.0F;
								tessellator.setBrightness((worldclient.getLightBrightnessForSkyBlocks(i1, j2, l, 0) * 3 + 15728880) / 4);
								tessellator.setColorRGBA_F(f15, f15, f15, ((1.0F - f14 * f14) * 0.3F + 0.5F) * f1);
								tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
								tessellator.addVertexWithUV(i1 - f6 + 0.5D, l1, l - f7 + 0.5D, 0.0F * f8 + f16, l1 * f8 / 4.0F + f10 * f8 + f11);
								tessellator.addVertexWithUV(i1 + f6 + 0.5D, l1, l + f7 + 0.5D, 1.0F * f8 + f16, l1 * f8 / 4.0F + f10 * f8 + f11);
								tessellator.addVertexWithUV(i1 + f6 + 0.5D, i2, l + f7 + 0.5D, 1.0F * f8 + f16, i2 * f8 / 4.0F + f10 * f8 + f11);
								tessellator.addVertexWithUV(i1 - f6 + 0.5D, i2, l - f7 + 0.5D, 0.0F * f8 + f16, i2 * f8 / 4.0F + f10 * f8 + f11);
								tessellator.setTranslation(0.0D, 0.0D, 0.0D);
							}
						}
					}
				}
			}
			
			if (b1 >= 0) {
				tessellator.draw();
			}
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			this.disableLightmap(partialTickTime);
		}
	}
	
	/**
	 * Setup orthogonal projection for rendering GUI screen overlays
	 */
	public void setupOverlayRendering() {
		ScaledResolution scaledresolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}
	
	/**
	 * calculates fog and calls glClearColor
	 */
	private void updateFogColor(float partialTickTime) {
		WorldClient worldclient = this.mc.theWorld;
		EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
		float f1 = 0.25F + 0.75F * this.mc.gameSettings.renderDistanceChunks / 16.0F;
		f1 = 1.0F - (float) Math.pow(f1, 0.25D);
		Vec3 vec3 = worldclient.getSkyColor(this.mc.renderViewEntity, partialTickTime);
		float f2 = (float) vec3.xCoord;
		float f3 = (float) vec3.yCoord;
		float f4 = (float) vec3.zCoord;
		Vec3 vec31 = worldclient.getFogColor(partialTickTime);
		this.fogColorRed = (float) vec31.xCoord;
		this.fogColorGreen = (float) vec31.yCoord;
		this.fogColorBlue = (float) vec31.zCoord;
		float f5;
		
		if (this.mc.gameSettings.renderDistanceChunks >= 4) {
			Vec3 vec32 = MathHelper.sin(worldclient.getCelestialAngleRadians(partialTickTime)) > 0.0F ? worldclient.getWorldVec3Pool().getVecFromPool(-1.0D, 0.0D, 0.0D) : worldclient.getWorldVec3Pool().getVecFromPool(1.0D, 0.0D, 0.0D);
			f5 = (float) entitylivingbase.getLook(partialTickTime).dotProduct(vec32);
			
			if (f5 < 0.0F) {
				f5 = 0.0F;
			}
			
			if (f5 > 0.0F) {
				float[] afloat = worldclient.provider.calcSunriseSunsetColors(worldclient.getCelestialAngle(partialTickTime),partialTickTime);
				
				if (afloat != null) {
					f5 *= afloat[3];
					this.fogColorRed = this.fogColorRed * (1.0F - f5) + afloat[0] * f5;
					this.fogColorGreen = this.fogColorGreen * (1.0F - f5) + afloat[1] * f5;
					this.fogColorBlue = this.fogColorBlue * (1.0F - f5) + afloat[2] * f5;
				}
			}
		}
		
		this.fogColorRed += (f2 - this.fogColorRed) * f1;
		this.fogColorGreen += (f3 - this.fogColorGreen) * f1;
		this.fogColorBlue += (f4 - this.fogColorBlue) * f1;
		float f8 = worldclient.getRainStrength(partialTickTime);
		float f9;
		
		if (f8 > 0.0F) {
			f5 = 1.0F - f8 * 0.5F;
			f9 = 1.0F - f8 * 0.4F;
			this.fogColorRed *= f5;
			this.fogColorGreen *= f5;
			this.fogColorBlue *= f9;
		}
		
		f5 = worldclient.getWeightedThunderStrength(partialTickTime);
		
		if (f5 > 0.0F) {
			f9 = 1.0F - f5 * 0.5F;
			this.fogColorRed *= f9;
			this.fogColorGreen *= f9;
			this.fogColorBlue *= f9;
		}
		
		Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entitylivingbase, partialTickTime);
		float f10;
		
		if (this.cloudFog) {
			Vec3 vec33 = worldclient.getCloudColour(partialTickTime);
			this.fogColorRed = (float) vec33.xCoord;
			this.fogColorGreen = (float) vec33.yCoord;
			this.fogColorBlue = (float) vec33.zCoord;
		} else if (block.getMaterial() == Material.water) {
			f10 = EnchantmentHelper.getRespiration(entitylivingbase) * 0.2F;
			this.fogColorRed = 0.02F + f10;
			this.fogColorGreen = 0.02F + f10;
			this.fogColorBlue = 0.2F + f10;
		} else if (block.getMaterial() == Material.lava) {
			this.fogColorRed = 0.6F;
			this.fogColorGreen = 0.1F;
			this.fogColorBlue = 0.0F;
		}
		
		f10 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * partialTickTime;
		this.fogColorRed *= f10;
		this.fogColorGreen *= f10;
		this.fogColorBlue *= f10;
		double d0 = (entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * partialTickTime) * worldclient.provider.getVoidFogYFactor();
		
		if (entitylivingbase.isPotionActive(Potion.blindness)) {
			int i = entitylivingbase.getActivePotionEffect(Potion.blindness).getDuration();
			
			if (i < 20) {
				d0 *= 1.0F - i / 20.0F;
			} else {
				d0 = 0.0D;
			}
		}
		
		if (d0 < 1.0D) {
			if (d0 < 0.0D) {
				d0 = 0.0D;
			}
			
			d0 *= d0;
			this.fogColorRed = (float) (this.fogColorRed * d0);
			this.fogColorGreen = (float) (this.fogColorGreen * d0);
			this.fogColorBlue = (float) (this.fogColorBlue * d0);
		}
		
		float f11;
		
		if (this.bossColorModifier > 0.0F) {
			f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTickTime;
			this.fogColorRed = this.fogColorRed * (1.0F - f11) + this.fogColorRed * 0.7F * f11;
			this.fogColorGreen = this.fogColorGreen * (1.0F - f11) + this.fogColorGreen * 0.6F * f11;
			this.fogColorBlue = this.fogColorBlue * (1.0F - f11) + this.fogColorBlue * 0.6F * f11;
		}
		
		float f6;
		
		if (entitylivingbase.isPotionActive(Potion.nightVision)) {
			f11 = this.getNightVisionBrightness(this.mc.thePlayer, partialTickTime);
			f6 = 1.0F / this.fogColorRed;
			
			if (f6 > 1.0F / this.fogColorGreen) {
				f6 = 1.0F / this.fogColorGreen;
			}
			
			if (f6 > 1.0F / this.fogColorBlue) {
				f6 = 1.0F / this.fogColorBlue;
			}
			
			this.fogColorRed = this.fogColorRed * (1.0F - f11) + this.fogColorRed * f6 * f11;
			this.fogColorGreen = this.fogColorGreen * (1.0F - f11) + this.fogColorGreen * f6 * f11;
			this.fogColorBlue = this.fogColorBlue * (1.0F - f11) + this.fogColorBlue * f6 * f11;
		}
		
		if (this.mc.gameSettings.anaglyph) {
			f11 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
			f6 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
			float f7 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
			this.fogColorRed = f11;
			this.fogColorGreen = f6;
			this.fogColorBlue = f7;
		}
		
		GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
	}
	
	/**
	 * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts
	 * at 0 and goes to 80% of far plane
	 * distance and is used for sky rendering.
	 */
	private void setupFog(int type, float partialTickTime) {
		EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
		boolean flag = false;
		
		if (entitylivingbase instanceof EntityPlayer) {
			flag = ((EntityPlayer) entitylivingbase).capabilities.isCreativeMode;
		}
		
		if (type == 999) {
			GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, 8.0F);
			
			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi(34138, 34139);
			}
			
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
		} else {
			GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
			GL11.glNormal3f(0.0F, -1.0F, 0.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entitylivingbase, partialTickTime);
			float f1;
			
			if (entitylivingbase.isPotionActive(Potion.blindness)) {
				f1 = 5.0F;
				int j = entitylivingbase.getActivePotionEffect(Potion.blindness).getDuration();
				
				if (j < 20) {
					f1 = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - j / 20.0F);
				}
				
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
				
				if (type < 0) {
					GL11.glFogf(GL11.GL_FOG_START, 0.0F);
					GL11.glFogf(GL11.GL_FOG_END, f1 * 0.8F);
				} else {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.25F);
					GL11.glFogf(GL11.GL_FOG_END, f1);
				}
				
				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(34138, 34139);
				}
			} else if (this.cloudFog) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
			} else if (block.getMaterial() == Material.water) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				
				if (entitylivingbase.isPotionActive(Potion.waterBreathing)) {
					GL11.glFogf(GL11.GL_FOG_DENSITY, 0.05F);
				} else {
					GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F - EnchantmentHelper.getRespiration(entitylivingbase) * 0.03F);
				}
			} else if (block.getMaterial() == Material.lava) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
			} else {
				f1 = this.farPlaneDistance;
				
				if (this.mc.theWorld.provider.getWorldHasVoidParticles() && !flag) {
					double d0 = ((entitylivingbase.getBrightnessForRender(partialTickTime) & 15728640) >> 20) / 16.0D + (entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * partialTickTime + 4.0D) / 32.0D;
					
					if (d0 < 1.0D) {
						if (d0 < 0.0D) {
							d0 = 0.0D;
						}
						
						d0 *= d0;
						float f2 = 100.0F * (float) d0;
						
						if (f2 < 5.0F) {
							f2 = 5.0F;
						}
						
						if (f1 > f2) {
							f1 = f2;
						}
					}
				}
				
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
				
				if (type < 0) {
					GL11.glFogf(GL11.GL_FOG_START, 0.0F);
					GL11.glFogf(GL11.GL_FOG_END, f1);
				} else {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.75F);
					GL11.glFogf(GL11.GL_FOG_END, f1);
				}
				
				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(34138, 34139);
				}
				
				if (this.mc.theWorld.provider.doesXZShowFog((int) entitylivingbase.posX, (int) entitylivingbase.posZ)) {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.05F);
					GL11.glFogf(GL11.GL_FOG_END, Math.min(f1, 192.0F) * 0.5F);
				}
			}
			
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
		}
	}
	
	/**
	 * Update and return fogColorBuffer with the RGBA values passed as arguments
	 */
	private FloatBuffer setFogColorBuffer(float r, float g, float b, float a) {
		this.fogColorBuffer.clear();
		this.fogColorBuffer.put(r).put(g).put(b).put(a);
		this.fogColorBuffer.flip();
		return this.fogColorBuffer;
	}
	
	public MapItemRenderer getMapItemRenderer() {
		return this.theMapItemRenderer;
	}
}