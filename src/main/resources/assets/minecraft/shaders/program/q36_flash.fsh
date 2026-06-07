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

out vec4 fragColor;

vec2 rotateVector(vec2 v, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return vec2(v.x * c - v.y * s, v.x * s + v.y * c);
}

float customSmoothstep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
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

// Jagged radial bolt: returns intensity in [0,1] for one bolt at angle a0
float bolt(float angle, float normDist, float seed, float t) {
    // Pick angular position that drifts/snaps with time
    float slot = floor(t * 6.0 + seed * 17.0);
    float a0 = hash11(slot + seed) * 6.28318;
    float life = fract(t * 6.0 + seed * 17.0);
    float lifeMask = smoothstep(0.0, 0.1, life) * (1.0 - smoothstep(0.4, 1.0, life));

    // Jaggedness: perturb target angle along the radius
    float jag = (hash11(slot * 7.0 + floor(normDist * 12.0) + seed) - 0.5) * 0.35;
    float da = abs(mod(angle - a0 - jag + 3.14159, 6.28318) - 3.14159);

    // Thin line, brighter at kinks
    float thickness = 0.015 + 0.02 * (1.0 - normDist);
    float line = 1.0 - smoothstep(0.0, thickness, da);

    // Fade tail toward outer rim
    float radial = smoothstep(1.05, 0.15, normDist);
    return line * radial * lifeMask;
}

