package hu.jgj52.hutiersbot.Modals;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Modal;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GiveModal extends Modal {
    @Override
    public String getCustomId() {
        return "givetiermodal";
    }

    @Override
    public String getTitle() {
        return "Give tier";
    }

    @Override
    public List<Label> getLabels() {
        TextInput tier = TextInput.create("givetiermodal_tier", TextInputStyle.SHORT)
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
        //this is definitely not null safe but it is
        String tier = event.getValue("givetiermodal_tier").getAsString();
        Message message = event.getMessage();
        MessageEmbed.Footer footer = message.getEmbeds().getFirst().getFooter();
        if (footer == null) {
            event.reply("Hiba történt: footerben nincs data").setEphemeral(true).queue();
            return;
        }
        String id = footer.getText();
        if (id == null) {
            event.reply("Hiba történt: footerben nincs data").setEphemeral(true).queue();
            return;
        }
        Player player = Player.of(Integer.parseInt(id.split(" ")[0]));
        Gamemode gamemode = Gamemode.of(Integer.parseInt(id.split(" ")[1]));
        if (!event.getMember().getRoles().contains(gamemode.getRole())) {
            event.reply("Nem vagy teszter!").setEphemeral(true).queue();
            return;
        }
        Player tester = Player.of(event.getUser().getId());
        player.setTier(gamemode, tier);
        player.setLastTest(gamemode, System.currentTimeMillis());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Teszt eredmény");
        embed.setDescription("<@" + tester.getDiscordId() + "> **" + tier + "** tiert adott <@" + player.getDiscordId() + "> (" + player.getName() + ") játékosnak " + gamemode.getEmoji().getFormatted() + " **" + gamemode.getName() + "** játékmódból.");
        try {
            for (Map<String, Object> gms : Main.postgres.from("gamemodes").order("id").execute().get().data) {
                Gamemode gm = Gamemode.of(Integer.parseInt(gms.get("id").toString()));
                embed.addField(
                        gm.getEmoji().getFormatted() + " **" + gm.getName() + "**",
                        player.getTier(gm),
                        true
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Main.resultChannel.sendMessageEmbeds(embed.build()).queue();
        event.reply("Tier megadva!").queue(hook -> CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> event.getChannel().delete().queue()));
    }
}
