#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D VolumeSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseProjection;

in vec2 texCoord;
out vec4 fragColor;

const float FAR = 1.0e6;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / p.w;
}

float sceneDistance(ivec2 pixel, ivec2 size, vec3 originView) {
    float depth = texelFetch(DepthSampler, pixel, 0).r;
    if (depth >= 1.0) return FAR;
    return length(viewPosition((vec2(pixel) + 0.5) / vec2(size), depth) - originView);
}

void main() {
    vec3 scene = texture(DiffuseSampler, texCoord).rgb;
    ivec2 depthSize = textureSize(DepthSampler, 0);
    ivec2 volumeSize = textureSize(VolumeSampler, 0);
    vec4 eye = InverseProjection * vec4(0.0, 0.0, -1.0, 0.0);
    vec3 originView = eye.xyz / eye.w;

    ivec2 own = clamp(ivec2(texCoord * vec2(depthSize)), ivec2(0), depthSize - 1);
    float reference = sceneDistance(own, depthSize, originView);
    vec2 position = texCoord * vec2(volumeSize) - 0.5;
    vec2 base = floor(position);
    vec2 f = position - base;

    vec4 sum = vec4(0.0);
    float weights = 0.0;
    vec4 nearest = vec4(0.0, 0.0, 0.0, 1.0);
    float nearestDiff = FAR;
    for (int j = 0; j < 2; j++) {
        for (int i = 0; i < 2; i++) {
            ivec2 texel = clamp(ivec2(base) + ivec2(i, j), ivec2(0), volumeSize - 1);
            vec4 volume = texelFetch(VolumeSampler, texel, 0);
            vec2 uv = (vec2(texel) + 0.5) / vec2(volumeSize);
            ivec2 pixel = clamp(ivec2(uv * vec2(depthSize)), ivec2(0), depthSize - 1);
            float dist = sceneDistance(pixel, depthSize, originView);
            float diff = abs(dist - reference) / max(min(dist, reference), 1.0);
            float bilinear = (i == 0 ? 1.0 - f.x : f.x) * (j == 0 ? 1.0 - f.y : f.y);
            float weight = bilinear * exp(-diff * diff * 400.0);
            sum += volume * weight;
            weights += weight;
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearest = volume;
            }
        }
    }
    vec4 volume = weights > 1.0e-4 ? sum / weights : nearest;
    vec3 emitted = 1.0 - exp(-max(volume.rgb, vec3(0.0)));
    fragColor = vec4(emitted + clamp(volume.a, 0.0, 1.0) * scene, 1.0);
}
