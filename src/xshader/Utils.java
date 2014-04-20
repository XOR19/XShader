package xshader;

import java.io.File;

import net.minecraft.client.Minecraft;


public class Utils {

	public static Minecraft mc() {
		return Minecraft.getMinecraft();
	}
	
	public static File getXShaderDir(){
		return new File(Minecraft.getMinecraft().mcDataDir, "xshader");
	}
	
	public static float[] RGB2HSV(float r, float g, float b){
		float min = r;
		float max = r;
		if(g<min){
			min = g;
		}else if(g>max){
			max = g;
		}
		if(b<min){
			min = b;
		}else if(b>max){
			max = b;
		}
		float h;
		if(min==max){
			h=0;
		}else if(max==r){
			h = 60*(g-b)/(max-min);
		}else if(max==g){
			h = 60*(2+(b-r)/(max-min));
		}else{
			h = 60*(4+(r-g)/(max-min));
		}
		if(h<0){
			h += 360;
		}
		float s;
		if(max==0){
			s = 0;
		}else{
			s = (max-min)/max;
		}
		float v = max;
		return new float[]{h, s, v};
	}
	
	public static float[] HSV2RGB(float h, float s, float v){
		int hi = (int) Math.floor(h/60.0f);
		float f = h/60.0f-hi;
		float p = v*(1-s);
		float q = v*(1-s*f);
		float t = v*(1-s*(1-f));
		float r, g, b;
		switch(hi){
		case 0:
		case 6:
			r = v;
			g = t;
			b = p;
			break;
		case 1:
			r = q;
			g = v;
			b = p;
			break;
		case 2:
			r = p;
			g = v;
			b = t;
			break;
		case 3:
			r = p;
			g = q;
			b = v;
			break;
		case 4:
			r = t;
			g = p;
			b = v;
			break;
		case 5:
			r = v;
			g = p;
			b = q;
			break;
		default:
			r = 0;
			g = 0;
			b = 0;
			break;		
		}
		return new float[]{r, g, b};
	}
	
}
