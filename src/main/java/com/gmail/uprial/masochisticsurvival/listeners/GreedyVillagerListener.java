package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;

public class GreedyVillagerListener implements Listener {
    private final CustomLogger customLogger;
    private final boolean replaceProtection;
    private final boolean overpriceMending;
    private final boolean infoLogAboutActions;

    public GreedyVillagerListener(final CustomLogger customLogger,
                                  final boolean replaceProtection,
                                  final boolean overpriceMending,
                                  final boolean infoLogAboutActions) {
        this.customLogger = customLogger;
        this.replaceProtection = replaceProtection;
        this.overpriceMending = overpriceMending;
        this.infoLogAboutActions = infoLogAboutActions;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if((!event.isCancelled()) && (event.getRightClicked() instanceof Villager)) {
            final Villager villager = (Villager)event.getRightClicked();

            final List<MerchantRecipe> updatedRecipes = new ArrayList<>();
            boolean updatedAny = false;
            for(final MerchantRecipe recipe : villager.getRecipes()) {

                boolean updatedOne = false;
                final ItemStack result = recipe.getResult();
                final List<ItemStack> ingredients = recipe.getIngredients();
                final Map<Enchantment,Integer> enchantments
                        = ((result.getItemMeta() instanceof EnchantmentStorageMeta))
                        ? ((EnchantmentStorageMeta) result.getItemMeta()).getStoredEnchants()
                        : result.getEnchantments();

                for(Map.Entry<Enchantment,Integer> entry : enchantments.entrySet()) {
                    if ((replaceProtection)
                            && replacedProtection(villager, result, entry.getKey(), entry.getValue())) {

                        updatedOne = true;
                    } else if ((overpriceMending)
                            && overpricedMending(villager, entry.getKey(), ingredients)) {

                        updatedOne = true;
                    }
                }
                /*
                    Test data

                    result.setType(Material.NETHERITE_LEGGINGS);
                    result.addUnsafeEnchantment(Enchantment.PROTECTION, 3);

                    updatedOne = true;
                 */
                /*
                    Test data

                    result.setType(Material.ENCHANTED_BOOK);
                    final EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) result.getItemMeta();
                    itemMeta.addStoredEnchant(Enchantment.MENDING, 1, true);
                    result.setItemMeta(itemMeta);

                    updatedOne = true;
                 */
                /*
                    One potential "updated" boolean value
                    is split into updatedOne and updatedAny
                    for performance considerations,
                    to avoid creating big classes
                    on each Villager inventory opening.
                 */
                if(updatedOne) {
                    final MerchantRecipe updatedRecipe = new MerchantRecipe(
                            result,
                            recipe.getUses(),
                            recipe.getMaxUses(),
                            recipe.hasExperienceReward(),
                            recipe.getVillagerExperience(),
                            recipe.getPriceMultiplier(),
                            recipe.getDemand(),
                            recipe.getSpecialPrice()
                    );
                    updatedRecipe.setIngredients(ingredients);

                    updatedRecipes.add(updatedRecipe);

                    updatedAny = true;
                } else {
                    updatedRecipes.add(recipe);
                }
            }
            if(updatedAny) {
                villager.setRecipes(updatedRecipes);

                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("Updated recipes for %s", format(villager)));
                }
            }
        }
    }

    private boolean replacedProtection(final Villager villager,
                                       final ItemStack result,
                                       final Enchantment oldEnchantment,
                                       final int oldLevel) {
        if (!oldEnchantment.equals(Enchantment.PROTECTION)) {
            return false;
        }
        /*
            According to https://minecraft.wiki/w/Thorns,
            Thorns applies a durability penalty to the armor.
         */
        final Enchantment newEnchantment = Enchantment.THORNS;

        // Survival maximum level is 3
        final int newLevel = oldLevel + 3;

        if (result.getItemMeta() instanceof EnchantmentStorageMeta) {
            final EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) result.getItemMeta();
            itemMeta.removeStoredEnchant(oldEnchantment);
            // ignoreLevelRestriction is a must to overcome the survival maximum level
            itemMeta.addStoredEnchant(newEnchantment, newLevel, true);
            result.setItemMeta(itemMeta);
        } else {
            result.removeEnchantment(oldEnchantment);
            // unsafe is a must to overcome the survival maximum level
            result.addUnsafeEnchantment(newEnchantment, newLevel);
        }

        final String message = String.format("Updating %s recipes for %s: changing %s-%d to %s-%d...",
                format(villager), result.getType(),
                oldEnchantment.getName(), oldLevel,
                newEnchantment.getName(), newLevel);

        if(infoLogAboutActions) {
            customLogger.info(message);
        } else {
            customLogger.debug(message);
        }

        return true;
    }

    /*
        According to https://minecraft.wiki/w/Rarity,
        the Epic items.

        Dragon Egg is too unique.
        Mace is a duplicate of Heavy Core.

        According to https://minecraft.wiki/w/Non-renewable_resource,
        the hard items.
     */
    private static final Map<Material,Integer> PRIMARY_MARKUPS = ImmutableMap.<Material,Integer>builder()
            // A farm of Withers may bring a lot of nether stars
            .put(Material.NETHER_STAR, 4)
            // Rare structures
            .put(Material.HEART_OF_THE_SEA, 2)
            .put(Material.ELYTRA, 2)
            .put(Material.DRAGON_HEAD, 2)
            // The rarest item
            .put(Material.HEAVY_CORE, 1)
            // The rarest template
            .put(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 1)
            .build();

    /*
        According to https://minecraft.wiki/w/Ore,
        the most rare resource.

        Emerald is easily traded in villages.
     */
    private static final Map<Material,Integer> SECONDARY_MARKUPS = ImmutableMap.<Material,Integer>builder()
            .put(Material.DIAMOND_BLOCK, 16)
            .put(Material.GOLD_BLOCK, 16)
            // Redstone and lapis : diamond and gold ~ 4:1
            .put(Material.REDSTONE_BLOCK, 64)
            .put(Material.LAPIS_BLOCK, 64)
            .build();

    // WARNING: MerchantRecipe can only have maximum 2 ingredients.

    private boolean overpricedMending(final Villager villager,
                                      final Enchantment enchantment,
                                      final List<ItemStack> ingredients) {
        if(!enchantment.equals(Enchantment.MENDING)) {
            return false;
        }

        for(final ItemStack ingredient : ingredients) {
            if(PRIMARY_MARKUPS.containsKey(ingredient.getType())) {
                return false;
            }
        }

        ingredients.clear();

        final Material pmMaterial = RandomUtils.getSetItem(PRIMARY_MARKUPS.keySet());
        final int pmAmount = PRIMARY_MARKUPS.get(pmMaterial);
        ingredients.add(new ItemStack(pmMaterial, pmAmount));

        final Material smMaterial = RandomUtils.getSetItem(SECONDARY_MARKUPS.keySet());
        final int smAmount = SECONDARY_MARKUPS.get(smMaterial);
        ingredients.add(new ItemStack(smMaterial, smAmount));

        final String message = String.format("Updating %s ingredients for %s to %s x %d and %s x %d...",
                format(villager), enchantment.getName(), pmMaterial, pmAmount, smMaterial, smAmount);

        if(infoLogAboutActions) {
            customLogger.info(message);
        } else {
            customLogger.debug(message);
        }

        return true;
    }

    public static GreedyVillagerListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        boolean replaceProtection = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "replace-protection"), String.format("'replace protection' flag of %s", title));
        boolean replaceMending = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "overprice-mending"), String.format("'overprice mending' flag of %s", title));

        if(!replaceProtection && !replaceMending) {
            return null;
        }

        boolean infoLogAboutActions = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "info-log-about-actions"), String.format("'info-log-about-actions' flag of %s", title));

        return new GreedyVillagerListener(customLogger, replaceProtection, replaceMending, infoLogAboutActions);
    }

    @Override
    public String toString() {
        return String.format("{replace-protection: %b, overprice-mending: %b, info-log-about-actions: %b}",
                replaceProtection, overpriceMending, infoLogAboutActions);
    }
}
