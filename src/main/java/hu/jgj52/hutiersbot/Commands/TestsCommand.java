package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TestsCommand extends Command {
    @Override
    public String getName() {
        return "tests";
    }

    @Override
    public String getDescription() {
        return "How many tests someone did between these times";
    }

    @Override
    protected void addOptions(SlashCommandData command) {
        command.addOption(OptionType.STRING, "start", "Where this starts");
        command.addOption(OptionType.STRING, "end", "Where this ends");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        OptionMapping omStart = event.getOption("start");
        String start = omStart != null ? omStart.getAsString() : formatter.format(Instant.ofEpochMilli(0));

        OptionMapping omEnd = event.getOption("end");
        String end = omEnd != null ? omEnd.getAsString() : formatter.format(Instant.ofEpochMilli(System.currentTimeMillis()));

        try {
            PostgreSQL.QueryResult result = Main.postgres.query(
                    """
                                SELECT tester, COUNT(*) AS test_count
                                FROM tests
                                WHERE "timestamp" >= EXTRACT(EPOCH FROM ?::timestamp) * 1000
                                  AND "timestamp" <  EXTRACT(EPOCH FROM ?::timestamp) * 1000
                                  AND type = 0
                                GROUP BY tester
                                ORDER BY test_count DESC
                            """,
                    start,
                    end
            ).get();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Tesztek");
            embed.setDescription("Ennyi teszt történt a megadott időpontok között:");
            for (Map<String, Object> row : result.data) {
                Player player = Player.of(Integer.parseInt(row.get("tester").toString()));
                if (player == null) continue;
                embed.addField(player.getName(), row.get("test_count").toString(), true);
            }

            event.replyEmbeds(embed.build()).queue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
