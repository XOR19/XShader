#version 120

uniform sampler2D Texture0;
uniform sampler2D Texture1;
uniform bool texEnabled0;
uniform bool texEnabled1;

varying vec3 position;
varying vec3 normal;

flat varying float vmaterialID;

void main(){
	vec4 color = gl_Color;
	if(texEnabled0){
		color *= texture2D(Texture0, gl_TexCoord[0].xy);
	}
	if(texEnabled1){
	
		color.rgb *= texture2D(Texture1, gl_TexCoord[1].xy).rgb;
	
		/*vec4 lightmap = vec4(0.0f, 0.0f, 0.0f, 1.0f);
		
		//Separate lightmap types
		lightmap.r = clamp((gl_TexCoord[1].s * 33.05 / 32.0) - 1.05 / 32.0, 0.0, 1.0);
		lightmap.b = clamp((gl_TexCoord[1].t * 33.05 / 32.0) - 1.05 / 32.0, 0.0, 1.0);
	
		lightmap.b = pow(lightmap.b, 5.7);
		lightmap.r = pow(lightmap.r, 3.0);
		
		//color.xyz *= lightmap.r;
		color.xyz *= lightmap.b;*/
	}
	if(color.a<0.01)
		discard;
	gl_FragData[0] = color;
	gl_FragData[1] = vec4((normal+1.0)/2.0, 1.0);
	gl_FragData[2] = vec4(vmaterialID, 1.0, 1.0, 1.0);
}

