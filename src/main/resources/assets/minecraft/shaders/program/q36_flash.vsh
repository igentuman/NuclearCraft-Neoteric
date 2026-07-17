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
out vec4 dummyOut;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    oneTexel = 1.0 / InSize;
    texCoord = Position.xy / OutSize;

    // Create a more complex usage that the compiler can't optimize away
    // Use a conditional that depends on the uniform but always evaluates the same
    vec4 preserveUniforms;
    if (BlurPos.x * BlurDir.y > -999999.0) {
        preserveUniforms = vec4(BlurPos.xy, BlurDir.xy);
    } else {
        preserveUniforms = vec4(0.0);
    }

    // Add Radius with a non-zero coefficient that's extremely small
    preserveUniforms += vec4(Radius.y) * 0.000001;

    dummyOut = preserveUniforms;
}