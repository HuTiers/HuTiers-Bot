package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberJoinListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        event.getGuild().addRoleToMember(event.getMember(), Main.guild.getRoleById(Main.dotenv.get("MEMBER_ROLE_ID"))).queue();
    }
}
