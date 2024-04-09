
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LordOfSpaceApp extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 60;
    private static final Random RAND = new Random();
    private static final Image EXPLOSION_IMG = new Image(LordOfSpaceApp.class.getResourceAsStream("/explode/explosion.png"));

    private GraphicsContext gc;
    private VBox startMenu;
    private StackPane root;
    private Canvas canvas;
    private AnimationTimer gameLoop;

    private List<Enemy> enemies = new ArrayList<>();
    private List<Shot> shots = new ArrayList<>();
    private Player player;
    private boolean gameStarted = false;
    private int score = 0;
    private int finalScore = 0;
    private Text lastScoreText;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        setupStartMenu();
        setupGameLoop();

        root = new StackPane();
        root.getChildren().addAll(canvas, startMenu);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnMouseMoved(e -> {
            if (gameStarted) {
                player.setX(e.getX() - PLAYER_SIZE / 2);
            }
        });
        scene.setOnMouseClicked(e -> {
            if (gameStarted) {
                shots.add(new Shot(player.getX() + PLAYER_SIZE / 2 - Shot.SIZE / 2, player.getY() - Shot.SIZE));
            }
        });

        stage.setScene(scene);
        stage.setTitle("Lord of Space");
        stage.show();
    }

    private Font loadFont(String filename, double size) {
        try {
            return Font.loadFont(LordOfSpaceApp.class.getResourceAsStream("/fonts/" + filename), size);
        } catch (Exception e) {
            e.printStackTrace();
            return Font.font("Arial", size);
        }
    }

    private class SpinningPlanet extends VBox {
        private final double size = 50;
        private final ImageView imageView;

        public SpinningPlanet(String imagePath, Pos position) {
            this.imageView = new ImageView(new Image(LordOfSpaceApp.class.getResourceAsStream(imagePath)));

            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            setRotate(0);
            RotateTransition rt = new RotateTransition(Duration.seconds(5), imageView);
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.play();
            getChildren().add(imageView);
            setAlignment(position);
        }
    }

    private void setupStartMenu() {
        startMenu = new VBox(20);
        startMenu.setAlignment(Pos.CENTER);
        startMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 20px;");
        Text title = new Text("Welcome Lord of Space\nChoose your Spaceship");
        title.setFont(loadFont("PressStart2P-Regular.ttf", 24));
        title.setFill(Color.YELLOW);

        SpinningPlanet spinningPlanetTopLeft = new SpinningPlanet("/SpinPlanet/uranus.png", Pos.TOP_LEFT);
        SpinningPlanet spinningPlanetBottomRight = new SpinningPlanet("/SpinPlanet/splanet.png", Pos.BOTTOM_RIGHT);

        Button playerButton = createMenuButton(
            "/usership/player.png",
            "The Starlight Voyager",
            "Once a beacon of hope, the Starlight Voyager sailed through the cosmos \n under Captain Orion's command, outwitting foes with its dazzling \n maneuvers and leaving a trail of stardust in its wake."
        );
        Button ufoButton = createMenuButton(
            "/usership/ufo.png",
            "The Cosmic Defender",
            "This ship carries the legacy of General Luna, who stood against \n the dark forces of the galaxy, ensuring peace and safety for \n countless star systems with her unwavering resolve."
        );
        Button ufo2Button = createMenuButton(
            "/usership/ufo2.png",
            "The Nebula Striker",
            "Shrouded in mystery, the Nebula Striker was the vessel of choice \n for the Shadow Phantom, a figure whispered about in             legends, known \n for appearing just as swiftly as vanishing, leaving \n nothing but whispered tales."
            );
    
            lastScoreText = new Text("Last Score: 0");
            lastScoreText.setFont(loadFont("PressStart2P-Regular.ttf", 18));
            lastScoreText.setFill(Color.YELLOW);
    
            startMenu.getChildren().addAll(spinningPlanetTopLeft, spinningPlanetBottomRight, title, playerButton, ufoButton, ufo2Button, lastScoreText);
            VBox.setMargin(spinningPlanetTopLeft, new Insets(10, 10, 0, 10)); 
            VBox.setMargin(spinningPlanetBottomRight, new Insets(0, 10, 10, 0)); 
        }
    
        private Button createMenuButton(String imagePath, String shipName, String description) {
            ImageView imageView = new ImageView(new Image(LordOfSpaceApp.class.getResourceAsStream(imagePath)));
            imageView.setFitWidth(PLAYER_SIZE);
            imageView.setFitHeight(PLAYER_SIZE);
            Button button = new Button();
            button.setGraphic(imageView);
            button.setOnAction(e -> startGame(imagePath));
    
            Tooltip tooltip = new Tooltip(shipName + "\n" + description);
            tooltip.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: yellow; -fx-background-color: #000000bb; -fx-border-color: yellow; -fx-border-width: 1px;");
            tooltip.setShowDelay(Duration.millis(100)); 
            Tooltip.install(button, tooltip);
    
            return button;
        }
    
        private void startGame(String playerImage) {
            player = new Player(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, new Image(LordOfSpaceApp.class.getResourceAsStream(playerImage)));
            startMenu.setVisible(false);
            gameStarted = true;
            score = 0; 
            finalScore = 0; 
            gameLoop.start();
        }
    
        private void setupGameLoop() {
            gameLoop = new AnimationTimer() {
                private long lastSpawnTime = 0;
                private final long spawnInterval = 4_000_000_000L; 
    
                @Override
                public void handle(long now) {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, WIDTH, HEIGHT);
    
                    player.draw(gc);
                    player.update();
    
                    if (now - lastSpawnTime > spawnInterval) {
                        enemies.add(new Enemy(RAND.nextInt(WIDTH - (int) PLAYER_SIZE), 0, PLAYER_SIZE, new Image(LordOfSpaceApp.class.getResourceAsStream("/enemy/" + BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]))));
                        lastSpawnTime = now;
                    }
    
                    for (int i = enemies.size() - 1; i >= 0; i--) {
                        Enemy enemy = enemies.get(i);
                        enemy.draw(gc);
                        enemy.update();
                        if (enemy.getY() > HEIGHT) {
                            enemies.remove(i);
                        } else if (enemy.collidesWith(player) && !player.isExploding()) {
                            player.explode();
                            finalScore = score;
                            break;
                        }
                    }
    
                    for (int i = shots.size() - 1; i >= 0; i--) {
                        Shot shot = shots.get(i);
                        shot.draw(gc);
                        shot.update();
                        if (shot.getY() < 0) {
                            shots.remove(i);
                        } else {
                            for (int j = enemies.size() - 1; j >= 0; j--) {
                                Enemy enemy = enemies.get(j);
                                if (shot.collidesWith(enemy) && !enemy.isExploding()) {
                                    shots.remove(i);
                                    enemy.explode();
                                    score++;
                                    break;
                                }
                            }
                        }
                    }
    
                    if (player.isExploding()) {
                        displayFinalScore();
                    } else {
                        gc.setFill(Color.WHITE);
                        gc.setFont(Font.font(20));
                        gc.fillText("Score: " + score, WIDTH - 100, 30);
                    }
                }
            };
        }
    
        private void displayFinalScore() {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font(30));
            gc.fillText("Final Score: " + finalScore, WIDTH / 2 - 100, HEIGHT / 2);
            lastScoreText.setText("Last Score: " + finalScore);
        }
    
        private void stopGame() {
            gameLoop.stop();
            startMenu.setVisible(true);
            gameStarted = false;
            shots.clear();
            enemies.clear();
        }
    
        private static final String[] BOMBS_IMG = {
            "1.png", "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png", "9.png"
        };
    
        public static void main(String[] args) {
            launch(LordOfSpaceApp.class,args);
        }
    
        private class Player {
            private double x, y;
            private final double size;
            private final Image image;
            private boolean exploding = false;
            private int explosionStep = 0;
    
            public Player(double x, double y, double size, Image image) {
                this.x = x;
                this.y = y;
                this.size = size;
                this.image = image;
            }
    
            public void draw(GraphicsContext gc) {
                if (exploding) {
                    gc.drawImage(EXPLOSION_IMG, (explosionStep % 4) * 128, (explosionStep / 4) * 128, 128, 128, x, y, size, size);
                    explosionStep++;
                    if (explosionStep >= 16) {
                        stopGame();
                    }
                } else {
                    gc.drawImage(image, x, y, size, size);
                }
            }
    
            void update() {
                if (x < 0) {
                    x = 0;
                } else if (x > WIDTH - size) {
                    x = WIDTH - size;
                }
            }
    
            void setX(double x) {
                this.x = x;
            }
    
            double getX() {
                return x;
            }
    
            double getY() {
                return y;
            }
    
            void explode() {
                exploding = true;
                explosionStep = 0;
            }
    
            boolean isExploding() {
                return exploding;
            }
        }
    
        private class Enemy {
            private double x, y;
            private final double size;
            private final Image image;
            private boolean exploding = false;
            private int explosionStep = 0;
    
            public Enemy(double x, double y, double size, Image image) {
                this.x = x;
                this.y = y;
                this.size = size;
                this.image = image;
            }
    
            void draw(GraphicsContext gc) {
                if (exploding) {
                    gc.drawImage(EXPLOSION_IMG, (explosionStep % 4) * 128, (explosionStep / 4) * 128, 128, 128, x, y, size, size);
                    explosionStep++;
                    if (explosionStep >= 16) {
                        exploding = false;
                        explosionStep = 0;
                        respawn();
                    }
                } else {
                    gc.drawImage(image, x, y, size, size);
                }
            }
    
            void update() {
                y += 2;
            }
    
            double getY() {
                return y;
            }
    
            boolean collidesWith(Player player) {
                return x < player.getX() + player.size && x + size > player.getX() && y < player.getY() + player.size && y + size > player.getY();
            }
    
            void explode() {
                exploding = true;
                explosionStep = 0;
            }
    
            boolean isExploding() {
                return exploding;
            }
    
            void respawn() {
                x = RAND.nextInt(WIDTH - (int)size);
                y = -size;
            }
        }
    
        private class Shot {
            private double x, y;
            private static final int SIZE = 5;
    
            public Shot(double x, double y) {
                this.x = x;
                this.y = y;
            }
    
            void draw(GraphicsContext gc) {
                gc.setFill(Color.RED);
                gc.fillOval(x, y, SIZE, SIZE);
            }
    
            void update() {
                y -= 5;
            }
    
            double getY() {
                return y;
            }
    
            boolean collidesWith(Enemy enemy) {
                return x < enemy.x + enemy.size && x + SIZE > enemy.x && y < enemy.y + enemy.size && y + SIZE > enemy.y;
            }
        }
    }
    
    







