import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.io.IOException;
import java.util.*;


public class pokemonjavabot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "6000796411:AAGcYeN3oA9iBK1RzsPF293IpREllr_G_L8";
    private static final String BOT_USERNAME = "pokemonjavabot";

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/";
    private Retrofit retrofit;
    private PokeApiService pokeApiService;
    private Update currentUpdate;
    private Set<Pokemon> pokemonList;
    private Set <Pokemon> pokemonSquad;
    private  Pokemon currentPokemon;
    private Long chatId;
    private final Map<Long, pokemonjavabot> pokemonbotIstancies;
    private final Map<Long, UserState> userStates;

    private final Map<Long, BotCommandHandler> botcommandhandleristancies;






    public pokemonjavabot() {
        retrofit = new Retrofit.Builder()
                .baseUrl(POKEAPI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        pokeApiService = retrofit.create(PokeApiService.class);
        pokemonList = new HashSet<>();
        pokemonSquad = new HashSet<>(6);
        pokemonbotIstancies = new HashMap<>();
        userStates = new HashMap<>();
        botcommandhandleristancies = new HashMap<>();
    }

    @Override
    public void onUpdateReceived(Update update) {
        currentUpdate = update;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();

            System.out.println(update.getMessage().getText());
            System.out.println(update.getMessage().getFrom().getFirstName());

            BotCommandHandler commandHandler = new BotCommandHandler(userStates);
            String response;

            if (messageText.equals("/start")) {
                UserState userState = new UserState(); // Crea una nuova istanza di UserState
                userState.setRunning(true); // Imposta il flag di esecuzione su true
                userStates.put(chatId, userState); // Inserisci lo stato dell'utente nella mappa
                response = commandHandler.executeCommand("/start", update);
                Thread requestThread = new Thread(() -> processRequest(update, userState));
                requestThread.start();
            } else if (messageText.equals("/help")) {
                response = commandHandler.executeCommand("/help", update);
            } else if (messageText.equals("/info")) {
                response = commandHandler.executeCommand("/info", update);
            } else if (messageText.equals("/cerca")) {
                response = commandHandler.executeCommand("/cerca", update);
            } else if (messageText.equals("/stop")) {
                UserState userState = userStates.get(chatId);
                userState.setRunning(false);
                if (userState != null) {
                    userState.setRunning(false); // Imposta il flag di esecuzione su false
                }
               response = commandHandler.executeCommand("/stop", update);
            } else if (messageText.equals("/pokedex")) {
                response = commandHandler.executeCommand("/pokedex", update);
            } else if (messageText.equals("squadra")) {
                response = commandHandler.executeCommand("/squadra", update);
            } else {
                response = commandHandler.executeCommand(messageText, update);
            }
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
    private void processRequest(Update update, UserState userState) {
        String pokemonInfo;
        while (userState.isRunning()) {
            try {
                pokemonInfo = spawnRandomPokemon(update);
            } catch (RuntimeException e) {
                pokemonInfo = "Errore durante lo spawn del Pokémon casuale: " + e.getMessage();
            }

            SendMessage response = new SendMessage();
            response.setChatId(update.getMessage().getChatId());
            response.setText(pokemonInfo);

            try {
                execute(response); // Invia il messaggio di risposta
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(30000); // Sospende l'esecuzione del thread per 10 secondi
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Ripristina lo stato interrupt del thread
                e.printStackTrace(); // Gestisci l'eccezione in base alle tue esigenze
            }
        }
    }

    public void stopProcessing(Long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Il sentiero adesso è privo di erba alta");
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

        public class PokedexCommand implements BotCommand {
            private UserState userState;

            public PokedexCommand(UserState userState) {
                this.userState = userState;
            }

            @Override
            public String executeCommand() {
                Set<Pokemon> pokemonSet = userState.getPokemonList();
                if (pokemonSet.isEmpty()) {
                    return "Il Pokedex è vuoto. Cattura alcuni Pokémon!";
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Il Pokedex contiene i seguenti Pokémon:\n");
                    for (Pokemon pokemon : pokemonSet) {
                        sb.append("Nome: ").append(pokemon.getName()).append("\n");
                        sb.append("Altezza: ").append(convertDecimetersToCentimeters(pokemon.getHeight())).append(" cm").append("\n");
                        sb.append("Peso: ").append(convertHectogramsToKilograms(pokemon.getWeight())).append(" kg").append("\n\n");

                        Pokemon.Sprites sprites = pokemon.getSprites();
                    if (sprites != null) {
                        String frontSpriteUrl = sprites.getFrontDefault();
                        if (frontSpriteUrl != null) {
                            sb.append("").append(frontSpriteUrl).append("\n");
                        }
                    }

                    sb.append("\n");
                }
                return sb.toString();
            }
        }
    }

    public class SquadraCommand implements BotCommand {
        private UserState userState;

        public SquadraCommand(UserState userState) {
            this.userState = userState;
        }

        @Override
        public String executeCommand() {
            Set<Pokemon> pokemonSquad = userState.getPokemonSquad();
            if (pokemonSquad.isEmpty()) {
                return "La squadra è vuota. Cattura alcuni Pokémon!";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("La squadra contiene i seguenti Pokémon:\n");
                for (Pokemon pokemon : pokemonSquad) {
                    sb.append("Nome: ").append(pokemon.getName()).append("\n");
                    sb.append("Altezza: ").append(convertDecimetersToCentimeters(pokemon.getHeight())).append(" cm").append("\n");
                    sb.append("Peso: ").append(convertHectogramsToKilograms(pokemon.getWeight())).append(" kg").append("\n\n");

                    Pokemon.Sprites sprites = pokemon.getSprites();
                    if (sprites != null) {
                        String frontSpriteUrl = sprites.getFrontDefault();
                        if (frontSpriteUrl != null) {
                            sb.append("").append(frontSpriteUrl).append("\n");
                        }
                    }

                    sb.append("\n");
                }
                return sb.toString();
            }
        }
    }

    public class CatturaCommand implements BotCommand {
        private UserState userState;

        public CatturaCommand(UserState userState) {
            this.userState = userState;
        }

        @Override
        public String executeCommand() {
            Set<Pokemon> pokemonSquad = userState.getPokemonSquad();
            Set<Pokemon> pokemonList = userState.getPokemonList();
            pokemonList.add(currentPokemon);
            if (pokemonSquad.size() < 6) {
                pokemonSquad.add(currentPokemon);
                return "Hai catturato " + currentPokemon.getName() + " e l'hai aggiunto alla tua squadra!";
            } else {
                return "La tua squadra è già completa!";
            }
        }
    }
   public class RimuoviCommand implements BotCommand {
       private UserState userState;
       private String pokemonName;

       public RimuoviCommand(UserState userState, String pokemonName) {
           this.userState = userState;
           this.pokemonName = pokemonName;
       }

       @Override
       public String executeCommand() {
           Set<Pokemon> pokemonSquad = userState.getPokemonSquad();
           Pokemon pokemonToRemove = null;
           for (Pokemon pokemon : pokemonSquad) {
               if (pokemon.getName().equalsIgnoreCase(pokemonName)) {
                   pokemonToRemove = pokemon;
                   break;
               }
           }
           if (pokemonToRemove != null) {
               pokemonSquad.remove(pokemonToRemove);
               return "Il Pokémon " + pokemonName + " è stato rimosso dalla squadra.";
           } else {
               return "Il Pokémon " + pokemonName + " non è presente nella squadra.";
           }
       }
   }

    public class BotCommandHandler {
        private final Map<Long,UserState> UserStates;

        public BotCommandHandler(Map<Long, UserState> userStates) {
            this.UserStates= userStates;
        }
        public void stopProcessing(Long chatId) {
            UserState userState = userStates.get(chatId);
            if (userState != null) {
                userState.setRunning(false);
            }}

        public String executeCommand(String command, Update update) {
            BotCommand botCommand;
            long chatId = update.getMessage().getChatId();
            UserState userState = userStates.get(chatId);

            if (command.equals("/start")) {
                userState = new UserState(chatId);
                userStates.put(chatId, userState);
                botCommand = new StartCommand();
            } else if (command.startsWith("/cerca ")) {
                botCommand = new SearchCommand(command.substring(7));
            } else if (command.equals("/cerca")) {
                return "Per la ricerca scrivi /cerca <nome_pokémon>";
            } else if (command.equals("/stop")) {
                userState.setRunning(false);
                botCommand = new StopCommand(chatId);
            } else if (command.equals("/pokedex")) {
                botCommand = new PokedexCommand(userState);
            } else if (command.equals("/info")) {
                return executeInfoCommand(userStates);
            } else if (command.equals("/help")) {
                return executeHelpCommand(userStates);
            } else if (command.equals("/squadra")) {
                botCommand = new SquadraCommand(userState);
            } else if (command.equals("/cattura")) {
                botCommand = new CatturaCommand(userState);
            } else if (command.startsWith("/rimuovi ")) {
                String pokemonName = command.substring(9);
                botCommand = new RimuoviCommand((UserState) userState.getPokemonSquad(), pokemonName);
            } else {
                return "Comando non valido.";
            }

            return botCommand.executeCommand();
        }


        public String executeHelpCommand(Map<Long, UserState> userStates) {
            StringBuilder sb = new StringBuilder();
            sb.append("Ecco alcuni comandi disponibili:\n");
            sb.append("/start - Avvia il bot\n");
            sb.append("/help - Mostra l'elenco dei comandi disponibili\n");
            sb.append("/info - Mostra informazioni sul bot\n");
            sb.append("/cerca <nome_pokémon> - Cerca informazioni su un Pokémon\n");
            sb.append("/stop Esci dall'erba alta per non incontrare altri pokemon!");
            sb.append("/cattura ti permette di catturare il pokémon apparso!");
            sb.append("/rimuovi consente di liberare un pokémon dalla squadra ");

            return sb.toString();}
        public String executeInfoCommand(Map<Long, UserState> userStates) {
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
    private String spawnRandomPokemon(Update update) {
        try {
            Random random = new Random();
            int pokemonId = random.nextInt(898) + 1; // Genera un ID casuale compreso tra 1 e 898

            Call<Pokemon> call = pokeApiService.getPokemon(String.valueOf(pokemonId));
            Response<Pokemon> response = call.execute();

            if (response.isSuccessful()) {
                Pokemon pokemon = response.body();
                String pokemonName = pokemon.getName();
                String imageUrl = pokemon.getSprites().getFrontDefault();
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setPhoto(new InputFile(imageUrl));
                sendPhoto.setChatId(update.getMessage().getChatId().toString());
                currentPokemon = pokemon;
                pokemonList.add(pokemon);

                try {
                    execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                SendMessage risposta = new SendMessage();
                risposta.setText("È apparso " + pokemonName + "! Vuoi catturarlo e aggiungerlo alla tua squadra? (/cattura)");
                risposta.setChatId(update.getMessage().getChatId().toString());
                // Invia la tastiera personalizzata con i pulsanti "Si" e "No"
                ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
                KeyboardButton catturaButton = new KeyboardButton("/cattura");
                KeyboardButton noButton = new KeyboardButton("No");
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRow.add(catturaButton);
                keyboardRow.add(noButton);
                replyMarkup.setKeyboard(List.of(keyboardRow));
                risposta.setReplyMarkup(replyMarkup);
                try {
                    execute(risposta);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                return "";
            } else {
                throw new RuntimeException("Errore nella richiesta API: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante l'esecuzione della richiesta API", e);
        }
    }
    private BotCommandHandler getbotistancies(Long chatId){
        return botcommandhandleristancies.computeIfAbsent(chatId, k-> new BotCommandHandler(userStates));
    }

}