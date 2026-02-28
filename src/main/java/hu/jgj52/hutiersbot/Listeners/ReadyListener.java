package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.List;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();

        Main.guild = jda.getGuildById(Main.dotenv.get("GUILD_ID"));
        Main.resultChannel = jda.getTextChannelById(Main.dotenv.get("RESULT_CHANNEL_ID"));
        Main.testerRole = jda.getRoleById(Main.dotenv.get("TESTER_ROLE_ID"));

        CommandListUpdateAction jdaCommands = jda.updateCommands();
        List<SlashCommandData> cmds = new ArrayList<>();
        for (Command cmd : Main.commands) cmds.add(cmd.command());
        jdaCommands.addCommands(cmds).queue();
    }
}
