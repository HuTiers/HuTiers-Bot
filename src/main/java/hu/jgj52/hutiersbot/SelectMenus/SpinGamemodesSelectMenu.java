package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpinGamemodesSelectMenu extends SelectMenu {
    public static final Map<String, Gamemode> gamemodes = new HashMap<>();
    @Override
    public String getCustomId() {
        return "spingamemode";
    }

    @Override
    public String getPlaceholder() {
        return "Válassz játékmódot";
    }

    @Override
    public Map<String, Map<Emoji, String>> getOptions() {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").order("id").execute().get();
            Map<String, Map<Emoji, String>> data = new LinkedHashMap<>();
            for (Map<String, Object> row : result.data) {
                Gamemode gamemode = Gamemode.of(Integer.parseInt(row.get("id").toString()));
                Map<Emoji, String> emojiStringMap = new HashMap<>();
                emojiStringMap.put(gamemode.getEmoji(), gamemode.getName());
                data.put(String.valueOf(gamemode.getId()), emojiStringMap);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(StringSelectInteractionEvent event) {
        Gamemode gamemode = Gamemode.of(Integer.parseInt(event.getValues().getFirst()));
        gamemodes.put(event.getUser().getId(), gamemode);
        event.reply("Gamemode beállítva!").setEphemeral(true).queue();
    }
}
