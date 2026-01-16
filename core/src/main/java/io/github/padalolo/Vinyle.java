package io.github.padalolo;

import com.badlogic.gdx.graphics.Texture;
import com.kotcrab.vis.ui.widget.VisImage;

public class Vinyle extends VisImage {
    
    private Album album;
    private Texture circleTexture;
    private boolean isRotating;
    private float rotationSpeed;
    private float currentRotation;

    private float x;
    private float y;
    
    public Vinyle(Texture texture, float x, float y) {
        super(texture);
        this.circleTexture = texture;
        this.album = null;
        this.x=x;
        this.y=y;
        this.isRotating = false;
        this.rotationSpeed = 0.07f;
        this.currentRotation = 0f;
        setupOrigin();
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

    public float getXx() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getYy() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
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
    
    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album=album;
    }

    public Texture getCircleTexture() {
        return circleTexture;
    }

    public void resetRotation() {
        this.currentRotation = 0f;
        setRotation(0f);
    }
    
    /**
     * Configure l'origine de rotation au centre du vinyle
     */
    private void setupOrigin() {
        // L'origine sera mise à jour quand les dimensions seront connues
    }
    
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        // Met à jour l'origine de rotation au centre quand la taille change
        setOrigin(getWidth() / 2f, getHeight() / 2f);
    }
}