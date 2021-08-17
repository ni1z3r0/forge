package forge.adventure.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import forge.adventure.stage.WorldStage;

public class GameScene extends HudScene {

    public GameScene() {
        super(WorldStage.getInstance());

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void render() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
        hud.draw();

    }

    @Override
    public void ResLoaded() {


    }
}

