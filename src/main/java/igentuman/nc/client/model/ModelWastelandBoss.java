package igentuman.nc.client.model;// Made with Blockbench 4.12.4

import igentuman.nc.client.model.animation.*;
import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.NuclearCraft.rl;
import static java.lang.Integer.*;

public class ModelWastelandBoss<T extends EntityWastelandBoss> extends HumanoidModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(rl("wasteland_boss"), "main");

	// Animation variables
	private BossAnimation currentAnimation = null;
	private float animationEndTime = MAX_VALUE;
	private float currentAnimationTime = 0;
	private boolean isAnimating = false;
	public ModelPart root;

	public ModelWastelandBoss(ModelPart root) {
        super(root);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		CubeDeformation none = CubeDeformation.NONE;
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 59).addBox(-7.0F, -16.0F, -7.0F, 14.0F, 13.0F, 14.0F, none)
				.texOffs(0, 86).addBox(-5.0F, -3.0F, -3.0F, 10.0F, 4.0F, 8.0F, none), PartPose.offset(0.0F, -10.0F, 0.0F));

		//mouth
		PartDefinition bottom = head.addOrReplaceChild("bottom", CubeListBuilder.create(), PartPose.offset(0.25F, -7.0F, 0.25F));
		bottom.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 86).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(5.0F, 0.9718F, -7.3048F, 0.1772F, -0.1719F, -0.0306F));
		bottom.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 89).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 1.0F, 1.0F, none), PartPose.offsetAndRotation(3.5F, 0.9718F, -7.3048F, 0.1752F, 0.0859F, 0.0152F));
		bottom.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(84, 56).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, none)
				.texOffs(44, 89).addBox(-3.5F, 0.5F, -0.5F, 1.0F, 1.0F, 1.0F, none)
				.texOffs(68, 56).addBox(-8.0F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(2.0F, 0.9718F, -7.3048F, 0.1745F, 0.0F, 0.0F));
		bottom.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(80, 56).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(0.5F, 0.9718F, -7.3048F, 0.1752F, 0.0859F, 0.0152F));
		bottom.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(76, 56).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(-2.5F, 0.9718F, -7.3048F, 0.1787F, -0.2148F, -0.0385F));
		bottom.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(72, 56).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(-4.0F, 0.9718F, -7.3048F, 0.439F, 0.1719F, 0.0306F));

		PartDefinition top = head.addOrReplaceChild("top", CubeListBuilder.create(), PartPose.offset(0.25F, -7.0F, 0.25F));
		top.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(40, 89).addBox(4.75F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, none)
				.texOffs(36, 89).addBox(3.25F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, none)
				.texOffs(48, 86).addBox(-1.25F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, none)
				.texOffs(52, 89).addBox(-2.75F, -0.25F, -0.5F, 1.0F, 1.0F, 1.0F, none), PartPose.offsetAndRotation(-0.25F, -1.2897F, -7.1745F, 2.8362F, 0.0F, 0.0F));
		top.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(88, 56).addBox(1.75F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(-0.25F, -1.2897F, -7.1745F, 2.6616F, 0.0F, 0.0F));
		top.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(52, 86).addBox(0.25F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(-0.25F, -1.2897F, -7.1745F, 2.8292F, 0.2079F, -0.0666F));
		top.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(44, 86).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(-3.9964F, -1.1004F, -7.4057F, 2.8292F, -0.2079F, 0.0666F));
		top.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(40, 86).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, none), PartPose.offsetAndRotation(-5.5F, -1.0512F, -7.2497F, 2.9671F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-1.0F, 9.0F, 4.0F));
		body.addOrReplaceChild("stomach_r1", CubeListBuilder.create().texOffs(0, 31).addBox(-11.5F, -8.0F, -5.5F, 23.0F, 17.0F, 11.0F, none), PartPose.offsetAndRotation(1.5F, -7.8998F, 0.7157F, 0.0873F, 0.0F, 0.0F));
		body.addOrReplaceChild("chest_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -2.0F, -5.0F, 32.0F, 16.0F, 15.0F, none), PartPose.offsetAndRotation(0.0F, -17.0F, -4.0F, 0.2182F, 0.0F, 0.0F));

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.25F, -17.0F, 0.25F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(17.7934F, -6.2538F, 4.75F));
		left_arm.addOrReplaceChild("left_lower_arm_r1", CubeListBuilder.create().texOffs(56, 59).addBox(-4.5F, -7.5F, -5.0F, 9.0F, 15.0F, 10.0F, none), PartPose.offsetAndRotation(2.7066F, 18.7538F, -1.75F, -0.2182F, 0.0F, 0.0F));
		left_arm.addOrReplaceChild("left_upper_arm_r1", CubeListBuilder.create().texOffs(56, 84).addBox(-3.0F, -7.5F, -3.5F, 6.0F, 16.0F, 7.0F, none), PartPose.offsetAndRotation(1.2066F, 4.7538F, -0.25F, 0.0F, 0.0F, -0.1745F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-18.75F, -6.5F, 3.75F));
		right_arm.addOrReplaceChild("right_lower_arm_r1", CubeListBuilder.create().texOffs(68, 31).addBox(-4.5F, -7.5F, -5.0F, 9.0F, 15.0F, 10.0F, none), PartPose.offsetAndRotation(-1.75F, 19.0F, -0.75F, -0.2182F, 0.0F, 0.0F));
		right_arm.addOrReplaceChild("right_upper_arm_r1", CubeListBuilder.create().texOffs(82, 84).addBox(-3.0F, -7.5F, -3.5F, 6.0F, 16.0F, 7.0F, none), PartPose.offsetAndRotation(-0.25F, 5.0F, 0.75F, 0.0F, 0.0F, 0.1745F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(8.0F, 10.0F, 5.0F));
		left_leg.addOrReplaceChild("left_lower_leg", CubeListBuilder.create().texOffs(94, 0).addBox(-5.0F, 1.0F, -5.6632F, 9.0F, 8.0F, 7.0F, none), PartPose.offset(0.0F, 6.0F, 0.0F));
		left_leg.addOrReplaceChild("left_upper_leg", CubeListBuilder.create().texOffs(94, 56).addBox(-3.5F, -5.0F, -3.5F, 7.0F, 8.0F, 7.0F, none), PartPose.offsetAndRotation(-0.5F, 4.5F, -1.5F, -0.1745F, 0.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-7.0F, 10.0F, 4.0F));
		right_leg.addOrReplaceChild("right_lower_leg", CubeListBuilder.create().texOffs(94, 15).addBox(-4.0F, 1.0F, -4.6632F, 9.0F, 8.0F, 7.0F, none), PartPose.offset(0.0F, 6.0F, 0.0F));
		right_leg.addOrReplaceChild("right_upper_leg", CubeListBuilder.create().texOffs(0, 98).addBox(-3.5F, -5.0F, -3.5F, 7.0F, 8.0F, 7.0F, none), PartPose.offsetAndRotation(0.5F, 4.5F, -0.5F, -0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return List.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

		this.bodyParts().forEach(ModelPart::resetPose);
		this.headParts().forEach(ModelPart::resetPose);
		if (isAnimating && currentAnimation != null) {
			if (ageInTicks >= animationEndTime) {
				isAnimating = false;
				currentAnimation = null;
				currentAnimationTime = 0;
				animationEndTime = MAX_VALUE;
			} else {
				if(animationEndTime == MAX_VALUE) {
					animationEndTime = ageInTicks + currentAnimation.getAnimationDuration();
				}
				// Calculate animation time as the time passed since animation started
				currentAnimationTime = ageInTicks - (animationEndTime - currentAnimation.getAnimationDuration());
				animateWithDefinition(currentAnimation, currentAnimationTime);
			}
		} else {
				this.head.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.5F;
				this.head.xRot = headPitch * ((float)Math.PI / 180F) * 0.5F;

				// Walking animation
				float walkSpeed = 0.5F;
				float walkAmount = Math.min(limbSwingAmount * 1.5F, 1.0F);

				// Leg animations
				this.leftLeg.xRot = (float) Math.cos(limbSwing * walkSpeed) * 0.8F * walkAmount;
				this.rightLeg.xRot = (float) Math.cos(limbSwing * walkSpeed + Math.PI) * 0.8F * walkAmount;

				// Arm animations
				this.leftArm.xRot = (float) Math.cos(limbSwing * walkSpeed + Math.PI) * 0.5F * walkAmount;
				this.rightArm.xRot = (float) Math.cos(limbSwing * walkSpeed) * 0.5F * walkAmount;

				// Subtle body movement
				this.body.yRot = (float) Math.cos(limbSwing * walkSpeed * 0.5F) * 0.1F * walkAmount;

				// Idle animation using ageInTicks for continuous subtle movement
				float idleIntensity = 0.05F * (1.0F - walkAmount); // Reduce idle animation when walking
				this.body.zRot = (float) Math.sin(ageInTicks * 0.05F) * idleIntensity;

				// Subtle breathing animation
				float breathingSpeed = 0.1F;
				float breathingAmount = 0.025F;
				this.body.xScale += Math.sin(ageInTicks * breathingSpeed*3) * breathingAmount/5;
				this.body.zScale += Math.sin(ageInTicks * breathingSpeed*3) * breathingAmount/5;
				this.body.xRot += Math.sin(ageInTicks * breathingSpeed) * breathingAmount;
			}
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return List.of(this.head);
	}

	private void animateWithDefinition(BossAnimation animation, float time) {
		animation.playAnimation(this, time);
	}

	/**
	 * Handle events from the entity to trigger animations
	 */
	public void handleEntityEvent(byte eventId) {
		switch (eventId) {
			case 4: // Slam attack
				runAnimation(SlamAnimation.instance);
				break;
			case 5: // Radiation burst
				runAnimation(RadiationBurstAnimation.instance);
				break;
			case 6: // Summon ghouls
				runAnimation(SummonAnimation.instance);
				break;
			case 7: // Ranged attack
				runAnimation(RangeAttackAnimation.instance);
				break;
			case 8: // Regular attack
				runAnimation(AttackAnimation.instance);
				break;
		}
	}

	/**
	 * Start playing an animation
	 */
	private void runAnimation(BossAnimation animation) {
		this.currentAnimation = animation;
		this.animationEndTime = MAX_VALUE;
		this.currentAnimationTime = 0;
		this.isAnimating = true;
	}
}

