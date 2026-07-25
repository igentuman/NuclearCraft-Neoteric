package igentuman.nc.client.model;

import igentuman.nc.entity.EntityFeralGhoul;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

import static igentuman.nc.NuclearCraft.rl;

public class ModelFeralGhoul extends HumanoidModel<EntityFeralGhoul> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(rl("feral_ghoul"), "main");

    public ModelFeralGhoul(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.1F)),
                PartPose.offset(0.0F, 3.5F, -2.2F));

        partdefinition.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, 1.5F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 2.4F, -2.7F, 0.4363F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-4.2F, 4.4F, -1.7F, -0.3491F, -0.1F, 0.1F));

        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(4.2F, 4.4F, -1.7F, -0.3491F, 0.1F, -0.1F));

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(-1.9F, 12.0F, 2.1F, -0.0175F, 0.0F, 0.0262F));

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(1.9F, 12.0F, 2.1F, -0.0175F, 0.0F, -0.0262F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(EntityFeralGhoul entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.head.setPos(0.0F, 3.5F, -2.2F);
        this.head.xRot = 0.11344640137963141F + headPitch * 0.017453292F;
        this.head.yRot = netHeadYaw * 0.017453292F;

        this.body.xRot = 0.4363323129985824F;

        this.rightArm.setPos(-4.2F, 4.4F, -1.7F);
        this.leftArm.setPos(4.2F, 4.4F, -1.7F);

        this.rightLeg.setPos(-1.9F, 12.0F, 2.1F);
        this.leftLeg.setPos(1.9F, 12.0F, 2.1F);

        this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;

        this.rightArm.xRot = -0.3490658503988659F + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = -0.3490658503988659F + Mth.cos(limbSwing * 0.6662F) * 2F * limbSwingAmount * 0.5F;

        this.rightArm.yRot = -0.10000736613927509F;
        this.leftArm.yRot = 0.10000736613927509F;

        this.rightArm.zRot = 0.10000736613927509F;
        this.leftArm.zRot = -0.10000736613927509F;

        this.rightLeg.xRot = -0.017453292519943295F + Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = -0.017453292519943295F + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        this.rightLeg.zRot = 0.02617993877991494F;
        this.leftLeg.zRot = -0.02617993877991494F;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return java.util.List.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return java.util.List.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }
}
