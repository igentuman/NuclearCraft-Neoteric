#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float Time;
uniform vec3 BlockPos;
uniform float DistortionAmount;
uniform vec3 CameraPos;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = texCoord0 - center;
    float dist = length(offset);

    vec3 blockToCamera = CameraPos - BlockPos;
    float distanceToBlock = length(blockToCamera);

    if (distanceToBlock < DistortionAmount) {
        float distortion = DistortionAmount * (1.0 - distanceToBlock / DistortionAmount);
        offset *= 1.0 + distortion * sin(Time);
    }

    vec4 color = texture(Sampler0, center + offset) * ColorModulator;
    fragColor = color;
}