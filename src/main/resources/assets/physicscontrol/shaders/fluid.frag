#version 120

varying vec4 fColor;

void main() {
//    float sum = 0.0;
//    gl_FragColor = vec4(sin(vec3(gl_FragCoord)), 1.0);
    gl_FragColor = fColor;
}