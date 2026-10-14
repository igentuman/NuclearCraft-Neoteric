package igentuman.nc.client.bomb;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.client.bomb.NukeTimeline.*;

public final class NukeVisualState {

    public static final int PACKED_SIZE = 32;
    private static final float DETAIL_NEAR = 384f;
    private static final float DETAIL_FAR = 1536f;

    public float age;
    public float r;
    public float flash;
    public float morph;
    public float growth;
    public float fade;
    public float heat;
    public float baseHeat;

    public float fireballRadius;
    public float baseRadius;
    public float baseHeight;
    public float capCenterY;
    public float capRadius;
    public float capThickness;
    public float rimRadius;
    public float stemRadius;
    public float groundOffsetY;
    public float groundRingOffsetY;

    public float thermalRadius;
    public float thermalStrength;
    public float hazeStrength;
    public float hazeRadius;
    public float groundRadius;
    public float groundIntensity;

    public float boundsRadius;
    public float boundsTop;
    public float boundsBottom;

    public void evaluate(ActiveBomb bomb, float partialTick) {
        age = bomb.ageSeconds(partialTick);
        r = scale(bomb.yield);
        flash = flash(age);
        morph = morph(age);
        growth = matureGrowth(age);
        fade = fade(age);
        heat = heat(age);
        baseHeat = baseHeat(age);

        float grow = 1f + MATURE_GROWTH * growth;
        fireballRadius = fireballRadius(age, r);
        baseRadius = fireballRadius * (1f + BASE_SPREAD * morph) * (1f + 0.1f * growth);
        baseHeight = baseRadius * (1f - BASE_FLATTEN * morph);
        float seed = CAP_SEED * fireballRadius;
        capRadius = Mth.lerp(morph, seed, CAP_RADIUS * r) * grow;
        capThickness = Mth.lerp(morph, seed, CAP_THICKNESS * r) * grow;
        capCenterY = Mth.lerp(morph, 0f, CAP_HEIGHT * r) + MATURE_ASCENT * r * growth;
        rimRadius = morph * 0.45f * CAP_THICKNESS * r * grow;
        stemRadius = STEM_RADIUS * r * smootherstep(morph * 4f) * grow;

        thermalRadius = thermalRadius(age, r);
        thermalStrength = thermalStrength(age);
        hazeStrength = hazeStrength(age);
        hazeRadius = Math.min(Math.max(thermalRadius, 0f), HAZE_RADIUS * r);
        groundRadius = groundRadius(age, r);
        groundIntensity = groundIntensity(age);

        float surface = bomb.surface.heightOffset(0f, 0f);
        groundOffsetY = Float.isNaN(surface) ? 0f : surface;
        groundRingOffsetY = groundRadius > 0f ? bomb.surface.ringAverage(groundRadius, groundOffsetY) : groundOffsetY;

        float horizontal = Math.max(Math.max(capRadius + rimRadius, baseRadius), groundRadius);
        boundsRadius = horizontal * BOUNDS_MARGIN;
        boundsTop = Math.max(capCenterY + capThickness + rimRadius, baseHeight) * BOUNDS_MARGIN + 0.1f * r;
        boundsBottom = Math.min(Math.min(groundOffsetY, groundRingOffsetY),
                Math.min(capCenterY - capThickness - rimRadius, -baseHeight)) - 0.25f * r;
    }

    public boolean visible() {
        return age >= 0f && fade > 0f;
    }

    public float capExtent() {
        return Math.max(capRadius + rimRadius, baseRadius) * BOUNDS_MARGIN + 0.1f * r;
    }

    public void pack(float[] out, ActiveBomb bomb, Vec3 camera, float detailBias) {
        double dx = bomb.epicenter.getX() + 0.5 - camera.x;
        double dy = bomb.epicenter.getY() + 0.5 - camera.y;
        double dz = bomb.epicenter.getZ() + 0.5 - camera.z;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float detail = detailBias * (1f - Mth.clamp((distance - DETAIL_NEAR) / (DETAIL_FAR - DETAIL_NEAR), 0f, 1f));

        out[0] = (float) dx;
        out[1] = (float) dy;
        out[2] = (float) dz;
        out[3] = r;
        out[4] = age;
        out[5] = morph;
        out[6] = fade;
        out[7] = (bomb.seed & 0xFFFF) / 65535f;
        out[8] = fireballRadius;
        out[9] = capCenterY;
        out[10] = capRadius;
        out[11] = capThickness;
        out[12] = rimRadius;
        out[13] = stemRadius;
        out[14] = capExtent();
        out[15] = boundsTop;

        out[16] = boundsBottom;
        out[17] = groundRadius;
        out[18] = groundOffsetY;
        out[19] = groundIntensity;
        out[20] = heat;
        out[21] = flash;
        out[22] = growth;
        out[23] = detail;
        out[24] = groundRingOffsetY;
        out[25] = groundRadius * 1.1f + 0.3f * r;
        out[26] = baseRadius;
        out[27] = baseHeight;
        out[28] = baseHeat;
        out[29] = 0f;
        out[30] = 0f;
        out[31] = 0f;
    }
}
