package hu.jgj52.hutiersbot.api;

import com.google.gson.Gson;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import io.javalin.Javalin;
import io.javalin.config.RoutesConfig;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        PostgreSQL postgres = hu.jgj52.hutiersbot.Main.postgres;
        Javalin.create(config -> {
            Gson gson = new Gson();
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            RoutesConfig route = config.routes;
            route.get("/v2/player/{player}", context -> {
                String playerName = context.pathParam("player");

                try {
                    List<Map<String, Object>> data = postgres.from("players").eq("name", playerName).execute().get().data;
                    if (data.isEmpty()) {
                        context.status(404).result("Player not found");
                        return;
                    }
                    Map<String, Object> row = data.get(0);
                    Player player = Player.of(row);

                    List<Map<String, Object>> dat = new ArrayList<>();
                    Map<String, Object> tiers = new HashMap<>();
                    Map<String, Object> retired = new HashMap<>();
                    Map<String, Object> tester = new HashMap<>();
                    for (Map<String, Object> gm : hu.jgj52.hutiersbot.Main.gamemodes) {
                        Gamemode gamemode = Gamemode.of(gm);
                        tiers.put(gamemode.getName(), player.getTier(gamemode));
                        retired.put(gamemode.getName(), player.getRetired(gamemode));
                        tester.put(gamemode.getName(), player.getTester(gamemode));
                    }
                    dat.add(tiers);
                    dat.add(retired);
                    dat.add(tester);

                    context.status(200).json(dat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v3/player/{player}", context -> {
                String uuid = context.pathParam("player");

                try {
                    List<Map<String, Object>> data = postgres.from("players").eq("uuid", uuid).execute().get().data;
                    if (data.isEmpty()) {
                        context.status(404).result("Player not found");
                        return;
                    }
                    Map<String, Object> row = data.get(0);
                    Player player = Player.of(row);

                    List<Map<String, Object>> dat = new ArrayList<>();
                    Map<String, Object> tiers = new HashMap<>();
                    Map<String, Object> retired = new HashMap<>();
                    Map<String, Object> tester = new HashMap<>();
                    for (Map<String, Object> gm : hu.jgj52.hutiersbot.Main.gamemodes) {
                        Gamemode gamemode = Gamemode.of(gm);
                        tiers.put(gamemode.getName(), player.getTier(gamemode));
                        retired.put(gamemode.getName(), player.getRetired(gamemode));
                        tester.put(gamemode.getName(), player.getTester(gamemode));
                    }
                    dat.add(tiers);
                    dat.add(retired);
                    dat.add(tester);

                    context.status(200).json(dat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v4/player/{player}", context -> {
                String uuid = context.pathParam("player");
                boolean name = false;
                if (context.queryParam("byName") != null && Boolean.parseBoolean(context.queryParam("byName"))) {
                    name = true;
                }

                try {
                    List<Map<String, Object>> data;
                    if (!name) {
                        data = postgres.from("players").eq("uuid", uuid).execute().get().data;
                    } else {
                        data = postgres.from("players").eq("name", uuid).execute().get().data;
                    }
                    if (data.isEmpty()) {
                        context.status(404).result("Player not found");
                        return;
                    }
                    Map<String, Object> row = data.get(0);
                    Player player = Player.of(row);

                    context.status(200).json(LeaderboardCache.getPlayer(player));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v2/playerCount", context -> {
                try {
                    context.json(postgres.from("players").execute().get().data.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v2/gamemode/kit/{gamemode}", context -> {
                String gm = context.pathParam("gamemode");

                try {
                    context.status(200).json(postgres.from("gamemodes").eq("name", gm).execute().get().data.getFirst().get("kit"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v2/overall/{from}/{to}", context -> {
                int from = Integer.parseInt(context.pathParam("from"));
                int to = Integer.parseInt(context.pathParam("to"));

                List<Map<String, Object>> slice = LeaderboardCache.getSlice(from, to);
                context.status(200).json(slice);
            });

            route.get("/v2/gamemodes", context -> {
                try {
                    List<Map<String, Object>> data = new ArrayList<>();
                    for (Map<String, Object> row : hu.jgj52.hutiersbot.Main.gamemodes) {
                        Map<String, Object> d = new HashMap<>();
                        d.put("id", row.get("id"));
                        d.put("name", row.get("name"));
                        d.put("html", row.get("html"));
                        d.put("priority", row.get("priority"));
                        data.add(d);
                    }
                    context.status(200).json(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start(34325);
    }
}
