
varying mat4 gBufferMatrix;
varying mat4 gBufferMatrixInverse;

void main(){
	gl_Position = gl_Vertex;
	gl_TexCoord[0] = gl_MultiTexCoord0;
	gBufferMatrix = gl_ProjectionMatrix;
	gBufferMatrixInverse = gl_ProjectionMatrixInverse;
}