package hu.jgj52.hutiersbot.Types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import org.postgresql.util.PGobject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {

    private static final Map<Integer, Player> players = new ConcurrentHashMap<>();

    private static volatile List<Map<String, Object>> tableCache = new CopyOnWriteArrayList<>();
    private static volatile long tableCacheLastUpdated = 0;
    private static final long CACHE_TTL_MS = 5000;

    private static synchronized void refreshTableCache() {
        if (System.currentTimeMillis() - tableCacheLastUpdated < CACHE_TTL_MS) return;
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").execute().get();
            if (!result.isEmpty() && !result.hasError()) {
                tableCache = new CopyOnWriteArrayList<>(result.data);
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
        Map<String, Object> row = getRowFromCache(id);
        if (row == null) return null;
        return of(row);
    }

    public static Player of(String discordId) {
        refreshTableCache();
        for (Map<String, Object> row : tableCache) {
            Object val = row.get("discord_id");
            if (val != null && val.toString().equals(discordId)) {
                return of(row);
            }
        }
        return null;
    }

    public static Player of(Map<String, Object> data) {
        int incoming = Integer.parseInt(data.get("id").toString());
        return players.computeIfAbsent(incoming, id -> new Player(data));
    }

    private volatile boolean canUpdate = true;

    private final int id;
    private int weight;
    private volatile String name;
    private volatile String uuid;
    private volatile String discordId;
    private volatile JsonObject tiers;
    private volatile JsonObject lastTest;
    private volatile JsonObject retired;
    private volatile JsonObject tester;

    private volatile long lastUpdated = 0;
    private static final long INSTANCE_TTL_MS = 5000;

    private Player(Map<String, Object> data) {
        this.id = Integer.parseInt(data.get("id").toString());
        applyData(data);
    }

    private synchronized void applyData(Map<String, Object> data) {
        try {
            weight = data.get("weight") != null ? Integer.parseInt(data.get("weight").toString()) : 0;
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
        Map<String, Object> row = getRowFromCache(id);
        if (row != null) {
            applyData(row);
            lastUpdated = System.currentTimeMillis();
        }
    }

    public int getId() { return id; }

    public int getWeight() { return weight; }

    public String getName() { update(); return name; }

    public String getUUID() { update(); return uuid; }

    public String getDiscordId() { update(); return discordId; }

    public JsonObject getTiers() { update(); return tiers == null ? new JsonObject() : tiers; }

    public JsonObject getLastTest() { update(); return lastTest == null ? new JsonObject() : lastTest; }

    public JsonObject getRetired() { update(); return retired == null ? new JsonObject() : retired; }

    public JsonObject getTester() { update(); return tester == null ? new JsonObject() : tester; }

    public String getTier(Gamemode gamemode) {
        JsonObject obj = getTiers();
        return obj.has(String.valueOf(gamemode.getId()))
                ? obj.get(String.valueOf(gamemode.getId())).getAsString()
                : "";
    }

    public Long getLastTest(Gamemode gamemode) {
        JsonObject obj = getLastTest();
        return obj.has(String.valueOf(gamemode.getId())) && !obj.get(String.valueOf(gamemode.getId())).getAsString().isBlank()
                ? obj.get(String.valueOf(gamemode.getId())).getAsLong()
                : 0L;
    }

    public Boolean getRetired(Gamemode gamemode) {
        JsonObject obj = getRetired();
        return obj.has(String.valueOf(gamemode.getId()))
                && obj.get(String.valueOf(gamemode.getId())).getAsBoolean();
    }

    public Boolean getTester(Gamemode gamemode) {
        JsonObject obj = getTester();
        return obj.has(String.valueOf(gamemode.getId()))
                && obj.get(String.valueOf(gamemode.getId())).getAsBoolean();
    }

    private void set(Map<String, Object> data) {
        canUpdate = false;
        Main.postgres.from("players")
                .eq("id", getId())
                .update(data)
                .thenAccept(r -> canUpdate = true);
        tableCacheLastUpdated = 0;
        lastUpdated = 0;
    }

    public void setWeight(int weight) {
        this.weight = weight;
        set(Map.of("weight", weight));
    }

    public void setName(String name) {
        this.name = name;
        set(Map.of("name", name));
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
        set(Map.of("uuid", uuid));
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
        set(Map.of("discord_id", discordId));
    }

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
        JsonObject obj = getTiers();
        obj.addProperty(String.valueOf(gamemode.getId()), tier);
        setTiers(obj);
    }

    public void setLastTest(Gamemode gamemode, Long value) {
        JsonObject obj = getLastTest();
        obj.addProperty(String.valueOf(gamemode.getId()), value);
        setLastTest(obj);
    }

    public void setRetired(Gamemode gamemode, Boolean value) {
        JsonObject obj = getRetired();
        obj.addProperty(String.valueOf(gamemode.getId()), value);
        setRetired(obj);
    }

    public void setTester(Gamemode gamemode, Boolean value) {
        JsonObject obj = getTester();
        obj.addProperty(String.valueOf(gamemode.getId()), value);
        setTester(obj);
    }
}