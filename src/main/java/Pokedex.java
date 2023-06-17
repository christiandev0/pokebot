import java.util.ArrayList;
import java.util.List;

public class Pokedex {
    private String pokemonName;
    private int height;
    private String weight;
    private List<String> types;
    Pokedex(String pokemonName, int height, String weight, List<String> types) {
        this.pokemonName = pokemonName;
        this.height = height;
        this.weight = weight;
        this.types = types;
    }

}

