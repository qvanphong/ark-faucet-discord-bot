package tech.qvanphong.discordfaucet;

import discord4j.common.JacksonResources;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final RestClient client;
    private final GatewayDiscordClient discordClient;

    public ApplicationRunner(RestClient client, GatewayDiscordClient discordClient)
    {
        this.client = client;
        this.discordClient = discordClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.registCommands();
        discordClient.onDisconnect().block();

//        Thread keepAliveThread = new Thread(() -> discordClient.onDisconnect().block());
//        keepAliveThread.setDaemon(false);
//        keepAliveThread.setName( "DiscordApplication Keep-Alive Thread");
//        keepAliveThread.start();
    }

    private void registCommands() throws IOException {


        //Create an ObjectMapper that supported Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below.
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final ApplicationService applicationService = client.getApplicationService();
        final Long applicationId = client.getApplicationId().block();

        //These are commands already registered with discord from previous runs of the bot.
        Map<String, ApplicationCommandData> discordCommands = applicationService
                .getGlobalApplicationCommands(applicationId)
                .collectMap(ApplicationCommandData::name)
                .block();

        //Get our commands json from resources as command data
        Map<String, ApplicationCommandRequest> commands = new HashMap<>();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

            commands.put(request.name(), request);

            //Check if this is a new command that has not already been registered.
            if (!discordCommands.containsKey(request.name())) {
                //Not yet created with discord, lets do it now.
                applicationService.createGlobalApplicationCommand(applicationId, request).block();

                LOGGER.info("Created global command: " + request.name());
            }
        }

        //Check if any  commands have been deleted or changed.
        for (ApplicationCommandData discordCommand : discordCommands.values()) {
            long discordCommandId = Long.parseLong(discordCommand.id());

            ApplicationCommandRequest command = commands.get(discordCommand.name());

            if (command == null) {
                //Removed command.json, delete global command
                applicationService.deleteGlobalApplicationCommand(applicationId, discordCommandId).block();

                LOGGER.info("Deleted global command: " + discordCommand.name());
                continue; //Skip further processing on this command.
            }

            //Check if the command has been changed and needs to be updated.
            if (hasChanged(discordCommand, command)) {
                applicationService.modifyGlobalApplicationCommand(applicationId, discordCommandId, command).block();

                LOGGER.info("Updated global command: " + command.name());
            }
        }
    }

    private boolean hasChanged(ApplicationCommandData existingCommand, ApplicationCommandRequest command) {
        return command.description().toOptional().map(value -> !existingCommand.description().equals(value)).orElse(false)
                || existingCommand.defaultPermission().toOptional().orElse(true) != command.defaultPermission().toOptional().orElse(true)
                || !existingCommand.options().equals(buildOptions(command.options()));
    }

    private Possible<List<ApplicationCommandOptionData>> buildOptions(Possible<List<ApplicationCommandOptionData>> options) {
        if (options.isAbsent()) {
            return options;
        }
        List<ApplicationCommandOptionData> newOptions = new ArrayList<>();
        for (ApplicationCommandOptionData optionData : options.get()) {
            // turn required == false into absent, to fix equality checks
            newOptions.add(ApplicationCommandOptionData.builder()
                    .from(optionData)
                    .required(optionData.required().toOptional()
                            .filter(it -> it)
                            .map(Possible::of)
                            .orElse(Possible.absent()))
                    .options(buildOptions(optionData.options()))
                    .build());
        }
        return Possible.of(newOptions);
    }
}
