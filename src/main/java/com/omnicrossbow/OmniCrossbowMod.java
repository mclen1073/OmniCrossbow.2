package com.omnicrossbow;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmniCrossbowMod implements ModInitializer {
	public static final String MOD_ID = "omnicrossbow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final double BASE_SPEED = 1.9D;

	@Override
	public void onInitialize() {
		UseItemCallback.EVENT.register(OmniCrossbowMod::handleCrossbowUse);
		LOGGER.info("OmniCrossbow initialized: crouch + use crossbow to fire your offhand item.");
	}

	private static InteractionResultHolder<ItemStack> handleCrossbowUse(Player player, Level level, InteractionHand hand) {
		ItemStack usedStack = player.getItemInHand(hand);
		if (!(usedStack.getItem() instanceof CrossbowItem)) {
			return InteractionResultHolder.pass(usedStack);
		}

		if (!player.isCrouching()) {
			return InteractionResultHolder.pass(usedStack);
		}

		InteractionHand ammoHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack ammoStack = player.getItemInHand(ammoHand);
		if (ammoStack.isEmpty() || ammoStack.getItem() instanceof CrossbowItem) {
			return InteractionResultHolder.pass(usedStack);
		}

		if (level.isClientSide()) {
			return InteractionResultHolder.success(usedStack);
		}

		ItemStack shotStack = ammoStack.copyWithCount(1);
		launchItemProjectile((ServerLevel) level, player, shotStack);
		applyFiringEffects((ServerLevel) level, player, shotStack);

		if (!player.getAbilities().instabuild) {
			ammoStack.shrink(1);
			usedStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
				? net.minecraft.world.entity.EquipmentSlot.MAINHAND
				: net.minecraft.world.entity.EquipmentSlot.OFFHAND);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		player.getCooldowns().addCooldown(usedStack.getItem(), 8);

		return InteractionResultHolder.consume(usedStack);
	}

	private static void launchItemProjectile(ServerLevel level, Player player, ItemStack shotStack) {
		Vec3 look = player.getLookAngle();
		Vec3 start = player.getEyePosition().add(look.scale(0.45D));
		ItemEntity projectile = new ItemEntity(level, start.x, start.y - 0.1D, start.z, shotStack);

		double speed = BASE_SPEED;
		if (shotStack.is(Items.ANVIL) || shotStack.is(Items.CHIPPED_ANVIL) || shotStack.is(Items.DAMAGED_ANVIL)) {
			speed = 1.35D;
		}
		if (shotStack.isEdible()) {
			speed = 2.1D;
		}

		projectile.setPickUpDelay(40);
		projectile.setDeltaMovement(look.scale(speed));
		projectile.setNoGravity(true);
		projectile.setInvulnerable(true);

		level.addFreshEntity(projectile);
	}

	private static void applyFiringEffects(ServerLevel level, Player player, ItemStack shotStack) {
		Vec3 look = player.getLookAngle();
		Vec3 origin = player.getEyePosition().add(look.scale(0.8D));

		if (shotStack.is(Items.FIRE_CHARGE) || shotStack.is(Items.BLAZE_POWDER) || shotStack.is(Items.MAGMA_CREAM)) {
			level.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 16, 0.25, 0.25, 0.25, 0.01);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.85F, 1.1F);
			return;
		}

		if (shotStack.isEdible()) {
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, origin.x, origin.y, origin.z, 12, 0.2, 0.2, 0.2, 0.02);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.6F, 1.3F);
			return;
		}

		if (shotStack.is(Items.ENDER_PEARL) || shotStack.is(Items.ENDER_EYE) || shotStack.is(Items.CHORUS_FRUIT)) {
			level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y, origin.z, 28, 0.35, 0.35, 0.35, 0.2);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.0F);
			return;
		}

		if (shotStack.is(Items.WITHER_ROSE) || shotStack.is(Items.WITHER_SKELETON_SKULL) || shotStack.is(Items.NETHER_STAR)) {
			fireWitherBeam(level, player, origin, look);
			return;
		}

		// Extra interaction 1: TNT burst visuals + explosion audio.
		if (shotStack.is(Items.TNT)) {
			level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, origin.x, origin.y, origin.z, 1, 0.0, 0.0, 0.0, 0.0);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.0F);
			return;
		}

		// Extra interaction 2: Golden foods buff the shooter.
		if (shotStack.is(Items.GOLDEN_APPLE) || shotStack.is(Items.ENCHANTED_GOLDEN_APPLE) || shotStack.is(Items.GLISTERING_MELON_SLICE)) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
			level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, origin.x, origin.y, origin.z, 8, 0.2, 0.2, 0.2, 0.01);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.25F);
			return;
		}

		// Extra interaction 3: Toxic items poison nearby mobs.
		if (shotStack.is(Items.SPIDER_EYE) || shotStack.is(Items.POISONOUS_POTATO) || shotStack.is(Items.FERMENTED_SPIDER_EYE)) {
			applyAreaEffectToMobs(level, origin, 4.0D, new MobEffectInstance(MobEffects.POISON, 100, 0));
			level.sendParticles(ParticleTypes.ITEM_SLIME, origin.x, origin.y, origin.z, 16, 0.3, 0.3, 0.3, 0.02);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 0.8F, 0.75F);
			return;
		}

		// Extra interaction 4: Slime/honey ammo slows nearby mobs.
		if (shotStack.is(Items.SLIME_BALL) || shotStack.is(Items.HONEY_BOTTLE) || shotStack.is(Items.HONEYCOMB)) {
			applyAreaEffectToMobs(level, origin, 4.5D, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
			level.sendParticles(ParticleTypes.FALLING_HONEY, origin.x, origin.y, origin.z, 14, 0.3, 0.3, 0.3, 0.01);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.9F, 0.9F);
			return;
		}

		// Extra interaction 5: Prismarine/nautilus gives brief water mobility.
		if (shotStack.is(Items.PRISMARINE_CRYSTALS) || shotStack.is(Items.PRISMARINE_SHARD) || shotStack.is(Items.NAUTILUS_SHELL)) {
			player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 140, 0));
			player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 140, 0));
			level.sendParticles(ParticleTypes.BUBBLE_POP, origin.x, origin.y, origin.z, 18, 0.3, 0.3, 0.3, 0.05);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.1F);
			return;
		}

		level.sendParticles(ParticleTypes.CRIT, origin.x, origin.y, origin.z, 10, 0.2, 0.2, 0.2, 0.05);
	}

	private static void applyAreaEffectToMobs(ServerLevel level, Vec3 center, double radius, MobEffectInstance effect) {
		AABB area = new AABB(center, center).inflate(radius);
		for (Mob mob : level.getEntitiesOfClass(Mob.class, area, mob -> mob.isAlive() && !mob.isSpectator())) {
			mob.addEffect(new MobEffectInstance(effect));
		}
	}

	private static void fireWitherBeam(ServerLevel level, Player player, Vec3 start, Vec3 look) {
		Vec3 end = start.add(look.scale(24.0D));
		AABB beamBox = new AABB(start, end).inflate(1.1D);

		for (Mob mob : level.getEntitiesOfClass(Mob.class, beamBox, mob -> mob.isAlive() && !mob.isSpectator())) {
			Vec3 mobCenter = mob.getBoundingBox().getCenter();
			double maxDistance = 0.75D + (mob.getBbWidth() * 0.6D);
			if (distancePointToSegment(mobCenter, start, end) <= maxDistance) {
				mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
			}
		}

		for (int i = 0; i <= 32; i++) {
			double t = i / 32.0D;
			Vec3 p = start.lerp(end, t);
			level.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
			level.sendParticles(ParticleTypes.ENTITY_EFFECT, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.95F, 0.7F);
	}

	private static double distancePointToSegment(Vec3 point, Vec3 start, Vec3 end) {
		Vec3 segment = end.subtract(start);
		double segmentLengthSquared = segment.lengthSqr();
		if (segmentLengthSquared == 0.0D) {
			return point.distanceTo(start);
		}

		double t = point.subtract(start).dot(segment) / segmentLengthSquared;
		t = Math.max(0.0D, Math.min(1.0D, t));
		Vec3 projection = start.add(segment.scale(t));
		return point.distanceTo(projection);
	}
}
