#version 120

varying vec3 position;
varying vec3 normal;

flat varying float vmaterialID;

attribute float materialID;

void main(){
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  	gl_FrontColor = gl_Color;
  	gl_TexCoord[0] = gl_MultiTexCoord0;
  	gl_TexCoord[1] = gl_TextureMatrix[1]*gl_MultiTexCoord1;
  	normal = normalize(gl_NormalMatrix * gl_Normal);
  	position = vec3(gl_ModelViewMatrix * gl_Vertex);
  	vmaterialID = materialID;
}
