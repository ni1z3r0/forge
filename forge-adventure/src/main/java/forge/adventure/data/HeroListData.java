package forge.adventure.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import forge.adventure.util.Res;

import java.util.List;

public class HeroListData {
    static private HeroListData instance;
    public List<HeroData> heroes;
    public String avatar;
    private TextureAtlas avatarSprites;

    static private HeroListData read() {
        Json json = new Json();
        FileHandle handle = forge.adventure.util.Res.CurrentRes.GetFile("world/heroes.json");
        if (handle.exists()) {
            instance = json.fromJson(HeroListData.class, handle);
            instance.avatarSprites = Res.CurrentRes.getAtlas(instance.avatar);

            instance.avatarSprites.getTextures().first().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        return instance;
    }

    static public String getHero(int raceIndex, boolean female) {
        if (instance == null)
            instance = read();
        HeroData data = instance.heroes.get(raceIndex);

        if (female)
            return data.female;
        return data.male;

    }

    public static TextureRegion getAvatar(int heroRace, boolean isFemale, int avatarIndex) {

        if (instance == null)
            instance = read();
        HeroData data = instance.heroes.get(heroRace);
        Array<Sprite> sprites;
        if (isFemale)
            sprites = instance.avatarSprites.createSprites(data.femaleAvatar);
        else
            sprites = instance.avatarSprites.createSprites(data.maleAvatar);
        avatarIndex %= sprites.size;
        if (avatarIndex < 0) {
            avatarIndex += sprites.size;
        }
        return sprites.get(avatarIndex);
    }

    public static Array<String> getRaces() {
        if (instance == null)
            instance = read();
        Array<String> ret = new Array<>();
        for (HeroData hero : instance.heroes) {
            ret.add(hero.name);
        }
        return ret;
    }
}
