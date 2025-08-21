package com.msdoggirl.jukeboxmutes;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("jukeboxmutes")
@Mod.EventBusSubscriber(modid = "jukeboxmutes", value = Dist.CLIENT)
public class MuteMusicMod {

    private static final List<SoundInstance> activeRecords = new CopyOnWriteArrayList<>();
    private static final List<SoundInstance> pendingMusic = new CopyOnWriteArrayList<>();

    public MuteMusicMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onSoundPlay(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || sound.getSource() == null) return;

        SoundSource category = sound.getSource();
        String soundId = sound.getLocation().toString();
        String catName = category.name();

    

        if ("RECORDS".equalsIgnoreCase(catName)) {
            activeRecords.add(sound);
    
        }

        if ("MUSIC".equalsIgnoreCase(catName)) {
            pendingMusic.add(sound);
    
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Cleanup finished record sounds
        activeRecords.removeIf(s -> !Minecraft.getInstance().getSoundManager().isActive(s));

        // Cleanup finished music sounds and stop if a record is active
        for (SoundInstance music : pendingMusic) {
            if (!Minecraft.getInstance().getSoundManager().isActive(music)) {
                pendingMusic.remove(music);
                continue;
            }

            if (!activeRecords.isEmpty()) {
                Minecraft.getInstance().getSoundManager().stop(music);
                pendingMusic.remove(music);
                System.out.println("[MuteMusicMod] MUSIC forcibly stopped: " + music.getLocation());
            }
        }
    }
}
