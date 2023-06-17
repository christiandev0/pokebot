import java.util.ArrayList;
import java.util.List;

public class PokemonList {
    private List<Result> allPokemons;

    public PokemonList() {
        this.allPokemons = new ArrayList<>();
    }

    public List<Result> getAllPokemons() {
        return allPokemons;
    }

    public void setAllPokemons(List<Result> allPokemons) {
        this.allPokemons = allPokemons;
    }
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
