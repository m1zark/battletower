package com.m1zark.battletower.gui;

import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.config.InventoryConfig;
import com.m1zark.battletower.data.PlayerInfo;
import com.m1zark.battletower.utils.Items;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.GUI.Icon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.Optional;

public class SharedIcons {
    public static Icon BorderIcon(int slot, DyeColor color, String name) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(name)))
                .add(Keys.DYE_COLOR, color)
                .build());
    }

    public static Icon itemIcon(int slot, Items item) {
        ItemStack ItemStack = item.parseItem();
        ItemStack.offer(Keys.DISPLAY_NAME,TextSerializers.FORMATTING_CODE.deserialize(item.getName()));

        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&aPurchase Cost: &d" + item.getCost() + " BP")));
        itemLore.add(Text.of(Chat.embedColours("")));
        ItemStack.get(Keys.ITEM_LORE).ifPresent(lore -> lore.forEach(text -> itemLore.add(Text.of(text))));
        ItemStack.offer(Keys.ITEM_LORE, itemLore);

        return new Icon(slot, ItemStack);
    }

    public static Icon vendorIcon(int slot, String vendor) {
        String vendorName = InventoryConfig.getVendorInfo(vendor, "display-name");
        String vendorIcon = InventoryConfig.getVendorInfo(vendor, "display-icon");

        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours(InventoryConfig.getVendorInfo(vendor,"display-info"))));

        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, vendorIcon).orElse(ItemTypes.BARRIER)).quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(vendorName)))
                .add(Keys.ITEM_LORE, itemLore)
                .build());
    }

    static Icon pageIcon(int slot, boolean nextOrLast) {
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, nextOrLast ? "pixelmon:trade_holder_right" : "pixelmon:trade_holder_left").get())
                .quantity(1)
                .add(Keys.DISPLAY_NAME, nextOrLast ? Text.of(TextColors.GREEN, "\u2192 ", "Next Page", TextColors.GREEN, " \u2192") : Text.of(TextColors.RED, "\u2190 ", "Previous Page", TextColors.RED, " \u2190"))
                .build());
    }

    static Icon backIcon(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .quantity(1)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "\u2192 ", "Back to Main Menu", TextColors.GREEN, " \u2192"))
                .build());
    }

    static Icon infoIcon(int slot, Player p) {
        Optional<PlayerInfo> pl = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(p.getUniqueId())).findFirst();

        ArrayList<Text> itemLore = new ArrayList<>();
        if(pl.isPresent()) {
            itemLore.add(Text.of(Chat.embedColours("&bBattle Point Balance: &a" + pl.get().getBpTotal())));
            itemLore.add(Text.of(Chat.embedColours("")));
            itemLore.add(Text.of(Chat.embedColours("&bTotal Wins: &a" + pl.get().getTotalWins())));
            itemLore.add(Text.of(Chat.embedColours("&bBest Win Streak: &a" + pl.get().getWinStreak())));
        }

        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_panel").get()).quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Player Information"))
                .add(Keys.ITEM_LORE, itemLore)
                .build());
    }
}
