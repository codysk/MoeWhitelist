package moe.two.minecraft.sponge.whitelist.moewhitelist;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Plugin(
        id = "moewhitelist",
        name = "Moewhitelist",
        version = "1.0",
        description = "A Simple whitelist plugin for sponge",
        authors = {
                "Skywind"
        }
)
public class Moewhitelist {

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    private static Set<String> whitelist;
    private Path whitelist_file_path;

    @Listener
    public void onServerStart(GameStartingServerEvent event) throws Exception {

        logger.info("MoeWhitelist loading...");
        Path world_directory_path = game.getSavesDirectory();
        whitelist_file_path = world_directory_path.resolve(Sponge.getServer().getDefaultWorldName()).resolve("MoeWhitelist.json");
        whitelist = loadWhitelist(whitelist_file_path);

        registerCommand();

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
                logger.warn("Load Whitelist File failed! using as empty");
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

    private void saveWhitelist(Set<String> whitelist, Path whitelist_file_path) {
        synchronized (this) {
            try {
                String json_str = (new Gson()).toJson(whitelist);
                Files.write(whitelist_file_path, json_str.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("Save Whitelist File failed!");
            }
        }
    }

    private void registerCommand() {
        CommandSpec add_command = CommandSpec.builder()
                .permission("MoeWhitelist.command.add")
                .description(Text.of("add player to whitelist"))
                .arguments(
                        GenericArguments.string(Text.of("nickname"))
                )
                .executor((src, args) -> {
                    String nickname = args.<String>getOne("nickname").get();
                    whitelist.add(nickname);
                    saveWhitelist(whitelist, whitelist_file_path);
                    src.sendMessage(Text.of(nickname + " Added!"));
                    return CommandResult.success();
                })
                .build();
        CommandSpec del_command = CommandSpec.builder()
                .permission("MoeWhitelist.command.del")
                .description(Text.of("del player from whitelist"))
                .arguments(
                        GenericArguments.string(Text.of("nickname"))
                )
                .executor((src, args) -> {
                    String nickname = args.<String>getOne("nickname").get();
                    whitelist.remove(nickname);
                    saveWhitelist(whitelist, whitelist_file_path);
                    src.sendMessage(Text.of(nickname + " Deleted!"));
                    return CommandResult.success();
                })
                .build();
        CommandSpec list_command = CommandSpec.builder()
                .permission("MoeWhitelist.command.list")
                .description(Text.of("list player on whitelist"))
                .executor((src, args) -> {
                    src.sendMessage(Text.of(whitelist.toString()));
                    return CommandResult.success();
                })
                .build();

        CommandSpec moewhitelist_command = CommandSpec.builder()
                .permission("MoeWhitelist.command")
                .description(Text.of("MoeWhitelist Manage Command"))
                .child(add_command, "add", "a")
                .child(del_command, "del", "d")
                .child(list_command, "list", "l")
                .build();

        Sponge.getCommandManager().register(this, moewhitelist_command, "MoeWhitelist", "wl", "WL");
    }
}
