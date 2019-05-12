#version 400 core

layout (location=0) in vec3 vertex;

uniform mat4 transform;
uniform vec2 screenSize;

void main(void) {
    gl_Position = transform * vec4(vertex, 1);
}