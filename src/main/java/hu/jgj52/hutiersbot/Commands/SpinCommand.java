package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.SelectMenus.SpinGamemodesSelectMenu;
import hu.jgj52.hutiersbot.SelectMenus.SpinTiersSelectMenu;
import hu.jgj52.hutiersbot.Types.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Random;

public class SpinCommand extends Command {
    @Override
    public String getName() {
        return "spin";
    }

    @Override
    public String getDescription() {
        return "Pörgess tiereket";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Pörgetés");
        embed.setDescription("Válassz ki gamemodeot, és tiert");
        embed.setFooter("Tiert válaszd ki később");

        event.replyEmbeds(embed.build()).addComponents(ActionRow.of(new SpinGamemodesSelectMenu().selectmenu()), ActionRow.of(new SpinTiersSelectMenu().selectmenu())).queue();
    }
}
