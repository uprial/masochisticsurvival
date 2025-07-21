package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
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
    private final boolean replaceMending;

    public GreedyVillagerListener(final CustomLogger customLogger,
                                  final boolean replaceProtection,
                                  final boolean replaceMending) {
        this.customLogger = customLogger;
        this.replaceProtection = replaceProtection;
        this.replaceMending = replaceMending;
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
                    if ((replaceProtection) && (entry.getKey().equals(Enchantment.PROTECTION))) {
                        final Enchantment oldEnchantment = entry.getKey();
                        /*
                            According to https://minecraft.wiki/w/Thorns,
                            Thorns applies a durability penalty to the armor.
                         */
                        final Enchantment newEnchantment = Enchantment.THORNS;

                        final int oldLevel = entry.getValue();
                        // Survival maximum level is 3
                        final int newLevel = oldLevel + 3;

                        if(customLogger.isDebugMode()) {
                            customLogger.debug(String.format("Updating %s recipes for %s: changing %s-%d to %s-%d...",
                                    format(villager), result.getType(),
                                    oldEnchantment.getName(), oldLevel,
                                    newEnchantment.getName(), newLevel));
                        }

                        if(result.getItemMeta() instanceof EnchantmentStorageMeta) {
                            final EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta)result.getItemMeta();
                            itemMeta.removeStoredEnchant(oldEnchantment);
                            // ignoreLevelRestriction is a must to overcome the survival maximum level
                            itemMeta.addStoredEnchant(newEnchantment, newLevel, true);
                            result.setItemMeta(itemMeta);
                        } else {
                            result.removeEnchantment(oldEnchantment);
                            // unsafe is a must to overcome the survival maximum level
                            result.addUnsafeEnchantment(newEnchantment, newLevel);
                        }

                        updatedOne = true;
                    } else if ((replaceMending) && (entry.getKey().equals(Enchantment.MENDING))) {
                        /*
                            According to https://minecraft.wiki/w/Rarity,
                            two Epic items not from the End.
                         */
                        final Set<Material> ingredientTypes = new HashSet<>();
                        for(final ItemStack ingredient : ingredients) {
                            ingredientTypes.add(ingredient.getType());
                        }
                        if(!ingredientTypes.contains(Material.HEAVY_CORE)
                                || !ingredientTypes.contains(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)) {

                            ingredients.clear();

                            ingredients.add(new ItemStack(Material.HEAVY_CORE));
                            ingredients.add(new ItemStack(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE));

                            updatedOne = true;
                        }
                    }
                }
                /*
                    Test data #1

                    result.setType(Material.NETHERITE_LEGGINGS);
                    result.addUnsafeEnchantment(Enchantment.PROTECTION, 3);

                    updatedOne = true;
                 */
                /*
                    Test data #2

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

    public static GreedyVillagerListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        boolean replaceProtection = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "replace-protection"), String.format("'replace protection' flag of %s", title));
        boolean replaceMending = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "replace-mending"), String.format("'replace mending' flag of %s", title));

        if(!replaceProtection && !replaceMending) {
            return null;
        }

        return new GreedyVillagerListener(customLogger, replaceProtection, replaceMending);
    }

    @Override
    public String toString() {
        return String.format("{replace-protection: %b, replace-mending: %b}",
                replaceProtection, replaceMending);
    }
}
