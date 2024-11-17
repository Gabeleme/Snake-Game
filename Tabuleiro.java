import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.ArrayList;

public class Tabuleiro extends JFrame {

    private JPanel painel;
    private JPanel menu;
    private JButton iniciarButton;
    private JButton resetButton;
    private JButton pauseButton;
    private JButton modoJogoButton; // botão para alterar se a colisão com a parede vai matar a cobra ou se ela vai aparecer do outro lado da tela
    private JTextField placarField;
    private int x, y;
    private String direcao = "direita";
    private long tempoAtualizacao = 100; // deixa o jogo mais lerdo se aumentar o valor passado aqui
    private final long velocidadeInicio = 100; //velocidade inicial da cobra
    private final long velocidadeMinima = 40; //limite de velocidade
    private int incremento = 10; // Incremento fixo para suavidade nos movimentos
    private Quadrado obstaculo, cobra;
    private int larguraTabuleiro, alturaTabuleiro;
    private int placar = 0; //o placar começa em 0
    private boolean modoDeColisao = true; // true - acontece a colisao com a parede e false - a cobra reaparece do outro lado da tela
    private Random random = new Random(); //vai ser usado para gerar posições aleatorias para a maça
    private ArrayList<Point> corpoCobra; // Lista para o corpo da cobra
    private boolean jogoRodando = false; //criado para ser utilizado para saber se o jogo ta rodando ou não, se ele foi iniciado
    private boolean comeuMaca = false; // usado para indicar se a cobra comeu a maça


