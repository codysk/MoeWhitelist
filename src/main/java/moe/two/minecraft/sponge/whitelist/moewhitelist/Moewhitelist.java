package moe.two.minecraft.sponge.whitelist.moewhitelist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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

    @Inject
    Game game;

    private static Set<String> whitelist;

    @Listener
    public void onServerStart(GameStartingServerEvent event) throws Exception {
        logger.info("MoeWhitelist loading...");
        Path world_directory_path = game.getSavesDirectory();
        Path whitelist_file_path = world_directory_path.resolve("whitelist.json");

        whitelist = loadWhitelist(whitelist_file_path);

        // TODO: register command for manager interface

        logger.info("MoeWhitelist loaded");
    }

    @Listener
    public void onClientLogin(ClientConnectionEvent.Login event) {
        if (!event.getProfile().getName().isPresent()){
            event.setMessage(TextSerializers.FORMATTING_CODE.deserialize("Who are you??"));
            event.setCancelled(true);
            return;
        }
        String username = event.getProfile().getName().get();
        if (!isWhitelisted(username)) {
            logger.info("User " + username + " not in Whitelist!");
            event.setMessage(TextSerializers.FORMATTING_CODE.deserialize("User " + username + " not in Whitelist!"));
            event.setCancelled(true);
        }
    }

    private Boolean isWhitelisted(String username) {
        return whitelist.contains(username);
    }

    private Set<String> loadWhitelist(Path whitelist_file_path) throws Exception {
        Set<String> whitelist = new HashSet<>();

        if (Files.exists(whitelist_file_path)){

            try {
                String json_str = new String(Files.readAllBytes(whitelist_file_path));
                JsonArray json_arr = (new JsonParser()).parse(json_str).getAsJsonArray();

                for (JsonElement name_element:
                        json_arr) {
                    String name_str = name_element.getAsString();
                    whitelist.add(name_str);
                }

            } catch (Exception e){
                e.printStackTrace();
                logger.warn("Load Whitelist File Err! using as empty");
            }

        } else {
            logger.warn("Whitelist File Not Found. Create empty one");
            try {
                Files.write(whitelist_file_path, "[]".getBytes());
            } catch (Exception e){
                logger.error("Create Whitelist File failed!");
                throw e;
            }
        }
        return whitelist;
    }
}
