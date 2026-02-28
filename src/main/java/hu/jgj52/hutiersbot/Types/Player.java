package hu.jgj52.hutiersbot.Types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player {
    private static final List<Player> players = new ArrayList<>();

    public static Player of(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }

        Player player = new Player(id);
        players.add(player);
        return player;
    }

    public static Player of(String discordId) {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").eq("discord_id", discordId).execute().get();
            if (result.isEmpty()) return null;
            if (result.hasError()) return null;
            Map<String, Object> data = result.data.getFirst();
            return of(Integer.parseInt(data.get("id").toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private final int id;
    private String name;
    private String uuid; // yeah it's in string in db too ignore that
    private String discordId; // same
    private JsonObject tiers;
    private JsonObject lastTest;
    private JsonObject retired;

    private Player (int id) {
        this.id = id;
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").eq("id", id).single().get();
            Map<String, Object> data = result.data.getFirst();
            name = data.get("name").toString();
            uuid = data.get("uuid").toString();
            discordId = data.get("discord_id").toString();
            Gson gson = new Gson();
            tiers = gson.fromJson(data.get("tiers").toString(), JsonObject.class);
            lastTest = gson.fromJson(data.get("last_test").toString(), JsonObject.class);
            retired = gson.fromJson(data.get("retired").toString(), JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        update();
        return name;
    }

    public String getUUID() {
        update();
        return uuid;
    }

    public String getDiscordId() {
        update();
        return discordId;
    }

    public JsonObject getTiers() {
        update();
        return tiers;
    }

    public JsonObject getLastTest() {
        update();
        return lastTest;
    }

    public JsonObject getRetired() {
        update();
        return retired;
    }

    public String getTier(Gamemode gamemode) {
        update();
        for (String id : tiers.keySet()) {
            if (Integer.parseInt(id) == gamemode.getId()) {
                return tiers.get(id).getAsString();
            }
        }

        return null;
    }

    public Long getLastTest(Gamemode gamemode) {
        update();
        for (String id : lastTest.keySet()) {
            if (Integer.parseInt(id) == gamemode.getId()) {
                return lastTest.get(id).getAsLong();
            }
        }

        return null;
    }

    public Boolean getRetired(Gamemode gamemode) {
        update();
        for (String id : retired.keySet()) {
            if (Integer.parseInt(id) == gamemode.getId()) {
                return retired.get(id).getAsBoolean();
            }
        }

        return null;
    }

    private void set(Map<String, Object> data) {
        update();
        Main.postgres.from("players").eq("id", getId()).update(data);
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTier(Gamemode gamemode, String tier) {
        update();
        for (String id : tiers.keySet()) {
            if (Integer.parseInt(id) == gamemode.getId()) {
                JsonObject tiers = this.tiers;
                tiers.addProperty(String.valueOf(gamemode.getId()), tier);
                setTiers(tiers);
                break;
            }
        }
    }

    public void setLastTest(Gamemode gamemode, Long lastTest) {
        update();
        for (String id : this.lastTest.keySet()) {
            if (Integer.parseInt(id) == gamemode.getId()) {
                JsonObject lt = this.lastTest;
                lt.addProperty(String.valueOf(gamemode.getId()), lastTest);
                setLastTest(lt);
                break;
            }
        }
    }

    public void setRetired(Gamemode gamemode, Boolean retired) {
        update();
        for (String id : this.retired.keySet()) {
            if (Integer.parseInt(id) == gamemode.getId()) {
                JsonObject r = this.retired;
                r.addProperty(String.valueOf(gamemode.getId()), retired);
                setLastTest(r);
                break;
            }
        }
    }

    private void update() {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("players").eq("id", id).single().get();
            Map<String, Object> data = result.data.getFirst();
            name = data.get("name").toString();
            uuid = data.get("uuid").toString();
            discordId = data.get("discord_id").toString();
            Gson gson = new Gson();
            tiers = gson.fromJson(data.get("tiers").toString(), JsonObject.class);
            lastTest = gson.fromJson(data.get("last_test").toString(), JsonObject.class);
            retired = gson.fromJson(data.get("retired").toString(), JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
