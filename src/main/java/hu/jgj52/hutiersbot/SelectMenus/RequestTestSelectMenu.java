package hu.jgj52.hutiersbot.SelectMenus;

import hu.jgj52.hutiersbot.Buttons.CloseButton;
import hu.jgj52.hutiersbot.Buttons.HighTestGiveButton;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.api.LeaderboardCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
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
            Map<String, Map<Emoji, String>> data = new LinkedHashMap<>();
            for (Map<String, Object> row : Main.gamemodes) {
                Gamemode gamemode = Gamemode.of(row);
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
            List<Player> players = new ArrayList<>();
            for (Map<String, Object> data : LeaderboardCache.getSlice(0, -1)) {
                Player p = Player.of(data);
                if (p.getTier(gamemode).equals(tier)) {
                    if (!p.getRetired(gamemode) && p.getWeight() != -1 && player != p) {
                        players.add(p);
                    }
                }
            }
            Player pl;
            if (!players.isEmpty()) {
                pl = players.get(new Random().nextInt(0, players.size()));
            } else {
                pl = null;
            }
            Main.guild.createTextChannel(event.getUser().getName().replaceAll("\\.", "") + "-" + gamemode.getId() + "-" + event.getUser().getId(), gamemode.getCategory())
                    .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .addPermissionOverride(Main.guild.getPublicRole(), EnumSet.noneOf(Permission.class), EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(gamemode.getRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .queue(channel -> {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Szia, " + event.getUser().getName() + "!");
                        embed.setDescription("Tiered: " + tier + ".");
                        if (pl != null) {
                            embed.addField("Pörgetett ember", "<@" + pl.getDiscordId() + "> (" + pl.getName() + ")", false);
                            Main.guild.retrieveMemberById(pl.getDiscordId()).queue(member -> channel.upsertPermissionOverride(member).setAllowed(Permission.VIEW_CHANNEL).queue(), t -> embed.setFooter("Az embert nem sikerült a channelhez adni."));
                        } else {
                            embed.addField("Pörgetett ember", "Nem sikerült pörgetni embert.", false);
                        }
                        channel.sendMessage("<@" + event.getUser().getId() + ">").addEmbeds(embed.build()).setComponents(ActionRow.of(new HighTestGiveButton().button(), new CloseButton().button())).queue();
                        event.reply("<#" + channel.getId() + ">").setEphemeral(true).queue();
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
