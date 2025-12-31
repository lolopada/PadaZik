package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * Listener that enables dragging a desktop window by handling touch/mouse
 * events.
 * Uses reflection to access LWJGL3 window management methods.
 */
public class WindowDragListener extends InputListener {
    private boolean draggingWindow;
    private int dragMouseX, dragMouseY;
    private int dragWinX, dragWinY;
    private Object desktopWindow;

    // Cached reflection methods for performance
    private java.lang.reflect.Method getPosX;
    private java.lang.reflect.Method getPosY;
    private java.lang.reflect.Method setPos;

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button != 0)
            return false; // Only left mouse button

        try {
            Object gfx = Gdx.graphics;
            java.lang.reflect.Method getWindow = gfx.getClass().getMethod("getWindow");
            desktopWindow = getWindow.invoke(gfx);

            // Cache reflection methods for reuse in touchDragged
            getPosX = desktopWindow.getClass().getMethod("getPositionX");
            getPosY = desktopWindow.getClass().getMethod("getPositionY");
            setPos = desktopWindow.getClass().getMethod("setPosition", int.class, int.class);

            // Get current window position
            dragWinX = (Integer) getPosX.invoke(desktopWindow);
            dragWinY = (Integer) getPosY.invoke(desktopWindow);

            // Calculate absolute mouse position (window position + relative mouse position)
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
        if (!draggingWindow || desktopWindow == null || getPosX == null || getPosY == null || setPos == null)
            return;

        try {
            // Get current window position using cached methods
            int currentWinX = (Integer) getPosX.invoke(desktopWindow);
            int currentWinY = (Integer) getPosY.invoke(desktopWindow);

            // Calculate current absolute mouse position
            int curMouseX = currentWinX + Gdx.input.getX();
            int curMouseY = currentWinY + Gdx.input.getY();

            // Calculate offset from initial drag position
            int dx = curMouseX - dragMouseX;
            int dy = curMouseY - dragMouseY;

            // Set new window position using cached method
            int newWinX = dragWinX + dx;
            int newWinY = dragWinY + dy;
            setPos.invoke(desktopWindow, newWinX, newWinY);
        } catch (Exception e) {
            // Ignore reflection errors
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
