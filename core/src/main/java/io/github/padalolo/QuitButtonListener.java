package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;

public class QuitButtonListener extends InputListener {
    private final VisLabel quitLabel;

    public QuitButtonListener(VisLabel quitLabel) {
        this.quitLabel = quitLabel;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button == 0) { // Only respond to left click
            event.stop(); // Stop propagation only for quit button
            Gdx.app.exit();
            return true;
        }
        return false;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer,
            com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
        if (pointer == -1) {
            quitLabel.setColor(com.badlogic.gdx.graphics.Color.RED);
        }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
        if (pointer == -1) {
            quitLabel.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
    }

}
