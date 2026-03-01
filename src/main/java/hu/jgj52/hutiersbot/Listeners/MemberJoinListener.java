package hu.jgj52.hutiersbot.Listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberJoinListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById("1379804960191156264")).queue();
    }
}
