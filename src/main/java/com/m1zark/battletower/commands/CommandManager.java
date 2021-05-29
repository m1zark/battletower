package com.m1zark.battletower.commands;

import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.commands.Admin.Arena;
import com.m1zark.battletower.commands.Admin.BP;
import com.m1zark.battletower.commands.Admin.Reload;
import com.m1zark.battletower.config.InventoryConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.HashMap;

public class CommandManager {
    public void registerCommands(BattleTower plugin) {
        Sponge.getCommandManager().register(plugin, BT, "battletower", "bt");

        BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Registering commands...")));
    }

    CommandSpec addBP = CommandSpec.builder()
            .permission("bt.admin.bp")
            .description(Text.of("Adds bp to players balance."))
            .arguments(
                    GenericArguments.playerOrSource(Text.of("player")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new BP())
            .build();

    CommandSpec market = CommandSpec.builder()
            .permission("bt.player.market")
            .arguments(
                    GenericArguments.optional(GenericArguments.withSuggestions(GenericArguments.string(Text.of("type")), InventoryConfig.getVendors()))
            )
            .executor(new Market())
            .build();

    CommandSpec reload = CommandSpec.builder()
            .permission("bt.admin.reload")
            .description(Text.of("Reload all config files."))
            .executor(new Reload())
            .build();

    CommandSpec arena_edit = CommandSpec.builder()
            .permission("bt.admin.arena")
            .arguments(
                    GenericArguments.string(Text.of("name")),
                    GenericArguments.choices(Text.of("type"), new HashMap<String, String>(){{put("playerSpawn","playerSpawn");put("npcSpawn","npcSpawn");}}),
                    GenericArguments.choices(Text.of("action"), new HashMap<String, String>(){{put("set","set");put("delete","delete");}})
            )
            .executor(new Arena.Edit())
            .build();

    CommandSpec arena = CommandSpec.builder()
            .permission("bt.admin.arena")
            .arguments(
                    GenericArguments.choices(Text.of("action"), new HashMap<String, String>(){{put("create","create");put("delete","delete");}}),
                    GenericArguments.string(Text.of("name"))
            )
            .executor(new Arena())
            .child(arena_edit, "edit")
            .build();

    CommandSpec entrance = CommandSpec.builder()
            .permission("bt.admin.entrance")
            .arguments(GenericArguments.choices(Text.of("action"), new HashMap<String, String>(){{put("set","set");put("delete","delete");}}))
            .executor(new BT.Entrance())
            .build();

    CommandSpec npc = CommandSpec.builder()
            .permission("bt.admin.npc")
            .executor(new BT.CreateNPC())
            .build();

    CommandSpec unlock = CommandSpec.builder()
            .permission("bt.admin.unlock")
            .executor(new BT.UnlockArenas())
            .build();

    CommandSpec rules = CommandSpec.builder()
            .permission("bt.player.rules")
            .executor(new BT())
            .build();

    CommandSpec leaderboaard = CommandSpec.builder()
            .permission("bt.player.leaderboard")
            .arguments(GenericArguments.choices(Text.of("type"), new HashMap<String, String>(){{put("wins","wins");put("streak","streak");}}))
            .executor(new Leaderboard())
            .build();

    CommandSpec BT = CommandSpec.builder()
            .child(market, "market")
            .child(reload, "reload")
            .child(addBP, "add")
            .child(arena, "arena")
            .child(entrance, "entrance")
            .child(npc, "createNPC")
            .child(rules, "rules")
            .child(leaderboaard, "leaderboard","lb")
            .child(unlock, "unlock")
            .build();
}
