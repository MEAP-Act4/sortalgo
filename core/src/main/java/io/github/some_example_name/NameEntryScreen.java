package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

// We extend InputAdapter so this screen can directly listen to keyboard typing!
public class NameEntryScreen extends InputAdapter implements Screen {
    final PongBreakoutFusion game;
    OrthographicCamera camera;
    Viewport viewport;
    Texture background;

    boolean isSinglePlayer;
    boolean enteringPlayer2 = false;

    String player1Name = "";
    String player2Name = "";

    float stateTime = 0f;

    public NameEntryScreen(final PongBreakoutFusion game, boolean isSinglePlayer) {
        this.game = game;
        this.isSinglePlayer = isSinglePlayer;
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        background = new Texture(Gdx.files.internal("game_bg.png"));
    }

    @Override
    public void show() {
        // Tell LibGDX to send keyboard strokes directly to this screen
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public boolean keyTyped(char character) {
        String currentName = enteringPlayer2 ? player2Name : player1Name;

        // Handle Backspace
        if (character == '\b' && currentName.length() > 0) {
            currentName = currentName.substring(0, currentName.length() - 1);
        } 
        // Handle Enter Key (Confirm)
        else if (character == '\r' || character == '\n') {
            if (currentName.trim().isEmpty()) {
                currentName = enteringPlayer2 ? "Player 2" : "Player 1";
            }
            
            if (!isSinglePlayer && !enteringPlayer2) {
                // Move on to Player 2
                player1Name = currentName;
                enteringPlayer2 = true;
            } else {
                // Finished! Launch the GameScreen
                if (enteringPlayer2) player2Name = currentName;
                else player1Name = currentName;

                String p2Final = isSinglePlayer ? "The AI" : player2Name;
                game.setScreen(new GameScreen(game, player1Name, p2Final, isSinglePlayer));
            }
            return true;
        } 
        // Handle normal typing (Limit to 12 characters, letters/numbers/spaces only)
        else if (currentName.length() < 12 && (Character.isLetterOrDigit(character) || character == ' ')) {
            currentName += character;
        }

        // Save back to the correct variable
        if (enteringPlayer2) player2Name = currentName;
        else player1Name = currentName;

        return true;
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        // Dimmed background
        game.batch.setColor(0.3f, 0.3f, 0.4f, 1f); 
        game.batch.draw(background, 0, 0, 640, 480);
        game.batch.setColor(Color.WHITE);

        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "PLAYER SETUP", 0, 400, 640, Align.center, false);

        // Blinking cursor logic
        String cursor = (stateTime % 1.0f < 0.5f) ? "_" : "";
        
        game.font.getData().setScale(0.8f);
        game.font.setColor(Color.CYAN);

        if (!enteringPlayer2) {
            game.font.draw(game.batch, "Enter Player 1 Name:", 0, 280, 640, Align.center, false);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, player1Name + cursor, 0, 230, 640, Align.center, false);
        } else {
            game.font.draw(game.batch, "Enter Player 2 Name:", 0, 280, 640, Align.center, false);
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, player2Name + cursor, 0, 230, 640, Align.center, false);
        }

        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "Type your name and press ENTER to confirm.", 0, 80, 640, Align.center, false);

        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    
    @Override 
    public void hide() { 
        // Stop listening to keyboard when we leave this screen
        Gdx.input.setInputProcessor(null); 
    }
    
    @Override 
    public void dispose() { background.dispose(); }
}