#version 440 core

layout (location=0) in vec3 vertex;
layout (location=1) in vec2 vertex_uv;

out vec2 uv;

void main(void) {
    gl_Position = vec4(vertex, 1);
    uv = vertex_uv;
}
