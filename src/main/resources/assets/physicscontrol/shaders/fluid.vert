#version 120

layout (location = 0) in vec3 position;

varying vec4 fColor;

void main() {
//    gl_Position = gl_Vertex;
//    gl_TexCoord[0] = gl_MultiTexCoord0;
//    gl_FrontColor = gl_Color;
    gl_Position = vec4(position.x, position.y, position.z, 1.0);
    fColor = vec4(1.0, 0.0, 0.0, 1.0);
}