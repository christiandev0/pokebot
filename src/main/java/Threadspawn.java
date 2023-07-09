import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Threadspawn implements Runnable {
    private boolean isRunning;
    private pokemonjavabot bot;
    private Update update;
    private pokemonjavabot.BotCommandHandler commandHandler;
    private  UserState userState;

    public Threadspawn(pokemonjavabot bot, Update update, UserState userState, pokemonjavabot.BotCommandHandler commandHandler) {
        this.isRunning = userState.isRunning();
        this.bot = bot;
        this.update = update;
        this.commandHandler = commandHandler;
        this.userState = userState;
    }

    public Threadspawn() {
    }
    public void stopThread() {
        isRunning = false;
    }
    @Override
    public void run() {
        while (isRunning) {
            try {
                String pokemonInfo = commandHandler.spawnRandomPokemon(update, userState);
                SendMessage response = new SendMessage();
                response.setChatId(update.getMessage().getChatId());
                response.setText(pokemonInfo);
                try {
                    bot.execute(response); // Invia il messaggio di risposta
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(15000); // Sospende l'esecuzione del thread per 10 secondi
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Ripristina lo stato interrupt del thread
                    e.printStackTrace(); // Gestisci l'eccezione in base alle tue esigenze
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
