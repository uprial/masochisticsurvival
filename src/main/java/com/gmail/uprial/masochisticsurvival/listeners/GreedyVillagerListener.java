package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
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

    private static class EnchantmentMutator {
        final Enchantment enchantment;
        final int survivalMaxLevel;

        EnchantmentMutator(final Enchantment enchantment, final int survivalMaxLevel) {
            this.enchantment = enchantment;
            this.survivalMaxLevel = survivalMaxLevel;
        }

        Enchantment getEnchantment() {
            return enchantment;
        }

        int getSurvivalMaxLevel() {
            return survivalMaxLevel;
        }
    }

    private final Map<Enchantment,EnchantmentMutator> enchantmentMutators = new HashMap<>();

    public GreedyVillagerListener(final CustomLogger customLogger,
                                  final boolean replaceProtection,
                                  final boolean replaceMending) {
        this.customLogger = customLogger;
        this.replaceProtection = replaceProtection;
        this.replaceMending = replaceMending;

        if(replaceProtection) {
            /*
                According to https://minecraft.wiki/w/Thorns,
                Thorns applies a durability penalty to the armor.
             */
            enchantmentMutators.put(Enchantment.PROTECTION,
                    new EnchantmentMutator(Enchantment.THORNS, 3));
        }

        if(replaceMending) {
            /*
                According to https://minecraft.wiki/w/Mending,
                Mending can be obtained by trading with a librarian,
                which means no weapons or tools with it can be traded.

                IMO, knockback prevents swords from dealing a lot of melee damage per second,
                and isn't a potential upgrade.
             */
            enchantmentMutators.put(Enchantment.MENDING,
                    new EnchantmentMutator(Enchantment.KNOCKBACK, 2));
        }
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
                final Map<Enchantment,Integer> enchantments
                        = ((result.getItemMeta() instanceof EnchantmentStorageMeta))
                        ? ((EnchantmentStorageMeta) result.getItemMeta()).getStoredEnchants()
                        : result.getEnchantments();

                for(Map.Entry<Enchantment,Integer> entry : enchantments.entrySet()) {
                    final EnchantmentMutator mutator = enchantmentMutators.get(entry.getKey());
                    if(mutator != null) {
                        final int oldLevel = entry.getValue();
                        final int newLevel = oldLevel + mutator.getSurvivalMaxLevel();

                        final Enchantment oldEnchantment = entry.getKey();
                        final Enchantment newEnchantment = mutator.getEnchantment();
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
                    }
                }

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
                    updatedRecipe.setIngredients(recipe.getIngredients());

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
