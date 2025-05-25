package igentuman.nc.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.entity.EntityFeralGhoulBoss;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

import static igentuman.nc.NuclearCraft.rl;

public class ModelFeralGhoulBoss extends HumanoidModel<EntityFeralGhoulBoss> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(rl("feral_ghoul_boss"), "main");

    // Special animation states for boss attacks
    private boolean isDoingSlamAttack = false;
    private boolean isDoingRadiationBurstAttack = false;
    private float attackAnimationProgress = 0.0F;

    public ModelFeralGhoulBoss(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.4F, -8.8F, -4.4F, 8.8F, 8.8F, 8.8F, new CubeDeformation(-0.1F)),
                PartPose.offset(0.0F, -1.0F, -3.0F));

        partdefinition.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.4F, -8.8F, -4.4F, 8.8F, 8.8F, 8.8F, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(22, 37)
                        .addBox(-6.0F, 0.0F, -3.0F, 12.0F, 12.0F, 6.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, -3.5F, 0.4363F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(-4.8F, -2.5F, -3.0F, 6.0F, 20.0F, 6.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-6.5F, 1.0F, -2.7F, -0.3491F, -0.1F, 0.1F));

        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .mirror()
                        .addBox(-1.2F, -2.5F, -3.0F, 6.0F, 20.0F, 6.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(6.5F, 1.0F, -2.7F, -0.3491F, 0.1F, -0.1F));

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-2.6F, 0.0F, -3.6F, 5.2F, 12.0F, 5.2F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(-2.5F, 8.0F, 3.0F, -0.0175F, 0.0F, 0.0262F));

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .mirror()
                        .addBox(-2.6F, 0.0F, -3.6F, 5.2F, 12.0F, 5.2F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(2.5F, 8.0F, 3.0F, -0.0175F, 0.0F, -0.0262F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(EntityFeralGhoulBoss entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetAllPose();

        this.handleEntityEvent(entity);

        if (this.isDoingSlamAttack) {
            this.animateSlamAttack(ageInTicks);
        } else if (this.isDoingRadiationBurstAttack) {
            this.animateRadiationBurst(ageInTicks);
        } else {
            // Regular walking/idle animation
            this.animateNormal(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    private void resetAllPose() {
        this.head.setPos(0.0F, -1.0F, -3.0F);
        this.body.setPos(0.0F, -1.0F, -3.5F);
        this.rightArm.setPos(-6.5F, 1.0F, -2.7F);
        this.leftArm.setPos(6.5F, 1.0F, -2.7F);
        this.rightLeg.setPos(-2.5F, 8.0F, 3.0F);
        this.leftLeg.setPos(2.5F, 8.0F, 3.0F);

        // Reset rotations
        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.head.zRot = 0.0F;

        this.body.xRot = 0.4363F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;

        this.rightArm.xRot = -0.3490658503988659F;
        this.rightArm.yRot = -0.1F;
        this.rightArm.zRot = 0.1F;

        this.leftArm.xRot = -0.3490658503988659F;
        this.leftArm.yRot = 0.1F;
        this.leftArm.zRot = -0.1F;

        this.rightLeg.xRot = -0.0175F;
        this.rightLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0262F;

        this.leftLeg.xRot = -0.0175F;
        this.leftLeg.yRot = 0.0F;
        this.leftLeg.zRot = -0.0262F;
    }

    private void animateNormal(EntityFeralGhoulBoss entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Apply head rotations
        this.head.xRot = 0.11344640137963141F + headPitch * 0.017453292F;
        this.head.yRot = netHeadYaw * 0.017453292F;

        // Add the arm swinging animation
        this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;

        // Set arm positions for walking
        this.rightArm.xRot = -0.3490658503988659F + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = -0.3490658503988659F + Mth.cos(limbSwing * 0.6662F) * 2F * limbSwingAmount * 0.5F;

        // Set leg positions for walking
        this.rightLeg.xRot = -0.017453292519943295F + Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = -0.017453292519943295F + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    }

    private void animateSlamAttack(float ageInTicks) {
        float animProgress = Math.min(1.0F, this.attackAnimationProgress);
        float animProgressDown = Math.max(0.0F, this.attackAnimationProgress - 1.0F);

        if (animProgress < 1.0F) {
            this.rightArm.xRot = -1.8F * animProgress - 0.3F;
            this.leftArm.xRot = -1.8F * animProgress - 0.3F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            this.body.xRot = 0.4F - 0.2F * animProgress;
        }
        else {
            this.rightArm.xRot = -2.1F + 2.5F * animProgressDown;
            this.leftArm.xRot = -2.1F + 2.5F * animProgressDown;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            this.body.xRot = 0.2F + 0.2F * animProgressDown;
        }

        this.attackAnimationProgress += 0.05F;
        if (this.attackAnimationProgress >= 2.0F) {
            this.isDoingSlamAttack = false;
            this.attackAnimationProgress = 0.0F;
        }
    }

    private void animateRadiationBurst(float ageInTicks) {
        float animProgress = Math.min(1.0F, this.attackAnimationProgress);

        this.rightArm.xRot = -1.5F * animProgress;
        this.leftArm.xRot = -1.5F * animProgress;
        this.rightArm.zRot = -0.4F * animProgress;
        this.leftArm.zRot = 0.4F * animProgress;
        this.body.xRot = 0.4F - 0.1F * animProgress;

        float pulse = Mth.sin(ageInTicks * 0.3F) * 0.1F;
        this.rightArm.yRot = pulse;
        this.leftArm.yRot = -pulse;

        this.head.xRot = -0.2F * animProgress;

        this.attackAnimationProgress += 0.05F;
        if (this.attackAnimationProgress >= 1.0F) {
            this.isDoingRadiationBurstAttack = false;
            this.attackAnimationProgress = 0.0F;
        }
    }

    private void handleEntityEvent(EntityFeralGhoulBoss entity) {
        if (entity.hurtTime > 0) {
            this.body.xRot += 0.1F;
            this.head.xRot -= 0.1F;
        }
    }

    public void handleEntityEvent(byte eventId) {
        switch (eventId) {
            case 4: // Slam attack
                this.isDoingSlamAttack = true;
                this.isDoingRadiationBurstAttack = false;
                this.attackAnimationProgress = 0.0F;
                break;
            case 5: // Radiation burst
                this.isDoingRadiationBurstAttack = true;
                this.isDoingSlamAttack = false;
                this.attackAnimationProgress = 0.0F;
                break;
            case 6: // Summon ghouls - can reuse radiation burst animation
                this.isDoingRadiationBurstAttack = true;
                this.isDoingSlamAttack = false;
                this.attackAnimationProgress = 0.0F;
                break;
            case 7: // Ranged attack
                // Similar to slam attack but with different arm positions
                this.isDoingSlamAttack = true;
                this.isDoingRadiationBurstAttack = false;
                this.attackAnimationProgress = 0.0F;
                break;
        }
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return java.util.List.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return java.util.List.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        poseStack.scale(1.45F, 1.45F, 1.45F);
        poseStack.translate(0, -0.2, 0);
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}
