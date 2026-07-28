#version 150

in vec4 Position;

uniform mat4 ProjMat;
uniform vec2 OutSize;
uniform vec2 InSize;
uniform vec2 BlurPos;
uniform vec2 BlurDir;
uniform vec2 Radius;
uniform vec2 ScreenSize;
out vec2 texCoord;
out vec2 oneTexel;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    oneTexel = 1.0 / InSize;
    texCoord = Position.xy / OutSize;

    gl_Position.x += (BlurPos.x + BlurPos.y + BlurDir.x + BlurDir.y + Radius.x + Radius.y) * 1e-8;
}