void main() {
    // Get the original pixel color and depth
    vec4 originalColor = texture(DiffuseSampler, texCoord);
    float originalDepth = texture(DepthSampler, texCoord).r;
    
    // Radius.x = lens radius in pixels
    // Radius.y = magnification factor (values > 1.0 will magnify)
    float magnification = max(0.05, Radius.y); // Allow <1 for inverse (sucking) lens

    // Scale radius properly based on screen resolution
    // This makes radius a percentage of screen height rather than fixed pixels
    float screenHeight = InSize.y;
    float normalizedRadius = Radius.x / 2160.0; // Base resolution height
    float lensRadiusPixels = normalizedRadius * screenHeight;

    // Convert to UV space properly accounting for aspect ratio
    float lensRadiusUV = lensRadiusPixels / screenHeight;

    // Increase feathering for smoother transition
    float featherAmount = clamp(BlurDir.x * 1.5, 0.2, 0.65);
    float rotationIntensity = 0.7; // Controls how much the black hole spins

    // Purple glow settings
    vec3 glowColor = vec3(0.5, 0.0, 0.7);
    float glowIntensity = 0.1;

    // Darkness settings for black hole center
    float maxDarkness = 0.4;
    float darkCoreSize = 0.2;
    float darknessFalloff = 0.75;

    // Calculate aspect ratio correction
    float aspectRatio = InSize.x / InSize.y;

    // Apply aspect ratio correction to distance calculation
    vec2 centeredCoord = vec2((texCoord.x - BlurPos.x) * aspectRatio, texCoord.y - BlurPos.y);
    float distToCenter = length(centeredCoord);

    // Define the inner and outer radius of the effect with wider transition
    float innerRadius = lensRadiusUV * aspectRatio * (1.0 - featherAmount);
    float outerRadius = lensRadiusUV * aspectRatio;
    float glowRadius = outerRadius * 1.2;
    
    // Quick exit if pixel is too far from center to be affected by any part of the effect
    if (distToCenter > glowRadius) {
        fragColor = originalColor;
        return;
    }
    
    // If the current pixel is behind an object that's in front of the black hole, don't apply effect
    if (originalDepth < BlackHoleDepth) {
        fragColor = originalColor;
        return;
    }
    
    vec4 resultColor = originalColor;

    // Calculate purple glow effect that extends beyond the distortion
    float glowFactor = 0.0;
    if (distToCenter <= glowRadius) {
        float edgeGlowPos = innerRadius * 0.95;
        float glowWidth = outerRadius * 0.4;

        glowFactor = smoothstep(edgeGlowPos - glowWidth, edgeGlowPos, distToCenter) *
        (1.0 - smoothstep(edgeGlowPos, edgeGlowPos + glowWidth * 2.0, distToCenter));

        glowFactor *= glowIntensity;
    }

    if (distToCenter <= outerRadius) {
        float normalizedDist = distToCenter / (lensRadiusUV * aspectRatio);

        float rotationAngle = (1.0 - normalizedDist) * rotationIntensity;
        vec2 rotatedCoord = rotateVector(centeredCoord, rotationAngle);

        float distortionFactor = 1.0 - (normalizedDist * normalizedDist) * (1.0 - 1.0/magnification);
        vec2 distortedCoord = rotatedCoord * distortionFactor;
        vec2 newTexCoord = vec2(distortedCoord.x / aspectRatio + BlurPos.x, distortedCoord.y + BlurPos.y);

        // Sample the distorted position
        vec4 distortedColor = texture(DiffuseSampler, clamp(newTexCoord, 0.0, 1.0));
        float distortedDepth = texture(DepthSampler, clamp(newTexCoord, 0.0, 1.0)).r;

        // Calculate darkness factor based on distance from center
        float darknessFactor = 0.0;
        float darkEdge = innerRadius * darknessFalloff;
        float darkCore = innerRadius * darkCoreSize;

        if (distToCenter < darkEdge) {
            if (distToCenter < darkCore) {
                darknessFactor = maxDarkness;
            } else {
                darknessFactor = maxDarkness * (1.0 - smoothstep(darkCore, darkEdge, distToCenter));
            }

            distortedColor.rgb *= (1.0 - darknessFactor);
        }

        float blendFactor;
        if (distToCenter <= innerRadius * 0.9) {
            blendFactor = 0.0;
        } else {
            blendFactor = customSmoothstep(innerRadius * 0.9, outerRadius, distToCenter);
        }

        // Only apply distortion if the distorted pixel is not behind an object
        // In Minecraft, lower depth values are further from the camera
        if (distortedDepth >= originalDepth || distortedDepth >= BlackHoleDepth) {
            resultColor = mix(distortedColor, originalColor, blendFactor);
        } else {
            // If the distorted pixel is behind something, don't apply distortion
            resultColor = originalColor;
        }
    }

    if (glowFactor > 0.0) {
        // Only apply glow if it doesn't interfere with objects in front
        resultColor.rgb = mix(resultColor.rgb, glowColor, glowFactor * (1.0 - distToCenter/glowRadius));
    }

    // ---- Energy FX: sparkles + lightning + rim pulse ----
    if (distToCenter <= glowRadius) {
        float normDist = distToCenter / outerRadius;
        float angle = atan(centeredCoord.y, centeredCoord.x);
        float t = Time;

        vec3 sparkColor = vec3(0.9, 0.7, 1.0);
        vec3 boltColor  = vec3(0.8, 0.5, 1.0);
        vec3 rimColor   = vec3(0.7, 0.3, 1.0);

        // Sparkles: streak outward from center, fast
        float sparkle = 0.0;
        const int SPARK_COUNT = 24;
        float sparkSpeed = 1.8;   // radii per second
        float sparkLife = 0.55;   // seconds
        for (int i = 0; i < SPARK_COUNT; i++) {
            float fi = float(i);
            // Each slot spawns sequentially over lifespan
            float cycle = t / sparkLife + fi * 0.137;
            float birth = floor(cycle);
            float age = fract(cycle) * sparkLife;
            float ang = hash11(birth * 13.0 + fi) * 6.28318;
            float r = age * sparkSpeed;
            if (r > 1.2) continue;
            vec2 pPos = vec2(cos(ang), sin(ang)) * r * outerRadius;
            float d = length(centeredCoord - pPos);
            float size = 0.0025 + 0.002 * (1.0 - r);
            float head = 1.0 - smoothstep(0.0, size, d);
            // Tail behind particle
            vec2 tailDir = vec2(cos(ang), sin(ang));
            vec2 rel = centeredCoord - pPos;
            float along = dot(rel, -tailDir);
            float perp  = abs(dot(rel, vec2(-tailDir.y, tailDir.x)));
            float tail = (along > 0.0 && along < 0.03)
                ? (1.0 - smoothstep(0.0, 0.001 + 0.002*(1.0-r), perp)) * (1.0 - along/0.03)
                : 0.0;
            float life = 1.0 - smoothstep(0.7, 1.0, r);
            sparkle += (head + tail * 0.7) * life;
        }
        sparkle = clamp(sparkle, 0.0, 2.0);

        // Lightning: sum of a few bolts
        float bolts = 0.0;
        bolts += bolt(angle, normDist, 0.13, t);
        bolts += bolt(angle, normDist, 0.41, t);
        bolts += bolt(angle, normDist, 0.77, t);
        bolts = clamp(bolts, 0.0, 1.5);

        // Rim pulse: bright thin ring at innerRadius, sin-modulated
        float rimPos = innerRadius / outerRadius;
        float rim = 1.0 - smoothstep(0.0, 0.08, abs(normDist - rimPos));
        float pulse = 0.55 + 0.45 * sin(t * 50.0);
        float rimFx = rim * pulse;

        // Compose (additive, gated by being inside affected zone)
        vec3 fx = sparkColor * sparkle * 1.5
                + boltColor  * bolts   * 1.2
                + rimColor   * rimFx   * 0.6;

        resultColor.rgb += fx;
    }

    fragColor = resultColor;
}