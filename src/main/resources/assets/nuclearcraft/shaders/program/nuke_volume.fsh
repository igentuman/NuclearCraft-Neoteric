#version 150

uniform sampler2D DepthSampler;
uniform sampler2D NoiseSampler;
uniform mat4 InverseProjection;
uniform mat4 InverseView;
uniform mat4 ShapeA0;
uniform mat4 ShapeA1;
uniform mat4 ShapeA2;
uniform mat4 ShapeA3;
uniform mat4 ShapeB0;
uniform mat4 ShapeB1;
uniform mat4 ShapeB2;
uniform mat4 ShapeB3;
uniform int VolumeCount;
uniform int MaxSamples;
uniform int LightProbes;
uniform vec4 Light;
uniform vec4 Ambient;

in vec2 texCoord;
out vec4 fragColor;

const int MAX_VOLUMES = 4;
const int MAX_INTERVALS = 8;
const int MAX_STEPS = 128;
const float FAR = 1.0e6;
const float MIN_TRANSMITTANCE = 0.01;
const float NOISE_SLICES = 64.0;
const float NOISE_GRID = 8.0;
const float NOISE_TILE = 66.0;
const float NOISE_ATLAS = 528.0;
const float FLOW_PERIOD = 6.0;
const float AERIAL_EXTINCTION = 9.0;
const float GROUND_EXTINCTION = 5.0;
const vec3 SMOKE_ALBEDO = vec3(0.56, 0.52, 0.49);
const vec3 DUST_ALBEDO = vec3(0.52, 0.44, 0.35);
const vec3 SUN_COLOR = vec3(1.0, 0.95, 0.86);

struct Volume {
    vec3 center;
    float r;
    float age;
    float morph;
    float fade;
    float seed;
    float fireball;
    float capY;
    float capRadius;
    float capThickness;
    float rim;
    float stem;
    float capExtent;
    float top;
    float bottom;
    float groundRadius;
    float groundY;
    float groundIntensity;
    float heat;
    float flash;
    float growth;
    float detail;
    float groundRingY;
    float groundExtent;
    float baseRadius;
    float baseHeight;
    float baseHeat;
};

struct Sample {
    float sigma;
    vec3 emission;
    vec3 albedo;
    float ambient;
    float underGlow;
    float smokeFill;
};

Volume volumes[MAX_VOLUMES];
vec2 intervals[MAX_INTERVALS];

Volume unpack(mat4 a, mat4 b) {
    Volume v;
    v.center = a[0].xyz;
    v.r = max(a[0].w, 1.0);
    v.age = a[1].x;
    v.morph = a[1].y;
    v.fade = a[1].z;
    v.seed = a[1].w;
    v.fireball = a[2].x;
    v.capY = a[2].y;
    v.capRadius = max(a[2].z, 1.0e-3);
    v.capThickness = max(a[2].w, 1.0e-3);
    v.rim = a[3].x;
    v.stem = a[3].y;
    v.capExtent = a[3].z;
    v.top = a[3].w;
    v.bottom = b[0].x;
    v.groundRadius = b[0].y;
    v.groundY = b[0].z;
    v.groundIntensity = b[0].w;
    v.heat = b[1].x;
    v.flash = b[1].y;
    v.growth = b[1].z;
    v.detail = b[1].w;
    v.groundRingY = b[2].x;
    v.groundExtent = b[2].y;
    v.baseRadius = max(b[2].z, 1.0e-3);
    v.baseHeight = max(b[2].w, 1.0e-3);
    v.baseHeat = b[3].x;
    return v;
}

