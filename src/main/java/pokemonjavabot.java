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

import java.io.IOException;
import java.util.List;
import java.util.Random;


public class pokemonjavabot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "6000796411:AAGcYeN3oA9iBK1RzsPF293IpREllr_G_L8";
    private static final String BOT_USERNAME = "pokemonjavabot";

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/";
    private Retrofit retrofit;
    private PokeApiService pokeApiService;
    private Update currentUpdate;
    private boolean isRunning = true; // Variabile flag per indicare se il thread deve essere eseguito


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
        Thread requestThread = new Thread(() -> processRequest(update));
        requestThread.start();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            System.out.println(update.getMessage().getText());
            System.out.println(update.getMessage().getFrom().getFirstName());

            if (messageText.equals("/start")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Benvenuto!");
                sendMessage.setChatId(update.getMessage().getChatId().toString());
                isRunning = true;

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
                String response = commandHandler.executeCommand(messageText, update);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(response);
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }} else if (messageText.startsWith("/stop")) {
                BotCommandHandler commandHandler = new BotCommandHandler();
                String response = commandHandler.executeCommand(messageText, update);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(response);
                sendMessage.setChatId(update.getMessage().getChatId().toString());

                // Passa l'ID della chat al comando StopCommand
                BotCommand botCommand = new StopCommand(update.getMessage().getChatId());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else {
                BotCommandHandler commandHandler = new BotCommandHandler();
                String response = commandHandler.executeCommand(messageText, update);
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

    private void processRequest(Update update) {
        // Gestisci la richiesta qui
        String pokemonInfo = "";
        while (isRunning) {
        try {
            pokemonInfo = spawnRandomPokemon();
        } catch (RuntimeException e) {
            pokemonInfo = "Errore durante lo spawn del Pokémon casuale: " + e.getMessage();
        }
        try {
            Thread.sleep(10000); // Sospende l'esecuzione del thread per 1 minuto
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ripristina lo stato interrupt del thread
            e.printStackTrace(); // Gestisci l'eccezione in base alle tue esigenze
        }
        SendMessage response = new SendMessage();
                response.setChatId(update.getMessage().getChatId());
                response.setText(pokemonInfo);

        try {
            execute(response); // Invia il messaggio di risposta
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        }
    }

    public void stopProcessing(Long chatId) {
        isRunning = false;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Processo interrotto.");
        sendMessage.setChatId(chatId.toString());

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
            return "Fai attenzione potresti imbatterti in un pokémon selvatico!";
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
    public class StopCommand implements BotCommand {
        private Long chatId;

        public StopCommand(Long chatId) {
            this.chatId = chatId;
        }
        @Override
        public String executeCommand() {
            stopProcessing(chatId);

            return "Hai interrotto la ricerca di Pokémon. Grazie per aver utilizzato il bot!";
        }
    }


    public class BotCommandHandler {
        public String executeCommand(String command, Update update) {
            BotCommand botCommand;
            if (command.equals("/start")) {
                botCommand = new StartCommand();
            } else if (command.startsWith("/cerca ")) {
                botCommand = new SearchCommand(command.substring(7));
            } else if (command.equals("/cerca")) {
                return "Per la ricerca scrivi /cerca <nome_pokémon>";
            } else if (command.equals("/stop")) {
                botCommand = new StopCommand(update.getMessage().getChatId());
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
            sb.append("/stop Esci dall'erba alta per non incontrare altri pokemon!");

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

    public interface PokeApiService {
        @GET("pokemon/{pokemonName}")
        Call<Pokemon> getPokemon(@Path("pokemonName") String pokemonName);
    }

    public static void main(String[] args) {
        pokemonjavabot bot = new pokemonjavabot();
        bot.start();
    }

    private void start() {
        System.out.println("Benvenuto!\nComincia la tua avventura!");
    }
    private String spawnRandomPokemon() {
        try {
            Random random = new Random();
            int pokemonId = random.nextInt(898) + 1; // Genera un ID casuale compreso tra 1 e 898

            Call<Pokemon> call = pokeApiService.getPokemon(String.valueOf(pokemonId));
            Response<Pokemon> response = call.execute();

            if (response.isSuccessful()) {
                Pokemon pokemon = response.body();
                String pokemonName = pokemon.getName();
                String imageUrl = pokemon.getSprites().getFrontDefault();

                return "È apparso " + pokemonName + "!\n" + imageUrl;
            } else {
                throw new RuntimeException("Errore nella richiesta API: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante l'esecuzione della richiesta API", e);
        }
    }
}