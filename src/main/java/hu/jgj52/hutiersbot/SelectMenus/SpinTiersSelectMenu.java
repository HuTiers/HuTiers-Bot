package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import hu.jgj52.hutiersbot.api.LeaderboardCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.*;

public class SpinTiersSelectMenu extends SelectMenu {
    @Override
    public String getCustomId() {
        return "spintiers";
    }

    @Override
    public String getPlaceholder() {
        return "Válassz tiert";
    }

    @Override
    public Map<String, Map<Emoji, String>> getOptions() {
        Map<String, Map<Emoji, String>> map = new LinkedHashMap<>();
        map.put("LT5", Map.of());
        map.put("HT5", Map.of());
        map.put("LT4", Map.of());
        map.put("HT4", Map.of());
        map.put("LT3", Map.of());
        map.put("HT3", Map.of());
        map.put("LT2", Map.of());
        map.put("HT2", Map.of());
        map.put("LT1", Map.of());
        map.put("HT1", Map.of());
        return map;
    }

    @Override
    public void execute(StringSelectInteractionEvent event) {
        event.getMessage().editMessageComponents(ActionRow.of(new SpinGamemodesSelectMenu().selectmenu()), ActionRow.of(selectmenu())).queue();
        String tier = event.getValues().getFirst();
        Gamemode gamemode = SpinGamemodesSelectMenu.gamemodes.get(event.getUser().getId());
        if (gamemode == null) return;
        try {
            List<Player> players = new ArrayList<>();
            List<Player> retired = new ArrayList<>();
            List<Player> banned = new ArrayList<>();
            for (Map<String, Object> data : LeaderboardCache.getSlice(0, -1)) {
                Player player = Player.of(data);
                if (player.getTier(gamemode).equals(tier)) {
                    if (player.getRetired(gamemode)) {
                        retired.add(player);
                    } else if (player.getWeight() == -1) {
                        banned.add(player);
                    } else {
                        players.add(player);
                    }
                }
            }
            if (players.isEmpty()) {
                event.reply("Nincs egy ilyen játékos se.").queue();
                return;
            }
            Player[] options = players.toArray(new Player[0]);
            Player player = options[new Random().nextInt(0, options.length)];
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(gamemode.getName() + " " + tier);
            embed.setDescription(player.getName() + " (<@" + player.getDiscordId() + ">)");
            String value = "";
            for (Player p : players) {
                value = value + p.getName().replaceAll("_", "\\\\_") + " (<@" + p.getDiscordId() + ">)\n";
            }
            embed.addField("Lehetséges", value, false);
            String v = "";
            for (Player p : retired) {
                v = v + p.getName().replaceAll("_", "\\\\_") + " (<@" + p.getDiscordId() + ">)\n";
            }
            if (!v.isEmpty()) {
                embed.addField("Retired", v, false);
            }
            String val = "";
            for (Player p : banned) {
                val = val + p.getName().replaceAll("_", "\\\\_") + " (<@" + p.getDiscordId() + ">)\n";
            }
            if (!val.isEmpty()) {
                embed.addField("Banned", val, false);
            }
            event.replyEmbeds(embed.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
