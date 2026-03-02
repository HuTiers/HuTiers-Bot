package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class CloseButton extends Button {
    @Override
    public String getCustomId() {
        return "closehightest";
    }

    @Override
    public String getLabel() {
        return "Bezárás";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        if (event.getMember().getRoles().contains(Main.testerRole)) {
            event.getChannel().delete().queue();
        }
    }
}
