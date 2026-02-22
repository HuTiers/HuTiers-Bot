package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.StartTestSelectMenu;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

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
                StartTestSelectMenu.queue.get(gm).remove(next);
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
                    channel.sendMessageEmbeds(embed.build()).queue();
                    event.getHook().editOriginal("<#" + channel.getId() + ">").queue();
                });
                return;
            }
        }
        event.getHook().editOriginal("Nem tesztelsz semmiből!").queue();
    }
}