    public Tabuleiro() {

        larguraTabuleiro = alturaTabuleiro = 600;

        cobra = new Quadrado(10, 10, new Color(0, 100, 0)); //coloca a cor da cobra para um verde bem escuro
        cobra.x = larguraTabuleiro / 2; // define a posição da cobra para o centro do tabuleiro
        cobra.y = alturaTabuleiro / 2;

        inicializarCobra(); // Inicializa a cobra com um segmento.

        obstaculo = new Quadrado(10, 10, Color.red);

        posicaoDaMaca(); //chamando o metodo de posicionar a maça aleatoriamente

        //o professor tinha colocado esses aqui para a maça aparecer, vou deixar comentado para poder lembrar depois
        //obstaculo.x = larguraTabuleiro / 2;
        //obstaculo.y = alturaTabuleiro / 2;

        setTitle("Jogo da Cobrinha");
        setSize(alturaTabuleiro, larguraTabuleiro + 30);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menu = new JPanel();
        menu.setLayout(new FlowLayout());

        iniciarButton = new JButton("Iniciar");
        resetButton = new JButton("Reiniciar");
        pauseButton = new JButton("Pausar");
        modoJogoButton = new JButton("Modo: Colisão"); // o modo padrão vai ser colidir com a parede
        placarField = new JTextField("Placar: 0");
        placarField.setEditable(false);

        menu.add(iniciarButton);
        menu.add(resetButton);
        menu.add(pauseButton);
        menu.add(modoJogoButton); // adiciona o botão de troca de modo na tela
        menu.add(placarField);

        painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(new Color(170, 255, 170)); //define a cor de fundo do tabuleiro, nesse caso a cor definida e fundo é verde claro
                g.fillRect(0, 0, getWidth(), getHeight()); //faz com que todo o fundo da tela fique preenchido com a cor, tanto a altura quanto a largura da tela

                g.setColor(cobra.cor); //define a cor da cobra
                for(Point p : corpoCobra){ //desenha cada um dos pedaços da cobra quando ela cresce
                    g.fillRect(p.x, p.y, cobra.altura, cobra.largura);
                }
               
                g.setColor(obstaculo.cor);
                g.fillRect(obstaculo.x, obstaculo.y, obstaculo.largura, obstaculo.altura);
            }
        };

        add(menu, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);

        setVisible(true);

        // ActionListener para o botão Iniciar
        iniciarButton.addActionListener(e -> {
            Iniciar();
            painel.requestFocusInWindow(); // Devolve o foco para o painel
        });

        // ActionListener para o botão Reset
        resetButton.addActionListener(e -> {
            Reiniciar();

        });

        // ActionListener para o botão Pausar
        pauseButton.addActionListener(e -> {
            Pausar();

        });

        // ActionListener para o botão de escolher colisão ou não com a parede
        modoJogoButton.addActionListener(e -> {
            modoDeColisao = !modoDeColisao; // faz a alteração entre os modos
            modoJogoButton.setText(modoDeColisao ? "Modo Colisão" : "Modo Reaparecer");
        });

        painel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                // Exemplo de uso do campo de Texto placarField
                //placarField.setText("Placar: " + placar++);

                // aqui onde eu posso mudar ele de andar na setinha e sim nas outras teclas
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: // pode andar para a esquerda utilizando a setinha
                    case KeyEvent.VK_A: // pode andar para a esquerda utilizando a letra A
                        if (!direcao.equals("direita")) {
                            direcao = "esquerda";
                        }
                        break;
                    case KeyEvent.VK_RIGHT: // pode andar para a direita utilizando a setinha
                    case KeyEvent.VK_D: // pode andar para a direita uitlizando a leta D
                        if (!direcao.equals("esquerda")) {
                            direcao = "direita";
                        }
                        break;
                    case KeyEvent.VK_UP: // pode andar para cima utilizando a setinha
                    case KeyEvent.VK_W: // pode ansar para cima utilizando a letra W
                        if (!direcao.equals("baixo")) {
                            direcao = "cima";
                        }
                        break;
                    case KeyEvent.VK_DOWN: // pode andar para baixo utilizando a setinha
                    case KeyEvent.VK_S: // pode andar para baixo utilizando a letra S
                        if (!direcao.equals("cima")) {
                            direcao = "baixo";
                        }
                        break;

                }
            }
        });

        painel.setFocusable(true);
        painel.requestFocusInWindow(); //reconehce os eventos gerados ao clicar no teclado, fazendo com que ao clicar no botão ele execute a funcionalidade dele dentro do jogo
    }

    //metodo de inicializar a cobra
    private void inicializarCobra(){
        corpoCobra = new ArrayList<>(); // Cria uma nova lista para armazenar as partes do corpo da cobra.
        corpoCobra.add(new Point(cobra.x, cobra.y)); // Adiciona a posição inicial da cabeça da cobra na lista como o primeiro segmento.
    }

    //metodo de mover a cobra
    private void moverCobra(int novaPosicaoX, int novaPosicaoY) {
        corpoCobra.add(0, new Point(novaPosicaoX, novaPosicaoY)); // Adiciona a nova posição da cabeça da cobra na frente da lista, simulando o movimento da cabeça para a nova posição.
        if (!comeuMaca)
            corpoCobra.remove(corpoCobra.size() - 1); // Remove o último segmento da cobra, a não ser que a cobra tenha acabado de comer uma maçã.
        else
            comeuMaca = false; // Se a cobra comeu a maçã, o `comeuMaca` é redefinido como `false`, pois a cobra não remove a cauda neste movimento.

        cobra.x = novaPosicaoX; // Atualiza a posição X da cobra para a nova posição.
        cobra.y = novaPosicaoY; // Atualiza a posição Y da cobra para a nova posição.
    }


    private void Iniciar() {
        jogoRodando = true; // aqui define a variavel como true (jogo iniciado)
        tempoAtualizacao = velocidadeInicio; //define a velocidade da cobra 
        new Thread(() -> {
            while (jogoRodando) { //passa a variavel como parametro dentro do while
                try {
                    Thread.sleep(tempoAtualizacao);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Define a direção do movimento da cobra, alterando as coordenadas x ou y.
                int novaPosicaoX = cobra.x;
                int novaPosicaoY = cobra.y;
                
                // decremmenta ou incrementa a cada vez que a cobra anda
                switch (direcao) {
                    case "esquerda":
                        novaPosicaoX -= incremento;
                        break;
                    case "direita":
                        novaPosicaoX += incremento;
                        break;
                    case "cima":
                        novaPosicaoY -= incremento;
                        break;
                    case "baixo":
                        novaPosicaoY += incremento;
                        break;
                }

                moverCobra(novaPosicaoX, novaPosicaoY); //chama o metodo e mover a cobra para uma nova posição
                verificarBordaDoTabuleiro(); //chama o metodo para verificar se a cobra colidiu 
                colisaoCobraComMaca(); //chama o metodo de verificar se a cobra colidiu com a maça
                verificarColisaoComCorpo(); //chama o metodo de verificar se a cobra colidiu com o prorpio corpo
                painel.repaint();

            }
        }).start();

    }
    // Thread é usado apra atualizar a posição da cobra

    // Método para verificar a colisão da cabeça da cobra com o próprio corpo
    private void verificarColisaoComCorpo() {
        Point cabeca = corpoCobra.get(0); // A cabeça da cobra é o primeiro ponto na lista
        for (int i = 1; i < corpoCobra.size(); i++) { // Começa do segundo elemento até o final
            if (cabeca.equals(corpoCobra.get(i))) { // Verifica se a cabeça colide com o corpo
                JOptionPane.showMessageDialog(this, "Você colidiu com o próprio corpo! Jogo encerrado.", "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);

                jogoRodando = false; //variavel que define se o jogo está rodando ou não

                tempoAtualizacao = velocidadeInicio; //retorna a cobra pra velocidade inicial

                direcao = "direita"; // define que ao reiniciar a posição que a cobra vai andar inicialmente vai ser para a direita 

                cobra.x = larguraTabuleiro / 2; // Posição inicial da cobra
                cobra.y = larguraTabuleiro / 2; // Posição inicial da cobra
                
                inicializarCobra(); // Reinicializa o corpo da cobra

                posicaoDaMaca(); // metodo de posicionar a maça aleatoriamente quando o jogo começar novamente
        
                placar = 0; // reinicia o placar quando o jogo for reiniciado
        
                placarField.setText("Placar: 0"); // mostra na tela do usuario o placar quando o jogo for reiniciado em 0
                
                JOptionPane.showMessageDialog(this, "Clique em Iniciar para Jogar Novamente", "Fim de Jogo",JOptionPane.INFORMATION_MESSAGE);

                break;
            }
        }
    }

    private void Reiniciar() {
        // Adicione aqui a lógica para reiniciar o jogo

        jogoRodando = false; // variavel que verifica se o jogo está rodando ou não, ao clicar no reiniciar essa variavel pausa o jogo, definindo ela como falsa (jogo sem rodar)

        tempoAtualizacao = velocidadeInicio; //retorna a cobra pra velocidade inicial

        direcao = "direita"; // define que ao reiniciar a posição que a cobra vai andar inicialmente vai ser para a direita 

        cobra.x = larguraTabuleiro / 2; // Posição inicial da cobra
        cobra.y = larguraTabuleiro / 2; // Posição inicial da cobra

        inicializarCobra(); // Reinicializa o corpo da cobra (resetando seu crescimento).
        posicaoDaMaca(); // metodo de posicionar a maça aleatoriamente quando o jogo começar novamente

        placar = 0; // reinicia o placar quando o jogo for reiniciado

        placarField.setText("Placar: 0"); // mostra na tela do usuario o placar quando o jogo for reiniciado em 0

        JOptionPane.showMessageDialog(this, "Jogo Reiniciado!", "Reset", JOptionPane.INFORMATION_MESSAGE);

        Iniciar(); // chamando o metodo de iniciar para que depois que clicar em reiniciar o jogo ja inicie em seguida automaticamente sem que o usuario precise clicar novamente em iniciar 
        painel.requestFocusInWindow(); // faz com que o painel receba os eventos do teclado, no caso que ele entenda os comandos do teclado para a cobra andar depois de reiniciado
    }

    //metodo de pausar o jogo e logicamente também despausar 
    private void Pausar() {
        if(jogoRodando){ //se o jogoRodando estiver true (jogo rodando) ele vai cair no if
           jogoRodando = false; //pausa o jogo 
           JOptionPane.showMessageDialog(this, "Jogo Pausado!", "Pause", JOptionPane.INFORMATION_MESSAGE); //exibe a mensagem avisando que o jogo está pausado
           pauseButton.setText("Retomar"); //muda a escrita do botão que antes era pausar para despausar ja que o jogo está pausado
        }else{ //se o jogo não estiver rodando ele está pausado, então cai no else para podes despausar
            jogoRodando = true; //define o jogo como rodando
            JOptionPane.showMessageDialog(this, "Jogo Retomado!", "Retomado", JOptionPane.INFORMATION_MESSAGE); //exibe a mensagem avisando que o jogo está despausado
            pauseButton.setText("Pausar"); // muda o texto do botão de volta para pausar
            Iniciar(); // Retoma o jogo
            painel.requestFocusInWindow(); // faz com que o painel receba os eventos do teclado, no caso que ele entenda os comandos do teclado para a cobra andar depois que o jogo sair do pause
        }
    }

    // método criado para realizar a verificação da borda do tabuleiro
    private void verificarBordaDoTabuleiro() {
        if (modoDeColisao) { //verifica se o modo de colisão esta ligado (modo padrão)
            /* == verifica se a cobra ultrapassou o limite do tabuleiro, no caso a altura e a largura dele, então independente do lado que ela for vai ser verificado 
             * == verifica a cordenada tanto do eixo x quanto do y da cobra, verifica também se a coordenada dos eixos não é maior ou igual o tamanho do tabuleiro, por
             * que se for significa que ultrapassou a parede
            */
            if (cobra.x < 0 || cobra.x >= larguraTabuleiro || cobra.y < 0 || cobra.y >= alturaTabuleiro) {
                JOptionPane.showMessageDialog(this, "Você colidiu! Jogo encerrado.", "Fim de Jogo",JOptionPane.INFORMATION_MESSAGE);

                jogoRodando = false; // variavel que verifica se o jogo está rodando ou não, ao clicar no reiniciar essa variavel pausa o jogo, definindo ela como falsa (jogo sem rodar)

                tempoAtualizacao = velocidadeInicio; //retorna a cobra pra velocidade inicial

                direcao = "direita"; // define que ao reiniciar a posição que a cobra vai andar inicialmente vai ser para a direita 

                cobra.x = larguraTabuleiro / 2; // Posição inicial da cobra
                cobra.y = larguraTabuleiro / 2; // Posição inicial da cobra
                
                inicializarCobra(); // Reinicializa o corpo da cobra (resetando seu crescimento).

                posicaoDaMaca(); // metodo de posicionar a maça aleatoriamente quando o jogo começar novamente
        
                placar = 0; // reinicia o placar quando o jogo for reiniciado
        
                placarField.setText("Placar: 0"); // mostra na tela do usuario o placar quando o jogo for reiniciado em 0
                
                JOptionPane.showMessageDialog(this, "Clique em Iniciar para Jogar Novamente", "Fim de Jogo",JOptionPane.INFORMATION_MESSAGE);

                //System.exit(0); adicionado apenas para o programa não ficar em loop durante os testes 

            }
        } else { // modo que a cobra vai reaparecer no outro lado da tela quando ela colidir com a parede
            // Verifica se a cobra saiu pela borda esquerda
            if (cobra.x < 0) { // se a posição x da cobra for menor que 0, isso significa que é a parte da esquerda da cobra, então se ela sair do limite da tela pelo lado esquerdo 
                // Move a cobra para a borda direita pois ela saiu pela esquerda
                cobra.x = larguraTabuleiro - cobra.largura;
            }
            // Verifica se a cobra saiu pela borda direita, que é quando o x da cobra é maior ou igual ao tamanho do tabuleiro
            else if (cobra.x >= larguraTabuleiro) {
                // Move a cobra para a borda esquerda
                cobra.x = 0;
            }
            // Verifica se a cobra saiu pela borda superior
            if (cobra.y < 0) {
                // Move a cobra para a borda inferior 
                cobra.y = alturaTabuleiro - cobra.altura;
            }
            // Verifica se a cobra saiu pela borda inferior
            else if (cobra.y >= alturaTabuleiro) {
                // Move a cobra para a borda superior 
                cobra.y = 0;
            }
        }
    }

    //metodo que vai definir/gerar a posição da maça no tabuleiro de forma aleatoria
    private void posicaoDaMaca(){

        //vai gerar aleatoriamente a posição da maça no eixo x e y e garantir que ela fique dentro dos parametros de tamanho do tabuleiro
        obstaculo.x = random.nextInt(larguraTabuleiro / incremento) * incremento;
        obstaculo.y = random.nextInt(alturaTabuleiro / incremento) * incremento; 

    }

    //metodo que vai verificar a colisão da cabeça da cobra com a maça, fazneod com que ao colidir a maça suam e reapareça em outro lugar, mudando tbm o placar adicionando um ponto pra cada colisão (maça comida)
    public void colisaoCobraComMaca(){
        //verifica cada eixo da cabeça da cobra e da maçã estão no mesmo lugar e se obtem o mesmo tamanho, se isso ocorrer é considerado a colisão entre a cabeça da cobra e a maça
        if(cobra.x < obstaculo.x + obstaculo.largura && cobra.x + cobra.largura > obstaculo.x && cobra.y < obstaculo.y + obstaculo.altura && cobra.y + cobra.altura > obstaculo.y){
            comeuMaca = true; 
            placar++; //aumenta o placar
            placarField.setText("Placar: " + placar); //atualiza o valor do placar na tela
            posicaoDaMaca(); //reposiciona a maça usando o metodo de posicionar ela de forma aleatoria 

            //vai diminuindo o tempo de atualização para que o jogo va ficando mais rapido 
            if(tempoAtualizacao > velocidadeMinima){ //adicionando um limite minimo para que o jogo não fique tão rapido assim
                tempoAtualizacao -= 1; 

            }
        }
    }

    public static void main(String[] args) {
        new Tabuleiro();
    }
}