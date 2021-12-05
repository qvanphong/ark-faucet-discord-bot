package tech.qvanphong.discordfaucet.command;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.utility.GuildTokenConfigUtility;

import java.util.Optional;

@Component
public class DonateCommand implements SlashCommand {
    private final GuildTokenConfigUtility guildTokenConfigUtility;

    public DonateCommand(GuildTokenConfigUtility guildTokenConfigUtility) {
        this.guildTokenConfigUtility = guildTokenConfigUtility;
    }

    @Override
    public String getName() {
        return "donate";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) return event.reply("Không thể lấy guild id");

        long guildId = guildIdOptional.get().asLong();
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("ARK Faucet Bot Address:")
                .footer("Cảm ơn sự đóng góp của bạn \uD83D\uDE04", null);

        for (TokenConfig tokenConfig : guildTokenConfigUtility.getTokenConfigs(guildId)) {
            if (tokenConfig.getSenderAddress() != null && !tokenConfig.getSenderAddress().isBlank()) {
                embedBuilder.addField(tokenConfig.getName().toUpperCase(), "Address", true)
                        .addField("\u200B", tokenConfig.getSenderAddress(), true)
                        .addField("\u200B", "\u200B", true);
            }
        }

        return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                .addEmbed(embedBuilder.build())
                .build())
                .then();
    }
}
