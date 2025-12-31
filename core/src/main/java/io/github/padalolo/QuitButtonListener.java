package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class QuitButtonListener extends InputListener {

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button == 0) { // Only respond to left click
            event.stop(); // Stop propagation only for quit button
            Gdx.app.exit();
            return true;
        }
        return false;
    }
}
