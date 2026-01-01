package hu.jgj52.hutiersbot.Types;

import hu.jgj52.hutiersbot.Main;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Gamemode {
    private final int id;
    private final String name;
    private final Category category;
    private final Emoji emoji;
    private final Role role;

    public Gamemode(Map<String, Object> data) {
        id = Integer.parseInt(data.get("id").toString());
        name = data.get("name").toString();
        category = Main.guild.getCategoryById(data.get("category_id").toString());
        emoji = Emoji.fromFormatted(data.get("emoji").toString());
        role = Main.guild.getRoleById(data.get("role_id").toString());
    }

    public int getId() {
        return id;
    }
    @NotNull
    public String getName() {
        return name;
    }
    @NotNull
    public Category getCategory() {
        return category;
    }
    @NotNull
    public Emoji getEmoji() {
        return emoji;
    }
    @NotNull
    public Role getRole() {
        return role;
    }
}
