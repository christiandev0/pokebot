import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
//siu
public class pokemonjavabot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "6000796411:AAGcYeN3oA9iBK1RzsPF293IpREllr_G_L8";
    private static final String BOT_USERNAME = "pokemonjavabot";

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/";
    private Retrofit retrofit;
    private PokeApiService pokeApiService;

    public pokemonjavabot() {
        retrofit = new Retrofit.Builder()
                .baseUrl(POKEAPI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        pokeApiService = retrofit.create(PokeApiService.class);
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update.getMessage().getText());
        System.out.println(update.getMessage().getFrom().getFirstName());

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
                String response = getPokemonInfo(messageText);
                System.out.println(update.getMessage().getText());
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

    private void sendTextMessage(String string, String s) {
    }

    private String getPokemonInfo(String pokemonName) {
        try {
            Call<Pokemon> call = pokeApiService.getPokemon(pokemonName);
            Response<Pokemon> response = call.execute();

            if (response.isSuccessful()) {
                Pokemon pokemon = response.body();
                if (pokemon != null) {
                    return "Name: " + pokemon.getName() + "\nHeight: " + pokemon.getHeight() + "\nWeight: " + pokemon.getWeight();
                } else {
                    return "No information found for the given Pokémon.";
                }
            } else {
                return "Failed to retrieve Pokémon information.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while processing the request.";
        }
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
    }
}
