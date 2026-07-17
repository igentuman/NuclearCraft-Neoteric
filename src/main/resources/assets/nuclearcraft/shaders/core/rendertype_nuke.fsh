#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1; // noise_channel_0 - low-freq base
uniform sampler2D Sampler2; // noise_channel_1 - mid-freq detail
uniform sampler2D Sampler3; // noise_channel_2 - high-freq grain

uniform vec4 ColorModulator;
uniform float GameTime;

// x = phase tag
//   0 = fireball
//   1 = primary shockwave ring (additive, warm)
//   2 = mushroom smoke cap (alpha)
//   3 = mushroom stem column (alpha)
//   4 = ground dust cloud (alpha)
//   5 = secondary white shockwave (additive)
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

float tfbm(vec2 p) {
    float a = texture(Sampler1, fract(p * 0.50)).r;
    float b = texture(Sampler2, fract(p * 1.27 + vec2(0.13, 0.41))).r;
    float c = texture(Sampler3, fract(p * 2.91 - vec2(0.27, 0.19))).r;
    return a * 0.55 + b * 0.30 + c * 0.15;
}

// Domain-warped variant — gives billowy / convective look.
float tfbm_warp(vec2 p) {
    vec2 w = vec2(tfbm(p + 0.7), tfbm(p - 0.3)) - 0.5;
    return tfbm(p + 1.6 * w);
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
        // FIREBALL: hot core + radial fire licks + thermal bloom
        // Shrunk to fit inside unit circle so quad corners stay invisible.
        float fbScale = 0.88;
        float rf = r / fbScale;
        vec2 cf = c / fbScale;
        float coreEdge = 0.62 + 0.04 * sin(gt * 0.9);
        float n1 = fbm(cf * 5.0 + gt * 0.08);
        float n2 = fbm(cf * 2.0 - gt * 0.04);
        float ang = atan(cf.y, cf.x);
        float flame = fbm(vec2(ang * 2.5, rf * 4.0 - gt * 0.3));
        float licks = clamp(smoothstep(0.55, 0.95, rf + (flame - 0.5) * 0.30)
                          - smoothstep(0.85, 1.00, rf), 0.0, 1.0) * (0.5 + 0.6 * flame);
        float hot   = smoothstep(coreEdge, 0.0, rf) * (0.95 + 0.25 * n1);
        float bloom = smoothstep(0.95, coreEdge * 0.85, rf) * (0.70 + 0.45 * n2) * (1.0 - progress * 0.45);
        float envelope = smoothstep(1.00, 0.82, r);
        vec3 coreCol  = mix(vec3(1.00, 1.00, 0.92), vec3(1.00, 0.55, 0.18), pow(progress, 0.6));
        vec3 bloomCol = mix(vec3(1.00, 0.65, 0.22), vec3(0.65, 0.22, 0.08), pow(progress, 0.5));
        vec3 lickCol  = mix(vec3(1.00, 0.80, 0.30), vec3(1.00, 0.40, 0.10), progress);
        col.rgb = coreCol * hot * 1.8 + bloomCol * bloom * 1.4 + lickCol * licks * 1.6;
        col.a = clamp(hot * 1.4 + bloom * 0.9 + licks * 0.8, 0.0, 1.0) * envelope;
        col.rgb *= envelope;
        if (r > 1.0) discard;
    } else if (phase == 1) {
        // PRIMARY SHOCKWAVE: warm expanding ring on horizontal quad
        float ringR = mix(0.05, 0.95, progress);
        float thick = mix(0.22, 0.05, progress);
        float ring = exp(-pow((r - ringR) / thick, 2.0));
        float n = fbm(c * 4.0 + vec2(gt * 0.05));
        ring *= 0.6 + 0.4 * n;
        vec3 ringCol = mix(vec3(1.00, 0.95, 0.70), vec3(1.00, 0.45, 0.18), progress);
        col.rgb = ringCol * ring;
        col.a = ring * (1.0 - progress * 0.5);
        if (r > 1.0) discard;
    } else if (phase == 2) {
        // MUSHROOM CAP: TOROIDAL cap - outer billowing ring, hot interior visible through hole
        vec2 cs = vec2(c.x, c.y * 1.35);
        float rs = length(cs);
        float ang = atan(cs.y, cs.x);
        float roll = gt * 0.07 + sin(rs * 4.5 - gt * 0.20) * 0.8;
        vec2 cr = vec2(cos(ang + roll), sin(ang + roll)) * rs;
        vec2 q = cr * 2.4 + vec2(sin(gt * 0.03) * 0.4, -gt * 0.05);
        float n = tfbm_warp(q * 0.6);
        float outer = smoothstep(1.0, 0.28, rs);
        float hole = smoothstep(0.30, 0.05, rs);
        float bodyMask = clamp(outer - hole * 0.85, 0.0, 1.0);
        // soft envelope kills any contribution at quad corners
        float envelope = smoothstep(1.05, 0.0, rs);
        float density = pow(n, 0.75) * bodyMask;
        float bottomHole = hole * smoothstep(0.5, -0.9, c.y);
        float hotMask = bottomHole * (0.55 + 0.7 * n) * (1.0 - progress * 0.55) * envelope;
        float rim = smoothstep(0.55, 1.0, rs) * smoothstep(0.4, -0.6, c.y) * (1.0 - progress * 0.5);
        rim *= (0.5 + 0.6 * n) * outer;
        vec3 hot = mix(vec3(1.00, 0.40, 0.10), vec3(1.00, 0.85, 0.45), n);
        vec3 smoke = mix(vec3(0.10, 0.07, 0.05), vec3(0.70, 0.55, 0.40), density);
        col.rgb = smoke * (0.55 + density) + hot * (hotMask * 2.0 + rim * 0.7);
        col.a = clamp(density * 2.4 + hotMask * 1.1 + rim * 0.5, 0.0, 0.99) * envelope;
        if (rs > 1.05) discard;
    } else if (phase == 3) {
        // MUSHROOM STEM: vertical column, rising hot smoke, fire at base
        float xc = c.x;            // -1..1 horizontal
        float yc = local.y;        // 0..1 (0 base, 1 top)
        float widthMask = smoothstep(1.1, 0.25, abs(xc));
        vec2 q = vec2(xc * 1.1, yc * 2.2 - gt * 0.14);
        float n = tfbm_warp(q * 0.55);
        float density = pow(n, 0.7) * widthMask;
        density *= mix(1.0, 0.75, smoothstep(0.85, 1.0, yc));
        float base = smoothstep(0.45, 0.0, yc);
        float fire = base * widthMask * pow(n, 0.4) * (1.0 - progress * 0.6);
        vec3 hot = mix(vec3(1.00, 0.40, 0.10), vec3(1.00, 0.85, 0.45), n);
        vec3 smoke = mix(vec3(0.12, 0.10, 0.08), vec3(0.62, 0.50, 0.36), density);
        col.rgb = smoke * (0.55 + density) + hot * fire * 1.7;
        col.a = clamp(density * 2.0 + fire * 0.9, 0.0, 0.95) * widthMask;
    } else if (phase == 4) {
        // GROUND DUST CLOUD: oblate volumetric billowing dust (vertical billboard)
        vec2 cs = vec2(c.x, c.y * 1.6 + 1.15);
        float rs = length(cs);
        vec2 q = vec2(c.x * 0.9, c.y * 1.2 - gt * 0.02);
        float n = tfbm_warp(q);
        n += 0.35 * texture(Sampler3, fract(c * 1.8 - vec2(gt * 0.015, 0.0))).r;
        float body = smoothstep(1.0, 0.0, rs);
        float density = pow(n * 0.9, 0.65) * body;
        float bottom = smoothstep(0.35, -0.95, c.y) * (1.0 - progress * 0.7);
        float rim = bottom * pow(n, 0.4) * body;
        vec3 dust = mix(vec3(0.18, 0.14, 0.11), vec3(0.72, 0.58, 0.42), density);
        vec3 rimCol = vec3(1.0, 0.50, 0.20) * rim * 0.6;
        col.rgb = dust * (0.5 + density) + rimCol;
        col.a = clamp(density * 2.4, 0.0, 0.92) * (1.0 - progress * 0.45);
        if (rs > 1.05) discard;
    } else {
        // SECONDARY SHOCKWAVE: thin white semi-transparent ring
        float ringR = mix(0.05, 0.95, progress);
        float thick = mix(0.10, 0.025, progress);
        float ring = exp(-pow((r - ringR) / thick, 2.0));
        col.rgb = vec3(1.0, 1.0, 1.0) * ring;
        col.a = ring * 0.55 * (1.0 - progress * 0.7);
        if (r > 1.0) discard;
    }

    if (phase == 0 || phase == 1 || phase == 5) {
        vec2 atlasUV = mix(SpriteRect.xy, SpriteRect.zw, clamp(local, 0.0, 1.0));
        vec4 atlas = texture(Sampler0, atlasUV);
        col.rgb *= 0.7 + 0.5 * atlas.rgb;
        col.a *= mix(1.0, 0.6 + 0.4 * atlas.a, 0.35);
    }

    col.a *= master;
    col *= vertexColor;
    if (col.a < 0.01) discard;
    col *= ColorModulator;
    fragColor = col;
}
