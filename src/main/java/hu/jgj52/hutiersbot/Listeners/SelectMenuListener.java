package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class SelectMenuListener extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String id = event.getCustomId();
        for (SelectMenu selectmenu : Main.selectmenus) {
            if (Objects.equals(selectmenu.getCustomId(), id)) {
                selectmenu.execute(event);
                break;
            }
        }
    }
}
