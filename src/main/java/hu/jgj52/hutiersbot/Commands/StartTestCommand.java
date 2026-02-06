package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.SelectMenus.StartTestSelectMenu;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class StartTestCommand extends Command {
    @Override
    public String getName() {
        return "starttest";
    }

    @Override
    public String getDescription() {
        return "Start the testing thing";
    }

    @Override
    public void addOptions(SlashCommandData command) {
        command.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Teszt várólista indítása");
        embed.setDescription("Válaszd ki a játékmódot, amből tesztelni szeretnél embereket.");
        embed.setFooter("Hogy befejezd a tesztelést, válaszd ki újra a játékmódot.");

        SelectMenu selectmenu = new StartTestSelectMenu();

        event.getChannel().sendMessage("").addEmbeds(embed.build()).setComponents(ActionRow.of(selectmenu.selectmenu())).queue();

        event.reply("sent").setEphemeral(true).queue();
    }
}
