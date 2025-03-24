#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
in float Time;
in float BlackholeRadius;
in float DistortionAmount;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = texCoord0 - center;
    float dist = length(offset);
    if (dist < BlackholeRadius) {
        float distortion = DistortionAmount * (BlackholeRadius - dist) / BlackholeRadius;
        offset *= 1.0 + distortion * sin(Time);
    }
    //Merge of position_color_tex and rendertype_lightning
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}