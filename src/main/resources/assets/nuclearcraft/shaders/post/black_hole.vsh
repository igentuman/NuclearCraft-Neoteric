#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <nuclearcraft:black_hole_config.glsl>

out vec2 texCoord;
flat out mat4 inverseProjection;
flat out vec3 rayOrigin;
flat out vec2 centerUV;
flat out float projectedRadius;

void main() {
    // Use the terrain projection, including camera bobbing and screen effects.
    vec4 clip = ProjMat * vec4(CenterAndRadius.xyz, 1.0);
    if (clip.w <= 0.05) {
        gl_Position = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    inverseProjection = inverse(ProjMat);
    // The perspective eye projects to clip w = 0, even with view bobbing.
    vec4 eye = inverseProjection * vec4(0.0, 0.0, -1.0, 0.0);
    rayOrigin = eye.xyz / eye.w;
    centerUV = clip.xy / clip.w * 0.5 + 0.5;
    projectedRadius = abs(ProjMat[1][1] * CenterAndRadius.w / clip.w) * 0.5;

    texCoord = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    gl_Position = vec4(texCoord * 2.0 - 1.0, 0.0, 1.0);
}
