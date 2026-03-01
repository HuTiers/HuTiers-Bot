package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.*;

public class QueueSelectMenu extends SelectMenu {
    @Override
    public String getCustomId() {
        return "queueu";
    }

    @Override
    public String getPlaceholder() {
        return "Válassz játékmódot";
    }

    @Override
    public Map<String, Map<Emoji, String>> getOptions() {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").order("id").execute().get();
            Map<String, Map<Emoji, String>> data = new LinkedHashMap<>();
            for (Map<String, Object> row : result.data) {
                Gamemode gamemode = Gamemode.of(Integer.parseInt(row.get("id").toString()));
                Map<Emoji, String> emojiStringMap = new HashMap<>();
                emojiStringMap.put(gamemode.getEmoji(), gamemode.getName());
                data.put(String.valueOf(gamemode.getId()), emojiStringMap);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(StringSelectInteractionEvent event) {
        event.getMessage().editMessageComponents(ActionRow.of(selectmenu())).queue();
        Gamemode gamemode = Gamemode.of(Integer.parseInt(event.getValues().getFirst()));
        Member member = event.getMember();
        Role role = gamemode.getQueueRole();

        if (member.getRoles().contains(role)) {
            event.getGuild().removeRoleFromMember(member, role)
                    .queue(success -> event.reply(role.getAsMention() + " sikeresen elvéve")
                            .setEphemeral(true).queue());
        } else {
            event.getGuild().addRoleToMember(member, role)
                    .queue(success -> event.reply(role.getAsMention() + " sikeresen megadva")
                            .setEphemeral(true).queue());
        }
    }
}
