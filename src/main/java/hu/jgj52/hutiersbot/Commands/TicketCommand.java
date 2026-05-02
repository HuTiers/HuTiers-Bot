package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class TicketCommand extends Command {
    @Override
    public String getName() {
        return "ticket";
    }

    @Override
    public String getDescription() {
        return "Nyiss ticketet";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String channelName = "ticket-" + event.getUser().getName().replaceAll("\\.", "");
        AtomicBoolean re = new AtomicBoolean(false);
        Main.tickets.getChannels().forEach(c -> c.getPermissionContainer().getMemberPermissionOverrides().forEach((o -> {
            try {
                if (o.getMember().getId().equals(event.getUser().getId())) {
                    event.reply("Már van ticketed!").queue();
                    re.set(true);
                }
            } catch (NullPointerException ignored) {}
        })));
        if (re.get() || event.getMember() == null) return;
        Main.tickets.createTextChannel(channelName).queue(channel -> {
            channel.upsertPermissionOverride(event.getMember()).setAllowed(Permission.VIEW_CHANNEL).queue();
            event.reply("<#" + channel.getId() + ">").queue();
        });
    }
}
