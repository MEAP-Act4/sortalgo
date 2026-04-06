package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class PongBreakoutFusion extends Game {
    public SpriteBatch batch;
    public BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        
        // --- ENHANCED FONT GENERATION ---
        // 1. Load the .ttf file from your assets folder
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("arcade.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        
        // 2. Customize your font settings!
        parameter.size = 17; // Font size in pixels
        parameter.color = Color.YELLOW;
        parameter.borderWidth = 2; // Gives the text a nice arcade outline
        parameter.borderColor = Color.BLACK;
        parameter.shadowOffsetX = 3; // Adds a drop shadow
        parameter.shadowOffsetY = 3;
        parameter.shadowColor = new Color(0, 0, 0, 0.5f); // Semi-transparent black shadow
        
        // 3. Generate the font and clean up
        font = generator.generateFont(parameter);
        generator.dispose(); // VERY IMPORTANT: Dispose the generator when done to prevent memory leaks
        
        // Go to your MainMenuScreen (or wherever your game starts)
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // Important! This tells the Game class to render the active screen
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}