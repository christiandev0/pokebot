import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public class pokemonjavabot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "YOUR_BOT_TOKEN";
    private static final String BOT_USERNAME = "YOUR_BOT_USERNAME";

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/";
    private Retrofit retrofit;
    private PokeApiService pokeApiService;
    private Update currentUpdate; // Aggiunta variabile per memorizzare l'update corrente

    public pokemonjavabot() {
        retrofit = new Retrofit.Builder()
                .baseUrl(POKEAPI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        pokeApiService = retrofit.create(PokeApiService.class);
    }

    @Override
    public void onUpdateReceived(Update update) {
        currentUpdate = update; // Memorizza l'update corrente

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if (messageText.equals("/start")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Benvenuto!");
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                String response = getPokemonInfo(messageText);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(response);
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendPokemonInfo(String response, String gifUrl) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(response);
        sendMessage.setChatId(currentUpdate.getMessage().getChatId().toString());

        SendAnimation sendAnimation = new SendAnimation();
        sendAnimation.setAnimation(new InputFile(gifUrl));
        sendAnimation.setChatId(currentUpdate.getMessage().getChatId().toString());

        try {
            execute(sendMessage);
            execute(sendAnimation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getPokemonInfo(String pokemonName) {
        try {
            Call<Pokemon> call = pokeApiService.getPokemon(pokemonName);
            Response<Pokemon> response = call.execute();

            if (response.isSuccessful()) {
                Pokemon pokemon = response.body();
                if (pokemon != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Name: ").append(pokemon.getName()).append("\n");
                    sb.append("Height: ").append(pokemon.getHeight()).append("\n");
                    sb.append("Weight: ").append(pokemon.getWeight()).append("\n");

                    List<Type> types = pokemon.getTypes();
                    StringBuilder typesStringBuilder = new StringBuilder();
                    for (Type type : types) {
                        typesStringBuilder.append(type.getTypeDetails().getName()).append(", ");
                    }
                    String pokemonTypes = typesStringBuilder.toString().trim();
                    pokemonTypes = pokemonTypes.substring(0, pokemonTypes.length() - 1); // Remove the trailing comma
                    sb.append("Type: ").append(pokemonTypes).append("\n");

                    if (pokemon.getSprites() != null) {
                        String gifUrl = pokemon.getSprites().getFrontDefault();
                        if (gifUrl != null) {
                            sendPokemonInfo(sb.toString(), gifUrl);
                        } else {
                            return "No information found for the given Pokémon.";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while processing the request.";
        }

        return "Continua a cercare!";
    }


    @Override
    public String getBotUsername() {
        return "pokemonjavabot";
    }

    @Override
    public String getBotToken() {
        return "6000796411:AAGcYeN3oA9iBK1RzsPF293IpREllr_G_L8";
    }

    public interface PokeApiService {
        @GET("pokemon/{pokemonName}")
        Call<Pokemon> getPokemon(@Path("pokemonName") String pokemonName);
    }

    public static void main(String[] args) {
        pokemonjavabot bot = new pokemonjavabot();
        bot.start();
    }

    private void start() {
        System.out.println("Benvenuto!");
    }
}
