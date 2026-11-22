// Logarithmic radiance keeps low-intensity haze and bright fireball samples in
// the same RGBA8 target. The top code represents (2^13 - 1) / 32.
const float NUKE_RADIANCE_SCALE = 32.0;
const float NUKE_RADIANCE_LOG_RANGE = 13.0;

vec3 encodeNukeRadiance(vec3 radiance) {
    return clamp(log2(1.0 + NUKE_RADIANCE_SCALE * max(radiance, vec3(0.0)))
            / NUKE_RADIANCE_LOG_RANGE, 0.0, 1.0);
}

vec3 decodeNukeRadiance(vec3 encoded) {
    return (exp2(encoded * NUKE_RADIANCE_LOG_RANGE) - 1.0) / NUKE_RADIANCE_SCALE;
}
