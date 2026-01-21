package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Buttons.JoinQueueButton;
import hu.jgj52.hutiersbot.Buttons.LeaveQueueButton;
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

public class StartTestSelectMenu extends SelectMenu {
    public static final Map<Gamemode, List<Player>> queue = new HashMap<>();
    public static final Map<Gamemode, List<Player>> testers = new HashMap<>();

    @Override
    public String getCustomId() {
        return "starttest";
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
        try {
            event.getMessage().editMessageComponents(ActionRow.of(selectmenu())).queue();
            Gamemode gamemode = Gamemode.of(Integer.parseInt(event.getValues().getFirst()));
            Player player = Player.of(event.getUser().getId());
            if (player == null) return;
            if (!event.getMember().getRoles().contains(gamemode.getRole())) {
                event.reply("Nem vagy teszter " + gamemode.getEmoji().getFormatted() + " " + gamemode.getName() + " játékmódból!").setEphemeral(true).queue();
                return;
            }
            if (testers.get(gamemode) != null) {
                List<Player> ts = new ArrayList<>(testers.get(gamemode));
                ts.add(player);
                testers.put(gamemode, ts);
            } else {
                queue.put(gamemode, List.of());
                testers.put(gamemode, List.of(player));
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(gamemode.getEmoji().getFormatted() + " " + gamemode.getName());
            embed.setDescription("Csatlakozz a queue-hoz, hogy leteszteljenek.");
            String emberek = "";
            embed.addField("Emberek", emberek, false);
            String teszterek = "";
            for (Player p : testers.get(gamemode)) {
                teszterek = teszterek + "<@" + p.getDiscordId() + "> (" + p.getName() + ")\n";
            }
            embed.addField("Teszterek", teszterek, false);
            event.getChannel().sendMessage("here@").addEmbeds(embed.build()).setComponents(ActionRow.of(new JoinQueueButton().button(), new LeaveQueueButton().button())).queue();
            event.reply("Teszt elindítva!").setEphemeral(true).queue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
