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
                        tiers.put(String.valueOf(gamemode.getId()), player.getTier(gamemode));
                        retired.put(String.valueOf(gamemode.getId()), player.getRetired(gamemode));
                        tester.put(String.valueOf(gamemode.getId()), player.getTester(gamemode));
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
                        tiers.put(String.valueOf(gamemode.getId()), player.getTier(gamemode));
                        retired.put(String.valueOf(gamemode.getId()), player.getRetired(gamemode));
                        tester.put(String.valueOf(gamemode.getId()), player.getTester(gamemode));
                    }
                    dat.add(tiers);
                    dat.add(retired);
                    dat.add(tester);

                    context.status(200).json(dat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v2/gamemode/kit/{gamemode}", context -> {
                String gm = context.pathParam("gamemode");

                try {
                    Gamemode gamemode = Gamemode.of(postgres.from("gamemodes").eq("name", gm).execute().get().data.getFirst());

                    context.status(200).json(new Object[]{gamemode});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            route.get("/v2/overall/{from}/{to}", context -> {
                int from = Integer.parseInt(context.pathParam("from"));
                int to = Integer.parseInt(context.pathParam("to"));
                int number = (to == -1) ? 999_999_999 : to; // complete Valve

                try {
                    List<Map<String, Object>> playerData = postgres.from("players").execute().get().data;
                    List<Player> players = new ArrayList<>();
                    for (Map<String, Object> row : playerData) {
                        Player p = Player.of(row);
                        if (p == null) continue;
                        players.add(p);
                    }

                    List<Gamemode> gamemodes = new ArrayList<>();
                    for (Map<String, Object> row : hu.jgj52.hutiersbot.Main.gamemodes) {
                        Object idObj = row.get("id");
                        int id = (idObj instanceof Number) ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());
                        gamemodes.add(Gamemode.of(id));
                    }

                    Map<String, Integer> tierPoints = Map.of(
                            "LT5", 1, "HT5", 2, "LT4", 3, "HT4", 4,
                            "LT3", 6, "HT3", 10, "LT2", 16, "HT2", 28,
                            "LT1", 40, "HT1", 50
                    );

                    Map<Player, Integer> pointsMap = new HashMap<>();
                    for (Player player : players) {
                        int sum = 0;
                        for (Gamemode gm : gamemodes) {
                            String tier = player.getTier(gm);
                            sum += tierPoints.getOrDefault(tier, 0);
                        }
                        pointsMap.put(player, sum);
                    }

                    List<Player> sortedPlayers = new ArrayList<>(players);
                    sortedPlayers.sort((a, b) -> pointsMap.get(b).compareTo(pointsMap.get(a)));

                    List<Map<String, Object>> result = new ArrayList<>();
                    Player lastPlayer = null;

                    for (int i = from; i < sortedPlayers.size() && result.size() < number; i++) {
                        Player player = sortedPlayers.get(i);
                        int points = pointsMap.get(player);

                        Map<String, Object> entry = new HashMap<>();
                        entry.put("id", player.getId());
                        entry.put("uuid", player.getUUID());
                        entry.put("name", player.getName());
                        entry.put("points", points);
                        Map<String, Object> tiers = new HashMap<>();
                        Map<String, Object> retired = new HashMap<>();
                        Map<String, Object> tester = new HashMap<>();
                        for (Map<String, Object> gm : hu.jgj52.hutiersbot.Main.gamemodes) {
                            Gamemode gamemode = Gamemode.of(gm);
                            tiers.put(String.valueOf(gamemode.getId()), player.getTier(gamemode));
                            retired.put(String.valueOf(gamemode.getId()), player.getRetired(gamemode));
                            tester.put(String.valueOf(gamemode.getId()), player.getTester(gamemode));
                        }
                        entry.put("tiers", tiers);
                        entry.put("retired", retired);
                        entry.put("tester", tester);

                        if (lastPlayer != null && points == pointsMap.get(lastPlayer)) {
                            entry.put("place", result.get(result.size() - 1).get("place"));
                        } else {
                            entry.put("place", i + 1);
                        }

                        result.add(entry);
                        lastPlayer = player;
                    }

                    context.status(200).json(result);

                } catch (Exception e) {
                    e.printStackTrace();
                    context.status(500).result("Internal server error");
                }
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
