package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Types.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LeaveTestCommand extends Command {
    @Override
    public String getName() {
        return "leavetest";
    }

    @Override
    public String getDescription() {
        return "Leave the test";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }
}
