package me.exphc.SilkSpawners;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Formatter;
import java.lang.Byte;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.Material.*;
import org.bukkit.material.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.inventory.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.scheduler.*;
import org.bukkit.enchantments.*;
import org.bukkit.*;

import org.bukkit.craftbukkit.block.CraftCreatureSpawner;

class SilkSpawnersBlockListener extends BlockListener {
    static Logger log = Logger.getLogger("Minecraft");

    SilkSpawners plugin;

    public SilkSpawnersBlockListener(SilkSpawners pl) {
        plugin = pl;
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);

        log.info("broke spawner for "+spawner.getCreatureType().getName());
        // TODO: set data on item

        // TODO: if using silk touch, drop spawner itself (optionally)

        // Drop egg
        ItemStack dropItem = plugin.creature2Egg.get(spawner.getCreatureType());
        World world = player.getWorld();
        world.dropItemNaturally(player.getLocation(), dropItem);
    }

    public void onBlockPlace(BlockPlaceEvent event) {
        Block blockPlaced = event.getBlockPlaced();

        if (blockPlaced.getType() != Material.MOB_SPAWNER) {
            return;
        }

        log.info("place spawner ");

        // TODO: get data from item
        ItemStack item = event.getItemInHand();

        CraftCreatureSpawner spawner = new CraftCreatureSpawner(blockPlaced);
        spawner.setCreatureType(CreatureType.fromName("Zombie"));   
    }
}

public class SilkSpawners extends JavaPlugin {
    static Logger log = Logger.getLogger("Minecraft");
    SilkSpawnersBlockListener blockListener;

    ConcurrentHashMap<CreatureType,ItemStack> creature2Egg;

    public void onEnable() {
        /* Crafting test
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        //item.addEnchantment(Enchantment.SILK_TOUCH, 1); // cannot craft to enchanted items
        item.setDurability((short)1000);        // works!
        //ItemStack item = new ItemStack(Material.WOOL, 1, (short)1); // orange wool

        ShapelessRecipe recipe = new ShapelessRecipe(item);
        recipe.addIngredient(2, Material.DIRT);
        Bukkit.getServer().addRecipe(recipe);
        */

        // Load creature to egg map
        creature2Egg = new ConcurrentHashMap<CreatureType,ItemStack>();

        MemorySection eggSection = (MemorySection)getConfig().get("eggs");
        Map<String,Object> eggMapStrings = eggSection.getValues(true);

        Iterator it = eggMapStrings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String creatureString = (String)pair.getKey();

            CreatureType creatureType = CreatureType.fromName(creatureString);
            if (creatureType == null) {
                log.info("Invalid creature type: " + creatureString);
                continue;
            }

            // TODO: http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs in Bukkit?
            Integer entityInteger = (Integer)pair.getValue();
            short entityID = (short)entityInteger.intValue();

            ItemStack eggItem = new ItemStack(Material.MONSTER_EGG, 1, entityID);

            creature2Egg.put(creatureType, eggItem);
        }

        // Listeners
        blockListener = new SilkSpawnersBlockListener(this);

        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, org.bukkit.event.Event.Priority.Normal, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, org.bukkit.event.Event.Priority.Normal, this);


        log.info("SilkSpawners enabled");
    }

    public void onDisable() {
        log.info("SilkSpawners disabled");
    }
}


