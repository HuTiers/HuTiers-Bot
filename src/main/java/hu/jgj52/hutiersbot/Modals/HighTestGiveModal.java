package hu.jgj52.hutiersbot.Modals;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Modal;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HighTestGiveModal extends Modal {
    @Override
    public String getCustomId() {
        return "hightestgivemodal";
    }

    @Override
    public String getTitle() {
        return "Tier adás";
    }

    @Override
    public List<Label> getLabels() {
        TextInput tier = TextInput.create("hightestgivemodal_tier", TextInputStyle.SHORT)
                .setMaxLength(3)
                .setMinLength(3)
                .setPlaceholder("LT2")
                .build();
        return List.of(
                Label.of("Milyen tiert kapjon?", tier)
        );
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        String tier = event.getValue("hightestgivemodal_tier").getAsString().toUpperCase();
        String[] dats = event.getChannel().getName().split("-");
        try {
            Player player = Player.of(dats[2]);
            Player tester = Player.of(event.getUser().getId());
            Gamemode gamemode = Gamemode.of(Integer.parseInt(dats[1]));
            if (player == null || tester == null || gamemode == null) return;
            if (!Main.guild.retrieveMemberById(tester.getDiscordId()).complete().getRoles().contains(Main.regulatorRole)) {
                event.reply("Nem vagy regulátor!").queue();
                return;
            }
            Map<String, Object> log = new HashMap<>();
            log.put("tester", tester.getId());
            log.put("tested", player.getId());
            log.put("gamemode", gamemode.getId());
            log.put("timestamp", System.currentTimeMillis());
            log.put("tier", tier);
            log.put("type", 1);
            Main.postgres.from("tests").insert(log);
            Main.logChannel.sendMessage(tester.getUUID() + " " + tester.getName() + " <@" + tester.getDiscordId() + ">\n" + tier + "\n" + player.getUUID() + " " + player.getName() + " <@" + player.getDiscordId() + ">\nHighTest").queue();
            player.setTier(gamemode, tier);
            player.setLastTest(gamemode, System.currentTimeMillis());
            event.getInteraction().reply("Tier beállítva!").setEphemeral(true).queue();
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(5000);
                    event.getChannel().delete().queue();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
