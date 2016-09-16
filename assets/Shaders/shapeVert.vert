uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;

varying vec4 myPos;

void main(){
    vec4 modelSpacePos = vec4(inPosition, 1.0);
    gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
    myPos = modelSpacePos;
}
