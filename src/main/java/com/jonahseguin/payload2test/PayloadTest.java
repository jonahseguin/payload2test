package com.jonahseguin.payload2test;

import com.jonahseguin.payload.PayloadAPI;
import com.jonahseguin.payload.PayloadHook;
import com.jonahseguin.payload.PayloadMode;
import com.jonahseguin.payload.database.PayloadDatabase;
import com.jonahseguin.payload.mode.profile.ProfileCache;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mongodb.morphia.mapping.DefaultCreator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Getter
public class PayloadTest extends JavaPlugin implements Listener {

    private static PayloadTest instance;

    private PayloadDatabase payloadDatabase = null;
    private ProfileCache<TestProfile> cache = null;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Awaiting cache provision from Payload");

        PayloadHook hook = null;
        try {
            hook = PayloadAPI.get().requestProvision(this).get(3, TimeUnit.MINUTES);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
            getLogger().warning("Failed to initialize PayloadHook");
            return;
        }
        getLogger().info("Provision received from Payload");

        payloadDatabase = PayloadDatabase.fromConfigFile(this, "database.yml", "Test Database");
        payloadDatabase.start();

        payloadDatabase.getMorphia().getMapper().getOptions().setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return instance.getClassLoader();
            }
        });
        payloadDatabase.getMorphia().map(TestProfile.class);

        this.cache = hook.createProfileCache(payloadDatabase, "Test Profile", TestProfile.class);
        this.cache.withInstantiator(TestProfile::new);
        this.cache.setMode(PayloadMode.NETWORK_NODE);

        if (cache.start()) {
            // success
            getLogger().info("Profile cache started successfully");
        }
        else {
            // fail
            getLogger().info("Profile cache failed to start");
        }

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        if (this.cache != null) {
            this.cache.stop();
        }
        if (this.payloadDatabase != null) {
            this.payloadDatabase.stop();
        }
        instance = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TestProfile profile = this.cache.getLocalProfile(player);
        if (profile != null) {
            player.sendMessage(ChatColor.GREEN + "Profile cached!  Hi, " + profile.getUsername() + " @ " + profile.getLoginIp());
        }
        else {
            player.sendMessage(ChatColor.RED + "Your profile isn't cached :( something went wrong");
        }
    }

    public static PayloadTest getInstance() {
        return instance;
    }

}
