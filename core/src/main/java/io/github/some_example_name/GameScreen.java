package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils; 
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {
    final PongBreakoutFusion game;
    String player1Name;
    String player2Name;
    boolean isSinglePlayer;

    // --- Game State Management ---
    // Added ASKING_NAMES state
   enum GameState { PLAYING, PAUSED, SETTINGS, MATCH_OVER }
GameState gameState = GameState.PLAYING;

    OrthographicCamera camera;
    Viewport viewport;
    ShapeRenderer shape;
    Texture paddleImage;
    Texture background;
    
    // --- Animated Ball Variables ---
    Texture ballSpriteSheet;
    Animation<TextureRegion> ballAnimation;
    float stateTime;
    
    Color currentBgColor;
    Color targetBgColor;
    float colorChangeTimer = 0f;
    float colorChangeDuration = 3f;
    
    Texture brickSheet; 
    TextureRegion[][] brickRegions;

    Sound winSound;
    Sound loseSound;
    Sound hitSound;
    Sound spawnSound;
    Sound scoreSound;
    Sound serveSound;
    Music bgMusic;

    // --- Settings Variables ---
    float musicVolume = 0.4f;
    float soundVolume = 1.0f;
    int matchTimerMinutes = 3; 
    int currentSettingsIndex = 0;

    int currentLevel = 1;
    float levelTimer;
    
    int p1Ammo = 1;
    int p2Ammo = 1;
    float p2LaunchTimer = 0f; 

    float aiBaseSpeed = 80f; 
    float aiMaxSpeed = 400f; 

    float playerPaddleSpeed = 350f; 

    float blockSpawnTimer = 0f;
    float blockMoveSpeed;
    float blockMoveDirection = 1f;

    Rectangle p1Paddle;
    Rectangle p2Paddle;
    int p1Score = 0;
    int p2Score = 0;

    Array<GameBall> balls;
    Array<GameBall> ballsToAdd;
    Array<Brick> bricks;
    Array<Brick> bricksToAdd;
    Array<FloatingText> floatingTexts;

    float bgScrollY = 0f;
    float shakeTimer = 0f;
    float shakeIntensity = 0f;
    float freezeTimer = 0f;
    int p1ComboMultiplier = 1;
    
    float animTimer = 0f; 

    Preferences prefs;

    class FloatingText {
        String text; float x, y; Color color; float lifeTime = 0f; float maxLifeTime; float baseScale; float velocityY;
        public FloatingText(String text, float x, float y, Color color, float maxLifeTime, float baseScale, float velocityY) {
            this.text = text; this.x = x; this.y = y; this.color = new Color(color); 
            this.maxLifeTime = maxLifeTime; this.baseScale = baseScale; this.velocityY = velocityY;
        }
    }

    class GameBall {
        Rectangle rect; float speedX; float speedY; int owner; int lastHitBy;
        boolean isQuickSort = false; float quickSortTimer = 0f;
        public GameBall(float x, float y, float sX, float sY, int owner) {
            this.rect = new Rectangle(x, y, 30, 30);
            this.speedX = sX; this.speedY = sY; this.owner = owner; this.lastHitBy = owner;
        }
    }

    class Brick {
        Rectangle rect; int health; Color color = Color.WHITE; boolean isPowerUp = false; int powerUpType = -1; float glowTimer = 0f;
        float lifeTime; float spawnScale; float hitPunchScale; 
        TextureRegion currentRegion; int sheetRow; int sheetCol;

        public Brick(float x, float y, float width, float height, int health) {
            this.rect = new Rectangle(x, y, width, height);
            this.health = health;
            this.lifeTime = (float)(Math.random() * 10f); this.spawnScale = 0f; this.hitPunchScale = 0f;

            if (Math.random() < 0.20f) {
                isPowerUp = true; powerUpType = (int) (Math.random() * 5); 
            }
            sheetRow = (int)(Math.random() * 6);
            sheetCol = ((int)(Math.random() * 3)) * 2; 
            updateTexture();
        }

        public void updateTexture() {
            if (health > 1) { currentRegion = brickRegions[sheetRow][sheetCol]; } 
            else { currentRegion = brickRegions[sheetRow][sheetCol + 1]; }

            if (isPowerUp) {
                if (powerUpType == 0) color = Color.YELLOW; else if (powerUpType == 1) color = Color.CYAN; 
                else if (powerUpType == 2) color = Color.GREEN; else if (powerUpType == 3) color = Color.ORANGE; 
                else if (powerUpType == 4) color = Color.PURPLE; 
            } else { color = Color.WHITE; }
        }
    }

    public GameScreen(final PongBreakoutFusion game, String p1Name, String p2Name, boolean isSinglePlayer) {
        this.game = game;
        this.isSinglePlayer = isSinglePlayer;
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);

        shape = new ShapeRenderer();
        paddleImage = new Texture(Gdx.files.internal("paddle.png"));
        background = new Texture(Gdx.files.internal("game_bg.png"));
        
        // --- Animated Ball Setup ---
        ballSpriteSheet = new Texture(Gdx.files.internal("frozen-alien-space-planet-cartoon-fantasy-set.png"));
        int frameWidth = ballSpriteSheet.getWidth() / 4;
        int frameHeight = ballSpriteSheet.getHeight() / 3;
        TextureRegion[][] tmp = TextureRegion.split(ballSpriteSheet, frameWidth, frameHeight);
        TextureRegion[] animationFrames = new TextureRegion[4 * 3];
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                animationFrames[index++] = tmp[i][j];
            }
        }
        ballAnimation = new Animation<TextureRegion>(0.1f, animationFrames);
        stateTime = 0f;
        
        currentBgColor = new Color(MathUtils.random(0.2f, 1f), MathUtils.random(0.2f, 1f), MathUtils.random(0.2f, 1f), 1f);
        targetBgColor = new Color(MathUtils.random(0.2f, 1f), MathUtils.random(0.2f, 1f), MathUtils.random(0.2f, 1f), 1f);
        
        brickSheet = new Texture(Gdx.files.internal("brick_sheet.png"));
        int brickW = brickSheet.getWidth() / 6;
        int brickH = brickSheet.getHeight() / 6;
        brickRegions = TextureRegion.split(brickSheet, brickW, brickH);

        winSound = Gdx.audio.newSound(Gdx.files.internal("win.mp3"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("lose.mp3"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));
        spawnSound = Gdx.audio.newSound(Gdx.files.internal("spawn.mp3"));
        scoreSound = Gdx.audio.newSound(Gdx.files.internal("score.mp3"));
        serveSound = Gdx.audio.newSound(Gdx.files.internal("serve.mp3"));
        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("bg_music.wav"));

        bgMusic.setLooping(true);
        bgMusic.setVolume(musicVolume);
        bgMusic.play();

        p1Paddle = new Rectangle(20, 200, 30, 90);
        p2Paddle = new Rectangle(590, 200, 30, 90);

        balls = new Array<>(); ballsToAdd = new Array<>();
        bricks = new Array<>(); bricksToAdd = new Array<>();
        floatingTexts = new Array<>(); 

        startLevel(1);
    }

    private void triggerShake(float duration, float intensity) { shakeTimer = duration; shakeIntensity = intensity; }
    private void triggerFreeze(float duration) { freezeTimer = duration; }

    private void spawnFloatingText(String text, float x, float y, Color color, float scale) {
        floatingTexts.add(new FloatingText(text, x, y, color, 1.0f, scale, 50f));
    }

    private void playSound(Sound sound) { sound.play(soundVolume); }

    // --- NEW: BUBBLE SORT Leaderboard Saving Algorithm ---
  private void checkAndSaveHighScore() {
        // 1. Save Player 1's score if they actually scored points
        if (p1Score > 0) {
            LeaderboardManager.updateLeaderboard(player1Name, p1Score);
        }
        // Note: The Bucket Sort logic is handled inside LeaderboardManager.updateLeaderboard
    }

    private void startLevel(int level) {
        currentLevel = level;
        if (level == 1) { p1Score = 0; p2Score = 0; p1ComboMultiplier = 1; }
        
        p1Ammo = 1; p2Ammo = 1; 
        balls.clear(); bricks.clear(); floatingTexts.clear();
        spawnInitialBricks();

        int activeLines = getActiveBrickLines();
        if (activeLines >= 3) { levelTimer = 120f; } else { levelTimer = 60f; }

        blockMoveSpeed = 10f + (level * 5f); 
        
        // Do not change state to PLAYING here, it is handled by the name popup!
        gameState = GameState.PLAYING;

        spawnFloatingText("LEVEL " + currentLevel + " START!", 320, 240, Color.YELLOW, 2.0f);
    }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameState == GameState.PLAYING) gameState = GameState.PAUSED;
            else if (gameState == GameState.PAUSED) gameState = GameState.PLAYING;
            else if (gameState == GameState.SETTINGS) gameState = GameState.PAUSED;
        }

        if (gameState == GameState.PAUSED) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.O)) gameState = GameState.SETTINGS;
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                game.setScreen(new MainMenuScreen(game)); dispose(); return;
            }
        }

        if (gameState == GameState.SETTINGS) handleSettingsInput();

        if (gameState == GameState.MATCH_OVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) startLevel(currentLevel + 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                game.setScreen(new MainMenuScreen(game)); dispose(); return;
            }
        }

        if (gameState == GameState.PLAYING) {
            if (freezeTimer > 0) { freezeTimer -= delta; } 
            else { animTimer += delta; updateGame(delta); }
        }

        ScreenUtils.clear(Color.BLACK);

        float centerX = 320f; float centerY = 240f;
        if (shakeTimer > 0 && gameState == GameState.PLAYING) {
            shakeTimer -= delta;
            camera.position.x = centerX + (float)(Math.random() - 0.5) * shakeIntensity;
            camera.position.y = centerY + (float)(Math.random() - 0.5) * shakeIntensity;
        } else {
            camera.position.set(centerX, centerY, 0);
        }
        
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        bgScrollY += 50f * delta;
        if (bgScrollY > 480f) bgScrollY -= 480f; 

        colorChangeTimer += delta;
        float progress = colorChangeTimer / colorChangeDuration;
        currentBgColor.lerp(targetBgColor, progress * 0.05f); 
        if (colorChangeTimer >= colorChangeDuration) {
            colorChangeTimer = 0f;
            targetBgColor.set(MathUtils.random(0.2f, 1f), MathUtils.random(0.2f, 1f), MathUtils.random(0.2f, 1f), 1f);
        }

        game.batch.begin();
        game.batch.setColor(currentBgColor);
        game.batch.draw(background, 0, -bgScrollY, 640, 480);
        game.batch.draw(background, 0, 480 - bgScrollY, 640, 480);
        game.batch.setColor(Color.WHITE);

        for (Brick brick : bricks) {
            brick.lifeTime += delta;
            if (brick.spawnScale < 1f) {
                brick.spawnScale += delta * 4f; 
                if (brick.spawnScale > 1f) brick.spawnScale = 1f;
            }
            if (brick.hitPunchScale > 0f) {
                brick.hitPunchScale -= delta * 5f;
                if (brick.hitPunchScale < 0f) brick.hitPunchScale = 0f;
            }

            float breathe = (float)Math.sin(brick.lifeTime * 4f) * 0.05f; 
            float currentScale = brick.spawnScale + breathe + brick.hitPunchScale;
            float drawWidth = brick.rect.width * currentScale * 1.35f;
            float drawHeight = brick.rect.height * currentScale * 1.35f;
            float drawX = brick.rect.x - (drawWidth - brick.rect.width) / 2f;
            float drawY = brick.rect.y - (drawHeight - brick.rect.height) / 2f;

            if (brick.isPowerUp) {
                brick.glowTimer += delta * 5f;
                float pulse = (float) Math.sin(brick.glowTimer) * 0.5f + 0.5f;
                game.batch.setColor(brick.color.r, brick.color.g, brick.color.b, 0.3f + (pulse * 0.5f));
                game.batch.draw(brick.currentRegion, drawX - 4, drawY - 4, drawWidth + 8, drawHeight + 8);
            }
            
            game.batch.setColor(brick.color);
            game.batch.draw(brick.currentRegion, drawX, drawY, drawWidth, drawHeight);
            game.batch.setColor(Color.WHITE);
        }

        game.batch.draw(paddleImage, p1Paddle.x, p1Paddle.y, p1Paddle.width, p1Paddle.height);
        game.batch.draw(paddleImage, p2Paddle.x, p2Paddle.y, p2Paddle.width, p2Paddle.height);

        // --- Draw Animated Ball ---
        if (gameState == GameState.PLAYING || gameState == GameState.MATCH_OVER) {
            stateTime += delta; 
        }
        TextureRegion currentBallFrame = ballAnimation.getKeyFrame(stateTime, true);

        if (p1Ammo > 0) game.batch.draw(currentBallFrame, p1Paddle.x + p1Paddle.width + 5, p1Paddle.y + (p1Paddle.height/2) - 15, 30, 30);
        if (p2Ammo > 0) game.batch.draw(currentBallFrame, p2Paddle.x - 35, p2Paddle.y + (p2Paddle.height/2) - 15, 30, 30);

        for (GameBall b : balls) {
            if (b.isQuickSort) {
                game.batch.setColor(Color.PURPLE);
                game.batch.draw(currentBallFrame, b.rect.x - (b.speedX * delta * 2), b.rect.y, b.rect.width, b.rect.height);
            }
            game.batch.setColor(Color.WHITE);
            game.batch.draw(currentBallFrame, b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        game.batch.end();

        // UI & OVERLAYS
        game.batch.begin();
        float breatheAnim = (float)Math.sin(animTimer * 5f) * 0.05f; 

        game.font.getData().setScale(0.9f + breatheAnim); 
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, player1Name + ": " + p1Score, 20, 465);
        if (p1ComboMultiplier > 1) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "Combo x" + p1ComboMultiplier, 20, 440);
        }
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, player2Name + ": " + p2Score, 0, 465, 620, Align.right, false);
        game.font.draw(game.batch, "LEVEL " + currentLevel, 0, 470, 640, Align.center, false);
        
        int minutes = (int)(levelTimer / 60);
        int seconds = (int)(levelTimer % 60);
        if (levelTimer <= 10f) game.font.setColor(Color.RED);
        game.font.draw(game.batch, String.format("TIME: %d:%02d", minutes, seconds), 0, 445, 640, Align.center, false);
        game.font.setColor(Color.WHITE); 

        game.font.getData().setScale(0.9f); 
        if (p1Ammo > 0) {
            game.font.setColor(Color.CYAN); game.font.draw(game.batch, "PRESS SPACE!", 20, 25);
        }
        if (p2Ammo > 0) {
            game.font.setColor(Color.CYAN); game.font.draw(game.batch, isSinglePlayer ? "CPU LAUNCHING..." : "PRESS ENTER!", 0, 25, 620, Align.right, false);
        }
        game.font.setColor(Color.WHITE);

        for (int i = floatingTexts.size - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            float popScale = Math.max(0.01f, ft.baseScale * (ft.lifeTime < 0.2f ? (ft.lifeTime / 0.2f) * 1.5f : 1f));
            float alpha = ft.lifeTime > ft.maxLifeTime - 0.3f ? Math.max(0f, (ft.maxLifeTime - ft.lifeTime) / 0.3f) : 1f;
            ft.color.a = alpha;
            game.font.setColor(ft.color);
            game.font.getData().setScale(popScale);
            game.font.draw(game.batch, ft.text, ft.x - 100, ft.y, 200, Align.center, false);
            
            if (gameState == GameState.PLAYING && freezeTimer <= 0) {
                ft.y += ft.velocityY * delta; ft.lifeTime += delta;
                if (ft.lifeTime >= ft.maxLifeTime) floatingTexts.removeIndex(i);
            }
        }
        if (gameState == GameState.PAUSED) {
            game.font.getData().setScale(1.5f); game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "PAUSED", 0, 300, 640, Align.center, false);
            game.font.getData().setScale(0.9f);
            game.font.draw(game.batch, "Press [ESCAPE] to Resume", 0, 240, 640, Align.center, false);
            game.font.draw(game.batch, "Press [O] for Settings", 0, 200, 640, Align.center, false);
            game.font.draw(game.batch, "Press [Q] to Quit to Main Menu", 0, 160, 640, Align.center, false);
        } 
        else if (gameState == GameState.SETTINGS) {
            game.font.getData().setScale(1.5f); game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "SETTINGS", 0, 360, 640, Align.center, false);
            game.font.getData().setScale(1.0f);
            
            String[] options = { "Match Time: " + matchTimerMinutes + " Mins", "Music Volume: " + (int)(musicVolume * 100) + "%", "Sound Volume: " + (int)(soundVolume * 100) + "%" };
            for (int i = 0; i < options.length; i++) {
                if (i == currentSettingsIndex) game.font.setColor(Color.CYAN); else game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, (i == currentSettingsIndex ? "> " : "") + options[i], 0, 280 - (i * 40), 640, Align.center, false);
            }
            game.font.setColor(Color.GRAY);
            game.font.draw(game.batch, "Use UP/DOWN to select, LEFT/RIGHT to change.", 0, 120, 640, Align.center, false);
            game.font.draw(game.batch, "Press [ESCAPE] to Return", 0, 80, 640, Align.center, false);
        }
        else if (gameState == GameState.MATCH_OVER) {
            game.font.getData().setScale(1.5f); game.font.setColor(Color.RED);
            game.font.draw(game.batch, "TIME'S UP!", 0, 340, 640, Align.center, false);
            
            game.font.getData().setScale(1.0f); game.font.setColor(Color.YELLOW);
            String winner = (p1Score > p2Score) ? player1Name + " WINS!" : (p2Score > p1Score ? player2Name + " WINS!" : "IT'S A TIE!");
            game.font.draw(game.batch, winner, 0, 280, 640, Align.center, false);
            
            game.font.getData().setScale(0.65f); game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Press [ENTER] to Continue to Next Level", 0, 200, 640, Align.center, false);
            game.font.draw(game.batch, "Press [Q] to Quit to Main Menu", 0, 160, 640, Align.center, false);
        }

        game.font.getData().setScale(1.0f); game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    private void handleSettingsInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) { currentSettingsIndex = (currentSettingsIndex + 1) % 3; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) { currentSettingsIndex = (currentSettingsIndex - 1 + 3) % 3; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (currentSettingsIndex == 0 && matchTimerMinutes > 1) matchTimerMinutes--;
            if (currentSettingsIndex == 1 && musicVolume > 0f) { musicVolume -= 0.1f; bgMusic.setVolume(musicVolume); }
            if (currentSettingsIndex == 2 && soundVolume > 0f) soundVolume -= 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (currentSettingsIndex == 0 && matchTimerMinutes < 10) matchTimerMinutes++;
            if (currentSettingsIndex == 1 && musicVolume < 1.0f) { musicVolume += 0.1f; bgMusic.setVolume(musicVolume); }
            if (currentSettingsIndex == 2 && soundVolume < 1.0f) soundVolume += 0.1f;
        }
    }

  private void updateGame(float delta) {
        levelTimer -= delta;
        if (levelTimer <= 0) {
            checkAndSaveHighScore(); 
            gameState = GameState.MATCH_OVER; 
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) p1Paddle.y += playerPaddleSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) p1Paddle.y -= playerPaddleSpeed * delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && p1Ammo > 0) {
            launchNewBall(1); p1Ammo = 0; spawnFloatingText("LAUNCH!", p1Paddle.x + 50, p1Paddle.y + 45, Color.WHITE, 0.8f);
        }

        if (isSinglePlayer) {
            if (p2Ammo > 0) {
                p2LaunchTimer += delta;
                if (p2LaunchTimer > 1.2f) { launchNewBall(2); p2Ammo = 0; p2LaunchTimer = 0f; }
            }

            float currentAISpeed = Math.min(aiMaxSpeed, aiBaseSpeed + (currentLevel * 40f));
            float aiTargetY = 240f; GameBall targetBall = null; float minTime = 9999f;

            for (GameBall b : balls) {
                if (b.speedX > 0) { 
                    float timeToReach = (p2Paddle.x - b.rect.x) / b.speedX;
                    if (timeToReach > 0 && timeToReach < minTime) { minTime = timeToReach; targetBall = b; }
                }
            }

            if (targetBall != null) {
                float timeToReach = (p2Paddle.x - targetBall.rect.x) / targetBall.speedX;
                float predictedY = targetBall.rect.y + (targetBall.speedY * timeToReach);
                while (predictedY < 0 || predictedY > 480 - targetBall.rect.height) {
                    if (predictedY < 0) predictedY = Math.abs(predictedY);
                    if (predictedY > 480 - targetBall.rect.height) predictedY = 2 * (480 - targetBall.rect.height) - predictedY;
                }
                aiTargetY = predictedY;
            }

            float paddleCenter = p2Paddle.y + (p2Paddle.height / 2);
            if (paddleCenter < aiTargetY - 10) p2Paddle.y += currentAISpeed * delta;
            else if (paddleCenter > aiTargetY + 10) p2Paddle.y -= currentAISpeed * delta;
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) p2Paddle.y += playerPaddleSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) p2Paddle.y -= playerPaddleSpeed * delta;
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && p2Ammo > 0) { launchNewBall(2); p2Ammo = 0; }
        }

        if (p1Paddle.y < 0) p1Paddle.y = 0;
        if (p1Paddle.y > 480 - p1Paddle.height) p1Paddle.y = 480 - p1Paddle.height;
        if (p2Paddle.y < 0) p2Paddle.y = 0;
        if (p2Paddle.y > 480 - p2Paddle.height) p2Paddle.y = 480 - p2Paddle.height;

        boolean hitEdge = false;
        for (Brick b : bricks) {
            b.rect.y += blockMoveSpeed * blockMoveDirection * delta;
            if (b.rect.y < 0 || b.rect.y > 480 - b.rect.height) hitEdge = true;
        }
        if (hitEdge) blockMoveDirection *= -1; 

        blockSpawnTimer += delta;
        if (blockSpawnTimer >= 2.0f) {
            spawnSingleRandomBrick(); blockSpawnTimer = 0f;
        }

        for (int b = balls.size - 1; b >= 0; b--) {
            GameBall ball = balls.get(b);
            float speedMult = ball.isQuickSort ? 1.8f : 1.0f;
            ball.rect.x += ball.speedX * speedMult * delta;
            ball.rect.y += ball.speedY * speedMult * delta;
            
            if (ball.isQuickSort) {
                ball.quickSortTimer -= delta;
                if (ball.quickSortTimer <= 0) ball.isQuickSort = false;
            }

            if (ball.rect.y < 0 || ball.rect.y > 480 - ball.rect.height) {
                ball.speedY *= -1; ball.rect.y = Math.max(0, Math.min(ball.rect.y, 480 - ball.rect.height)); 
            }

            if (ball.rect.x < 0) {
                p2Score += 500; playSound(scoreSound); spawnFloatingText("+500", p2Paddle.x - 50, ball.rect.y, Color.RED, 1.2f); 
                if (ball.owner == 1) p1Ammo = 1; else p2Ammo = 1;
                balls.removeIndex(b); continue;
            }
            if (ball.rect.x > 640) {
                p1Score += 500; playSound(scoreSound); spawnFloatingText("+500", p1Paddle.x + 50, ball.rect.y, Color.GOLD, 1.2f); 
                if (ball.owner == 1) p1Ammo = 1; else p2Ammo = 1;
                balls.removeIndex(b); continue;
            }

            if (ball.rect.overlaps(p1Paddle)) {
                p1ComboMultiplier = 1; triggerShake(0.1f, 3f); ball.speedX = Math.abs(ball.speedX); ball.rect.x = p1Paddle.x + p1Paddle.width; ball.lastHitBy = 1;
            }
            if (ball.rect.overlaps(p2Paddle)) {
                triggerShake(0.1f, 3f); ball.speedX = -Math.abs(ball.speedX); ball.rect.x = p2Paddle.x - ball.rect.width; ball.lastHitBy = 2;
            }

            for (int i = bricks.size - 1; i >= 0; i--) {
                Brick brick = bricks.get(i);
                if (ball.rect.overlaps(brick.rect)) {
                    brick.health--; playSound(hitSound);
                    triggerFreeze(0.04f); triggerShake(0.1f, 6f);
                    brick.hitPunchScale = 0.4f;
                    
                    if (ball.lastHitBy == 1) {
                        int earned = 50 * p1ComboMultiplier; p1Score += earned; p1ComboMultiplier++; 
                        spawnFloatingText("+" + earned, brick.rect.x, brick.rect.y, Color.WHITE, 0.8f); 
                        if (p1ComboMultiplier % 5 == 0) spawnFloatingText("COMBO x" + p1ComboMultiplier + "!", 320, 300, Color.YELLOW, 1.5f); 
                    } else if (ball.lastHitBy == 2) { p2Score += 50; }

                    if (!ball.isQuickSort) ball.speedX *= -1;

                    if (brick.health <= 0) {
                        boolean wasPowerUp = brick.isPowerUp; int pType = brick.powerUpType;
                        bricks.removeIndex(i);
                        if (wasPowerUp) { activatePowerUp(pType, ball.lastHitBy, ball); }
                    } else {
                        brick.updateTexture(); 
                    }
                    break;
                }
            }
        }

        balls.addAll(ballsToAdd); ballsToAdd.clear();
        bricks.addAll(bricksToAdd); bricksToAdd.clear();
    }

    private int getActiveBrickLines() {
        Array<Float> uniqueLines = new Array<>();
        for (Brick b : bricks) {
            boolean found = false;
            for (float x : uniqueLines) { if (Math.abs(x - b.rect.x) < 10) { found = true; break; } }
            if (!found) uniqueLines.add(b.rect.x);
        }
        return uniqueLines.size;
    }

    private void launchNewBall(int playerNum) {
        float startX = (playerNum == 1) ? p1Paddle.x + p1Paddle.width + 5 : p2Paddle.x - 15;
        float startY = (playerNum == 1) ? p1Paddle.y + (p1Paddle.height / 2) : p2Paddle.y + (p2Paddle.height / 2);
        float baseSpeed = 250f; float levelMultiplier = 20f; 
        float speedX = (playerNum == 1) ? baseSpeed + (currentLevel * levelMultiplier) : -baseSpeed - (currentLevel * levelMultiplier);
        float speedY = (Math.random() > 0.5) ? baseSpeed + (currentLevel * levelMultiplier) : -baseSpeed - (currentLevel * levelMultiplier);
        balls.add(new GameBall(startX, startY, speedX, speedY, playerNum));
        playSound(serveSound);
    }

    private void spawnInitialBricks() {
        if (!isSinglePlayer) {
            float[] lanes = {150, 190, 420, 460};
            for (float x : lanes) {
                for (int y = 50; y < 430; y += 40) { bricks.add(new Brick(x, y, 30, 30, 1)); }
            }
        } else {
            int columns = Math.min(4, currentLevel);
            float spacing = 40f; float startX = 320f - ((columns * spacing) / 2f) + 5f; 
            for (int col = 0; col < columns; col++) {
                float x = startX + (col * spacing);
                for (int y = 40; y < 440; y += 40) { bricks.add(new Brick(x, y, 30, 30, (int) (Math.random() * 3) + 1)); }
            }
        }
    }

    private void spawnSingleRandomBrick() {
        int maxAttempts = 15; 
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            float randomX; float randomY = (float)(Math.random() * 350 + 50);

            if (!isSinglePlayer) {
                float[] lanes = {150, 190, 420, 460}; randomX = lanes[MathUtils.random(3)];
            } else {
                int columns = Math.min(4, currentLevel); float spacing = 40f; float startX = 320f - ((columns * spacing) / 2f) + 5f; 
                randomX = startX + ((int)(Math.random() * columns) * spacing);
            }

            Rectangle testRect = new Rectangle(randomX, randomY, 30, 30); boolean overlaps = false;

            for (Brick b : bricks) {
                if (testRect.overlaps(b.rect)) { overlaps = true; break; }
            }

            if (!overlaps) {
                int health = isSinglePlayer ? (int)(Math.random() * 3) + 1 : 1;
                bricks.add(new Brick(randomX, randomY, 30, 30, health));
                return; 
            }
        }
    }

    private void activatePowerUp(int type, int playerWhoHitIt, GameBall ball) {
        playSound(spawnSound);
        
        if (bricks.size == 0) {
            spawnFloatingText("EMPTY DATA SET", 320, 240, Color.RED, 1.2f);
            return;
        }

        if (type == 0) {
            spawnFloatingText("SELECTION SORT", 320, 240, Color.YELLOW, 1.2f);
            int minHealth = 999; int minIndex = -1;
            for (int i = 0; i < bricks.size; i++) {
                if (bricks.get(i).health < minHealth) {
                    minHealth = bricks.get(i).health; minIndex = i;
                }
            }
            if (minIndex >= 0 && minIndex < bricks.size) {
                bricks.removeIndex(minIndex);
                if (playerWhoHitIt == 1) p1Score += 100; else p2Score += 100;
            }
        } 
        else if (type == 1) {
            spawnFloatingText("INSERTION SORT", 320, 240, Color.CYAN, 1.2f);
            float startX = (playerWhoHitIt == 1) ? p1Paddle.x + 50 : p2Paddle.x - 70;
            float startY = 160;
            for (int i = 0; i < 4; i++) {
                bricksToAdd.add(new Brick(startX, startY, 30, 30, 1));
                startY += 40;
            }
        } 
        else if (type == 2) {
            spawnFloatingText("BUBBLE SORT", 320, 240, Color.GREEN, 1.2f);
            blockMoveDirection *= -1;
            blockMoveSpeed += 20f; 
        } 
        else if (type == 3) {
            spawnFloatingText("MERGE SORT", 320, 240, Color.ORANGE, 1.2f);
            int toRemove = Math.min(bricks.size, 2); 
            for (int i = 0; i < toRemove; i++) {
                if (bricks.size > 0) {
                    bricks.removeIndex(0);
                    if (playerWhoHitIt == 1) p1Score += 150; else p2Score += 150;
                }
            }
        } 
        else if (type == 4) {
            spawnFloatingText("QUICK SORT", 320, 240, Color.PURPLE, 1.5f);
            if (ball != null) {
                ball.isQuickSort = true; ball.quickSortTimer = 4f;
            }
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() { gameState = GameState.PAUSED; }
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shape.dispose();
        paddleImage.dispose();
        background.dispose();
        ballSpriteSheet.dispose(); 
        brickSheet.dispose();
        winSound.dispose();
        loseSound.dispose();
        hitSound.dispose();
        spawnSound.dispose();
        scoreSound.dispose();
        serveSound.dispose();
        bgMusic.dispose();
    }
}