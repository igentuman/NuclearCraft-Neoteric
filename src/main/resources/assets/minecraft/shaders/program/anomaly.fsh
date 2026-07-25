#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform vec2 BlurPos;
uniform vec2 Radius;
uniform vec2 BlurDir;
uniform float BlackHoleDepth;
uniform float Time;
uniform int AnomType;
uniform float Intensity;
uniform vec3 AnomColor;
uniform float FxPass;

out vec4 fragColor;

vec2 rotateVector(vec2 v, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return vec2(v.x * c - v.y * s, v.x * s + v.y * c);
}

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float aspect() { return InSize.x / InSize.y; }

vec2 toUV(vec2 cc) {
    float a = aspect();
    return vec2(cc.x / a + BlurPos.x, cc.y + BlurPos.y);
}

vec3 sampleScene(vec2 uv) {
    return texture(DiffuseSampler, clamp(uv, 0.0, 1.0)).rgb;
}

float bolt(float angle, float normDist, float seed, float t) {
    float slot = floor(t * 9.0 + seed * 17.0);
    float life = fract(t * 9.0 + seed * 17.0);
    float lifeMask = smoothstep(0.0, 0.05, life) * (1.0 - smoothstep(0.22, 1.0, life));
    lifeMask *= 0.55 + 0.45 * hash11(slot * 3.1 + floor(t * 45.0));
    float a0 = hash11(slot + seed) * 6.28318;
    float r = normDist;
    float wander = (hash11(slot * 7.0  + floor(r * 6.0)  + seed) - 0.5) * 0.45
                 + (hash11(slot * 13.0 + floor(r * 14.0) + seed) - 0.5) * 0.22
                 + (hash11(slot * 23.0 + floor(r * 30.0) + seed) - 0.5) * 0.11;
    float da = abs(mod(angle - a0 - wander + 3.14159, 6.28318) - 3.14159);
    float thickness = 0.006 + 0.018 * (1.0 - r);
    float coreLine = 1.0 - smoothstep(0.0, thickness, da);
    float glow = (1.0 - smoothstep(0.0, thickness * 6.0, da)) * 0.4;
    float radial = smoothstep(1.05, 0.06, r);
    return (coreLine + glow) * radial * lifeMask;
}

