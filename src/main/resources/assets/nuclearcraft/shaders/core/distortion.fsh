#version 150

uniform sampler2D Sampler0;
uniform float GameTime;
uniform float Intensity;
uniform float Radius;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    // Calculate normalized screen center
    vec2 center = vec2(0.5, 0.5);

    // Convert texture coordinates to screen space
    vec2 pos = texCoord0 - center;
    float dist = length(pos);

    // Calculate distortion based on distance from center
    float distortionFactor = smoothstep(Radius / 100.0, 0.0, dist) * Intensity;

    // Add some time-based animation
    vec2 timeOffset = vec2(sin(GameTime * 0.5), cos(GameTime * 0.3)) * 0.01 * Intensity;

    // Apply distortion to texture coordinates
    vec2 distortedPos = texCoord0 + pos * distortionFactor + timeOffset;

    // Ensure coordinates stay within bounds
    distortedPos = clamp(distortedPos, 0.0, 1.0);

    // Sample texture with distorted coordinates
    vec4 texColor = texture(Sampler0, distortedPos);
    texColor.r = min(texColor.r + 0.5, 1.0);
    fragColor = texColor;
}