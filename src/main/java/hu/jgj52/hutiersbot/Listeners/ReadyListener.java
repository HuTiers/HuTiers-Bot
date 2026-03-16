package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.api.LeaderboardCache;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();

        Main.jda = jda;
        Main.guild = jda.getGuildById(Main.dotenv.get("GUILD_ID"));
        Main.resultChannel = jda.getTextChannelById(Main.dotenv.get("RESULT_CHANNEL_ID"));
        Main.testerRole = jda.getRoleById(Main.dotenv.get("TESTER_ROLE_ID"));
        Main.regulatorRole = jda.getRoleById(Main.dotenv.get("REGULATOR_ROLE_ID"));
        Main.logChannel = jda.getTextChannelById(Main.dotenv.get("LOG_CHANNEL_ID"));
        Main.bannedRole = jda.getRoleById(Main.dotenv.get("BANNED_ROLE_ID"));

        CommandListUpdateAction jdaCommands = jda.updateCommands();
        List<SlashCommandData> cmds = new ArrayList<>();
        for (Command cmd : Main.commands) cmds.add(cmd.command());
        jdaCommands.addCommands(cmds).queue();

        try {
            for (Map<String, Object> data : Main.gamemodes) {
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
        CompletableFuture.runAsync(() -> {
            Role role = Main.guild.getRoleById(Main.dotenv.get("MEMBER_ROLE_ID"));
            if (role == null) return;
            for (Member member : Main.guild.loadMembers().get()) {
                if (!member.getRoles().contains(role)) {
                    Main.guild.addRoleToMember(member, role).queue();
                }
            }
        });
        run();
        hu.jgj52.hutiersbot.api.Main.main(new String[]{});
    }

    private int i = 0;

    private void run() {
        List<Activity> activities = List.of(
                Activity.watching(hu.jgj52.hutiersbot.api.Main.getConnections() + " ember használja a modot"),
                Activity.watching(hu.jgj52.hutiersbot.api.Main.getSiteConnections() + " ember nézi az oldalt"),
                Activity.competing(Player.of(LeaderboardCache.getSlice(0, -1).getFirst()).getName() + " az első"),
                Activity.playing(Main.gamemodes.size() + " játékmód"),
                Activity.watching(LeaderboardCache.getSlice(0, -1).size() + " tesztelt ember")
        );
        if (i < activities.size() - 1) i++; else i = 0;
        Main.jda.getPresence().setActivity(
                activities.get(i)
        );
        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(this::run);
    }
}
