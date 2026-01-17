package io.github.padalolo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.kotcrab.vis.ui.widget.VisImage;

public class Vinyle extends VisImage {
    
    private int albumIndex;
    private boolean isRotating;
    private float rotationSpeed;
    private float currentRotation;
    
    public Vinyle(Texture texture, float x, float y) {
        super(texture);
        this.albumIndex = -1;
        this.isRotating = false;
        this.rotationSpeed = 0.07f;
        this.currentRotation = 0f;
    }
    
    @Override
    public void act(float deltaTime) {
        super.act(deltaTime);
        
        if (isRotating) {
            currentRotation -= rotationSpeed * deltaTime * 360; // 360 degrés par seconde * vitesse
            if (currentRotation >= 360) {
                currentRotation -= 360;
            }
            setRotation(currentRotation);
        }
    }

    public int getAlbumIndex() {
        return albumIndex;
    }

    public void setAlbumIndex(int albumIndex) {
        this.albumIndex = albumIndex;
    }
    
    public void startRotation() {
        this.isRotating = true;
    }
    
    public void stopRotation() {
        this.isRotating = false;
    }
    
    public boolean isRotating() {
        return isRotating;
    }
    
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void resetRotation() {
        this.currentRotation = 0f;
        setRotation(0f);
    }
    
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        // Met à jour l'origine de rotation au centre quand la taille change
        setOrigin(getWidth() / 2f, getHeight() / 2f);
    }
    
    /**
     * Anime le vinyle vers une nouvelle position avec un effet de glissement
     * @param targetX Position X cible
     * @param targetY Position Y cible
     * @param duration Durée de l'animation en secondes
     */
    public void animateToPosition(float targetX, float targetY, float duration) {
        // Annule toutes les actions en cours sur ce vinyle
        clearActions();
        
        // Crée une action de mouvement avec interpolation pour un effet fluide
        addAction(Actions.moveTo(targetX, targetY, duration, com.badlogic.gdx.math.Interpolation.smooth));
    }
}