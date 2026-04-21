package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Buttons.*;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.ProfileGamemodesSelectMenu;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.api.LeaderboardCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.Map;

public class ProfileCommand extends Command {
    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getDescription() {
        return "Profil egy emberről";
    }

    @Override
    protected void addOptions(SlashCommandData command) {
        command.addOption(OptionType.USER, "ember", "Az ember, akinek a profilját akarod látni", true);
        command.addOption(OptionType.BOOLEAN, "beállítás", "Alapból igaz; Ha nem akarod beállítani tierjet, állítsd hamisra", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Player player = Player.of(event.getOption("ember").getAsUser().getId());
        OptionMapping set = event.getOption("beállítás");
        boolean s = true;
        if (set != null) {
            s = set.getAsBoolean();
        }
        if (player == null) {
            event.reply(event.getOption("ember").getAsUser().getAsTag() + " nincs fent a tierlisten!").setEphemeral(true).queue();
            return;
        }
        ReplyCallbackAction c = event.replyEmbeds(embed(player)).setEphemeral(event.getMember().getRoles().contains(Main.regulatorRole) && s);
        if (event.getMember().getRoles().contains(Main.regulatorRole) && s) {
            c.addComponents(
                    ActionRow.of(new ProfileGamemodesSelectMenu().selectmenu()),
                    ActionRow.of(new SetRetiredButton().button(), new UnretireButton().button()),
                    ActionRow.of(new SetTesterButton().button(), new UntesterButton().button()),
                    ActionRow.of(new SetTierButton().button()),
                    ActionRow.of(new BanButton().button(), new UnbanButton().button())
            );
        }
        c.queue();
    }

    public static MessageEmbed embed(Player player) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(player.getName().replaceAll("_", "\\\\_"));
        embed.setDescription("<@" + player.getDiscordId() + ">");
        embed.addField("Helyezés", LeaderboardCache.getPlayer(player).get("place").toString() + ".", false);
        embed.addField("Pontok", LeaderboardCache.getPlayer(player).get("points").toString(), false);
        embed.setThumbnail("https://nmsr.jgj52.hu/bust/" + player.getUUID());
        if (player.getWeight() == -1) {
            embed.setFooter("Banned");
        }
        try {
            for (Map<String, Object> data : Main.gamemodes) {
                Gamemode gamemode = Gamemode.of(data);
                embed.addField(gamemode.getEmoji().getFormatted() + " **" + gamemode.getName() + "**", player.getFormattedTier(gamemode) + (player.getTester(gamemode) ? "Teszter" : "") + (player.getLastTest(gamemode) != 0 && player.getLastTest(gamemode) + Main.testCooldown > System.currentTimeMillis() ? "\n<t:" + (player.getLastTest(gamemode) + Main.testCooldown) / 1000 + ":R>" : "\nAkármikor tesztelhet"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return embed.build();
    }
}
