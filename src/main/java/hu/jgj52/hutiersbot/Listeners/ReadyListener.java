package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Gamemode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        try {
            for (Map<String, Object> data : Main.postgres.from("gamemodes").order("id").execute().get().data) {
                Gamemode gamemode = Gamemode.of(Integer.parseInt(data.get("id").toString()));
                GuildMessageChannel channel = gamemode.getChannel();
                channel.getHistoryFromBeginning(100).queue(history -> {
                    for (Message message : history.getRetrievedHistory()) {
                        message.delete().queue();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
