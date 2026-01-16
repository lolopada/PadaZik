package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class WindowDragListener extends InputListener {
    private boolean draggingWindow;
    private int dragMouseX, dragMouseY;
    private int dragWinX, dragWinY;
    private Object desktopWindow;
    private java.lang.reflect.Method getPosX;
    private java.lang.reflect.Method getPosY;
    private java.lang.reflect.Method setPos;

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button != 0) {
            return false;
        }

        try {
            Object gfx = Gdx.graphics;
            java.lang.reflect.Method getWindow = gfx.getClass().getMethod("getWindow");
            desktopWindow = getWindow.invoke(gfx);

            getPosX = desktopWindow.getClass().getMethod("getPositionX");
            getPosY = desktopWindow.getClass().getMethod("getPositionY");
            setPos = desktopWindow.getClass().getMethod("setPosition", int.class, int.class);

            dragWinX = (Integer) getPosX.invoke(desktopWindow);
            dragWinY = (Integer) getPosY.invoke(desktopWindow);

            dragMouseX = dragWinX + Gdx.input.getX();
            dragMouseY = dragWinY + Gdx.input.getY();

            draggingWindow = true;
        } catch (Exception e) {
            desktopWindow = null;
            draggingWindow = false;
            getPosX = null;
            getPosY = null;
            setPos = null;
        }
        return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        if (!draggingWindow || desktopWindow == null || getPosX == null || getPosY == null || setPos == null) {
            return;
        }

        try {
            int currentWinX = (Integer) getPosX.invoke(desktopWindow);
            int currentWinY = (Integer) getPosY.invoke(desktopWindow);

            int curMouseX = currentWinX + Gdx.input.getX();
            int curMouseY = currentWinY + Gdx.input.getY();

            int dx = curMouseX - dragMouseX;
            int dy = curMouseY - dragMouseY;

            int newWinX = dragWinX + dx;
            int newWinY = dragWinY + dy;
            setPos.invoke(desktopWindow, newWinX, newWinY);
        } catch (Exception e) {
        }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        draggingWindow = false;
        desktopWindow = null;
        getPosX = null;
        getPosY = null;
        setPos = null;
    }
}
