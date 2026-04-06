package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LeaderboardScreen implements Screen {
    final PongBreakoutFusion game;
    OrthographicCamera camera;
    Viewport viewport;
    Texture background;

    // Arrays to hold the saved data
    String[] names = new String[10];
    int[] scores = new int[10];

    public LeaderboardScreen(final PongBreakoutFusion game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        background = new Texture(Gdx.files.internal("game_bg.png"));

        // Load the preferences
        Preferences prefs = Gdx.app.getPreferences("SortAlgoLeaderboard");
        for (int i = 0; i < 10; i++) {
            names[i] = prefs.getString("name_" + i, "---");
            scores[i] = prefs.getInteger("score_" + i, 0);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        // Draw dimmed background
        game.batch.setColor(0.3f, 0.3f, 0.4f, 1f); 
        game.batch.draw(background, 0, 0, 640, 480);
        game.batch.setColor(Color.WHITE);

        // Title
        game.font.getData().setScale(1.2f);
        game.font.setColor(Color.GOLD);
        game.font.draw(game.batch, "TOP 10 LEADERBOARD", 0, 440, 640, Align.center, false);

        // Draw the Scores
        game.font.getData().setScale(0.6f);
        float startY = 370;
        for (int i = 0; i < 10; i++) {
            if (scores[i] > 0) {
                game.font.setColor(Color.CYAN);
                game.font.draw(game.batch, (i + 1) + ". " + names[i], 150, startY);
                game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, String.valueOf(scores[i]), 400, startY);
            } else {
                game.font.setColor(Color.GRAY);
                game.font.draw(game.batch, (i + 1) + ". ---", 150, startY);
                game.font.draw(game.batch, "0", 400, startY);
            }
            startY -= 30;
        }

        // Return Text
        game.font.getData().setScale(0.5f);
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "Press ENTER or ESCAPE to Return", 0, 40, 640, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.batch.end();

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
    @Override public void dispose() { background.dispose(); }
}