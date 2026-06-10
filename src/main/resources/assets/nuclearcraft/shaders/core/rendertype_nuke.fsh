#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float GameTime;

// x = phase tag (0 = fireball, 1 = shockwave ring, 2 = mushroom smoke veil)
// y = phase progress [0..1]
// z = yield factor
// w = master alpha multiplier
uniform vec4 NukeData;

// xMin, yMin, xMax, yMax of the active sprite within the atlas
uniform vec4 SpriteRect;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash12(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i),              hash12(i + vec2(1.0, 0.0)), u.x),
               mix(hash12(i + vec2(0.0,1.0)), hash12(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * vnoise(p);
        p *= 2.07;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 sz = SpriteRect.zw - SpriteRect.xy;
    vec2 local = (texCoord0 - SpriteRect.xy) / max(sz, vec2(1e-5));
    vec2 c = local * 2.0 - 1.0;
    float r = length(c);

    int phase = int(NukeData.x + 0.5);
    float progress = clamp(NukeData.y, 0.0, 1.0);
    float yieldN = NukeData.z;
    float master = NukeData.w;
    float gt = GameTime * 600.0;

    vec4 col = vec4(0.0);

    if (phase == 0) {
        // FIREBALL: hot core + thermal bloom, view-independent volumetric look
        float coreEdge = 0.70 + 0.04 * sin(gt * 0.9);
        float n1 = fbm(local * 5.0 + gt * 0.08);
        float n2 = fbm(local * 2.0 - gt * 0.04);
        float hot   = smoothstep(coreEdge, 0.0, r) * (0.95 + 0.25 * n1);
        float bloom = smoothstep(1.0, coreEdge * 0.85, r) * (0.70 + 0.45 * n2) * (1.0 - progress * 0.45);
        vec3 coreCol  = mix(vec3(1.00, 1.00, 0.92), vec3(1.00, 0.55, 0.18), pow(progress, 0.6));
        vec3 bloomCol = mix(vec3(1.00, 0.65, 0.22), vec3(0.65, 0.22, 0.08), pow(progress, 0.5));
        col.rgb = coreCol * hot * 1.8 + bloomCol * bloom * 1.4;
        col.a = clamp(hot * 1.4 + bloom * 0.9, 0.0, 1.0);
    } else if (phase == 1) {
        // SHOCKWAVE: expanding thin ring on horizontal quad
        float ringR = mix(0.05, 0.95, progress);
        float thick = mix(0.22, 0.05, progress);
        float ring = exp(-pow((r - ringR) / thick, 2.0));
        float n = fbm(c * 4.0 + vec2(gt * 0.05));
        ring *= 0.6 + 0.4 * n;
        vec3 ringCol = mix(vec3(1.00, 0.95, 0.70), vec3(1.00, 0.45, 0.18), progress);
        col.rgb = ringCol * ring;
        col.a = ring * (1.0 - progress * 0.5);
        if (r > 1.0) discard;
    } else {
        // MUSHROOM SMOKE: dense dark cloud body, alpha-blended (NOT additive)
        vec2 q = local * 2.4 + vec2(0.0, -gt * 0.04);
        float n = fbm(q + fbm(q * 1.7));
        float mask = smoothstep(1.0, 0.10, r);
        float density = pow(n, 0.9) * mask;
        // grimy brown-grey gradient: dark interior, lighter dust at edges
        vec3 smoke = mix(vec3(0.08, 0.07, 0.06), vec3(0.42, 0.36, 0.28), density);
        col.rgb = smoke;
        col.a = clamp(density * 1.4, 0.0, 0.95);
    }

    // sprite atlas modulation for grain / detail
    vec2 atlasUV = mix(SpriteRect.xy, SpriteRect.zw, clamp(local, 0.0, 1.0));
    vec4 atlas = texture(Sampler0, atlasUV);
    col.rgb *= 0.7 + 0.5 * atlas.rgb;
    col.a *= mix(1.0, 0.6 + 0.4 * atlas.a, 0.35);

    col.a *= master;
    col *= vertexColor;
    if (col.a < 0.01) discard;
    col *= ColorModulator;
    fragColor = col;
}
