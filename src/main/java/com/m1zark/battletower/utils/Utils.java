package com.m1zark.battletower.utils;

import com.flowpowered.math.vector.Vector3d;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;

public class Utils {
    @Nullable
    public static PlayerPartyStorage getPlayerStorage(Player player) {
        return Pixelmon.storageManager.getParty((EntityPlayerMP) player);
    }

    public static String ordinal(int i) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];
        }
    }

    public static void teleportPlayer(Player player, Vector3d location, Vector3d rotation) {
        Location<World> loc = new Location<>(player.getWorld(), location.getX(), location.getY(), location.getZ());
        player.setLocationAndRotation(loc, rotation);
    }

    public static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }
}
