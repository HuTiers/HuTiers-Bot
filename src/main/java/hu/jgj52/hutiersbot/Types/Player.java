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

    public String getFormattedTier(Gamemode gamemode) {
        String tier;
        if (!getRetired(gamemode)) {
            tier = switch (getTier(gamemode)) {
                case "HT1" -> "<:ht1_1:1495530570111783103><:ht1_2:1495529420646256772>";
                case "LT1" -> "<:lt1_1:1495529382608109659><:lt1_2:1495812273380266140>";
                case "HT2" -> "<:ht2_1:1495529423208976585><:ht2_2:1495530594032160941>";
                case "LT2" -> "<:lt2_1:1495529386219409539><:lt2_2:1495529389310345368>";
                case "HT3" -> "<:ht3_1:1495529427533041714><:ht3_2:1495529430062469282>";
                case "LT3" -> "<:lt3_1:1495811171163574433><:lt3_2:1495529393127161866>";
                case "HT4" -> "<:ht4_1:1495538561137512498><:ht4_2:1495529434365558904>";
                case "LT4" -> "<:lt4_1:1495529395652132925><:lt4_2:1495811259822641272>";
                case "HT5" -> "<:ht5_1:1495811168936136814><:ht5_2:1495529438190768361>";
                case "LT5" -> "<:lt5_1:1495529399779459083><:lt5_2:1495529402669334618>";
                default -> "<:unranked_1:1495529379869229147><:unranked_2:1495529381328719882>";
            };
        } else {
            tier = switch (getTier(gamemode)) {
                case "HT1" -> "<:rht1_1:1495811172488712262><:rht1_2:1495529406100279499>";
                case "LT1" -> "<:rlt1_1:1495529447477084291><:rlt1_2:1495811261814800466>";
                case "HT2" -> "<:rht2_1:1495811174082805860><:rht2_2:1495529410953220126>";
                case "LT2" -> "<:rlt2_1:1495529452074045533><:rlt2_2:1495529366963355708>";
                case "HT3" -> "<:rht3_1:1495529413381722183><:rht3_2:1495529416103559278>";
                case "LT3" -> "<:rlt3_1:1495529368594813029><:rlt3_2:1495529370251690096>";
                case "HT4" -> "<:rht4_1:1495811175584239848><:rht4_2:1495529441982546151>";
                case "LT4" -> "<:rlt4_1:1495529372642312262><:rlt4_2:1495529374198534294>";
                case "HT5" -> "<:rht5_1:1495811178885283990><:rht5_2:1495529445786914878>";
                case "LT5" -> "<:rlt5_1:1495529376018599956><:rlt5_2:1495529377658572943>";
                default -> "<:unranked_1:1495529379869229147><:unranked_2:1495529381328719882>";
            };
        }
        return tier;
    }

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
        hu.jgj52.hutiersbot.api.Main.update(getUUID());
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