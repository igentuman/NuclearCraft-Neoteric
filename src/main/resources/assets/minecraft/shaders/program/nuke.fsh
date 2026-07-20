#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform vec2 BlurPos;          // screen-space ring center, in UV (0..1)
uniform vec2 Radius;           // x = ring radius (UV, aspect-corrected), y = ring thickness
uniform vec2 BlurDir;          // x = distortion strength, y = brightening multiplier
uniform float BlackHoleDepth;  // depth (0..1) of detonation point; used to skip occluded pixels

out vec4 fragColor;

void main() {
    vec4 src = texture(DiffuseSampler, texCoord);
    float srcDepth = texture(DepthSampler, texCoord).r;

    float ringR = Radius.x;
    float thick = max(Radius.y, 0.003);
    float strength = BlurDir.x;
    float bright = BlurDir.y;

    if (strength <= 0.0001 && bright <= 0.0001) {
        fragColor = src;
        return;
    }

    float aspect = InSize.x / InSize.y;
    vec2 d = vec2((texCoord.x - BlurPos.x) * aspect, texCoord.y - BlurPos.y);
    float r = length(d);

    // wide rejection band: outside outer falloff there is nothing to do
    float outer = ringR + thick * 3.0;
    float inner = max(0.0, ringR - thick * 3.0);
    if (r > outer || r < inner) {
        fragColor = src;
        return;
    }

    // pixels in front of detonation point are NOT distorted (foreground stays crisp)
    if (srcDepth < BlackHoleDepth - 0.0005) {
        fragColor = src;
        return;
    }

    // gaussian ring profile
    float ring = exp(-pow((r - ringR) / thick, 2.0));

    // radial outward displacement around the ring crest
    vec2 dir = (r > 1e-5) ? d / r : vec2(0.0);
    dir.x /= aspect;
    // amplify displacement non-linearly so the ring crest punches hard
    float disp = strength * ring * (0.15 + 0.85 * ring);

    // sign: pixels inside ring pull outward, pixels outside push inward - pinch
    float sgn = sign(ringR - r);
    vec2 sampleUV = clamp(texCoord + dir * disp * sgn, vec2(0.001), vec2(0.999));

    // chromatic split along the displacement axis (scales with displacement)
    float ca = disp * 0.85;
    vec3 col;
    col.r = texture(DiffuseSampler, clamp(sampleUV + dir * ca * sgn, vec2(0.001), vec2(0.999))).r;
    col.g = texture(DiffuseSampler, sampleUV).g;
    col.b = texture(DiffuseSampler, clamp(sampleUV - dir * ca * sgn, vec2(0.001), vec2(0.999))).b;

    // thermal heat haze hue shift at the ring crest
    col = mix(col, col * vec3(1.15, 1.05, 0.85), ring * 0.6);

    // additive crest brightening (compressed fireball glow band)
    col += vec3(1.0, 0.85, 0.55) * ring * bright;

    fragColor = vec4(col, 1.0);
}