void main() {
    vec4 originalColor = texture(DiffuseSampler, texCoord);

    if (FxPass < 0.5) {
        fragColor = originalColor;
        return;
    }

    float originalDepth = texture(DepthSampler, texCoord).r;

    float aspectRatio = aspect();
    vec2 centeredCoord = vec2((texCoord.x - BlurPos.x) * aspectRatio, texCoord.y - BlurPos.y);
    float distToCenter = length(centeredCoord);

    float normalizedRadius = Radius.x / 2160.0;
    float outerRadius = normalizedRadius * aspectRatio;
    float glowRadius = outerRadius * 1.35;

    if (distToCenter > glowRadius) {
        fragColor = originalColor;
        return;
    }
    if (originalDepth < BlackHoleDepth) {
        fragColor = originalColor;
        return;
    }

    float normDist = distToCenter / max(outerRadius, 0.0001);
    float angle = atan(centeredCoord.y, centeredCoord.x);
    float t = Time;
    float inten = clamp(Intensity, 0.0, 2.0);
    float edge = smoothstep(outerRadius, glowRadius, distToCenter);
    vec3 dir = vec3(0.0);
    if (distToCenter > 0.00001) dir = vec3(centeredCoord / distToCenter, 0.0);
    vec3 col = originalColor.rgb;

    if (AnomType == 0) {
        float mag = max(0.05, Radius.y);
        float bulge = pow(1.0 - clamp(normDist, 0.0, 1.0), 2.0);
        float rot = bulge * 1.4;
        vec2 cc = rotateVector(centeredCoord, rot);
        float strength = (1.0 / mag) - 1.0;
        float distortion = 1.0 / (1.0 + strength * bulge);
        vec2 dcc = cc * distortion;
        vec3 scene = sampleScene(toUV(dcc));
        float dark = 1.0 - (0.95 * inten) * (1.0 - smoothstep(0.0, outerRadius, distToCenter));
        scene *= clamp(dark, 0.0, 1.0);
        col = mix(scene, originalColor.rgb, edge);
    }
    else if (AnomType == 1) {
        vec3 scene = originalColor.rgb;
        float bolts = 0.0;
        bolts += bolt(angle, normDist, 0.07, t);
        bolts += bolt(angle, normDist, 0.19, t);
        bolts += bolt(angle, normDist, 0.31, t);
        bolts += bolt(angle, normDist, 0.43, t);
        bolts += bolt(angle, normDist, 0.57, t);
        bolts += bolt(angle, normDist, 0.67, t);
        bolts += bolt(angle, normDist, 0.79, t);
        bolts += bolt(angle, normDist, 0.91, t);
        float core = (1.0 - smoothstep(0.0, outerRadius * 0.22, distToCenter))
                   * (0.55 + 0.45 * hash11(floor(t * 45.0)));
        scene += AnomColor * clamp(bolts, 0.0, 3.0) * 1.6 * inten;
        scene += vec3(1.0) * pow(clamp(bolts, 0.0, 1.0), 3.0) * 1.3 * inten;
        scene += vec3(0.85, 0.92, 1.0) * core * inten;
        col = scene;
    }
    else if (AnomType == 2) {
        vec3 scene = originalColor.rgb;
        float glow = pow(1.0 - smoothstep(0.0, outerRadius, distToCenter), 1.5);
        float n = hash21(floor(texCoord * InSize / 3.0) + floor(t * 18.0));
        float geiger = 0.55 + 0.65 * hash11(floor(t * 12.0));
        scene += AnomColor * glow * (0.45 + 0.85 * n) * geiger * inten;
        float lum = dot(scene, vec3(0.299, 0.587, 0.114));
        scene = mix(scene, vec3(lum) * AnomColor * 1.3, glow * 0.25 * inten);
        col = scene;
    }
    else if (AnomType == 3) {
        float above = smoothstep(0.0, outerRadius * 0.25, centeredCoord.y);
        float horiz = 1.0 - smoothstep(0.0, outerRadius * 0.9, abs(centeredCoord.x));
        float zone = above * horiz;
        float wob = sin(texCoord.y * 60.0 + t * 6.0) + 0.5 * sin(texCoord.x * 90.0 - t * 7.0);
        vec2 off = vec2(wob * 0.005 * zone * inten, 0.0);
        vec3 scene = sampleScene(texCoord + off);
        float flick = 0.75 + 0.25 * hash11(floor(t * 25.0));
        vec2 flameCoord = vec2(centeredCoord.x, (centeredCoord.y - outerRadius * 0.1) / 1.25);
        float glow = pow(1.0 - smoothstep(0.0, outerRadius, length(flameCoord)), 1.6);
        float lick = 0.6 + 0.4 * sin(centeredCoord.x * 30.0 + t * 5.0) * sin(centeredCoord.y * 20.0 - t * 7.0);
        scene += AnomColor * glow * lick * 1.4 * flick * inten;
        float core = pow(1.0 - smoothstep(0.0, outerRadius * 0.45, distToCenter), 2.0);
        scene += vec3(1.0, 0.85, 0.5) * core * 1.1 * flick * inten;
        col = scene;
    }
    else if (AnomType == 4) {
        float fade = 1.0 - smoothstep(outerRadius * 0.55, outerRadius, distToCenter);
        vec2 warp = vec2(sin(centeredCoord.y * 22.0 + t * 2.3),
                         sin(centeredCoord.x * 22.0 - t * 2.0)) * 0.006 * fade;
        float ripple = sin(distToCenter * 60.0 - t * 6.0);
        vec2 cc = centeredCoord + warp + dir.xy * ripple * 0.004 * fade;
        vec2 caOff = dir.xy * 0.006 * fade;
        float r = sampleScene(toUV(cc + caOff)).r;
        float g = sampleScene(toUV(cc)).g;
        float b = sampleScene(toUV(cc - caOff)).b;
        vec3 scene = vec3(r, g, b);
        float rays  = 0.5 + 0.5 * sin(angle * 6.0 + t * 1.5 + distToCenter * 8.0);
        float rings = 0.5 + 0.5 * sin(distToCenter * 28.0 - t * 3.0);
        vec3 rainbow = 0.5 + 0.5 * cos(t * 1.5 + vec3(0.0, 2.094, 4.188) + angle * 2.0 + distToCenter * 10.0);
        float amt = fade * 0.6 * inten;
        vec3 trippy = scene * mix(vec3(1.0), rainbow * 1.7, rays * 0.7);
        trippy += AnomColor * rings * 0.25 * fade * inten;
        scene = mix(scene, trippy, clamp(amt, 0.0, 1.0));
        float invWave = 0.5 + 0.5 * sin(distToCenter * 18.0 - t * 2.5);
        invWave *= 0.5 + 0.5 * sin(distToCenter * 5.0 + angle * 2.0 - t * 1.1);
        float invMask = smoothstep(0.35, 0.8, invWave) * fade * inten;
        scene = mix(scene, vec3(1.0) - scene, invMask * 0.7);
        float vig = 1.0 - smoothstep(outerRadius * 0.3, glowRadius, distToCenter);
        scene *= 1.0 - 0.18 * vig * (0.5 + 0.5 * sin(t * 2.5));
        col = mix(scene, originalColor.rgb, 1.0 - fade);
    }
    else if (AnomType == 5) {
        float wave = sin(distToCenter * 40.0 - t * 6.0);
        vec2 cc = centeredCoord + dir.xy * wave * 0.006 * (1.0 - normDist);
        vec3 scene = sampleScene(toUV(cc));
        float ring = 0.0;
        for (int i = 0; i < 3; i++) {
            float phase = fract(t * 0.4 + float(i) / 3.0);
            ring += (1.0 - smoothstep(0.0, 0.06, abs(normDist - phase))) * (1.0 - phase);
        }
        scene += AnomColor * ring * 0.8 * inten;
        float sparkle = 0.0;
        const int SP = 18;
        for (int i = 0; i < SP; i++) {
            float fi = float(i);
            float cyc = t / 0.5 + fi * 0.21;
            float birth = floor(cyc);
            float age = fract(cyc) * 0.5;
            float ang = hash11(birth * 9.0 + fi) * 6.28318;
            float rad = age * 1.6;
            if (rad > 1.2) continue;
            vec2 pPos = vec2(cos(ang), sin(ang)) * rad * outerRadius;
            float d = length(centeredCoord - pPos);
            float sz = 0.0025 + 0.002 * (1.0 - rad);
            sparkle += (1.0 - smoothstep(0.0, sz, d)) * (1.0 - smoothstep(0.7, 1.0, rad));
        }
        scene += vec3(0.9, 0.8, 1.0) * sparkle * 1.4 * inten;
        col = mix(scene, originalColor.rgb, edge);
    }

    fragColor = vec4(col, 1.0);
}
