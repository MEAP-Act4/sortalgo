package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TutorialScreen implements Screen {
    final PongBreakoutFusion game;

    OrthographicCamera camera;
    Viewport viewport;

    Texture background;
    Texture paddleImage;
    Texture ballImage;
    Texture brickimage;

    // --- Live Background Demo Variables ---
    Rectangle demoPaddle1;
    Rectangle demoPaddle2;
    Rectangle demoBall;
    float ballSpeedX = 300f;
    float ballSpeedY = 250f;
    Array<Rectangle> displayBricks;

    float animTimer = 0f;
    
    // --- NEW: Scroll Variable ---
    float textScrollOffset = 0f;

    public TutorialScreen(final PongBreakoutFusion game) {
        this.game = game;
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);

        background = new Texture(Gdx.files.internal("game_bg.png"));
        paddleImage = new Texture(Gdx.files.internal("paddle.png"));
        ballImage = new Texture(Gdx.files.internal("ball.png"));
        brickimage = new Texture(Gdx.files.internal("brick.png"));
        
        // Setup a full AI vs AI match for the background demo
        demoPaddle1 = new Rectangle(20, 200, 30, 90);
        demoPaddle2 = new Rectangle(590, 200, 30, 90);
        demoBall = new Rectangle(300, 240, 30, 30);
        
        // Setup center blocks for the demo
        displayBricks = new Array<>();
        float[] lanes = {280, 320};
        for (float x : lanes) {
            for (int y = 80; y < 400; y += 40) {
                displayBricks.add(new Rectangle(x, y, 30, 30));
            }
        }
    }

    @Override
    public void render(float delta) {
        animTimer += delta;
        
        // --- NEW: Handle Scrolling Input ---
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            textScrollOffset += 300f * delta; // Scroll down (pushes text up)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            textScrollOffset -= 300f * delta; // Scroll up (pushes text down)
        }
        // Lock the scroll so it doesn't go too far up or down
        textScrollOffset = MathUtils.clamp(textScrollOffset, 0f, 350f); 
        
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // ==========================================
        // 1. UPDATE THE BACKGROUND DEMO (AI vs AI)
        // ==========================================
        demoBall.x += ballSpeedX * delta;
        demoBall.y += ballSpeedY * delta;

        if (demoBall.y <= 0 || demoBall.y >= 480 - demoBall.height) {
            ballSpeedY *= -1;
            demoBall.y = Math.max(0, Math.min(demoBall.y, 480 - demoBall.height));
        }

        if (demoBall.overlaps(demoPaddle1)) {
            ballSpeedX = Math.abs(ballSpeedX);
            demoBall.x = demoPaddle1.x + demoPaddle1.width;
        }
        if (demoBall.overlaps(demoPaddle2)) {
            ballSpeedX = -Math.abs(ballSpeedX);
            demoBall.x = demoPaddle2.x - demoBall.width;
        }

        if (demoBall.x < -50 || demoBall.x > 700) {
            demoBall.setPosition(300, 240);
        }

        float p1Center = demoPaddle1.y + (demoPaddle1.height / 2);
        if (p1Center < demoBall.y - 10) demoPaddle1.y += 280f * delta;
        if (p1Center > demoBall.y + 10) demoPaddle1.y -= 280f * delta;

        float p2Center = demoPaddle2.y + (demoPaddle2.height / 2);
        if (p2Center < demoBall.y - 10) demoPaddle2.y += 280f * delta;
        if (p2Center > demoBall.y + 10) demoPaddle2.y -= 280f * delta;

        demoPaddle1.y = MathUtils.clamp(demoPaddle1.y, 0, 480 - demoPaddle1.height);
        demoPaddle2.y = MathUtils.clamp(demoPaddle2.y, 0, 480 - demoPaddle2.height);

        // ==========================================
        // 2. DRAW EVERYTHING
        // ==========================================
        game.batch.begin();
        
        // Draw the background very dark
        game.batch.setColor(0.15f, 0.15f, 0.25f, 1f); 
        game.batch.draw(background, 0, 0, 640, 480);

        // Draw Demo Assets
        game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        game.batch.draw(paddleImage, demoPaddle1.x, demoPaddle1.y, demoPaddle1.width, demoPaddle1.height);
        game.batch.draw(paddleImage, demoPaddle2.x, demoPaddle2.y, demoPaddle2.width, demoPaddle2.height);
        game.batch.draw(ballImage, demoBall.x, demoBall.y, demoBall.width, demoBall.height);
        for (Rectangle brick : displayBricks) {
            game.batch.draw(brickimage, brick.x, brick.y, brick.width, brick.height);
        }
        
        // --- DRAW SCROLLING TEXT ---
        game.batch.setColor(Color.WHITE); 
        
        // We use textScrollOffset to dynamically shift everything up/down
        float startY = 370 + textScrollOffset; 
        
        // Larger readable text scales
        float headerScale = 0.6f;
        float bodyScale = 0.5f;
        
        // OBJECTIVE
        game.font.getData().setScale(headerScale);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "OBJECTIVE:", 20, startY);
        
        game.font.getData().setScale(bodyScale);
        game.font.draw(game.batch, "Break bricks and slip the ball past", 20, startY - 30);
        game.font.draw(game.batch, "your opponent to score points!", 20, startY - 60);
        game.font.draw(game.batch, "The highest score at time-up WINS.", 20, startY - 90);
        
        // CONTROLS
        game.font.getData().setScale(headerScale);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "CONTROLS:", 20, startY - 140);
        
        game.font.getData().setScale(bodyScale);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "PLAYER 1: W/S Keys  | SPACE to Launch", 20, startY - 170);
        game.font.draw(game.batch, "PLAYER 2: UP/DOWN   | ENTER to Launch", 20, startY - 200);
        
        // POWERUPS
        game.font.getData().setScale(headerScale);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "SORTING POWER-UPS:", 20, startY - 260);
        
        game.font.getData().setScale(bodyScale);
        float pUpY = startY - 295;
        float spacing = 35; 
        
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "[SELECTION] Destroys weakest brick", 20, pUpY);
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "[INSERTION] Spawns extra multi-balls", 20, pUpY - spacing);
        game.font.setColor(Color.GREEN);
        game.font.draw(game.batch, "[BUBBLE] Reverses moving walls", 20, pUpY - (spacing * 2));
        game.font.setColor(Color.ORANGE);
        game.font.draw(game.batch, "[MERGE] Deletes random bricks instantly", 20, pUpY - (spacing * 3));
        game.font.setColor(Color.PURPLE);
        game.font.draw(game.batch, "[QUICK] Ball becomes a piercing fireball", 20, pUpY - (spacing * 4));


        // --- DRAW TOP AND BOTTOM BANNERS OVER THE SCROLLING TEXT ---
        // This hides text that scrolls off the top or bottom!
        game.batch.setColor(0.1f, 0.1f, 0.15f, 0.95f); // Near solid dark blue/black
        game.batch.draw(background, 0, 400, 640, 80); // Top Banner
        game.batch.draw(background, 0, 0, 640, 90);   // Bottom Banner
        
        // TOP BANNER TEXT (Locked in place)
        game.font.getData().setScale(0.8f); 
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "HOW TO PLAY", 0, 455, 640, Align.center, false);

        // BOTTOM BANNER TEXT (Locked in place)
        float pulse = (MathUtils.sin(animTimer * 5f) + 1f) / 2f;
        game.font.getData().setScale(0.4f);
        game.font.setColor(Color.LIGHT_GRAY);
        game.font.draw(game.batch, "Use UP and DOWN Arrows to scroll", 0, 75, 640, Align.center, false);

        game.font.setColor(0.8f, 0.8f + (pulse * 0.2f), 1f, 1f); 
        game.font.getData().setScale(0.55f + (pulse * 0.05f));
        game.font.draw(game.batch, "Press ENTER to Return", 0, 40, 640, Align.center, false);

        game.font.getData().setScale(1.0f); // Reset scale safely
        game.batch.end();

        // ==========================================
        // 3. EXIT LOGIC
        // ==========================================
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        paddleImage.dispose();
        ballImage.dispose();
        brickimage.dispose();
    }
}