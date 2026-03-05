package hu.jgj52.hutiersbot.Modals;

import hu.jgj52.hutiersbot.Buttons.*;
import hu.jgj52.hutiersbot.Commands.ProfileCommand;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.SelectMenus.ProfileGamemodesSelectMenu;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Modal;
import hu.jgj52.hutiersbot.Types.Player;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetModal extends Modal {
    @Override
    public String getCustomId() {
        return "setmodala";
    }

    @Override
    public String getTitle() {
        return "Tier beállítása";
    }

    @Override
    public List<Label> getLabels() {
        TextInput tier = TextInput.create("settiermodala_tier", TextInputStyle.SHORT)
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
        String tier = event.getValue("settiermodala_tier").getAsString().toUpperCase();
        Player player = SetTierButton.active.get(event.getUser().getId());
        Gamemode gamemode = ProfileGamemodesSelectMenu.gamemodes.get(event.getUser().getId());
        SetTierButton.active.remove(event.getUser().getId());
        ProfileGamemodesSelectMenu.gamemodes.remove(event.getUser().getId());
        Player tester = Player.of(event.getUser().getId());
        if (player == null || gamemode == null || tester == null) return;
        Map<String, Object> log = new HashMap<>();
        log.put("tester", tester.getId());
        log.put("tested", player.getId());
        log.put("gamemode", gamemode.getId());
        log.put("timestamp", System.currentTimeMillis());
        log.put("tier", tier);
        log.put("type", 2);
        Main.postgres.from("tests").insert(log);
        Main.logChannel.sendMessage(tester.getUUID() + " " + tester.getName() + " <@" + tester.getDiscordId() + ">\n" + tier + "\n" + player.getUUID() + " " + player.getName() + " <@" + player.getDiscordId() + ">\nSet").queue();
        player.setTier(gamemode, tier);
        event.editMessageEmbeds(ProfileCommand.embed(player)).setComponents(
                ActionRow.of(new ProfileGamemodesSelectMenu().selectmenu()),
                ActionRow.of(new SetRetiredButton().button(), new UnretireButton().button()),
                ActionRow.of(new SetTesterButton().button(), new UntesterButton().button()),
                ActionRow.of(new SetTierButton().button()),
                ActionRow.of(new BanButton().button(), new UnbanButton().button())
        ).queue();
    }
}
