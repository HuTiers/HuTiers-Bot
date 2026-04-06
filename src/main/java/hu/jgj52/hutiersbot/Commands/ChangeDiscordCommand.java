package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class ChangeDiscordCommand extends Command {
    @Override
    public String getName() {
        return "changediscord";
    }

    @Override
    public String getDescription() {
        return "Változtasd meg a Minecraft fiókodhozz kötött Discord fiókot erre.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        SecureRandom random = new SecureRandom();
        String code = String.valueOf(random.nextInt(900000) + 100000);

        if (Player.of(event.getUser().getId()) != null) {
            event.reply("Már fent vagy a HuTiersen!").setEphemeral(true).queue();
            return;
        }

        try {
            PostgreSQL.QueryResult coder = Main.postgres.from("codes").eq("discord_id", event.getUser().getId()).eq("usecase", 1).execute().get();
            if (!coder.isEmpty()) {
                event.reply("Már van kódod: ``" + coder.data.getFirst().get("code").toString() + "``").setEphemeral(true).queue();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("code", code);
            data.put("discord_id", event.getUser().getId());
            data.put("usecase", 1);
            Main.postgres.from("codes").insert(data).get();
            event.reply("A kódod: ``" + code + "``\nLépj fel a hutiers.hu Minecraft szerverre és írd be hogy ``/changediscord " + code + "``, hogy a Minecraft fiókod erre a Discord fiókodra átkerüljön!").setEphemeral(true).queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("Hiba történt.").setEphemeral(true).queue();
        }
    }
}
