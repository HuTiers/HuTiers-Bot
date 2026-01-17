package hu.jgj52.hutiersbot.Modals;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Modal;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighTestGiveModal extends Modal {
    @Override
    public String getCustomId() {
        return "hightestgivemodal";
    }

    @Override
    public String getTitle() {
        return "Give tier";
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
        String tier = event.getValue("hightestgivemodal_tier").getAsString();
        String[] dats = event.getChannel().getName().split("-");
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").eq("discord_id", dats[2]).execute().get();
            JsonObject tiers = new Gson().fromJson(result.data.getFirst().get("data").toString(), JsonObject.class);
            tiers.addProperty(dats[1], tier);
            Map<String, Object> newData = new HashMap<>();
            newData.put("tiers", tiers.toString());
            Main.postgres.from("players").eq("discord_id", dats[2]).update(newData);
            event.getChannel().delete().queue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
