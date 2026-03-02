package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Commands.ProfileCommand;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.ProfileGamemodesSelectMenu;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class UnretireButton extends Button {
    @Override
    public String getCustomId() {
        return "unretireb";
    }

    @Override
    public String getLabel() {
        return "Unretire";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
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
        Gamemode gamemode = ProfileGamemodesSelectMenu.gamemodes.get(event.getUser().getId());
        if (gamemode == null) {
            event.reply("Először válassz ki játékmódot!").setEphemeral(true).queue();
            return;
        }
        ProfileGamemodesSelectMenu.gamemodes.remove(event.getUser().getId());
        player.setRetired(gamemode, false);
        event.editMessageEmbeds(ProfileCommand.embed(player)).setComponents(
                ActionRow.of(new ProfileGamemodesSelectMenu().selectmenu()),
                ActionRow.of(new SetRetiredButton().button(), new UnretireButton().button()),
                ActionRow.of(new SetTesterButton().button(), new UntesterButton().button()),
                ActionRow.of(new SetTierButton().button()),
                ActionRow.of(new BanButton().button(), new UnbanButton().button())
        ).queue();
    }
}
