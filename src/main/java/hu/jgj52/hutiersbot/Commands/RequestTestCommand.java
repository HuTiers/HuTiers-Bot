package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.SelectMenus.RequestTestSelectMenu;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class RequestTestCommand extends Command {
    @Override
    public String getName() {
        return "requesttest";
    }

    @Override
    public String getDescription() {
        return "Send the request test message";
    }

    @Override
    public void addOptions(SlashCommandData command) {
        command.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Magas teszt kérés");
        embed.setDescription("Válaszd ki a játékmódot, amiból szeretnél kérni magas tesztet.");

        SelectMenu selectmenu = new RequestTestSelectMenu();

        event.getChannel().sendMessage("").addEmbeds(embed.build()).setComponents(ActionRow.of(selectmenu.selectmenu())).queue();

        event.reply("sent").setEphemeral(true).queue();
    }
}
