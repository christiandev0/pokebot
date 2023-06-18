import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {
    private String name;
    private int height;
    private int weight;
    private List<Type> types;
    @SerializedName("sprites")
    private Sprites sprites;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public void setSprites(Sprites sprites) {
        this.sprites = sprites;
    }

    public static class Sprites {
        @SerializedName("front_default")
        private String frontDefault;
        @SerializedName("other")
        private OtherSprites otherSprites;

        public String getFrontDefault() {
            return frontDefault;
        }

        public void setFrontDefault(String frontDefault) {
            this.frontDefault = frontDefault;
        }

        public OtherSprites getOtherSprites() {
            return otherSprites;
        }

        public void setOtherSprites(OtherSprites otherSprites) {
            this.otherSprites = otherSprites;
        }
    }

    public static class OtherSprites {
        @SerializedName("official-artwork")
        private OfficialArtworkSprites officialArtwork;

        public OfficialArtworkSprites getOfficialArtwork() {
            return officialArtwork;
        }

        public void setOfficialArtwork(OfficialArtworkSprites officialArtwork) {
            this.officialArtwork = officialArtwork;
        }
    }

    public static class OfficialArtworkSprites {
        @SerializedName("front_default")
        private String frontDefault;

        public String getFrontDefault() {
            return frontDefault;
        }

        public void setFrontDefault(String frontDefault) {
            this.frontDefault = frontDefault;
        }
    }
}