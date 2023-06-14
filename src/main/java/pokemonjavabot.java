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
    private static final String BOT_TOKEN = "6000796411:AAGcYeN3oA9iBK1RzsPF293IpREllr_G_L8";
    private static final String BOT_USERNAME = "https://t.me/pokemonjavabot";

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/";
    private Retrofit retrofit;
    private PokeApiService pokeApiService;
    private Update currentUpdate;

    public pokemonjavabot() {
        retrofit = new Retrofit.Builder()
                .baseUrl(POKEAPI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        pokeApiService = retrofit.create(PokeApiService.class);
    }

    @Override
    public void onUpdateReceived(Update update) {
        currentUpdate = update;

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            System.out.println(update.getMessage().getText());
            System.out.println(update.getMessage().getFrom().getFirstName());

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
                BotCommandHandler commandHandler = new BotCommandHandler();
                String response = commandHandler.executeCommand(messageText);
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

    public interface BotCommand {
        String executeCommand();
    }

    public class StartCommand implements BotCommand {
        @Override
        public String executeCommand() {
            return "Benvenuto!\nInserisci il nome del pokemon per scoprirne le caratteristiche!";
        }
    }

    public class SearchCommand implements BotCommand {
        @Override
        public String executeCommand() {
            String response = getPokemonInfo(currentUpdate.getMessage().getText().toLowerCase());
            return response;
        }
    }

    public class BotCommandHandler {
        public String executeCommand(String command) {
            BotCommand botCommand;
            if (command.equals("/start")) {
                botCommand = new StartCommand();
            } else {
                botCommand = new SearchCommand();
            }

            return botCommand.executeCommand();
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
                    sb.append("Nome: ").append(pokemon.getName()).append("\n");
                    sb.append("Altezza: ").append(convertDecimetersToCentimeters(pokemon.getHeight())).append(" cm").append("\n");
                    sb.append("Peso: ").append(convertHectogramsToKilograms(pokemon.getWeight())).append(" kg").append("\n");

                    List<Type> types = pokemon.getTypes();
                    StringBuilder typesStringBuilder = new StringBuilder();
                    for (Type type : types) {
                        typesStringBuilder.append(type.getTypeDetails().getName()).append(", ");
                    }
                    String pokemonTypes = typesStringBuilder.toString().trim();
                    pokemonTypes = pokemonTypes.substring(0, pokemonTypes.length() - 1);
                    sb.append("Tipo: ").append(pokemonTypes).append("\n");

                    if (pokemon.getSprites() != null) {
                        String gifUrl = pokemon.getSprites().getFrontDefault();
                        if (gifUrl != null) {
                            sendPokemonInfo(sb.toString(), gifUrl);
                        } else {
                            return "Non ci sono informazioni per questo pokemon";
                        }
                    }
                } else {
                    // Il Pokémon non è stato trovato
                    return "Il Pokémon \"" + pokemonName + "\" non esiste o non sono presenti informazioni a riguardo.";
                }
            } else {
                // La risposta del server non è stata di successo
                return "Il Pokémon \"" + pokemonName + "\" non esiste o non sono presenti informazioni a riguardo.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore durante il caricamento della richiesta. Riprova.";
        }

        // Se il nome del Pokémon è nullo o vuoto, restituisci un messaggio di errore
        if (pokemonName == null || pokemonName.isEmpty()) {
            return "Nome del Pokémon non valido. Riprova.";
        }

        // Se si arriva a questo punto, la richiesta è stata completata con successo
        return "Se desideri continuare inserisci un altro Pokemon!";
    }

    private int convertDecimetersToCentimeters(int decimeters) {
        return decimeters * 10;
    }

    private double convertHectogramsToKilograms(int hectograms) {
        return hectograms / 10.0;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
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
