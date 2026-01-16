package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
        if (button == 0) {
            event.stop();
            Gdx.app.exit();
            return true;
        }
        return false;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer == -1) {
            quitLabel.setColor(Color.RED);
        }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer == -1) {
            quitLabel.setColor(Color.WHITE);
        }
    }
}
