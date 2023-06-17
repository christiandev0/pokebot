import java.util.ArrayList;
import java.util.List;

public class Pokedex {
    private List<Pokemon> capturedPokemon;

    public Pokedex() {
        capturedPokemon = new ArrayList<>();
    }

    public void addCapturedPokemon(Pokemon pokemon) {
        capturedPokemon.add(pokemon);
    }

    public List<Pokemon> getCapturedPokemon() {
        return capturedPokemon;
    }
}
