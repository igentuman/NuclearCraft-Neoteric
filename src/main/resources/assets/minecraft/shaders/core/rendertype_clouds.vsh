#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

uniform vec3 CloudOriginWorld;

out float vertexDistance;
out vec4 vertexColor;
out vec2 worldXZ;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(ModelViewMat, Position, 0);
    vertexColor = Color * ColorModulator;
    worldXZ = CloudOriginWorld.xz + Position.xz;
}
