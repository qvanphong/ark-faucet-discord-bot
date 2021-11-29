package tech.qvanphong.discordfaucet.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.ApplicationConfig;

import java.io.IOException;

@Component
public class BARKBalanceCommand implements SlashCommand {
    private ApplicationConfig applicationConfig;

    public BARKBalanceCommand(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public String getName() {
        return "barkbalance";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        String address = event.getOption("address").get().getValue().get().asString();
        String tokenId = "8259ce077b1e767227e5e0fce590d26d";
        String apiURL = applicationConfig.getAslpApiUrl();


        return Mono.just(fetchBalance(apiURL, tokenId, address))
                .flatMap(balance -> event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .addEmbed(EmbedCreateSpec.create()
                                .withColor(Color.RED)
                                .withTitle("Số dư: " + balance + " bARK")
                                .withFooter(EmbedCreateFields.Footer.of("Đây là tin nhắn bí mật \uD83D\uDE36", null)))
                        .ephemeral(true)
                        .build()));
    }

    private String fetchBalance(String apiUrl, String tokenId, String address) {
        Request request = new Request.Builder().url(apiUrl + "balance/" + tokenId + "/" + address).build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string().replaceAll("\"", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }
}
