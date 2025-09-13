package com.gmail.uprial.masochisticsurvival.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AngerHelperTest {
    // ==== getSmallestItem ====
    @Test
    public void testGetSmallestItem() {
        assertEquals(Long.valueOf(1L), AngerHelper.getSmallestItem(
                ImmutableList.<Long>builder()
                        .add(2L)
                        .add(1L)
                        .add(3L)
                        .build(), Double::valueOf));
    }

    @Test
    public void testNullSmallestItem() {
        assertNull(AngerHelper.getSmallestItem(
                ImmutableList.<Long>builder()
                        .build(), Double::valueOf));
    }

    @Test
    public void testNullSmallestItemValue() {
        assertNull(AngerHelper.getSmallestItem(
                ImmutableList.<Long>builder()
                        .add(2L)
                        .add(1L)
                        .add(3L)
                        .build(), (final Long l) -> null));
    }

    // ==== isSimulated ====
    @Test
    public void testIsSimulated() {
        final Chunk playerChunk = getChunk(1, 1);
        final Location playerLocation = mock(Location.class);
        when(playerLocation.getChunk()).thenReturn(playerChunk);

        final World playerWorld = mock(World.class);
        when(playerWorld.getSimulationDistance()).thenReturn(4);

        final Player player = mock(Player.class);
        when(player.getLocation()).thenReturn(playerLocation);
        when(player.getWorld()).thenReturn(playerWorld);

        final Location entityLocation = mock(Location.class);

        final Map<List<Integer>, Boolean> tests = ImmutableMap.<List<Integer>, Boolean>builder()
                .put(Arrays.asList(1, 1), true)
                .put(Arrays.asList(5, 5), true)
                .put(Arrays.asList(6, 6), false)
                .put(Arrays.asList(-3, -3), true)
                .put(Arrays.asList(-4, -4), false)
                .put(Arrays.asList(5, 6), false)
                .build();

        for(final Map.Entry<List<Integer>, Boolean> test : tests.entrySet()) {
            final Chunk entityChunk = getChunk(test.getKey().get(0), test.getKey().get(1));
            when(entityLocation.getChunk()).thenReturn(entityChunk);
            assertEquals(String.format("is %s simulated?", test.getKey()),
                    test.getValue(), AngerHelper.isSimulated(entityLocation, player));
        }
    }

    private Chunk getChunk(final int x, final int z) {
        final Chunk chunk = mock(Chunk.class);

        when(chunk.getX()).thenReturn(x);
        when(chunk.getZ()).thenReturn(z);

        return chunk;
    }
}