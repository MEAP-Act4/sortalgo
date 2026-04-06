package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {
    final PongBreakoutFusion game;
    
    OrthographicCamera camera;
    Viewport viewport;
    Vector3 touchPoint; 
    
    Texture background;
    Texture metallicButtonImage; 
    Music themeSong;
    
    // Bounds rectangles for 4 buttons now
    Rectangle singlePlayerBounds;
    Rectangle multiPlayerBounds;
    Rectangle leaderboardBounds; 
    Rectangle instructionsBounds; 
  
    float stateTime; 
    float bgScrollX = 0f;
    float bgScrollY = 0f;

    public MainMenuScreen(final PongBreakoutFusion game) {
        this.game = game;
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        touchPoint = new Vector3();
        
        background = new Texture(Gdx.files.internal("game_bg.png"));
        metallicButtonImage = new Texture(Gdx.files.internal("metallic_button.png")); 
        
        themeSong = Gdx.audio.newMusic(Gdx.files.internal("theme.wav")); 
        themeSong.setLooping(true); 
        themeSong.setVolume(0.5f);  
        themeSong.play();
        
        float btnW = 360; 
        float btnH = 50; // Made slightly thinner to fit 4 buttons
        
        // Stacked vertically
        singlePlayerBounds = new Rectangle(140, 220, btnW, btnH); 
        multiPlayerBounds = new Rectangle(140, 160, btnW, btnH);
        leaderboardBounds = new Rectangle(140, 100, btnW, btnH); 
        instructionsBounds = new Rectangle(140, 40, btnW, btnH); 
    }

    @Override
    public void render(float delta) {
        stateTime += delta; 
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        float mouseX = touchPoint.x;
        float mouseY = touchPoint.y;

        bgScrollX -= 30f * delta;
        bgScrollY -= 30f * delta;
        if (bgScrollX <= -640f) bgScrollX += 640f;
        if (bgScrollY <= -480f) bgScrollY += 480f;

        float floatY = MathUtils.sin(stateTime * 2f) * 10f; 

        game.batch.begin();
        
        game.batch.setColor(Color.WHITE);
        game.batch.draw(background, bgScrollX, bgScrollY, 640, 480);
        game.batch.draw(background, bgScrollX + 640, bgScrollY, 640, 480);
        game.batch.draw(background, bgScrollX, bgScrollY + 480, 640, 480);
        game.batch.draw(background, bgScrollX + 640, bgScrollY + 480, 640, 480);
        
        float titleScale = 1.2f + Math.abs(MathUtils.sin(stateTime * 3f)) * 0.1f;
        game.font.getData().setScale(titleScale);
        game.font.setColor(Color.ORANGE);
        game.font.draw(game.batch, "SORTALGO", 0, 380 + floatY, 640, Align.center, false);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "BREAKOUT & PONG", 0, 330 + floatY, 640, Align.center, false);

        // Draw 4 Animated Buttons
        drawAnimatedButton(singlePlayerBounds, mouseX, mouseY, "1 PLAYER (vs AI)");
        drawAnimatedButton(multiPlayerBounds, mouseX, mouseY, "2 PLAYERS (Local)");
        drawAnimatedButton(leaderboardBounds, mouseX, mouseY, "LEADERBOARD");
        drawAnimatedButton(instructionsBounds, mouseX, mouseY, "TUTORIAL");

        game.font.getData().setScale(1.0f); 
        game.batch.end();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (singlePlayerBounds.contains(mouseX, mouseY)) {
                themeSong.stop(); 
                game.setScreen(new NameEntryScreen(game, true)); 
                dispose(); 
            } else if (multiPlayerBounds.contains(mouseX, mouseY)) {
                themeSong.stop(); 
                game.setScreen(new NameEntryScreen(game, false)); 
                dispose(); 
            } else if (leaderboardBounds.contains(mouseX, mouseY)) {
                themeSong.stop(); 
                game.setScreen(new LeaderboardScreen(game));
                dispose();
            } else if (instructionsBounds.contains(mouseX, mouseY)) {
                themeSong.stop(); 
                game.setScreen(new TutorialScreen(game));
                dispose();
            }
        }
        }

    private void drawAnimatedButton(Rectangle bounds, float mouseX, float mouseY, String text) {
        boolean isHovered = bounds.contains(mouseX, mouseY);
        float scale = isHovered ? 1.15f + (MathUtils.sin(stateTime * 15f) * 0.05f) : 1.0f;
        float drawWidth = bounds.width * scale;
        float drawHeight = bounds.height * scale;
        float drawX = bounds.x - (drawWidth - bounds.width) / 2f;
        float drawY = bounds.y - (drawHeight - bounds.height) / 2f;

        if (isHovered && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            game.batch.setColor(Color.DARK_GRAY); 
        } else if (isHovered) {
            float pulse = (MathUtils.sin(stateTime * 8f) + 1f) / 2f;
            game.batch.setColor(0.8f, 0.9f + (pulse * 0.1f), 1f, 1f);
        } else {
            game.batch.setColor(Color.WHITE); 
        }
        
        game.batch.draw(metallicButtonImage, drawX, drawY, drawWidth, drawHeight);
        game.font.getData().setScale(scale * 0.7f); // Reduced font scale to fit thinner buttons
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, drawX, drawY + (drawHeight * 0.6f), drawWidth, Align.center, false);
        game.font.getData().setScale(1.0f); 
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        background.dispose();
        metallicButtonImage.dispose(); 
        themeSong.dispose(); 
    }
}