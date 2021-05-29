package com.m1zark.battletower.data;

import com.flowpowered.math.vector.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Arenas {
    private final String id;
    private Vector3d playerSpawn;
    private Vector3d playerRotation;
    private Vector3d trainerSpawn;
    private boolean inUse;
}
