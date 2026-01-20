package hu.jgj52.hutiersbot.SelectMenus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Buttons.HighTestButton;
import hu.jgj52.hutiersbot.Buttons.HighTestGiveButton;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.*;

public class RequestTestSelectMenu extends SelectMenu {
    @Override
    public String getCustomId() {
        return "requesthightest";
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
        Gson gson = new Gson();
        try {
            event.getMessage().editMessageComponents(ActionRow.of(selectmenu())).queue();
            Gamemode gamemode = Gamemode.of(Integer.parseInt(event.getValues().getFirst()));
            Player player = Player.of(event.getUser().getId());
            if (player == null) {
                event.reply("Nem vagy fent a tierlisten!").setEphemeral(true).queue();
                return;
            }
            if (player.getLastTest(gamemode) + Main.testCooldown > System.currentTimeMillis()) {
                event.reply("Az újratesztelési időkereted lejár <t:" + (player.getLastTest(gamemode) + Main.testCooldown) / 1000 + ":R>").setEphemeral(true).queue();
                return;
            }
            String tier = player.getTier(gamemode);
            if (tier.endsWith("4") || tier.endsWith("5")) {
                event.reply("Minimum LT3 kell legyél ebből a játékmódból!").setEphemeral(true).queue();
                return;
            }
            Main.guild.createTextChannel(event.getUser().getName().replaceAll("\\.", "") + "-" + gamemode.getId() + "-" + event.getUser().getId(), gamemode.getCategory())
                    .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .addPermissionOverride(Main.guild.getPublicRole(), EnumSet.noneOf(Permission.class), EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(gamemode.getRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .queue(channel -> {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Szia, " + event.getUser().getName() + "!");
                        embed.setDescription("Tiered: " + tier + ".\nKérlek, pingelj meg egy Regulatort, hogy kipörgesse, ki ellen kell játszanod.");
                        channel.sendMessage("<@" + event.getUser().getId() + ">").addEmbeds(embed.build()).setComponents(ActionRow.of(new HighTestGiveButton().button(), new HighTestButton().button())).queue();
                        event.reply("<#" + channel.getId() + ">").setEphemeral(true).queue();
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
