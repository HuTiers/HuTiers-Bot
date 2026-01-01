package hu.jgj52.hutiersbot.Types;

import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.Map;

public abstract class SelectMenu {
    public abstract String getCustomId();
    public abstract String getPlaceholder();
    public abstract Map<String, Map<Emoji, String>> getOptions();

    public StringSelectMenu selectmenu() {
        StringSelectMenu.Builder builder = StringSelectMenu.create(getCustomId());

        builder.setPlaceholder(getPlaceholder());

        for (Map.Entry<String, Map<Emoji, String>> entry : getOptions().entrySet()) {
            String label = entry.getKey();
            Map<Emoji, String> emojiMap = entry.getValue();

            if (emojiMap != null && !emojiMap.isEmpty()) {
                for (Map.Entry<Emoji, String> emojiEntry : emojiMap.entrySet()) {
                    builder.addOption(emojiEntry.getValue(), label, null, emojiEntry.getKey());
                }
            } else {
                builder.addOption(label, label);
            }
        }

        return builder.build();
    }

    public abstract void execute(StringSelectInteractionEvent event);
}
