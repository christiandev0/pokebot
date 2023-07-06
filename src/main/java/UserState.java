import java.util.HashSet;
import java.util.Set;

public class UserState {
    private long chatId; // ID della chat dell'utente
    private String currentState; // Stato corrente della conversazione con l'utente
    private Set<Pokemon> pokemonSquad; // Squadra Pokémon dell'utente
    private Set<Pokemon> pokemonList; // Pokedex dell'utente
    private boolean isRunning;

    public UserState() {
        this.isRunning = true; // Imposta il valore predefinito
    }

    // Costruttore
    public UserState(long chatId) {
        this.chatId = chatId;
        this.currentState = "start"; // Stato iniziale
        this.pokemonSquad = new HashSet<>(6);
        this.pokemonList = new HashSet<>(6);
        this.isRunning = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    // Metodi per accedere e modificare le informazioni della sessione

    public long getChatId() {
        return chatId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Set<Pokemon> getPokemonSquad() {
        return pokemonSquad;
    }

    public void addPokemonToSquad(Pokemon pokemon) {
        pokemonSquad.add(pokemon);
    }

    public void removePokemonFromSquad(String pokemonName) {
        Pokemon pokemonToRemove = null;
        for (Pokemon pokemon : pokemonSquad) {
            if (pokemon.getName().equalsIgnoreCase(pokemonName)) {
                pokemonToRemove = pokemon;
                break;
            }
        }
        if (pokemonToRemove != null) {
            pokemonSquad.remove(pokemonToRemove);
        }
    }

    public Set<Pokemon> getPokemonList() {
        return pokemonList;
    }

    public void addPokemonToList(Pokemon pokemon) {
        pokemonList.add(pokemon);
    }

    public void removePokemonFromList(String pokemonName) {
        Pokemon pokemonToRemove = null;
        for (Pokemon pokemon : pokemonList) {
            if (pokemon.getName().equalsIgnoreCase(pokemonName)) {
                pokemonToRemove = pokemon;
                break;
            }
        }
        if (pokemonToRemove != null) {
            pokemonList.remove(pokemonToRemove);
        }
    }

    // Altri metodi e logica specifici della sessione
}
