# HuTiers-Bot
This bot was mainly made for the HuTiers community, but you can use it in your own TierList too if you make slight modifications

**Building**
```shell
./gradlew build
```
**Running**
```shell
java -jar build/libs/HuTiers-Bot-1.0.jar
```

**Configuring PostgreSQL**
\
You need 3 tables:
- Name: players \
  Columns: 
    - id: smallint, not null, primary key, constraints type identity always, increment 1; start 1
    - uuid: varchar (don't ask why)
    - name: varchar
    - discord_id: varchar
    - tiers: jsonb
    - last_test: jsonb
    - retired: jsonb
    - tester: jsonb
    - weight: smallint
- Name: gamemodes \
  Columns:
    - id: smallint, not null, primary key, constraints type identity always, increment 1; start 1
    - name: varchar
    - html: varchar
    - category_id: varchar; this is the category where the tickets of the gamemode will be created
    - emoji: varchar; this is the emoji of the gamemode
    - role_id: varchar; the tester role of the gamemode
    - kit: varchar; HuTiersTagger has an InventoryManager class, you need to use it's encodeToBase64 method to get this
    - channel_id: varchar; the channel where the gamemode's queues will be sent
    - queue_role_id: varchar; the role that the bot pings when a queue starts
    - priority: smallint
- Name: tests \
  Columns:
    - id: smallint, not null, primary key, constraints type identity always, increment 1; start 1
    - tester: smallint; id of the tester
    - tested: smallint; id of the tested
    - gamemode: smallint; id of the gamemode
    - timestamp: bigint; time of the test
    - tier: varchar; tier gotten
    - type: smallint; 0=normal test 1=high test 2=set with /profile command

There's also a table where /changediscord and /connect puts data, but you have to run a minecraft server for it and I don't want to include that inhere

**Commands**
\
Admin commands:
- /queueping: Gives you an embed with a select menu to get and remove the queue ping role
- /requesttest: Gives you an embed where people can request high tests
- /starttest: Gives you an embed where the testers can choose which gamemode they wanna test and open a queue with it
- /profile: only admin command if you have the role with the same id as the REGULATOR_ROLE_ID in .env; you can run it normally by in the second option of it choosing false
User commands:
- /profile: shows a profile of the people you want
- /spin: lets you spin a people out of all the HT3s in the gamemode myGamemode for example

**API**
\
I put the HuTiers API into this because I can call WebSocket easier this way.
Port: 34325
Routes (all GET):
- /v2/player/{player}:\
    response: [{"gamemode_name": "tier_of_gamemode_name"}, {"gamemode_name": "retired_of_gamemode_name(true or false)"}]
    player is the player's name
- /v3/player/{player}:\
    response: [{"gamemode_name": "tier_of_gamemode_name"}, {"gamemode_name": "retired_of_gamemode_name(true or false)"}]
    player is the player's uuid
- /v4/player/{player}:\
    response: {tiers: {gamemode_id: "tier_of_gamemode_id"}, tester: {gamemode_id: "tester_of_gamemode_id(true|false)"}, retired: {gamemode_id: "retired_of_gamemode_id(true|false)"}, weight: int, id: int, place: int, uuid: "uuid_in_string", points: int}
    player is uuid or name if you include ?byName=true
- /v2/gamemode/kit/{gamemode}:\
    response: the gamemode's kit column
    gamemode is the gamemode's name
- /v2/overall/{from}/{count}:\
    response: an array of players. format is like /v4/player/{player}
    from is the place of the first member in the response minus one
    count is how many members do you want after the first. if all, then -1
- /v2/gamemodes:\
    response: [{"id": int, "name": "name", "html": "html", "priority": int}]
\
I know it's a bit confusing, but I'm not the greatest API maker

**Notes**
\
The bot gives the role with the id MEMBER_ROLE_ID in .env to all members every startup (and when they log in, every startup just because if someone joins while the bot's offline)
\
You are gonna need a lot of configuring (and rewriting my hardcoded messages of course) to make this actually work for you.
\
The system it works is also very stupid, but I was told to do it this way