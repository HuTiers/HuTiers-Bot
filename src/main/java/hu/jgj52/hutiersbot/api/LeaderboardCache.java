package hu.jgj52.hutiersbot.api;

import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.Player;

import java.util.*;

import static hu.jgj52.hutiersbot.Main.postgres;

public class LeaderboardCache {
    private static List<Map<String, Object>> cachedResult = new ArrayList<>();
    private static final Map<String, Integer> tierPoints = Map.of(
            "LT5", 1, "HT5", 2, "LT4", 3, "HT4", 4,
            "LT3", 6, "HT3", 10, "LT2", 16, "HT2", 28,
            "LT1", 40, "HT1", 50
    );

    public static synchronized void refresh() {
        try {
            List<Map<String, Object>> playerData = postgres.from("players").order("name").execute().get().data;
            List<Player> players = playerData.stream()
                    .map(Player::of)
                    .filter(Objects::nonNull)
                    .toList();

            List<Gamemode> gamemodes = hu.jgj52.hutiersbot.Main.gamemodes.stream()
                    .map(Gamemode::of)
                    .toList();

            Map<Player, Integer> pointsMap = new HashMap<>();
            for (Player player : players) {
                int sum = 0;
                for (Gamemode gm : gamemodes) {
                    sum += tierPoints.getOrDefault(player.getTier(gm), 0);
                }
                pointsMap.put(player, sum);
            }

            List<Player> sortedPlayers = new ArrayList<>(players);
            sortedPlayers.sort((a, b) -> pointsMap.get(b).compareTo(pointsMap.get(a)));

            List<Map<String, Object>> result = new ArrayList<>();
            int place = 0;
            int lastPoints = -1;

            for (Player player : sortedPlayers) {
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
                entry.put("weight", player.getWeight());

                if (points == lastPoints) {
                    entry.put("place", place);
                } else {
                    place++;
                    entry.put("place", place);
                }
                lastPoints = points;

                result.add(entry);
            }

            cachedResult = result;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long lastUpdate = 0;

    public static List<Map<String, Object>> getSlice(int from, int count) {
        if (lastUpdate + 5000 < System.currentTimeMillis()) {
            lastUpdate = System.currentTimeMillis();
            refresh();
        }
        int to = from + count;
        int number = (to == -1) ? cachedResult.size() : Math.min(to, cachedResult.size());
        return cachedResult.subList(Math.min(from, cachedResult.size()), number);
    }

    public static Map<String, Object> getPlayer(Player player) {
        if (lastUpdate + 5000 < System.currentTimeMillis()) {
            lastUpdate = System.currentTimeMillis();
            refresh();
        }
        for (Map<String, Object> d : cachedResult) {
            if (d.get("name").toString().equals(player.getName())) {
                return d;
            }
        }
        return Map.of();
    }
}