# OmniCrossbow (Minecraft 1.21)

**OmniCrossbow** lets you fire almost any item as improvised crossbow ammo.

## How it works

- Hold a crossbow in one hand.
- Put any item you want to launch in the other hand.
- **Crouch + use** the crossbow to launch that item.

## Custom firing effects

Different item categories produce different muzzle effects:

- **Fire-themed items** (`Fire Charge`, `Blaze Powder`, `Magma Cream`) create flame particles and blaze-like sounds.
- **Food items** create happy-villager particles and playful audio.
- **Ender-themed items** (`Ender Pearl`, `Ender Eye`, `Chorus Fruit`) produce portal particles and teleport audio.
- All other items emit crit particles.

## Technical details

- Built for **Fabric + Java 21 + Minecraft 1.21**.
- Uses Fabric's `UseItemCallback` event.
- Java-only implementation (no Kotlin).
