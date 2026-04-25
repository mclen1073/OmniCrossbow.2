# OmniCrossbow (Minecraft 1.21)

**OmniCrossbow** lets you fire almost any item as improvised crossbow ammo.

## How it works

- Hold a crossbow in one hand.
- Put any item you want to launch in the other hand.
- **Crouch + use** the crossbow to launch that item.

## Custom firing effects

Different item categories produce different muzzle effects and interactions:

- **Fire-themed items** (`Fire Charge`, `Blaze Powder`, `Magma Cream`) create flame particles and blaze-like sounds.
- **Food items** create happy-villager particles and playful audio.
- **Ender-themed items** (`Ender Pearl`, `Ender Eye`, `Chorus Fruit`) produce portal particles and teleport audio.
- **Wither items** (`Wither Rose`, `Wither Skeleton Skull`, `Nether Star`) fire a wither beam that applies the Wither effect to mobs in its path.
- **TNT** creates explosion visuals/sound on firing.
- **Golden food items** (`Golden Apple`, `Enchanted Golden Apple`, `Glistering Melon Slice`) grant regeneration/absorption to the shooter.
- **Toxic items** (`Spider Eye`, `Poisonous Potato`, `Fermented Spider Eye`) poison nearby mobs.
- **Sticky items** (`Slime Ball`, `Honey Bottle`, `Honeycomb`) slow nearby mobs.
- **Ocean items** (`Prismarine Crystals`, `Prismarine Shard`, `Nautilus Shell`) grant temporary water mobility buffs.
- All other items emit crit particles.

## Technical details

- Built for **Fabric + Java 21 + Minecraft 1.21**.
- Uses Fabric's `UseItemCallback` event.
- Java-only implementation (no Kotlin).
