package squee;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.entity.Creature;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.World.Environment;

public final class Squee extends JavaPlugin implements Listener {

    private final int BedRadius = 200;
    private final int OverworldRadius = BedRadius*2;
    private final int NetherRadius = 500;


    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block placedBlock = event.getBlock();
        if (event.getBlock().getType() == Material.RESPAWN_ANCHOR) {
            // Perform explosion logic here
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
        if ((placedBlock.getType() == Material.WHITE_BED || placedBlock.getType() == Material.LIGHT_GRAY_BED || placedBlock.getType() == Material.GRAY_BED || placedBlock.getType() == Material.BLACK_BED ||
            placedBlock.getType() == Material.BROWN_BED || placedBlock.getType() == Material.RED_BED || placedBlock.getType() == Material.ORANGE_BED || placedBlock.getType() == Material.YELLOW_BED ||
            placedBlock.getType() == Material.LIME_BED || placedBlock.getType() == Material.GREEN_BED || placedBlock.getType() == Material.CYAN_BED || placedBlock.getType() == Material.LIGHT_BLUE_BED ||
            placedBlock.getType() == Material.BLUE_BED || placedBlock.getType() == Material.PURPLE_BED || placedBlock.getType() == Material.MAGENTA_BED || placedBlock.getType() == Material.PINK_BED)
                && Math.sqrt(Math.pow(placedBlock.getLocation().getBlockX(), 2) + Math.pow(placedBlock.getLocation().getBlockZ(), 2)) > BedRadius) {
            player.sendMessage(Component.text("You feel scared so far from home... " + Math.sqrt(Math.pow(placedBlock.getLocation().getBlockX(), 2) + Math.pow(placedBlock.getLocation().getBlockZ(), 2))));

            // Cancel the block placement
            event.setCancelled(true);

            // Remove the placed block
            placedBlock.setType(Material.AIR);
        }
    }

    private Player findClosestPlayer(LivingEntity entity) {
        double closestDistance = 20;
        Player closestPlayer = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            double distance = player.getLocation().distance(entity.getLocation());

            if (distance < closestDistance) {
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Creature) {
            Creature creature = (Creature) event.getEntity();

            // Check if the spawned entity is a mob
            if (isHostileMob(creature.getType())) {

                // Get the distance of the spawn location
                double distanceSpawn = Math.sqrt(Math.pow(event.getLocation().getBlockX(), 2) + Math.pow(event.getLocation().getBlockZ(), 2));
                double health_multiplier = 1;
                
		World world = event.getLocation().getWorld();
                // Check the environment of the world
                if (world.getEnvironment() == Environment.NETHER) {
                    if (distanceSpawn > NetherRadius){
                        health_multiplier = 0.5 * (Math.pow(((distanceSpawn - NetherRadius) / 100), 0.5)) + 1;
                    }
                } else if (world.getEnvironment() == Environment.THE_END) {
                    health_multiplier = 2;
                } else {
                    if (distanceSpawn > OverworldRadius){
                        health_multiplier = 0.5 * (Math.pow(((distanceSpawn - OverworldRadius) / 100), 0.5)) + 1;
                    }
                }

                // Get the Y level of the spawn location
                if (event.getLocation().getBlockY() < 0)
                {
                    health_multiplier = health_multiplier * 2;
                }

                // Calculate modified health based on the equation 2 * level
                double modifiedHealth = (creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue()) * health_multiplier;

                // Set the max health attribute of the mob
                creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(modifiedHealth);
                creature.setHealth(modifiedHealth);

                // Set the attack damage attribute of the mob
                AttributeInstance attackDamageAttribute = creature.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                if (attackDamageAttribute != null) {
                    Double defaultAttackDamage = attackDamageAttribute.getDefaultValue();

                    // Check if the default value is not null
                    if (defaultAttackDamage != null) {
                        double modifiedAttackDamage = defaultAttackDamage * health_multiplier;

                        // Clear existing modifiers
                        for (AttributeModifier modifier : attackDamageAttribute.getModifiers()) {
                            attackDamageAttribute.removeModifier(modifier);
                        }

                        // Add the new modifier
                        attackDamageAttribute.addModifier(new AttributeModifier(UUID.randomUUID(), "customAttackDamage", modifiedAttackDamage, AttributeModifier.Operation.ADD_NUMBER));
                    }
                }

            }
        }
    }

    private boolean isHostileMob(EntityType entityType) {
        String entityTypeName = entityType.name();

        // Add more hostile mob types as needed
        return entityTypeName.equals("SPIDER") || entityTypeName.equals("CAVE_SPIDER")
                || entityTypeName.equals("ENDERMAN") || entityTypeName.equals("PIGLIN")
                || entityTypeName.equals("ZOMBIFIED_PIGLIN") || entityTypeName.equals("EVOKER")
                || entityTypeName.equals("VINDICATOR") || entityTypeName.equals("ILLAGER")
                || entityTypeName.equals("RAVAGER") || entityTypeName.equals("VEX")
                || entityTypeName.equals("CHICKEN_JOCKEY") || entityTypeName.equals("ENDERMITE")
                || entityTypeName.equals("GUARDIAN") || entityTypeName.equals("ELDER_GUARDIAN")
                || entityTypeName.equals("SHULKER") || entityTypeName.equals("SKELETON_HORSEMAN")
                || entityTypeName.equals("HUSK") || entityTypeName.equals("STRAY")
                || entityTypeName.equals("PHANTOM") || entityTypeName.equals("BLAZE")
                || entityTypeName.equals("CREEPER") || entityTypeName.equals("GHAST")
                || entityTypeName.equals("MAGMA_CUBE") || entityTypeName.equals("SILVERFISH")
                || entityTypeName.equals("SKELETON_SLIME") || entityTypeName.equals("SPIDER_JOCKEY")
                || entityTypeName.equals("ZOMBIE") || entityTypeName.equals("ZOMBIE_VILLAGER")
                || entityTypeName.equals("DROWNED") || entityTypeName.equals("WITHER_SKELETON")
                || entityTypeName.equals("WITCH") || entityTypeName.equals("HOGLIN")
                || entityTypeName.equals("ZOGLIN") || entityTypeName.equals("PIGLIN_BRUTE")
                || entityTypeName.equals("ENDER_DRAGON") || entityTypeName.equals("WITHER");
    }

        @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
