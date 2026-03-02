package hu.jgj52.hutiersbot.Types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import org.postgresql.util.PGobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player {
    private static final List<Player> players = new ArrayList<>();

    private static List<Map<String, Object>> tableCache = new ArrayList<>();
    private static long tableCacheLastUpdated = 0;
    private static final long CACHE_TTL_MS = 5000;

    private static void refreshTableCache() {
        if (System.currentTimeMillis() - tableCacheLastUpdated < CACHE_TTL_MS) return;
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").execute().get();
            if (!result.isEmpty() && !result.hasError()) {
                tableCache = result.data;
                tableCacheLastUpdated = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> getRowFromCache(int playerId) {
        refreshTableCache();
        for (Map<String, Object> row : tableCache) {
            Object idObj = row.get("id");
            if (idObj != null && Integer.parseInt(idObj.toString()) == playerId) {
                return row;
            }
        }
        return null;
    }

    public static Player of(int id) {
        try {
            refreshTableCache();
            Map<String, Object> row = getRowFromCache(id);
            if (row == null) return null;
            return of(row);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Player of(String discordId) {
        try {
            refreshTableCache();
            for (Map<String, Object> row : tableCache) {
                Object val = row.get("discord_id");
                if (val != null && val.toString().equals(discordId)) {
                    return of(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Player of(Map<String, Object> data) {
        int incoming = Integer.parseInt(data.get("id").toString());
        for (Player player : players) {
            if (player.getId() == incoming) {
                return player;
            }
        }
        Player player = new Player(data);
        players.add(player);
        return player;
    }

    private boolean canUpdate = true;

    private final Map<String, Object> data;
    private final int id;
    private String name;
    private String uuid;
    private String discordId;
    private JsonObject tiers;
    private JsonObject lastTest;
    private JsonObject retired;
    private JsonObject tester;

    private long lastUpdated = 0;
    private static final long INSTANCE_TTL_MS = 5000;

    private Player(Map<String, Object> data) {
        this.data = data;
        this.id = Integer.parseInt(data.get("id").toString());
        applyData(data);
    }

    private void applyData(Map<String, Object> data) {
        try {
            name = data.get("name") != null ? data.get("name").toString() : "";
            uuid = data.get("uuid") != null ? data.get("uuid").toString() : "";
            discordId = data.get("discord_id") != null ? data.get("discord_id").toString() : "";
            Gson gson = new Gson();
            Object tiersObj = data.get("tiers");
            Object lastTestObj = data.get("last_test");
            Object retiredObj = data.get("retired");
            Object testerObj = data.get("tester");
            tiers = tiersObj != null ? gson.fromJson(tiersObj.toString(), JsonObject.class) : new JsonObject();
            lastTest = lastTestObj != null ? gson.fromJson(lastTestObj.toString(), JsonObject.class) : new JsonObject();
            retired = retiredObj != null ? gson.fromJson(retiredObj.toString(), JsonObject.class) : new JsonObject();
            tester = testerObj != null ? gson.fromJson(testerObj.toString(), JsonObject.class) : new JsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update() {
        if (!canUpdate) return;
        if (System.currentTimeMillis() - lastUpdated < INSTANCE_TTL_MS) return;
        lastUpdated = System.currentTimeMillis();
        Map<String, Object> row = getRowFromCache(id);
        if (row != null) applyData(row);
    }

    public int getId() { return id; }

    public String getName() { update(); return name; }

    public String getUUID() { update(); return uuid; }

    public String getDiscordId() { update(); return discordId; }

    public JsonObject getTiers() { update(); return tiers == null ? new JsonObject() : tiers; }

    public JsonObject getLastTest() { update(); return lastTest == null ? new JsonObject() : lastTest; }

    public JsonObject getRetired() { update(); return retired == null ? new JsonObject() : retired; }

    public JsonObject getTester() {
        update();
        return tester == null ? new JsonObject() : tester;
    }

    public String getTier(Gamemode gamemode) {
        update();
        for (String key : getTiers().keySet()) {
            if (Integer.parseInt(key) == gamemode.getId()) {
                return getTiers().get(key).getAsString();
            }
        }
        return "";
    }

    public Long getLastTest(Gamemode gamemode) {
        update();
        for (String key : getLastTest().keySet()) {
            if (Integer.parseInt(key) == gamemode.getId()) {
                return getLastTest().get(key).getAsString().isBlank() ? 0 : getLastTest().get(key).getAsLong();
            }
        }
        return 0L;
    }

    public Boolean getRetired(Gamemode gamemode) {
        update();
        for (String key : getRetired().keySet()) {
            if (Integer.parseInt(key) == gamemode.getId()) {
                return !getRetired().get(key).getAsString().isBlank() && getRetired().get(key).getAsBoolean();
            }
        }
        return false;
    }

    public Boolean getTester(Gamemode gamemode) {
        update();
        for (String key : getTester().keySet()) {
            if (Integer.parseInt(key) == gamemode.getId()) {
                return !getTester().get(key).getAsString().isBlank() && getTester().get(key).getAsBoolean();
            }
        }
        return false;
    }

    private void set(Map<String, Object> data) {
        canUpdate = false;
        Main.postgres.from("players").eq("id", getId()).update(data).thenAccept(queryResult -> canUpdate = true);
        tableCacheLastUpdated = 0;
        lastUpdated = 0;
    }

    public void setName(String name) { this.name = name; set(Map.of("name", name)); }

    public void setUUID(String uuid) { this.uuid = uuid; set(Map.of("uuid", uuid)); }

    public void setDiscordId(String discordId) { this.discordId = discordId; set(Map.of("discord_id", discordId)); }

    public void setTiers(JsonObject tiers) {
        try {
            this.tiers = tiers;
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(tiers.toString());
            set(Map.of("tiers", obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLastTest(JsonObject lastTest) {
        try {
            this.lastTest = lastTest;
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(lastTest.toString());
            set(Map.of("last_test", obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRetired(JsonObject retired) {
        try {
            this.retired = retired;
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(retired.toString());
            set(Map.of("retired", obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTester(JsonObject tester) {
        try {
            this.tester = tester;
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(tester.toString());
            set(Map.of("tester", obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTier(Gamemode gamemode, String tier) {
        update();
        JsonObject tiers = getTiers();
        tiers.addProperty(String.valueOf(gamemode.getId()), tier);
        setTiers(tiers);
    }

    public void setLastTest(Gamemode gamemode, Long lastTest) {
        update();
        JsonObject lt = getLastTest();
        lt.addProperty(String.valueOf(gamemode.getId()), lastTest);
        setLastTest(lt);
    }

    public void setRetired(Gamemode gamemode, Boolean retired) {
        update();
        JsonObject r = getRetired();
        r.addProperty(String.valueOf(gamemode.getId()), retired);
        setRetired(r);
    }

    public void setTester(Gamemode gamemode, Boolean tester) {
        update();
        JsonObject t = getTester();
        t.addProperty(String.valueOf(gamemode.getId()), tester);
        setTester(t);
    }
}