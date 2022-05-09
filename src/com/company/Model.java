package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Model extends JPanel implements ActionListener {
    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean isDead = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;

    private int N_GHOSTS = 6;
    private int lives, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int pacman_x, pacman_y, pacman_dx, pacman_dy;
    private int req_dx, req_dy;


    private final short levelData[] = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };
    //niebeiskie = 0   prawe granica = 4
    //lewa granica = 1   dolna granica = 8
    //gorna granica = 2   biale kropki = 16

    private final int validSpeeds[] = {1,2,3,4,5,6,7,8};
    private final int maxSpeed = 6;
    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Model(){
        loadImages();
        initVariables();
        addKeyListener(new IAdapter());
        setFocusable(true);
        initGame();
    }

    private void loadImages(){
        down = new ImageIcon("/src/images/down.gif").getImage();
        up = new ImageIcon("/src/images/up.gif").getImage();
        left = new ImageIcon("/src/images/left.gif").getImage();
        right = new ImageIcon("/src/images/right.gif").getImage();
        ghost = new ImageIcon("/src/images/ghost.gif").getImage();
        heart = new ImageIcon("/src/images/heart.png").getImage();
    }

    private void initVariables(){
        screenData = new short[N_BLOCKS*N_BLOCKS];
        d = new Dimension(400,400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this); // timer zajmuje sie animacją, okresla, jak czesto obrazy są przerysowywane 40-40milisekund
        timer.start();
    }

    private void initGame(){
        lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel(){
        for(int i = 0; i < N_BLOCKS*N_BLOCKS; i++){
            screenData[i] = levelData[i];
        }
    }

    private void playGame(Graphics2D g2d){
        if(isDead){
            death();
        }else{
            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    public void movePacman(){
        int pos;
        short ch;

        if(pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0){
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
                    ch = screenData[pos];
            if((ch & 16)!=0){
                screenData[pos] = (short) (ch&15);
                score++;
            }
            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacman_dx = req_dx;
                    pacman_dy = req_dy;
                }
            }
            // sprawdzamy czy stoi w miejscu
            if ((pacman_dx == -1 && pacman_dy == 0 && (ch & 1) != 0)
                    || (pacman_dx == 1 && pacman_dy == 0 && (ch & 4) != 0)
                    || (pacman_dx == 0 && pacman_dy == -1 && (ch & 2) != 0)
                    || (pacman_dx == 0 && pacman_dy == 1 && (ch & 8) != 0)) {
                pacman_dx = 0;
                pacman_dy = 0;
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacman_dx;
        pacman_y = pacman_y + PACMAN_SPEED * pacman_dy;
    }

    public void drawPacman(Graphics2D g2d){
        if(req_dx == -1){
            g2d.drawImage(left, pacman_x+1, pacman_y+1, this);
        }else if(req_dx == 1){
            g2d.drawImage(right, pacman_x+1, pacman_y+1, this);
        }else if(req_dy == -1){
            g2d.drawImage(up, pacman_x+1, pacman_y+1, this);
        }else{
            g2d.drawImage(down, pacman_x+1, pacman_y+1, this);
        }
    }

    public void MoveGhosts(Graphics2D g2d){
        int pos;
        int count;
        for(int i=0; i<N_GHOSTS; i++){
            if(ghost_x[i]%BLOCK_SIZE == 0 && ghost_y[i]%BLOCK_SIZE == 0){
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screenData[pos] & 2) == 0 && ghost_dx[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != 1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if ((screenData[pos] & 8) == 0 && ghost_dx[i] != 1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }
        }
    }

    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE; //pozycja startowa
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7 * BLOCK_SIZE;  //pozycja startowa pacman
        pacman_y = 11 * BLOCK_SIZE;
        pacman_dx = 0;	//reset kierunku ruchu
        pacman_dy = 0;
        req_dx = 0;		//reset sterowania kierunkiem
        req_dy = 0;
        isDead = false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        if(inGame){
            playGame(g2d);
        }else{
            showIntroScreen(g2d);
        }
        Toolkit.getDefaultToolkit().sync();
    }

     class TAdapter extends KeyAdapter{ // funkcja do kontroli postacia
        public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();
            if(inGame){ //jesli jestesmy w grze
                if(key == KeyEvent.VK_LEFT){ //strzalka w lewo - ruch w lewo (dx-1)
                    req_dx = -1;
                    req_dy = 0;
                }
                else if(key == KeyEvent.VK_RIGHT){ //strzalka w prawo - ruch w prawo (dx+1)
                    req_dx = 1;
                    req_dy = 0;
                }
                else if(key == KeyEvent.VK_UP){ //strzalka w gore- ruch w gore (dy+1)
                    req_dx = 0;
                    req_dy = 1;
                }
                else if(key == KeyEvent.VK_DOWN){ //strzalka w dol - ruch w dol (dy-1)
                    req_dx = 0;
                    req_dy = -1;
                }
                else if(key == KeyEvent.VK_ESCAPE && timer.isRunning()){
                    inGame = false;
                }
            }else{
                if(key == KeyEvent.VK_SPACE){
                    inGame = true;
                    initGame();
                }
            }
        }
     }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
