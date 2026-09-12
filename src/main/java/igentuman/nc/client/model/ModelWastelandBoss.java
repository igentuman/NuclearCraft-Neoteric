package igentuman.nc.client.model;

import igentuman.nc.client.model.animation.BossAnimation;
import igentuman.nc.client.model.animation.BossAnimations;
import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class ModelWastelandBoss extends HumanoidModel<EntityWastelandBoss> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(rl("wasteland_boss"), "main");

    private final ModelPart root;

    public ModelWastelandBoss(ModelPart root) {
        super(root);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation deformation = CubeDeformation.NONE;

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 59).addBox(-7.0F, -16.0F, -7.0F, 14.0F, 13.0F, 14.0F, deformation)
                        .texOffs(0, 86).addBox(-5.0F, -3.0F, -3.0F, 10.0F, 4.0F, 8.0F, deformation),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition lowerTeeth = head.addOrReplaceChild("bottom", CubeListBuilder.create(), PartPose.offset(0.25F, -7.0F, 0.25F));
        lowerTeeth.addOrReplaceChild("tooth_1", CubeListBuilder.create().texOffs(36, 86)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(5.0F, 0.9718F, -7.3048F, 0.1772F, -0.1719F, -0.0306F));
        lowerTeeth.addOrReplaceChild("tooth_2", CubeListBuilder.create().texOffs(48, 89)
                        .addBox(-0.5F, 0.5F, -0.5F, 1.0F, 1.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(3.5F, 0.9718F, -7.3048F, 0.1752F, 0.0859F, 0.0152F));
        lowerTeeth.addOrReplaceChild("teeth_3", CubeListBuilder.create()
                        .texOffs(84, 56).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, deformation)
                        .texOffs(44, 89).addBox(-3.5F, 0.5F, -0.5F, 1.0F, 1.0F, 1.0F, deformation)
                        .texOffs(68, 56).addBox(-8.0F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(2.0F, 0.9718F, -7.3048F, 0.1745F, 0.0F, 0.0F));
        lowerTeeth.addOrReplaceChild("tooth_4", CubeListBuilder.create().texOffs(80, 56)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(0.5F, 0.9718F, -7.3048F, 0.1752F, 0.0859F, 0.0152F));
        lowerTeeth.addOrReplaceChild("tooth_5", CubeListBuilder.create().texOffs(76, 56)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-2.5F, 0.9718F, -7.3048F, 0.1787F, -0.2148F, -0.0385F));
        lowerTeeth.addOrReplaceChild("tooth_6", CubeListBuilder.create().texOffs(72, 56)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-4.0F, 0.9718F, -7.3048F, 0.439F, 0.1719F, 0.0306F));

        PartDefinition upperTeeth = head.addOrReplaceChild("top", CubeListBuilder.create(), PartPose.offset(0.25F, -7.0F, 0.25F));
        upperTeeth.addOrReplaceChild("teeth_1", CubeListBuilder.create()
                        .texOffs(40, 89).addBox(4.75F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, deformation)
                        .texOffs(36, 89).addBox(3.25F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, deformation)
                        .texOffs(48, 86).addBox(-1.25F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, deformation)
                        .texOffs(52, 89).addBox(-2.75F, -0.25F, -0.5F, 1.0F, 1.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-0.25F, -1.2897F, -7.1745F, 2.8362F, 0.0F, 0.0F));
        upperTeeth.addOrReplaceChild("tooth_2", CubeListBuilder.create().texOffs(88, 56)
                        .addBox(1.75F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-0.25F, -1.2897F, -7.1745F, 2.6616F, 0.0F, 0.0F));
        upperTeeth.addOrReplaceChild("tooth_3", CubeListBuilder.create().texOffs(52, 86)
                        .addBox(0.25F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-0.25F, -1.2897F, -7.1745F, 2.8292F, 0.2079F, -0.0666F));
        upperTeeth.addOrReplaceChild("tooth_4", CubeListBuilder.create().texOffs(44, 86)
                        .addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-3.9964F, -1.1004F, -7.4057F, 2.8292F, -0.2079F, 0.0666F));
        upperTeeth.addOrReplaceChild("tooth_5", CubeListBuilder.create().texOffs(40, 86)
                        .addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, deformation),
                PartPose.offsetAndRotation(-5.5F, -1.0512F, -7.2497F, 2.9671F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-1.0F, 9.0F, 4.0F));
        body.addOrReplaceChild("stomach", CubeListBuilder.create().texOffs(0, 31)
                        .addBox(-11.5F, -8.0F, -5.5F, 23.0F, 17.0F, 11.0F, deformation),
                PartPose.offsetAndRotation(1.5F, -7.8998F, 0.7157F, 0.0873F, 0.0F, 0.0F));
        body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-15.0F, -2.0F, -5.0F, 32.0F, 16.0F, 15.0F, deformation),
                PartPose.offsetAndRotation(0.0F, -17.0F, -4.0F, 0.2182F, 0.0F, 0.0F));

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.25F, -17.0F, 0.25F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(17.7934F, -6.2538F, 4.75F));
        leftArm.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(56, 59)
                        .addBox(-4.5F, -7.5F, -5.0F, 9.0F, 15.0F, 10.0F, deformation),
                PartPose.offsetAndRotation(2.7066F, 18.7538F, -1.75F, -0.2182F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(56, 84)
                        .addBox(-3.0F, -7.5F, -3.5F, 6.0F, 16.0F, 7.0F, deformation),
                PartPose.offsetAndRotation(1.2066F, 4.7538F, -0.25F, 0.0F, 0.0F, -0.1745F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-18.75F, -6.5F, 3.75F));
        rightArm.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(68, 31)
                        .addBox(-4.5F, -7.5F, -5.0F, 9.0F, 15.0F, 10.0F, deformation),
                PartPose.offsetAndRotation(-1.75F, 19.0F, -0.75F, -0.2182F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(82, 84)
                        .addBox(-3.0F, -7.5F, -3.5F, 6.0F, 16.0F, 7.0F, deformation),
                PartPose.offsetAndRotation(-0.25F, 5.0F, 0.75F, 0.0F, 0.0F, 0.1745F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(8.0F, 10.0F, 5.0F));
        leftLeg.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(94, 0)
                .addBox(-5.0F, 1.0F, -5.6632F, 9.0F, 8.0F, 7.0F, deformation), PartPose.offset(0.0F, 6.0F, 0.0F));
        leftLeg.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(94, 56)
                        .addBox(-3.5F, -5.0F, -3.5F, 7.0F, 8.0F, 7.0F, deformation),
                PartPose.offsetAndRotation(-0.5F, 4.5F, -1.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-7.0F, 10.0F, 4.0F));
        rightLeg.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(94, 15)
                .addBox(-4.0F, 1.0F, -4.6632F, 9.0F, 8.0F, 7.0F, deformation), PartPose.offset(0.0F, 6.0F, 0.0F));
        rightLeg.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(0, 98)
                        .addBox(-3.5F, -5.0F, -3.5F, 7.0F, 8.0F, 7.0F, deformation),
                PartPose.offsetAndRotation(0.5F, 4.5F, -0.5F, -0.1745F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(EntityWastelandBoss entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.5F;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.5F;
        float walkAmount = Math.min(limbSwingAmount * 1.5F, 1.0F);
        leftLeg.xRot += Mth.cos(limbSwing * 0.5F) * 0.8F * walkAmount;
        rightLeg.xRot += Mth.cos(limbSwing * 0.5F + Mth.PI) * 0.8F * walkAmount;
        leftArm.xRot += Mth.cos(limbSwing * 0.5F + Mth.PI) * 0.5F * walkAmount;
        rightArm.xRot += Mth.cos(limbSwing * 0.5F) * 0.5F * walkAmount;
        body.yRot = Mth.cos(limbSwing * 0.25F) * 0.1F * walkAmount;
        body.zRot = Mth.sin(ageInTicks * 0.05F) * 0.05F * (1.0F - walkAmount);
        body.xRot += Mth.sin(ageInTicks * 0.1F) * 0.025F;

        BossAnimation animation = BossAnimations.forEvent(entity.getAnimationEvent());
        float animationTime = ageInTicks - entity.getAnimationStartTick();
        if (animation != null && animationTime >= 0.0F && animationTime <= animation.durationTicks()) {
            animation.apply(this, animationTime);
        }
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return List.of(head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(body, rightArm, leftArm, rightLeg, leftLeg);
    }
}
