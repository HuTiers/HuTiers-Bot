package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.StartTestSelectMenu;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.ArrayList;
import java.util.List;

public class NextButton extends Button {
    @Override
    public String getCustomId() {
        return "nextbutton";
    }

    @Override
    public String getLabel() {
        return "Következő ember";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Player player = Player.of(event.getUser().getId());
        if (player == null) return;

        event.deferReply(true).queue();
        for (Gamemode gm : StartTestSelectMenu.testers.keySet()) {
            List<Player> pl = StartTestSelectMenu.testers.get(gm);
            if (pl.contains(player)) {
                if (StartTestSelectMenu.queue.get(gm).isEmpty()) {
                    event.getHook().editOriginal("Nincs senki a queueban!").queue();
                    return;
                }
                Player next = StartTestSelectMenu.queue.get(gm).getFirst();
                Member member = Main.guild.retrieveMemberById(next.getDiscordId()).complete();
                if (member == null) {
                    event.getHook().editOriginal(next.getName() + " nincs bent discordon!").queue();
                    return;
                }
                Main.guild.createTextChannel(next.getName(), gm.getCategory()).queue(channel -> {
                    channel.upsertPermissionOverride(member)
                            .setAllowed(Permission.VIEW_CHANNEL)
                            .queue();
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Szia, " + next.getName() + "!");
                    embed.setDescription("Most <@" + player.getDiscordId() + "> le fog tesztelni téged **" + gm.getName() + "** játékmódból");
                    embed.setFooter(next.getId() + " " + gm.getId());
                    channel.sendMessage("<@" + player.getDiscordId() + ">").addEmbeds(embed.build()).addComponents(ActionRow.of(new GiveTierButton().button(), new HighTestButton().button())).queue();
                    event.getHook().editOriginal("<#" + channel.getId() + ">").queue();

                    //the other part

                    GuildMessageChannel channel2 = gm.getChannel();

                    List<Player> nowPlayers = new ArrayList<>(StartTestSelectMenu.queue.getOrDefault(gm, List.of()));
                    nowPlayers.remove(next);
                    StartTestSelectMenu.queue.put(gm, nowPlayers);

                    channel2.retrieveMessageById(channel2.getLatestMessageId()).queue(message -> {
                        MessageEmbed oldEmbed = message.getEmbeds().get(0);
                        EmbedBuilder embed2 = new EmbedBuilder();
                        embed2.setTitle(oldEmbed.getTitle());
                        embed2.setDescription(oldEmbed.getDescription());
                        String value = "";
                        for (Player player2 : nowPlayers) {
                            value = value + "<@" + player2.getDiscordId() + "> (" + player2.getName() + ")\n";
                        }
                        embed2.addField(oldEmbed.getFields().get(0).getName(), value, false);
                        embed2.addField(oldEmbed.getFields().get(1));

                        channel2.editMessageById(message.getId(), MessageEditData.fromEmbeds(embed2.build())).queue();
                    });
                });
                return;
            }
        }
        event.getHook().editOriginal("Nem tesztelsz semmiből!").queue();
    }
}
