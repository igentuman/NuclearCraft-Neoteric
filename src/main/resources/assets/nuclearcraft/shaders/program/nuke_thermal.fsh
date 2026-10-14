#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseProjection;
uniform mat4 InverseView;
uniform mat4 Projection;
uniform mat4 ViewMat;
uniform mat4 ThermalCenters;
uniform mat4 ThermalParams;
uniform mat4 ThermalExtra;
uniform int ThermalCount;
uniform vec2 InSize;

in vec2 texCoord;
out vec4 fragColor;

const float FAR = 1.0e6;
const float SHELL_OFFSET_PX = 40.0;
const float HAZE_OFFSET_PX = 7.0;
const float MAX_OFFSET_PX = 48.0;
const int RIPPLES = 3;
const float RIPPLE_SPACING = 0.35;
const float RIPPLE_DECAY = 0.55;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / p.w;
}

float sceneDistance(vec2 uv, vec3 originView) {
    float depth = texture(DepthSampler, uv).r;
    if (depth >= 1.0) return FAR;
    return length(viewPosition(uv, depth) - originView);
}

vec2 sphere(vec3 ro, vec3 rd, vec3 center, float radius) {
    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float h = b * b - dot(oc, oc) + radius * radius;
    if (h < 0.0) return vec2(1.0, -1.0);
    h = sqrt(h);
    return vec2(-b - h, -b + h);
}

vec2 project(vec3 position) {
    vec4 clip = Projection * ViewMat * vec4(position, 1.0);
    return clip.xy / max(clip.w, 1.0e-4) * 0.5 + 0.5;
}

float hash3(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float valueNoise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash3(i), hash3(i + vec3(1.0, 0.0, 0.0)), f.x),
                   mix(hash3(i + vec3(0.0, 1.0, 0.0)), hash3(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
               mix(mix(hash3(i + vec3(0.0, 0.0, 1.0)), hash3(i + vec3(1.0, 0.0, 1.0)), f.x),
                   mix(hash3(i + vec3(0.0, 1.0, 1.0)), hash3(i + vec3(1.0, 1.0, 1.0)), f.x), f.y), f.z);
}

float shellPath(vec3 ro, vec3 rd, vec3 center, float radius, float width, float limit, out vec2 segment) {
    vec2 outer = sphere(ro, rd, center, radius);
    segment = vec2(1.0, -1.0);
    if (outer.y <= max(outer.x, 0.0)) return 0.0;
    vec2 inner = sphere(ro, rd, center, max(radius - width, 0.0));
    bool hitsInner = inner.y >= inner.x;
    vec2 first = vec2(max(outer.x, 0.0), min(hitsInner ? inner.x : outer.y, limit));
    vec2 second = hitsInner ? vec2(max(inner.y, 0.0), min(outer.y, limit)) : vec2(1.0, -1.0);
    float length0 = max(first.y - first.x, 0.0);
    float length1 = max(second.y - second.x, 0.0);
    segment = length0 > 0.0 ? first : second;
    return length0 + length1;
}

void main() {
    vec3 original = texture(DiffuseSampler, texCoord).rgb;
    if (ThermalCount <= 0) {
        fragColor = vec4(original, 1.0);
        return;
    }
    vec4 eye = InverseProjection * vec4(0.0, 0.0, -1.0, 0.0);
    vec3 originView = eye.xyz / eye.w;
    vec3 rayView = normalize(viewPosition(texCoord, 0.5) - originView);
    float limit = sceneDistance(texCoord, originView);
    vec3 ro = (InverseView * vec4(originView, 1.0)).xyz;
    vec3 rd = normalize(mat3(InverseView) * rayView);

    vec2 offset = vec2(0.0);
    float band = 0.0;
    float nearest = FAR;
    for (int i = 0; i < ThermalCount; i++) {
        vec3 center = ThermalCenters[i].xyz;
        float radius = ThermalCenters[i].w;
        vec4 params = ThermalParams[i];
        float age = ThermalExtra[i].x;
        float r = max(ThermalExtra[i].y, 1.0);
        float width = max(params.y, 0.5);

        if (params.x > 0.0 && radius > 0.0) {
            for (int j = 0; j < RIPPLES; j++) {
                float ringRadius = radius - float(j) * RIPPLE_SPACING * r;
                if (ringRadius <= width) break;
                float ringStrength = params.x * pow(RIPPLE_DECAY, float(j)) * (j == 1 ? -1.0 : 1.0);
                vec2 segment;
                float path = shellPath(ro, rd, center, ringRadius, width, limit, segment);
                if (path <= 0.0) continue;
                vec3 point = ro + rd * (0.5 * (segment.x + segment.y));
                vec3 normal = normalize(point - center);
                vec2 direction = (project(point + normal * width) - project(point)) * InSize;
                float directionLength = length(direction);
                if (directionLength < 1.0e-4) continue;
                float amount = clamp(path / width, 0.0, 4.0) * 0.25;
                offset += direction / directionLength * amount * ringStrength * SHELL_OFFSET_PX;
                band += amount * abs(ringStrength);
                nearest = min(nearest, segment.x);
            }
        }

        float hazeRadius = params.w;
        if (params.z > 0.0 && hazeRadius > 0.5) {
            vec2 hit = sphere(ro, rd, center, hazeRadius);
            float t0 = max(hit.x, 0.0);
            float t1 = min(hit.y, limit);
            if (t1 > t0) {
                vec3 point = ro + rd * (0.5 * (t0 + t1));
                vec3 local = (point - center) / (0.12 * r) - vec3(0.0, age * 1.5, 0.0);
                vec2 shimmer = vec2(valueNoise(local), valueNoise(local + vec3(19.1, 7.3, 3.7))) * 2.0 - 1.0;
                float depthFactor = clamp((t1 - t0) / hazeRadius, 0.0, 1.0);
                offset += shimmer * params.z * depthFactor * HAZE_OFFSET_PX;
                nearest = min(nearest, t0);
            }
        }
    }

    float magnitude = length(offset);
    if (magnitude > MAX_OFFSET_PX) offset *= MAX_OFFSET_PX / magnitude;
    vec2 edge = min(texCoord, 1.0 - texCoord);
    offset *= smoothstep(0.0, 0.04, min(edge.x, edge.y));
    vec2 shift = offset / InSize;
    vec2 lo = 0.5 / InSize;
    vec2 hi = 1.0 - lo;
    vec3 color = original;
    if (dot(shift, shift) > 0.0) {
        vec2 sampleUV = clamp(texCoord + shift, lo, hi);
        if (sceneDistance(sampleUV, originView) >= nearest) {
            color.r = texture(DiffuseSampler, clamp(texCoord + shift * 1.05, lo, hi)).r;
            color.g = texture(DiffuseSampler, sampleUV).g;
            color.b = texture(DiffuseSampler, clamp(texCoord + shift * 0.95, lo, hi)).b;
        }
    }
    float front = clamp(band, 0.0, 1.0);
    color = mix(color, color * 1.25 + vec3(0.05, 0.048, 0.044), front * 0.55);
    fragColor = vec4(color, 1.0);
}
