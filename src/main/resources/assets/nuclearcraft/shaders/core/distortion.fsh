#version 150

uniform sampler2D Sampler0;
uniform vec2 DistortionCenter;
uniform float DistortionRadius;
uniform float DistortionAmount;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 pos = texCoord0 - DistortionCenter;
    float dist = length(pos);
    float distortionFactor = smoothstep(DistortionRadius, 0.0, dist) * DistortionAmount;

    vec2 distortedPos = texCoord0 + pos * distortionFactor;
    vec4 texColor = texture(Sampler0, distortedPos);

    fragColor = texColor;
}