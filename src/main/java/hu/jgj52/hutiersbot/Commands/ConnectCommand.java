package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class ConnectCommand extends Command {
    @Override
    public String getName() {
        return "connect";
    }

    @Override
    public String getDescription() {
        return "Csatlakoztasd Discord fiókodat a Minecraft fiókoddal";
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
            PostgreSQL.QueryResult coder = Main.postgres.from("codes").eq("discord_id", event.getUser().getId()).execute().get();
            if (!coder.isEmpty()) {
                event.reply("Már van kódod: ``" + coder.data.getFirst().get("code").toString() + "``").setEphemeral(true).queue();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("code", code);
            data.put("discord_id", event.getUser().getId());
            Main.postgres.from("codes").insert(data).get();
            event.reply("A kódod: ``" + code + "``\nLépj fel a hutiers.hu Minecraft szerverre és írd be hogy ``/connect " + code + "``, hogy felkerülj a HuTiersre!").setEphemeral(true).queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("Hiba történt.").setEphemeral(true).queue();
        }
    }
}
