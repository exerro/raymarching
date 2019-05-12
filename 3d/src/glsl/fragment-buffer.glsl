#version 440 core

const int MAX_ITERATIONS = 255;
const float EPSILON = 0.0001;
const float diffuseLightingIntensity = 0.7;
const float specularLightingIntensity = 0.4;
const float specularLightingPower = 5;

struct Material {
	vec4 colour;
};

struct DistanceData {
	Material material;
	float distance;
};

in vec2 uv;

layout (location = 0) out vec4 fragment_colour;
layout (location = 1) out vec4 fragment_position;
layout (location = 2) out vec4 fragment_normal;
layout (location = 3) out vec4 fragment_lighting;

uniform vec4 ray_position;
uniform mat4 transform;
uniform float FOV;
uniform float aspectRatio;

/*$header*/

DistanceData distance_function(vec4 ray_position) {
	return /*$distance_function*/;
}

vec3 estimateNormal(vec4 p) {
	return normalize(vec3(
		distance_function(vec4(p.x + EPSILON, p.y, p.z, 1)).distance - distance_function(vec4(p.x - EPSILON, p.y, p.z, 1)).distance,
		distance_function(vec4(p.x, p.y + EPSILON, p.z, 1)).distance - distance_function(vec4(p.x, p.y - EPSILON, p.z, 1)).distance,
		distance_function(vec4(p.x, p.y, p.z  + EPSILON, 1)).distance - distance_function(vec4(p.x, p.y, p.z - EPSILON, 1)).distance
	));
}

void main(void) {
	vec4 rp = ray_position;
	vec4 rd = transform * vec4(normalize(vec3((uv * 2 - vec2(1, 1)) * vec2(aspectRatio, 1), -1/tan(FOV/2))), 0.0);
	float total_distance = 0;
	int i = 0;

	for (; i < MAX_ITERATIONS && total_distance < 1000; ++i) {
		DistanceData data = distance_function(rp);
		float distance = data.distance;
		total_distance += distance;
		rp += rd * distance;

		if (abs(distance) < 0.001) {
			vec3 normal = estimateNormal(rp);
			fragment_colour = data.material.colour;
			fragment_position = rp;
			fragment_normal = vec4(normal, 1);
			fragment_lighting = vec4(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, 1);
			return;
		}
	}

	fragment_colour = vec4(0);
	fragment_position = vec4(0);
	fragment_normal = vec4(0);
	fragment_lighting = vec4(0);
}