vec3 viewPosition(vec2 uv, float depth) {
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / p.w;
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float smin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

vec2 noiseAt(vec3 p) {
    vec3 c = fract(p) * NOISE_SLICES;
    float z = c.z - 0.5;
    float z0 = floor(z);
    float f = z - z0;
    float s0 = mod(z0, NOISE_SLICES);
    float s1 = mod(z0 + 1.0, NOISE_SLICES);
    vec2 t0 = vec2(mod(s0, NOISE_GRID), floor(s0 / NOISE_GRID)) * NOISE_TILE + 1.0;
    vec2 t1 = vec2(mod(s1, NOISE_GRID), floor(s1 / NOISE_GRID)) * NOISE_TILE + 1.0;
    vec2 a = texture(NoiseSampler, (t0 + c.xy) / NOISE_ATLAS).rg;
    vec2 b = texture(NoiseSampler, (t1 + c.xy) / NOISE_ATLAS).rg;
    return mix(a, b, f);
}

vec3 seedOffset(Volume v) {
    return vec3(17.31, 5.13, 11.71) * v.seed;
}

vec2 intersectBox(vec3 ro, vec3 inv, vec3 lo, vec3 hi) {
    vec3 a = (lo - ro) * inv;
    vec3 b = (hi - ro) * inv;
    vec3 near = min(a, b);
    vec3 far = max(a, b);
    return vec2(max(max(near.x, near.y), near.z), min(min(far.x, far.y), far.z));
}

vec2 aerialShape(Volume v, vec3 q, float rho) {
    vec3 c = q - vec3(0.0, v.capY, 0.0);
    float ry = c.y > 0.0 ? v.capThickness : v.capThickness * mix(1.0, 0.65, v.morph);
    float e = length(c / vec3(v.capRadius, ry, v.capRadius));
    float d = (e - 1.0) * min(v.capRadius, ry);
    if (v.rim > 1.0e-3) {
        float rimMajor = max(v.capRadius - v.rim * 1.1, 0.0);
        float rimY = v.capY - 0.25 * v.capThickness;
        float dRim = length(vec2(rho - rimMajor, q.y - rimY)) - v.rim;
        d = smin(d, dRim, 0.5 * v.rim);
    }
    if (v.stem > 1.0e-3) {
        float base = v.groundY - 0.1 * v.r;
        float span = max(v.capY - base, 1.0e-3);
        float h = clamp((q.y - base) / span, 0.0, 1.0);
        float flare = 1.0 + 1.2 * pow(1.0 - h, 3.0);
        vec2 bend = vec2(sin(h * 2.5 + v.seed * 6.2831), cos(h * 2.1 + v.seed * 4.0)) * v.stem * 0.3 * h;
        float dStem = max(length(q.xz - bend) - v.stem * flare, max(base - q.y, q.y - v.capY));
        d = smin(d, dStem, v.stem * 0.9);
    }
    float baseScale = min(v.baseRadius, v.baseHeight);
    float dBase = (length(q / vec3(v.baseRadius, v.baseHeight, v.baseRadius)) - 1.0) * baseScale;
    float k = 0.35 * baseScale + 1.0e-3;
    float h = clamp(0.5 + 0.5 * (d - dBase) / k, 0.0, 1.0);
    return vec2(mix(d, dBase, h) - k * h * (1.0 - h), h);
}

vec3 flowVelocity(Volume v, vec3 q, float rho) {
    vec3 radial = rho > 1.0e-3 ? vec3(q.x, 0.0, q.z) / rho : vec3(0.0);
    vec3 azimuthal = vec3(-radial.z, 0.0, radial.x);
    float stemMask = (1.0 - smoothstep(v.stem, v.stem * 2.5 + 1.0e-3, rho))
            * (1.0 - smoothstep(v.capY - v.capThickness, v.capY, q.y));
    vec3 velocity = vec3(0.0, 0.08 + 0.12 * v.morph, 0.0) * stemMask;
    vec2 rel = vec2(rho - 0.75 * v.capRadius, q.y - v.capY) / (v.capThickness + v.rim);
    float roll = (0.1 + 0.08 * v.morph) * exp(-dot(rel, rel) * 0.7);
    velocity += (radial * rel.y - vec3(0.0, rel.x, 0.0)) * roll;
    vec2 baseRel = vec2(rho - 0.55 * v.baseRadius, q.y - 0.35 * v.baseHeight) / v.baseHeight;
    float baseRoll = 0.07 * exp(-dot(baseRel, baseRel) * 1.2);
    velocity += (radial * baseRel.y - vec3(0.0, baseRel.x, 0.0)) * baseRoll;
    velocity += azimuthal * (0.04 + 0.05 * sin(rho / v.r * 2.3 + v.seed * 6.2831));
    return velocity * v.r * (1.0 - 0.4 * v.growth);
}

vec2 flowNoise(Volume v, vec3 q, float rho, float scale) {
    vec3 velocity = flowVelocity(v, q, rho);
    float tau = v.age / FLOW_PERIOD;
    float p0 = fract(tau);
    float p1 = fract(tau + 0.5);
    float w0 = 1.0 - abs(2.0 * p0 - 1.0);
    vec3 base = seedOffset(v);
    vec3 m0 = (q - velocity * p0 * FLOW_PERIOD) * scale + base + floor(tau) * vec3(0.37, 0.11, 0.53);
    vec3 m1 = (q - velocity * p1 * FLOW_PERIOD) * scale + base + floor(tau + 0.5) * vec3(0.37, 0.11, 0.53) + 0.5;
    return mix(noiseAt(m1), noiseAt(m0), w0);
}

vec3 fireColor(float t) {
    vec3 c = mix(vec3(0.35, 0.05, 0.01), vec3(1.0, 0.42, 0.07), smoothstep(0.0, 0.4, t));
    c = mix(c, vec3(1.0, 0.82, 0.48), smoothstep(0.4, 0.95, t));
    return mix(c, vec3(1.0), smoothstep(0.95, 3.0, t));
}

Sample evaluate(Volume v, vec3 p, bool detailed) {
    Sample s;
    s.sigma = 0.0;
    s.emission = vec3(0.0);
    s.albedo = SMOKE_ALBEDO;
    s.ambient = 1.0;
    s.underGlow = 0.0;
    s.smokeFill = 0.0;
    vec3 q = p - v.center;
    float rho = length(q.xz);

    vec2 shape = aerialShape(v, q, rho);
    float d = shape.x;
    float baseWeight = shape.y;
    float thickness = min(v.capRadius, v.capThickness);
    float local = v.stem > 1.0e-3
            ? mix(v.stem, thickness, smoothstep(v.capY - v.capThickness - v.stem, v.capY - 0.5 * v.capThickness, q.y))
            : thickness;
    local = mix(local, min(v.baseRadius, v.baseHeight), baseWeight);
    float amp = 0.4 * local;
    float soft = 0.25 * amp + 0.02 * v.r;
    float aerialSigma = 0.0;
    float aerialTemp = 0.0;
    float stemTint = 0.0;
    if (d < 1.1 * amp) {
        vec2 n = flowNoise(v, q, rho, 1.0 / (1.4 * v.r));
        float dn = d + (0.55 - n.x) * amp * 2.0;
        float density = clamp(-dn / soft, 0.0, 1.0);
        if (density > 0.0) {
            float erosion = n.y;
            if (detailed && v.detail > 0.0 && density < 1.0) {
                erosion = mix(erosion, noiseAt(q / (0.45 * v.r) + seedOffset(v) * 1.7 - vec3(0.0, v.age * 0.004, 0.0)).g, 0.5 * v.detail);
            }
            density = clamp(density - (1.0 - density) * erosion * 0.8, 0.0, 1.0);
            float core = clamp(-dn / (amp * 1.5 + soft), 0.0, 1.0);
            float cy = clamp((q.y - v.capY) / v.capThickness, 0.0, 1.0);
            float by = clamp(q.y / v.baseHeight, 0.0, 1.0);
            float turbulence = mix(0.25, 1.0, core) * (0.6 + 0.8 * n.x);
            float capTemp = v.heat * turbulence * (1.0 - 0.35 * cy * v.morph);
            float baseTemp = v.baseHeat * turbulence * (1.0 - 0.3 * by * v.morph);
            aerialTemp = mix(capTemp, baseTemp, baseWeight) + v.flash * (0.6 + 0.4 * core) * 5.0;
            float fireLife = 1.0 - smoothstep(6.0, 55.0, v.age);
            float capLevel = (q.y - v.capY) / v.capThickness;
            float capUnderside = smoothstep(-1.35, -0.7, capLevel)
                    * (1.0 - smoothstep(-0.4, 0.25, capLevel));
            float capInterior = 1.0 - smoothstep(0.45, 0.95, rho / v.capRadius);
            float capFire = (1.0 - baseWeight) * capUnderside * capInterior;
            float stemShape = v.morph * (1.0 - smoothstep(0.45, 1.2, rho / max(v.stem, 0.05 * v.r)))
                    * (1.0 - smoothstep(0.1, 0.75, q.y / max(v.capY, 0.1 * v.r)));
            float stemFire = 0.65 * stemShape;
            stemTint = stemShape * (1.0 - smoothstep(10.0, 20.0, v.age));
            float baseFire = baseWeight * (1.0 - smoothstep(0.4, 0.95, rho / v.baseRadius))
                    * (1.0 - smoothstep(0.0, 0.85, q.y / v.baseHeight));
            aerialTemp = max(aerialTemp, fireLife * max(capFire, max(stemFire, baseFire)));
            aerialSigma = density * AERIAL_EXTINCTION / v.r * (1.0 - 0.3 * v.growth);
            float floorY = v.capY - v.capThickness;
            float capAmbient = mix(0.35, 1.0, clamp((q.y - floorY) / (2.0 * v.capThickness), 0.0, 1.0));
            float baseAmbient = mix(0.45, 1.0, clamp(0.5 + 0.5 * q.y / v.baseHeight, 0.0, 1.0));
            s.ambient = mix(capAmbient, baseAmbient, baseWeight);
            float capUnder = 1.0 - clamp((q.y - floorY) / v.capThickness, 0.0, 1.0);
            s.underGlow = v.groundIntensity * mix(capUnder, 1.0 - by, baseWeight);
        }
    }

    float groundSigma = 0.0;
    float groundTemp = 0.0;
    if (v.groundRadius > 0.5 && v.groundIntensity > 1.0e-3) {
        float g = v.groundRadius;
        float ground = mix(v.groundY, v.groundRingY, clamp(rho / max(g, 1.0), 0.0, 1.0));
        float gy = q.y - ground;
        float frontOffset = (rho - g) / (0.1 * g + 0.08 * v.r);
        float front = exp(-frontOffset * frontOffset);
        float inside = 1.0 - smoothstep(g * 0.98, g * 1.06, rho);
        float centralOffset = rho / (0.9 * v.r);
        float central = exp(-centralOffset * centralOffset);
        float life = sqrt(v.groundIntensity);
        float height = v.r * (0.18 * (0.35 + 0.65 * front) * inside + 0.18 * front + 0.45 * central) * (0.4 + 0.6 * life);
        if (height > 0.05 && gy > -0.3 * v.r && gy < height * 1.6) {
            vec3 drift = vec3(0.0, v.age * 0.01, 0.0);
            vec2 n = noiseAt(vec3(q.x, q.y * 1.6, q.z) / (0.9 * v.r) + seedOffset(v) * 0.7 - drift);
            float h = height * (0.6 + 0.8 * n.x);
            float contact = 1.0 - smoothstep(1.5 * v.r, 3.0 * v.r, -v.groundY);
            float density = clamp((h - gy) / (0.35 * h + 0.5), 0.0, 1.0) * max(inside, front)
                    * smoothstep(-0.3 * v.r, 0.0, gy) * life * contact;
            groundSigma = density * GROUND_EXTINCTION / v.r;
            groundTemp = v.groundIntensity * front * (1.0 - clamp(gy / max(height, 1.0), 0.0, 1.0)) * 1.4 * n.x;
        }
    }

    float sigma = (aerialSigma + groundSigma) * v.fade;
    s.sigma = sigma;
    if (sigma <= 0.0) return s;
    float wa = aerialSigma / (aerialSigma + groundSigma);
    float temp = mix(groundTemp, aerialTemp, wa);
    s.albedo = mix(DUST_ALBEDO, SMOKE_ALBEDO, wa);
    float matureSmoke = smoothstep(12.0, 35.0, v.age);
    s.smokeFill = 0.48 * matureSmoke * smoothstep(0.7, 1.0, v.fade) * wa;
    s.ambient = mix(0.6, s.ambient, wa);
    s.underGlow *= wa;
    vec3 flameColor = mix(fireColor(temp), vec3(0.9, 0.18, 0.04), 0.65 * stemTint * wa);
    s.emission = flameColor * (temp * temp * 2.0 + temp * 0.2);
    return s;
}

float shadowDensity(vec3 p) {
    float sigma = 0.0;
    for (int i = 0; i < VolumeCount; i++) {
        Volume v = volumes[i];
        vec3 q = p - v.center;
        vec2 shape = aerialShape(v, q, length(q.xz));
        float thickness = mix(min(v.capRadius, v.capThickness), min(v.baseRadius, v.baseHeight), shape.y);
        sigma += clamp(-shape.x / (0.5 * thickness), 0.0, 1.0) * AERIAL_EXTINCTION / v.r * v.fade;
    }
    return sigma;
}

float shadow(vec3 p, float probeStep) {
    float optical = 0.0;
    for (int k = 0; k < LightProbes; k++) {
        optical += shadowDensity(p + Light.xyz * probeStep * (float(k) + 0.5));
    }
    return exp(-optical * probeStep);
}

float phaseHG(float cosTheta, float g) {
    float g2 = g * g;
    return (1.0 - g2) / pow(1.0 + g2 - 2.0 * g * cosTheta, 1.5);
}

int collectIntervals(vec3 ro, vec3 rd, float limit) {
    vec3 inv = 1.0 / rd;
    int count = 0;
    for (int i = 0; i < VolumeCount; i++) {
        Volume v = volumes[i];
        vec3 lo = v.center + vec3(-v.capExtent, v.bottom, -v.capExtent);
        vec3 hi = v.center + vec3(v.capExtent, v.top, v.capExtent);
        vec2 hit = intersectBox(ro, inv, lo, hi);
        hit.x = max(hit.x, 0.0);
        hit.y = min(hit.y, limit);
        if (hit.y > hit.x) intervals[count++] = hit;
        if (v.groundRadius > 0.5 && v.groundIntensity > 1.0e-3) {
            float g0 = min(v.groundY, v.groundRingY) - 0.3 * v.r;
            float g1 = max(v.groundY, v.groundRingY) + 1.1 * v.r;
            lo = v.center + vec3(-v.groundExtent, g0, -v.groundExtent);
            hi = v.center + vec3(v.groundExtent, g1, v.groundExtent);
            hit = intersectBox(ro, inv, lo, hi);
            hit.x = max(hit.x, 0.0);
            hit.y = min(hit.y, limit);
            if (hit.y > hit.x) intervals[count++] = hit;
        }
    }
    for (int i = 1; i < count; i++) {
        vec2 key = intervals[i];
        int j = i - 1;
        while (j >= 0 && intervals[j].x > key.x) {
            intervals[j + 1] = intervals[j];
            j--;
        }
        intervals[j + 1] = key;
    }
    int merged = 0;
    for (int i = 0; i < count; i++) {
        if (merged > 0 && intervals[i].x <= intervals[merged - 1].y) {
            intervals[merged - 1].y = max(intervals[merged - 1].y, intervals[i].y);
        } else {
            intervals[merged++] = intervals[i];
        }
    }
    return merged;
}

vec3 halo(vec3 ro, vec3 rd, float sceneDistance) {
    vec3 glow = vec3(0.0);
    for (int i = 0; i < VolumeCount; i++) {
        Volume v = volumes[i];
        if (v.flash <= 1.0e-3) continue;
        vec3 oc = v.center - ro;
        float along = max(dot(oc, rd), 0.0);
        float miss = length(oc - rd * along);
        float visible = sceneDistance < along - v.fireball ? 0.1 : 1.0;
        float width = 2.5 * v.fireball + 1.0;
        glow += vec3(1.0, 0.95, 0.85) * v.flash * v.fade * exp(-miss * miss / (width * width)) * 1.5 * visible;
    }
    return glow;
}

void main() {
    volumes[0] = unpack(ShapeA0, ShapeB0);
    volumes[1] = unpack(ShapeA1, ShapeB1);
    volumes[2] = unpack(ShapeA2, ShapeB2);
    volumes[3] = unpack(ShapeA3, ShapeB3);

    ivec2 depthSize = textureSize(DepthSampler, 0);
    ivec2 pixel = clamp(ivec2(texCoord * vec2(depthSize)), ivec2(0), depthSize - 1);
    vec2 uv = (vec2(pixel) + 0.5) / vec2(depthSize);
    float depth = texelFetch(DepthSampler, pixel, 0).r;

    vec4 eye = InverseProjection * vec4(0.0, 0.0, -1.0, 0.0);
    vec3 originView = eye.xyz / eye.w;
    vec3 rayView = normalize(viewPosition(uv, 0.5) - originView);
    float sceneDistance = depth >= 1.0 ? FAR : length(viewPosition(uv, depth) - originView);
    vec3 ro = (InverseView * vec4(originView, 1.0)).xyz;
    vec3 rd = normalize(mat3(InverseView) * rayView);

    vec3 color = halo(ro, rd, sceneDistance);
    float transmittance = 1.0;
    int count = collectIntervals(ro, rd, sceneDistance);
    if (count == 0) {
        fragColor = vec4(color, 1.0);
        return;
    }

    float total = 0.0;
    float minR = FAR;
    for (int i = 0; i < count; i++) total += intervals[i].y - intervals[i].x;
    for (int i = 0; i < VolumeCount; i++) minR = min(minR, volumes[i].r);
    float dt = max(total / float(max(MaxSamples, 1)), 0.01 * minR);
    float probeStep = 0.3 * minR;
    float jitter = hash(gl_FragCoord.xy);
    float cosTheta = dot(rd, Light.xyz);
    float phase = mix(1.0, phaseHG(cosTheta, 0.35), 0.6);
    vec3 ambientColor = mix(vec3(0.62), Ambient.rgb, 0.55) * Ambient.w;
    vec3 sunColor = SUN_COLOR * Light.w;
    int budget = MaxSamples + MAX_INTERVALS;
    int steps = 0;
    int occupied = 0;
    float lastShadow = 1.0;

    for (int k = 0; k < count; k++) {
        float t = intervals[k].x + dt * jitter;
        float end = intervals[k].y;
        for (int s = 0; s < MAX_STEPS; s++) {
            if (t >= end || steps >= budget || transmittance < MIN_TRANSMITTANCE) break;
            steps++;
            vec3 p = ro + rd * t;
            float sigma = 0.0;
            vec3 source = vec3(0.0);
            for (int i = 0; i < VolumeCount; i++) {
                Sample smp = evaluate(volumes[i], p, transmittance > 0.2);
                if (smp.sigma <= 0.0) continue;
                if ((occupied & 1) == 0) lastShadow = shadow(p, probeStep);
                occupied++;
                vec3 lit = sunColor * lastShadow * phase * 1.2 + ambientColor * smp.ambient * 0.8
                        + vec3(1.0, 0.42, 0.12) * smp.underGlow * 0.35;
                lit = max(lit, vec3(smp.smokeFill));
                sigma += smp.sigma;
                source += smp.sigma * (smp.albedo * lit + smp.emission);
            }
            if (sigma > 0.0) {
                float opacity = 1.0 - exp(-sigma * dt);
                color += transmittance * (source / sigma) * opacity;
                transmittance *= 1.0 - opacity;
            }
            t += dt;
        }
    }
    fragColor = vec4(color, clamp(transmittance, 0.0, 1.0));
}
