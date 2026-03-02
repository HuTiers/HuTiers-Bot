package hu.jgj52.hutiersbot;

import hu.jgj52.hutiersbot.Buttons.*;
import hu.jgj52.hutiersbot.Commands.*;
import hu.jgj52.hutiersbot.Listeners.*;
import hu.jgj52.hutiersbot.Modals.*;
import hu.jgj52.hutiersbot.SelectMenus.*;
import hu.jgj52.hutiersbot.Types.*;
import hu.jgj52.hutiersbot.Utils.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static List<Command> commands = new ArrayList<>();
    public static List<Button> buttons = new ArrayList<>();
    public static List<SelectMenu> selectmenus = new ArrayList<>();
    public static List<Modal> modals = new ArrayList<>();
    public static PostgreSQL postgres;
    public static Guild guild;
    public static Long testCooldown;
    public static TextChannel resultChannel;
    public static Role testerRole;
    public static Role regulatorRole;
    public static Dotenv dotenv;
    public static List<Map<String, Object>> gamemodes = new ArrayList<>();

    public static void main(String[] args) {
        dotenv = Dotenv.load();

        testCooldown = Long.parseLong(dotenv.get("TEST_COOLDOWN")) * 1000;

        try {
            postgres = new PostgreSQL(dotenv.get("POSTGRES_HOST"), Integer.parseInt(dotenv.get("POSTGRES_PORT")), dotenv.get("POSTGRES_DATABASE"), dotenv.get("POSTGRES_USER"), dotenv.get("POSTGRES_PASSWORD"));
            gamemodes = postgres.from("gamemodes").order("priority").execute().get().data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        JDABuilder builder = JDABuilder.createDefault(dotenv.get("TOKEN"), Set.of(GatewayIntent.values()));
        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new ButtonListener());
        builder.addEventListeners(new SelectMenuListener());
        builder.addEventListeners(new ModalListener());
        builder.addEventListeners(new ReadyListener());
        builder.addEventListeners(new MemberJoinListener());
        builder.build();

        buttons.add(new StopNameUpdatingButton());
        buttons.add(new CloseButton());
        buttons.add(new HighTestGiveButton());
        buttons.add(new JoinQueueButton());
        buttons.add(new LeaveQueueButton());
        buttons.add(new NextButton());
        buttons.add(new GiveTierButton());
        buttons.add(new SetRetiredButton());
        buttons.add(new UnretireButton());
        buttons.add(new SetTesterButton());
        buttons.add(new UntesterButton());
        buttons.add(new SetTierButton());

        commands.add(new UpdateNamesCommand());
        commands.add(new RequestTestCommand());
        commands.add(new SpinCommand());
        commands.add(new StartTestCommand());
        commands.add(new ConnectCommand());
        commands.add(new QueuePingCommand());
        commands.add(new ChangeDiscordCommand());
        commands.add(new ProfileCommand());

        selectmenus.add(new RequestTestSelectMenu());
        selectmenus.add(new StartTestSelectMenu());
        selectmenus.add(new QueueSelectMenu());
        selectmenus.add(new SpinGamemodesSelectMenu());
        selectmenus.add(new SpinTiersSelectMenu());
        selectmenus.add(new ProfileGamemodesSelectMenu());

        modals.add(new HighTestGiveModal());
        modals.add(new GiveModal());
        modals.add(new SetModal());

        hu.jgj52.hutiersbot.api.Main.main(args);
    }
}