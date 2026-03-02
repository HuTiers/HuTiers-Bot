package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Buttons.JoinQueueButton;
import hu.jgj52.hutiersbot.Buttons.LeaveQueueButton;
import hu.jgj52.hutiersbot.Buttons.NextButton;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.*;

public class StartTestSelectMenu extends SelectMenu {
    public static final Map<Gamemode, List<Player>> queue = new HashMap<>();
    public static final Map<Gamemode, List<Player>> testers = new HashMap<>();
    public static final Map<Gamemode, Message> messages = new HashMap<>();
    public static final Map<Gamemode, Boolean> canEnter = new HashMap<>();

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
            Map<String, Map<Emoji, String>> data = new LinkedHashMap<>();
            for (Map<String, Object> row : Main.gamemodes) {
                Gamemode gamemode = Gamemode.of(row);
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
            event.deferReply(true).queue();
            event.getMessage().editMessageComponents(ActionRow.of(selectmenu()), ActionRow.of(new NextButton().button())).queue();
            Gamemode gamemode = Gamemode.of(Integer.parseInt(event.getValues().getFirst()));
            Player player = Player.of(event.getUser().getId());
            if (player == null) return;
            GuildMessageChannel channel = gamemode.getChannel();
            if (channel == null) return;
            if (!event.getMember().getRoles().contains(gamemode.getRole())) {
                event.getHook().editOriginal("Nem vagy teszter " + gamemode.getEmoji().getFormatted() + " " + gamemode.getName() + " játékmódból!").queue();
                return;
            }
            if (testers.get(gamemode) != null && testers.get(gamemode).contains(player)) {
                testers.get(gamemode).remove(player);
                if (testers.get(gamemode).isEmpty()) {
                    canEnter.put(gamemode, false);
                    queue.get(gamemode).clear();
                    messages.get(gamemode).delete().complete();
                    messages.remove(gamemode);
                }
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(gamemode.getEmoji().getFormatted() + " " + gamemode.getName());
                embed.setDescription("Csatlakozz a queue-hoz, hogy leteszteljenek.");
                String emberek = "";
                for (Player p : queue.get(gamemode)) {
                    emberek = emberek + "<@" + p.getDiscordId() + "> (" + p.getName() + ")\n";
                }
                embed.addField("Emberek", emberek, false);
                String teszterek = "";
                for (Player p : testers.get(gamemode)) {
                    teszterek = teszterek + "<@" + p.getDiscordId() + "> (" + p.getName() + ")\n";
                }
                embed.addField("Teszterek", teszterek, false);
                event.getHook().editOriginal("Sikeresen kiléptél a tesztelésből.").queue();
                return;
            }
            for (List<Player> pl : testers.values()) {
                for (Player p : pl) {
                    if (p == player) {
                        event.getHook().editOriginal("Már tesztelsz egy gamemodeból!").queue();
                        return;
                    }
                }
            }
            if (testers.get(gamemode) != null) {
                List<Player> ts = new ArrayList<>(testers.get(gamemode));
                ts.add(player);
                testers.put(gamemode, ts);
            } else {
                queue.put(gamemode, new ArrayList<>());
                testers.put(gamemode, new ArrayList<>(List.of(player)));
            }
            canEnter.put(gamemode, true);
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
            channel.sendMessage(gamemode.getQueueRole().getAsMention()).addEmbeds(embed.build()).addComponents(ActionRow.of(new JoinQueueButton().button(), new LeaveQueueButton().button())).queue(message -> messages.put(gamemode, message));
            event.getHook().editOriginal("Teszt elindítva!").queue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
