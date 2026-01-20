package hu.jgj52.hutiersbot.Types;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gamemode {
    private static final List<Gamemode> gamemodes = new ArrayList<>();

    public static Gamemode of(int id) {
        for (Gamemode gamemode : gamemodes) {
            if (gamemode.getId() == id) {
                return gamemode;
            }
        }

        Gamemode gamemode = new Gamemode(id);
        gamemodes.add(gamemode);
        return gamemode;
    }

    private final int id;
    private String name;
    private Category category;
    private Emoji emoji;
    private Role role;

    private Gamemode(int id) {
        this.id = id;
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").eq("id", id).single().get();
            Map<String, Object> data = result.data.getFirst();
            name = data.get("name").toString();
            category = Main.guild.getCategoryById(data.get("category_id").toString());
            emoji = Emoji.fromFormatted(data.get("emoji").toString());
            role = Main.guild.getRoleById(data.get("role_id").toString());
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

    public Role getRole() {
        return role;
    }
}
