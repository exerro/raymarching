#version 440 core

const int MAX_ITERATIONS = 400;
const float EPSILON = 0.0001;
const vec4 LIGHT_POSITION = normalize(vec4(2, 3, 1, 0));
const float MAX_DISTANCE = 250;

struct Material {
	vec4 colour;
};

struct DistanceData {
	Material material;
	float distance;
};

in vec2 uv;

uniform vec4 ray_position;
uniform mat4 transform;
uniform float FOV;
uniform float aspectRatio;

/*$header*/

float lighting(vec4 position, vec3 normal) {
	vec4 reflection = normalize(reflect(position - ray_position, vec4(normal, 0.0)));
	float ambient = 0.3;
	float diffuse = max(0, dot(vec4(normal, 0.0), LIGHT_POSITION) * 0.7);
	float specular = pow(max(0, dot(reflection, LIGHT_POSITION)), 30) * 0.5;

	return ambient + diffuse + specular;
}

float distance_function(vec4 ray_position) {
	return 0/*$distance_function*/;
}

vec3 estimateNormal(vec4 p) {
	return normalize(vec3(
		distance_function(vec4(p.x + EPSILON, p.y, p.z, 1)) - distance_function(vec4(p.x - EPSILON, p.y, p.z, 1)),
		distance_function(vec4(p.x, p.y + EPSILON, p.z, 1)) - distance_function(vec4(p.x, p.y - EPSILON, p.z, 1)),
		distance_function(vec4(p.x, p.y, p.z  + EPSILON, 1)) - distance_function(vec4(p.x, p.y, p.z - EPSILON, 1))
	));
}

void main(void) {
	vec4 rp = ray_position;
	vec4 rd = transform * vec4(normalize(vec3((uv * 2 - vec2(1, 1)) * vec2(aspectRatio, 1), -1/tan(FOV/2))), 0.0);
	float total_distance = 0;
	int i = 0;

	gl_FragColor = vec4(1, 0, 0, 1);

	for (; i < MAX_ITERATIONS && total_distance < MAX_DISTANCE; ++i) {
		float distance = distance_function(rp);
		total_distance += distance;
		rp += rd * distance;

		if (abs(distance) < 0.001) {
			DistanceData data = /*$data_function*/;
			gl_FragColor = data.material.colour * vec4(vec3(lighting(rp, estimateNormal(rp))), 1);
//			gl_FragColor = vec4(i / 100, 0, 0, 1);
			return;
		}
	}

//	gl_FragColor = vec4(i / MAX_ITERATIONS, 0, 0, 1);
	gl_FragColor = vec4(0, 0, 0, 0);
}
