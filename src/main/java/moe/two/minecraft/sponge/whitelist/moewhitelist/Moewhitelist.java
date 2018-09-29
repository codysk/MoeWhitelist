package moe.two.minecraft.sponge.whitelist.moewhitelist;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.nio.file.Path;

@Plugin(
        id = "moewhitelist",
        name = "Moewhitelist",
        version = "1.0-SNAPSHOT",
        description = "A Simple whitelist plugin for sponge",
        authors = {
                "Skywind"
        }
)
public class Moewhitelist {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("MoeWhitelist Loaded~");
    }

    @Listener
    public void onClientLogin(ClientConnectionEvent.Login event) {
        if (!event.getProfile().getName().isPresent()){
            event.setMessage(TextSerializers.FORMATTING_CODE.deserialize("Who are you??"));
            event.setCancelled(true);
            return;
        }
        String username = event.getProfile().getName().get();
        //TODO: op always whitelisted
        if (!isWhitelisted(username)) {
            logger.info("User " + username + " not in Whitelist!");
            event.setMessage(TextSerializers.FORMATTING_CODE.deserialize("User " + username + " not in Whitelist!"));
            event.setCancelled(true);
        }
    }

    private Boolean isWhitelisted(String username) {
        //TODO: Chick if player is whitelisted
        return false;
    }
}
