package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Modals.HighTestGiveModal;
import hu.jgj52.hutiersbot.Types.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class HighTestGiveButton extends Button {
    @Override
    public String getCustomId() {
        return "hightestgivebutton";
    }

    @Override
    public String getLabel() {
        return "Give tier";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        event.replyModal(new HighTestGiveModal().modal()).queue();
    }
}
