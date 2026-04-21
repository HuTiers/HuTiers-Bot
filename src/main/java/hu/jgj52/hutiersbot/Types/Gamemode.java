package hu.jgj52.hutiersbot.Types;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gamemode {
    private static final List<Gamemode> gamemodes = new ArrayList<>();

    public static Gamemode of(Map<String, Object> data) {
        for (Gamemode gamemode : gamemodes) {
            if (gamemode.getId() == Integer.parseInt(data.get("id").toString())) {
                return gamemode;
            }
        }

        Gamemode gamemode = new Gamemode(data);
        gamemodes.add(gamemode);
        return gamemode;
    }

    public static Gamemode of(String name) {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").eq("name", name).execute().get();
            Map<String, Object> data = result.data.getFirst();
            return of(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Gamemode of(int id) {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").eq("id", id).execute().get();
            Map<String, Object> data = result.data.getFirst();
            return of(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private int id;
    private String name;
    private Category category;
    private Emoji emoji;
    private GuildMessageChannel channel;
    private Role queueRole;

    private Gamemode(Map<String, Object> data) {
        try {
            id = Integer.parseInt(data.get("id").toString());
            name = data.get("name").toString();
            category = Main.guild.getCategoryById(data.get("category_id").toString());
            emoji = Emoji.fromFormatted(data.get("emoji").toString());
            channel = Main.guild.getChannelById(GuildMessageChannel.class, data.get("channel_id").toString());
            queueRole = Main.guild.getRoleById(data.get("queue_role_id").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public GuildMessageChannel getChannel() {
        return channel;
    }

    public Role getQueueRole() {
        return queueRole;
    }

    // don't really need the update thing here like in player bc these not change often
}
