#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseProjection;
uniform mat4 Projection;
uniform vec2 InSize;
uniform vec3 CenterView;
uniform vec2 CenterUV;
uniform float CoreRadius;
uniform float ProjectedRadius;
uniform vec3 DiskNormal;
uniform vec3 DiskAxis;
uniform float AnimationTime;
uniform float FormationAge;

in vec2 texCoord;
out vec4 fragColor;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / p.w;
}

vec2 rotate(vec2 p, float angle) {
    float c = cos(angle), s = sin(angle);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

vec4 diskEmission(vec3 p, vec3 cameraDirection) {
    float r = length(p);
    float envelope = smoothstep(1.32, 1.65, r) * (1.0 - smoothstep(2.5, 3.65, r));
    float angle = atan(dot(p, cross(DiskNormal, DiskAxis)), dot(p, DiskAxis));
    // Differential rotation stretches the bright filaments into spirals.
    float phase = angle - AnimationTime * 1.8 / pow(max(r, 1.0), 1.5);
    float filaments = 0.55 + 0.22 * sin(phase * 7.0 + r * 18.0)
            + 0.14 * sin(phase * 13.0 - r * 31.0)
            + 0.09 * sin(phase * 29.0 + r * 47.0);
    float hot = 1.0 - smoothstep(1.4, 3.5, r);
    vec3 color = mix(vec3(0.32, 0.08, 0.28), vec3(1.0, 0.38, 0.075), hot);
    color = mix(color, vec3(1.0, 0.86, 0.58), pow(hot, 5.0));
    vec3 tangent = normalize(cross(DiskNormal, p));
    float beaming = clamp(1.0 + 0.45 * dot(tangent, cameraDirection), 0.55, 1.45);
    return vec4(color * 0.4 * (0.75 + filaments * 1.15) * beaming, envelope * (0.5 + 0.4 * filaments));
}

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);
    float originalDepth = texture(DepthSampler, texCoord).r;
    gl_FragDepth = originalDepth;
    vec2 aspect = vec2(InSize.x / InSize.y, 1.0);
    vec2 local = (texCoord - CenterUV) * aspect / max(ProjectedRadius, 0.00001);
    float screenRadius = length(local);
    if (screenRadius > 8.0) {
        fragColor = original;
        return;
    }

    // Minecraft includes view-bobbing translation in Projection. Unprojection therefore
    // returns points relative to a shifted eye, not directions from the view-space origin.
    vec4 eye = InverseProjection * vec4(0.0, 0.0, -1.0, 0.0);
    vec3 rayOrigin = eye.xyz / eye.w;
    vec3 ray = normalize(viewPosition(texCoord, 0.5) - rayOrigin);
    float sceneDistance = length(viewPosition(texCoord, originalDepth) - rayOrigin);
    float centerDistance = length(CenterView - rayOrigin);
    float settle = 1.0 - smoothstep(0.0, 2.5, FormationAge);
    float wobble = settle * settle;
    float polarAngle = atan(local.y, local.x);
    float horizon = 1.0 + wobble * (0.10 * sin(FormationAge * 17.0 + polarAngle * 3.0)
            + 0.055 * sin(FormationAge * 23.0 - polarAngle * 5.0));

    // Bend background light over an area wider than the horizon, fading to identity at the edge.
    float lensDistance = screenRadius / 1.65;
    float lensEnvelope = 1.0 - smoothstep(1.1, 4.8, lensDistance);
    float bend = 0.7 * lensEnvelope / max(lensDistance * lensDistance, 0.7);
    vec2 bent = rotate(local, lensEnvelope * (0.075 + wobble * 0.06 * sin(FormationAge * 15.0)));
    bent *= 1.0 - bend;
    vec2 sampleUV = clamp(CenterUV + bent * ProjectedRadius / aspect, 0.5 / InSize, 1.0 - 0.5 / InSize);
    float sampledDistance = length(viewPosition(sampleUV, texture(DepthSampler, sampleUV).r) - rayOrigin);
    vec3 background = original.rgb;
    if (sampledDistance >= centerDistance && sceneDistance >= centerDistance) {
        background = texture(DiffuseSampler, sampleUV).rgb;
    }
    // Keep the dark halo broad enough to remain visible outside the luminous disk.
    float halo = (1.0 - smoothstep(1.6, 6.6, screenRadius)) * 0.88;
    if (sceneDistance >= centerDistance) {
        background = mix(background, background * vec3(0.12, 0.08, 0.18), halo);
    }

    // Lensing and halo extend beyond the sphere used to trace the core and disk.
    float bodyRadius = CoreRadius * 0.75;
    vec3 origin = (rayOrigin - CenterView) / bodyRadius;
    float along = dot(origin, ray);
    float discriminant = along * along - dot(origin, origin) + 20.25;
    if (discriminant < 0.0) {
        fragColor = vec4(background, original.a);
        return;
    }
    float entry = max(0.0, -along - sqrt(discriminant));
    float sceneLimit = sceneDistance / bodyRadius;
    if (entry >= sceneLimit) {
        fragColor = vec4(background, original.a);
        return;
    }

    vec3 p = origin + ray * entry;
    vec3 direction = ray;
    vec3 emission = vec3(0.0);
    float transmission = 1.0;
    float travelled = entry;
    float closest = 4.5;
    bool captured = false;
    float effectDistance = sceneDistance;
    // Only rays through the small projected influence sphere reach this loop.
    for (int i = 0; i < 80; ++i) {
        float r = length(p);
        closest = min(closest, r);
        if (r < horizon) {
            captured = true;
            effectDistance = min(effectDistance, travelled * bodyRadius);
            break;
        }
        float stepLength = min(0.12, sceneLimit - travelled);
        if (stepLength <= 0.0001 || r > 4.51) break;
        vec3 gravity = -p / max(r * r * r, 0.5);
        direction = normalize(direction + (gravity - direction * dot(gravity, direction)) * stepLength * 0.65);
        vec3 next = p + direction * stepLength;
        float height = dot(p, DiskNormal);
        float nextHeight = dot(next, DiskNormal);
        // Integrate a thin slab instead of a zero-thickness plane, including edge-on views.
        float low = min(height, nextHeight);
        float high = max(height, nextHeight);
        float coverage = high - low < 0.00001 ? float(abs(height) < 0.045)
                : clamp((min(high, 0.045) - max(low, -0.045)) / (high - low), 0.0, 1.0);
        if (coverage > 0.0) {
            vec3 midpoint = (p + next) * 0.5;
            vec4 disk = diskEmission(midpoint - DiskNormal * dot(midpoint, DiskNormal), normalize(origin));
            float opacity = 1.0 - exp(-disk.a * stepLength * coverage / 0.065);
            emission += transmission * disk.rgb * opacity;
            transmission *= 1.0 - opacity;
            if (transmission < 0.5) effectDistance = min(effectDistance, travelled * bodyRadius);
        }
        p = next;
        travelled += stepLength;
    }

    // Lensed inner-disk light forms a narrow photon ring outside the black silhouette.
    float ring = exp(-pow((closest - horizon - 0.10) / 0.055, 2.0)) * 0.14;
    if (captured) background = vec3(0.0);
    vec3 color = background * transmission + emission;
    if (!captured && sceneDistance >= centerDistance) {
        color += vec3(0.9, 0.42, 0.17) * ring;
    }
    if (effectDistance < sceneDistance) {
        vec4 surface = Projection * vec4(rayOrigin + ray * effectDistance, 1.0);
        gl_FragDepth = min(originalDepth, surface.z / surface.w * 0.5 + 0.5);
    }
    fragColor = vec4(color, original.a);
}
