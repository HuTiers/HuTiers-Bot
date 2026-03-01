package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.SelectMenus.QueueSelectMenu;
import hu.jgj52.hutiersbot.Types.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class QueuePingCommand extends Command {
    @Override
    public String getName() {
        return "queueping";
    }

    @Override
    public String getDescription() {
        return "Queue message writer";
    }

    @Override
    protected void addOptions(SlashCommandData command) {
        command.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Queue ping");
        embed.setDescription("Válaszd ki, hogy miből szeretnéd, hogy meg legyél pingelve ha van queue");
        event.getChannel().sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(new QueueSelectMenu().selectmenu())).queue();
        event.reply("sent").setEphemeral(true).queue();
    }
}
