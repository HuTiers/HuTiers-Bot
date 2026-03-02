package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Buttons.*;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.ProfileGamemodesSelectMenu;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Map;

public class ProfileCommand extends Command {
    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getDescription() {
        return "Profil egy emberről";
    }

    @Override
    protected void addOptions(SlashCommandData command) {
        command.addOption(OptionType.USER, "ember", "Az ember, akinek a profilját akarod látni", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Player player = Player.of(event.getOption("ember").getAsUser().getId());
        if (player == null) {
            event.reply(event.getOption("ember").getAsUser().getAsTag() + " nincs fent a tierlisten!").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(embed(player)).addComponents(ActionRow.of(new ProfileGamemodesSelectMenu().selectmenu()), ActionRow.of(new SetRetiredButton().button(), new UnretireButton().button()), ActionRow.of(new SetTesterButton().button(), new UntesterButton().button()), ActionRow.of(new SetTierButton().button())).queue();
    }

    public static MessageEmbed embed(Player player) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(player.getName());
        embed.setDescription("<@" + player.getDiscordId() + ">");
        embed.setThumbnail("https://nmsr.jgj52.hu/bust/" + player.getUUID());
        try {
            for (Map<String, Object> data : Main.gamemodes) {
                Gamemode gamemode = Gamemode.of(data);
                embed.addField(gamemode.getEmoji().getFormatted() + " **" + gamemode.getName() + "**", (player.getRetired(gamemode) ? "R" : "") + player.getTier(gamemode) + (player.getTester(gamemode) ? " Teszter" : ""), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return embed.build();
    }
}
