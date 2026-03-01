package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
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
        return Map.of(
                "LT5", Map.of(),
                "HT5", Map.of(),
                "LT4", Map.of(),
                "HT4", Map.of(),
                "LT3", Map.of(),
                "HT3", Map.of(),
                "LT2", Map.of(),
                "HT2", Map.of(),
                "LT1", Map.of(),
                "HT1", Map.of()
        );
    }

    @Override
    public void execute(StringSelectInteractionEvent event) {
        event.getMessage().editMessageComponents(ActionRow.of(new SpinGamemodesSelectMenu().selectmenu()), ActionRow.of(selectmenu())).queue();
        String tier = event.getValues().getFirst();
        Gamemode gamemode = SpinGamemodesSelectMenu.gamemodes.get(event.getUser().getId());
        if (gamemode == null) return;
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").order("id").execute().get();
            List<Player> players = new ArrayList<>();
            for (Map<String, Object> data : result.data) {
                Player player = Player.of(data);
                if (player.getTier(gamemode).equals(tier) && !player.getRetired(gamemode)) {
                    players.add(player);
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
            event.replyEmbeds(embed.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
