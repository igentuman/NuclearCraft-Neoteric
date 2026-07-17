#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform float ShockwaveCount;
uniform vec4 Shockwave0;
uniform vec4 Shockwave1;
uniform vec4 Shockwave2;
uniform vec4 Shockwave3;
uniform float ShockwaveSoft;

in float vertexDistance;
in vec4 vertexColor;
in vec2 worldXZ;

out vec4 fragColor;

float shockwaveAlpha(vec4 sw) {
    float r = sw.w;
    if (r <= 0.0) return 1.0;
    float d = distance(worldXZ, sw.xz);
    float soft = max(ShockwaveSoft, 0.001);
    if (d <= r - soft) return 0.0;
    if (d >= r + soft) return 1.0;
    return smoothstep(r - soft, r + soft, d);
}

void main() {
    vec4 color = vertexColor;
    if (color.a < 0.1) {
        discard;
    }

    int count = int(ShockwaveCount + 0.5);
    float keep = 1.0;
    if (count > 0) keep = min(keep, shockwaveAlpha(Shockwave0));
    if (count > 1) keep = min(keep, shockwaveAlpha(Shockwave1));
    if (count > 2) keep = min(keep, shockwaveAlpha(Shockwave2));
    if (count > 3) keep = min(keep, shockwaveAlpha(Shockwave3));

    if (keep <= 0.001) discard;
    color.a *= keep;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
