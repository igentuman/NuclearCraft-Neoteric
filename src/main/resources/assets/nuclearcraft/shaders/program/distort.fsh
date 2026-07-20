#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform vec2 BlurPos;
uniform vec2 Radius;
uniform vec2 BlurDir;
uniform float EffectDepth; // normalized screen depth of the distortion center

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

void main() {
    vec4 originalColor = texture(DiffuseSampler, texCoord);
    float originalDepth = texture(DepthSampler, texCoord).r;

    // Radius.x = lens radius in pixels, Radius.y = magnification factor (> 1 magnifies).
    float magnification = max(1.1, Radius.y);

    // Treat radius as a fraction of screen height so it is resolution independent.
    float screenHeight = InSize.y;
    float normalizedRadius = Radius.x / 2160.0; // base resolution height
    float lensRadiusPixels = normalizedRadius * screenHeight;
    float lensRadiusUV = lensRadiusPixels / screenHeight;

    float featherAmount = clamp(BlurDir.x * 1.5, 0.2, 0.65);
    float rotationIntensity = 0.7; // how much the distortion swirls

    // --- Look tuning (tweak freely for your own effect) ---
    vec3 glowColor = vec3(0.5, 0.0, 0.7);
    float glowIntensity = 0.0;
    float maxDarkness = 0.4;
    float darkCoreSize = 0.2;
    float darknessFalloff = 0.75;
    // ------------------------------------------------------

    float aspectRatio = InSize.x / InSize.y;

    vec2 centeredCoord = vec2((texCoord.x - BlurPos.x) * aspectRatio, texCoord.y - BlurPos.y);
    float distToCenter = length(centeredCoord);

    float innerRadius = lensRadiusUV * aspectRatio * (1.0 - featherAmount);
    float outerRadius = lensRadiusUV * aspectRatio;
    float glowRadius = outerRadius * 1.2;

    // Cheap reject: pixel outside the whole effect.
    if (distToCenter > glowRadius) {
        fragColor = originalColor;
        return;
    }

    // Pixel is behind geometry that sits in front of the effect center.
    if (originalDepth < EffectDepth) {
        fragColor = originalColor;
        return;
    }

    vec4 resultColor = originalColor;

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

        vec4 distortedColor = texture(DiffuseSampler, clamp(newTexCoord, 0.0, 1.0));
        float distortedDepth = texture(DepthSampler, clamp(newTexCoord, 0.0, 1.0)).r;

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

        // Only distort if the sampled pixel isn't behind closer geometry.
        if (distortedDepth >= originalDepth || distortedDepth >= EffectDepth) {
            resultColor = mix(distortedColor, originalColor, blendFactor);
        } else {
            resultColor = originalColor;
        }
    }

    if (glowFactor > 0.0) {
        resultColor.rgb = mix(resultColor.rgb, glowColor, glowFactor * (1.0 - distToCenter/glowRadius));
    }

    fragColor = resultColor;
}
