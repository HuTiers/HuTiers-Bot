package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Modals.GiveModal;
import hu.jgj52.hutiersbot.Types.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class GiveTierButton extends Button {
    @Override
    public String getCustomId() {
        return "givetierbutton";
    }

    @Override
    public String getLabel() {
        return "Tier adás";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        event.replyModal(new GiveModal().modal()).queue();
    }
}
