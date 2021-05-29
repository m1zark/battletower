package com.m1zark.battletower.gui;

import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.data.PlayerInfo;
import com.m1zark.battletower.utils.Items;
import com.m1zark.battletower.config.InventoryConfig;
import com.m1zark.battletower.config.MessageConfig;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.m1utilities.api.Inventories;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class VendorUI extends InventoryManager {
    private Player player;
    private int page = 1;
    private int maxPage;
    private String vendor;
    private int balance;

    public VendorUI(Player p, int page, String vendor) {
        super(p, 6, Text.of(Chat.embedColours("&4&lBT Market &7\u00BB " + InventoryConfig.getVendorInfo(vendor,"display-name"))));
        this.player = p;
        this.page = page;
        this.vendor = vendor;

        Optional<PlayerInfo> pl = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(p.getUniqueId())).findFirst();
        this.balance = pl.get().getBpTotal();

        int size = InventoryConfig.getItemList(this.vendor).size();
        this.maxPage = size % 36 == 0 && size / 36 != 0 ? size / 36 : size / 36 + 1;

        this.setupInventory();
        this.setupItems();
    }

    public VendorUI(Player p) {
        super(p, 6, Text.of(Chat.embedColours("&4&lBT Market")));
        this.player = p;

        int size = InventoryConfig.getVendors().size();
        this.maxPage = size % 36 == 0 && size / 36 != 0 ? size / 36 : size / 36 + 1;

        this.setupInventory();
        this.setupVendors();
    }

    private void setupInventory() {
        int x = 0;
        for(int y = 4; x < 9; x++) { this.addIcon(SharedIcons.BorderIcon(x + 9 * y, DyeColors.GRAY, "")); }

        Icon previousPage = SharedIcons.pageIcon(48, false);
        previousPage.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                if(this.page > 1) { --this.page; }
                else { this.page = maxPage; }

                this.clearIcons(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35);
                setupItems();
                this.updateContents(0,35);
            }).delayTicks(1L).submit(BattleTower.getInstance());
        });
        this.addIcon(previousPage);

        this.addIcon(SharedIcons.infoIcon(49, this.player));

        Icon nextPage = SharedIcons.pageIcon(50, true);
        nextPage.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                if(this.page < maxPage) { ++this.page; }
                else { this.page = 1; }

                this.clearIcons(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35);
                setupItems();
                this.updateContents(0,35);
            }).delayTicks(1L).submit(BattleTower.getInstance());
        });
        this.addIcon(nextPage);
    }

    private void setupVendors() {
        List<String> vendors = InventoryConfig.getVendors();

        int index = (this.page - 1) * 36;
        int x;

        for (int y = 0; y < 4; y++) {
            for (x = 0; x < 9; x++, index++) {
                if(index >= vendors.size()) break;

                final int pos = index;
                Icon item = SharedIcons.vendorIcon(x + (9 * y), vendors.get(index));
                item.addListener(clickable -> this.player.openInventory(new VendorUI(this.player, 1, vendors.get(pos)).getInventory()));
                this.addIcon(item);
            }
        }
    }

    private void setupItems() {
        Icon back = SharedIcons.backIcon(45);
        back.addListener(clickable -> this.player.openInventory(new VendorUI(this.player).getInventory()));
        this.addIcon(back);

        List<Items> items = InventoryConfig.getItemList(this.vendor);

        int index = (this.page - 1) * 36;
        int x;

        for (int y = 0; y < 4; y++) {
            for (x = 0; x < 9; x++, index++) {
                if(index >= items.size()) break;

                final int pos = index;
                Icon item = SharedIcons.itemIcon(x + (9 * y), items.get(pos));
                item.addListener(clickable -> {
                    final int id = items.get(pos).getIndex();

                    Optional<Items> itemData = InventoryConfig.getItemList(this.vendor).stream().filter(listing -> listing.getIndex() == id).findFirst();

                    itemData.ifPresent(data -> {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            if (this.balance - data.getCost() >= 0) {
                                if(data.getType().equalsIgnoreCase("item")) {
                                    if(Inventories.giveItem(this.player, data.parseItem(), data.getCount())) {
                                        BattleTower.getInstance().getSql().updateBPTotal(this.player.getUniqueId(), true, data.getCost());
                                        Chat.sendMessage(this.player, MessageConfig.getMessages("messages.market.purchase-item").replace("{item}",data.getName()).replace("{cost}", String.valueOf(data.getCost())));
                                    } else {
                                        Chat.sendMessage(this.player, MessageConfig.getMessages("messages.market.inventory-full").replace("{item}",data.getName()));
                                    }
                                } else if(data.getType().equalsIgnoreCase("command")) {
                                    BattleTower.getInstance().getSql().updateBPTotal(this.player.getUniqueId(), true, data.getCost());
                                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), data.cmdParser(this.player));

                                    Chat.sendMessage(this.player, MessageConfig.getMessages("messages.market.purchase-item").replace("{item}",data.getName()).replace("{cost}", String.valueOf(data.getCost())));
                                }

                                this.clearIcons(49);
                                this.addIcon(SharedIcons.infoIcon(49, this.player));
                                this.updateContents(49);
                            } else {
                                Chat.sendMessage(this.player, MessageConfig.getMessages("messages.market.not-enough-bp").replace("{item}", data.getName()).replace("{cost}", String.valueOf(data.getCost())));
                            }
                        }).delayTicks(1).submit(BattleTower.getInstance());
                    });
                });
                this.addIcon(item);
            }
        }
    }
}