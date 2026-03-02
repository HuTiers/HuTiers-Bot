package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Modals.SetModal;
import hu.jgj52.hutiersbot.SelectMenus.ProfileGamemodesSelectMenu;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public class SetTierButton extends Button {
    public static final Map<String, Player> active = new HashMap<>();
    @Override
    public String getCustomId() {
        return "settierb";
    }

    @Override
    public String getLabel() {
        return "Tier beállítása";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
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
        Gamemode gamemode = ProfileGamemodesSelectMenu.gamemodes.get(event.getUser().getId());
        if (gamemode == null) {
            event.reply("Először válassz ki játékmódot!").setEphemeral(true).queue();
            return;
        }
        Player player = Player.of(member.getId());
        if (player == null) {
            event.reply("Nincs fent a tierlisten!").setEphemeral(true).queue();
            return;
        }
        active.put(event.getUser().getId(), player);
        event.replyModal(new SetModal().modal()).queue();
    }
}
