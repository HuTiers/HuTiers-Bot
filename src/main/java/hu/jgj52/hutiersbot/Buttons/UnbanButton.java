package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Commands.ProfileCommand;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class UnbanButton extends Button {
    @Override
    public String getCustomId() {
        return "unbanb";
    }

    @Override
    public String getLabel() {
        return "Unban";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        MessageEmbed embed = event.getMessage().getEmbeds().getFirst();
        String tag = embed.getDescription();
        if (tag == null) {
            event.reply("Hiba történt").setEphemeral(true).queue();
            return;
        }
        Member member = Main.guild.retrieveMemberById(tag.replaceAll("[^0-9]", "")).complete();
        if (member == null) {
            event.reply("Nem található ember!").setEphemeral(true).queue();
            return;
        }
        if (!event.getMember().getRoles().contains(Main.regulatorRole)) {
            event.reply("Nincs regulator rangod!").setEphemeral(true).queue();
            return;
        }
        Player player = Player.of(member.getId());
        if (player == null) {
            event.reply("Nincs fent a tierlisten!").setEphemeral(true).queue();
            return;
        }
        if (member.getRoles().contains(Main.bannedRole)) {
            Main.guild.removeRoleFromMember(member, Main.bannedRole).queue();
        }
        player.setWeight(0);
        event.editMessageEmbeds(ProfileCommand.embed(player)).queue();
    }
}
