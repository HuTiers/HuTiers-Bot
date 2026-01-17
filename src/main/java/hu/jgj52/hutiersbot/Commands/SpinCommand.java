package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Types.Command;
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
        return "Spin between strings";
    }

    @Override
    public void addOptions(SlashCommandData command) {
        command.addOption(OptionType.STRING, "strings", "devide them by spaces", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String[] options = event.getOption("strings").getAsString().split(" ");
        event.reply(options[new Random().nextInt(0, options.length)]).queue();
    }
}
