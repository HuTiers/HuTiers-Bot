package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.StartTestSelectMenu;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public class JoinQueueButton extends Button {
    @Override
    public String getCustomId() {
        return "enterqueuebutton";
    }

    @Override
    public String getLabel() {
        return "Belépés a queue-ba";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        if (StartTestSelectMenu.canEnter) {
            String gm = event.getMessage().getEmbeds().get(0).getTitle().split(" ")[1];
            Player player = Player.of(event.getUser().getId());
            if (player == null) {
                event.reply("Nem vagy fent a tierlisten!").setEphemeral(true).queue();
                return;
            }
            Gamemode gamemode = Gamemode.of(gm);
            if (player.getLastTest(gamemode) + Main.testCooldown > System.currentTimeMillis()) {
                event.reply("Az újratesztelési időkereted lejár <t:" + (player.getLastTest(gamemode) + Main.testCooldown) / 1000 + ":R>").setEphemeral(true).queue();
                return;
            }
            for (GuildChannel chl : gamemode.getCategory().getChannels()) {
                if (chl.getName().equalsIgnoreCase(player.getName())) {
                    event.reply("Már van nyitott teszted ebből a gamemodeból!").setEphemeral(true).queue();
                    return;
                }
            }

            List<Player> nowPlayers = new ArrayList<>(StartTestSelectMenu.queue.get(gamemode));
            if (nowPlayers.contains(player)) {
                event.reply("Már bent vagy a queueban!").setEphemeral(true).queue();
                return;
            }
            if (nowPlayers.size() >= 3) {
                event.reply("Tele van a queue!").setEphemeral(true).queue();
                return;
            }
            nowPlayers.add(player);
            StartTestSelectMenu.queue.put(gamemode, nowPlayers);

            MessageEmbed oldEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(oldEmbed.getTitle());
            embed.setDescription(oldEmbed.getDescription());
            String value = "";
            for (Player players : nowPlayers) {
                value = value + "<@" + players.getDiscordId() + "> (" + players.getName() + ")\n";
            }
            embed.addField(oldEmbed.getFields().get(0).getName(), value, false);
            embed.addField(oldEmbed.getFields().get(1));

            event.editMessageEmbeds(embed.build()).queue();
        } else {
            event.reply("Nem megy a queue!").setEphemeral(true).queue();
        }
    }
}
