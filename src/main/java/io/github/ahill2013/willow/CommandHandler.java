package io.github.ahill2013.willow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.*;

public class CommandHandler {

    private IDiscordClient _client;
    private Timer statusTimer;
    private TimerTask statusTask;
    private ArrayList<String> statusMessages;
    private int statusIndex;
    private static final Logger logger = LogManager.getLogger(CommandHandler.class);

    // Static Map of commands from string to functional implementation
    private static Map<String, Command> commandMap = new HashMap<>();

    //Statically populate commandMap with functions
    static {

        commandMap.put("weak", (event, args) -> {
            BotUtils.buildWeakEmbed(args);
            BotUtils.sendMessage(event.getChannel(), BotUtils.buildWeakEmbed(args));
        });


        commandMap.put("embed", (event, args) ->
                BotUtils.sendMessage(event.getChannel(), BotUtils.buildEmbedTest()));

        commandMap.put("ping", (event, args) ->
                BotUtils.sendMessage(event.getChannel(), "pong"));

        commandMap.put("help", (event, args) -> {
            StringBuilder helpMsg = new StringBuilder();
            helpMsg.append("Bot command prefix is: '>'\n\n");
            helpMsg.append("Available commands:\n");
            helpMsg.append("ping").append(" - an easy way to see if the bot is alive");
            helpMsg.append("help").append(" - This help message");

            BotUtils.sendMessage(event.getChannel(), helpMsg.toString());
        });

    }

    public CommandHandler(IDiscordClient client) {

        _client = client;

        statusMessages = new ArrayList<>(3);
        statusMessages.add("Pokémon Go");
        statusMessages.add("with Togepi");
        statusMessages.add("an EX Raid");

        statusIndex = 0;

        statusTimer = new Timer();
        statusTask = new TimerTask() {
            @Override
            public void run() {
                _client.changePlayingText(statusMessages.get(statusIndex % statusMessages.size()));
                statusIndex++;
            }
        };

    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        statusTimer.scheduleAtFixedRate(statusTask, 1000, 5 * 60 * 1000); //5 minutes
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {

        // Split input into array
        String[] argArray = event.getMessage().getContent().toLowerCase().split(" ");

        // If there is no command or prefix present, do nothing
        if (argArray.length == 0)
            return;
        // If the first argument does not start with the prefix, do nothing
        if (!argArray[0].startsWith(BotUtils.BOT_PREFIX))
            return;
        // Grab the command without the prefix
        String commandStr = argArray[0].substring(BotUtils.BOT_PREFIX.length());
        // Switch to a List for safer access of arguments
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command

        if (commandMap.containsKey(commandStr))
            commandMap.get(commandStr).runCommand(event, argsList);

    }

    @EventSubscriber
    public void onMessageDeleted(MessageDeleteEvent event) {
        logger.debug("Message deleted: " + event.getMessage().getContent());
    }

}
