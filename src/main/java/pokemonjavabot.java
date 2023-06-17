import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;





public class pokemonjavabot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "6000796411:AAGcYeN3oA9iBK1RzsPF293IpREllr_G_L8";
    private static final String BOT_USERNAME = "pokemonjavabot";

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/";
    private Retrofit retrofit;
    private PokeApiService pokeApiService;
    private Update currentUpdate;
    public interface PokeApiService {
        @GET("pokemon/{pokemonName}")
        Call<Pokemon> getPokemon(@Path("pokemonName") String pokemonName);

        @GET("pokemon")
        Call<PokemonList> getPokemonList();
    }


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
            }
            if (messageText.equals("/help")) {
                BotCommandHandler commandHandler = new BotCommandHandler();
                String response = commandHandler.executeHelpCommand();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(response);
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (messageText.equals("/info")) {
                BotCommandHandler commandHandler = new BotCommandHandler();
                String response = commandHandler.executeInfoCommand();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(response);
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (messageText.startsWith("/cerca ")) {
                BotCommandHandler commandHandler = new BotCommandHandler();
                String response = commandHandler.executeCommand(messageText);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(response);
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }}
            else {
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
                sendSpawnMessage();
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
            return "Inserisci il nome del pokemon per scoprirne le caratteristiche!";
        }
    }

    public class SearchCommand implements BotCommand {
        private String pokemonName;

        public SearchCommand(String pokemonName) {
            this.pokemonName = pokemonName;
        }

        @Override
        public String executeCommand() {
            String response = getPokemonInfo(pokemonName.toLowerCase());
            return response;
        }
    }

    public class BotCommandHandler {
        public String executeCommand(String command) {
            BotCommand botCommand;
            if (command.equals("/start")) {
                botCommand = new StartCommand();
            } else if (command.startsWith("/cerca ")) {
                botCommand = new SearchCommand(command.substring(7));
            } else if (command.equals("/cerca")) {
                return "Per la ricerca scrivi /cerca <nome_pokémon>";
            } else {
                // Nessun comando corrispondente trovato, restituisci un messaggio di errore o una stringa vuota
                return "Comando non valido.";
            }
            return botCommand.executeCommand();
        }


        public String executeHelpCommand() {
            StringBuilder sb = new StringBuilder();
            sb.append("Ecco alcuni comandi disponibili:\n");
            sb.append("/start - Avvia il bot\n");
            sb.append("/help - Mostra l'elenco dei comandi disponibili\n");
            sb.append("/info - Mostra informazioni sul bot\n");
            sb.append("/cerca <nome_pokémon> - Cerca informazioni su un Pokémon\n");

            return sb.toString();}
        public String executeInfoCommand() {
            StringBuilder sb = new StringBuilder();
            sb.append("Questo è un bot per ottenere informazioni sui Pokémon.\n");
            sb.append("È possibile utilizzare il comando /cerca seguito dal nome di un Pokémon per cercare informazioni su di esso.\n");
            sb.append("Spero che ti sia utile! Buon divertimento!");

            return sb.toString();
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
                    String capitalizedPokemonName = capitalizeFirstLetter(pokemon.getName());
                    sb.append("Nome: ").append(capitalizedPokemonName).append("\n");
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
                            return "Non ci sono informazioni per questo pokémon";
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

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
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
    public static void main(String[] args) {
        pokemonjavabot bot = new pokemonjavabot();
        bot.start();
    }

    private void start() {
        System.out.println("Benvenuto!");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::sendSpawnMessage, 0, 5, TimeUnit.SECONDS);
    }

    private void sendSpawnMessage() {
        String randomPokemon = spawnRandomPokemon();
        SendMessage sendMessage = new SendMessage();
        Pokemon capturedPokemon = new Pokemon();
        capturedPokemon.setName(randomPokemon);

        // Ottieni l'URL dell'immagine del Pokémon
        String imageUrl = getPokemonImageUrl(randomPokemon);
        String nomepokemon = capturedPokemon.getName();
        sendMessage.setText("È apparso un " + nomepokemon + "!");

        sendMessage.setText("Vuoi provare a catturarlo?!");
        sendMessage.setChatId(currentUpdate.getMessage().getChatId().toString());

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(imageUrl));
        sendPhoto.setChatId(currentUpdate.getMessage().getChatId().toString());

        try {
            execute(sendPhoto);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private String getPokemonImageUrl(String pokemonName) {
        try {
            Call<Pokemon> call = pokeApiService.getPokemon(pokemonName);
            Response<Pokemon> response = call.execute();

            if (response.isSuccessful()) {
                Pokemon pokemon = response.body();
                if (pokemon != null && pokemon.getSprites() != null) {
                    return pokemon.getSprites().getFrontDefault();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ""; // Restituisce una stringa vuota se l'URL dell'immagine non è disponibile
    }


    private String spawnRandomPokemon() {
        try {
            Call<PokemonList> call = pokeApiService.getPokemonList();
            Response<PokemonList> response = call.execute();

            if (response.isSuccessful()) {
                PokemonList pokemonList = response.body();
                if (pokemonList != null && pokemonList.getResults() != null) {
                    List<Result> results = pokemonList.getResults();
                    Random random = new Random();
                    int randomIndex = random.nextInt(results.size());
                    Result randomResult = results.get(randomIndex);
                    return randomResult.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    return "Vuoi provare a catturarlo?";
    }
}
