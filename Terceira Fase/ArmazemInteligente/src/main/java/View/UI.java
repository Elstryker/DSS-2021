package View;

import java.util.*;

import Business.Armazem.ArmazemLNFacade;

import Business.IArmazemLN;
import Util.Coordenadas;
import Util.EstadoPalete;
import Util.Tuple;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


class MapaThread implements Runnable {
    IArmazemLN model;
    private AtomicBoolean running;
    
    public MapaThread (IArmazemLN model) {
        this.model = model;
        this.running = new AtomicBoolean(true);
    }
    
    public void interrupt () {
        running.set(false);
    }
    
    public void run() {
        while (running.get()) {
            try {
                int[][] mapa = model.getMapa();
                
                System.out.print("\033[H\033[2J");
                System.out.flush();
                
                UI.showLogo();
                
                for (int i = 0; i < 12; i++) {
                    System.out.print("                                                               ");
                    for (int j = 0; j < 16; j++) {
                        switch (mapa[i][j]) {
                            case 0:
                                System.out.print(" ");
                                break;
                            case 1:
                                if ((i == 0 && j == 2) || (i == 2 && j == 0))
                                    System.out.print(UI.ANSI_BLUE + "╔" + UI.ANSI_RESET);
                                else if ((i == 2 && j == 2) || (i == 11 && j == 15))
                                    System.out.print(UI.ANSI_BLUE +"╝" + UI.ANSI_RESET);
                                else if ((i == 0 && j == 15) || (i == 9 && j == 2))
                                    System.out.print(UI.ANSI_BLUE + "╗" + UI.ANSI_RESET);
                                else if ((i == 9 && j == 0) || (i == 11 && j == 2))
                                    System.out.print(UI.ANSI_BLUE + "╚" + UI.ANSI_RESET);
                                else if (i == 0 || i == 11 || (i == 2 && j == 1) || (i == 9 && j == 1))
                                    System.out.print(UI.ANSI_BLUE + "═");
                                else
                                    System.out.print(UI.ANSI_BLUE + "║" + UI.ANSI_RESET);
                                break;
                            case 2: 
                                System.out.print(UI.ANSI_YELLOW + "☺" + UI.ANSI_RESET);
                                break;
                            case 3: 
                                System.out.print(UI.ANSI_GREEN + "☻" + UI.ANSI_RESET);
                                break;
                            case 4: 
                                System.out.print(UI.ANSI_BLUE + "○" + UI.ANSI_RESET);
                                break;
                            case 5: 
                                System.out.print(UI.ANSI_RED + "◙" + UI.ANSI_RESET);
                                break;
                        }
                    }
                    System.out.println();
                }
                
                System.out.println("\nPressione 'Enter' para voltar ao menu principal.\n");
                Thread.sleep(1000);
                
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
        Thread.currentThread().interrupt();
    }
    
}


public class UI {
    private IArmazemLN model;
    private Scanner scan;
    private List<String> opcoes;
    private List<MenuHandler> handlers;
    private int opcao;
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    public interface MenuHandler {
        void execute();
    }
    
    public UI() {
        this.model = new ArmazemLNFacade();
        this.scan = new Scanner(System.in);
        this.opcoes = Arrays.asList("1. Ver Mapa do Armazém em tempo real",
                "2. Consultar localização de todas as paletes em armazém",
                "0. Sair do programa");
        this.opcao = 0;
        this.handlers = new ArrayList<>();
        
        addHandler(this::showMapa);
        addHandler(this::showPaletes);
    }
    
    private void addHandler(UI.MenuHandler h) {
        this.handlers.add(h);
    }

    public void show(String s){
      System.out.println(s);
    }

    private void showMenu() {
        for (String s : opcoes)
            System.out.println(s);
    }
    
    private int Menu() {
        int op = -1;
        
        showLogo();
        showMenu();
        
        do {
            try {
                if (scan.hasNextInt()) {
                    op = scan.nextInt();
                } else scan.next();
            } catch (InputMismatchException e) {
                op = -1;
            }

            if (op<0 || op>(this.opcoes.size()-1) ) {
                showLogo();
                System.out.println("\nA opção selecionada não é válida.\n");
                showMenu();
                op = -1;
            }
        } while (op < 0 || op > 2); 
        return op;
    }
    
    public void start() {
        this.model.start();
        
        boolean login = verificaLogin();
        showLogo();
        
        if (login) {
            do {
                opcao = Menu();
                if (opcao > 0)
                    this.handlers.get(opcao-1).execute();
            } while (opcao != 0);
        }
        this.model.desligaSistema();
        exitScreen(login);
    }
    
    
    public void showPaletes () {
        Map<Integer,Tuple<String,Tuple<EstadoPalete, Coordenadas>>> paletesInfo = this.model.getPaletes();
        
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        showLogo();
      
        System.out.print(ANSI_CYAN + "                   --------------------------------------------------------------------------------------------");
        System.out.print("\n                   |                                      " + ANSI_RESET + String.format("%s", "Listagem de paletes") + ANSI_CYAN + "                                 |");
        System.out.print("\n                   --------------------------------------------------------------------------------------------");
        System.out.print("\n                   |"  + ANSI_RESET + "      Palete" + ANSI_CYAN + "      ||" + ANSI_RESET + "         Material     " + ANSI_CYAN + "    || " + ANSI_RESET + "   Coordenadas " + ANSI_CYAN + "   ||  " + ANSI_RESET + "     Estado     " + ANSI_CYAN + "   |");
        System.out.println("\n                   --------------------------------------------------------------------------------------------");
        
        for (Map.Entry<Integer, Tuple<String,Tuple<EstadoPalete, Coordenadas>>> e : paletesInfo.entrySet()) {
            String s = null;
            int idPalete = e.getKey();
            Tuple<String,Tuple<EstadoPalete, Coordenadas>> t = e.getValue();
            String material = t.getO();
            EstadoPalete estado = t.getT().getO();
            Coordenadas coordenadas = t.getT().getT();
            String coordenadasTextual;
            if(coordenadas.getX()==0 && coordenadas.getY()==0){
                coordenadasTextual="ERRO";
            }else{
                coordenadasTextual = "("+coordenadas.getX() +"," + coordenadas.getY()+")";
            }


            switch (estado) {
                case RECEM_CHEGADA -> s = ANSI_RED + "RECÉM CHEGADA" + ANSI_CYAN;
                case EM_LEVANTAMENTO -> s = ANSI_YELLOW + "EM LEVANTAMENTO" + ANSI_CYAN;
                case TRANSPORTE -> s = ANSI_GREEN + "EM TRANSPORTE" + ANSI_CYAN;
                case ARMAZENADA -> s = ANSI_BLUE + "ARMAZENADA" + ANSI_BLUE;
            }
            
            System.out.println("                   |" + ANSI_RESET + String.format("%9d%-9s", idPalete, " ") + ANSI_CYAN + "|" +
            String.format("%-10s%-21s", "|", ANSI_RESET + material, " ") + ANSI_CYAN + "|" +
            String.format("%-8s%-16s", "|", ANSI_RESET + coordenadasTextual)  + ANSI_CYAN + "|" +
            String.format("%-6s%-26s", "|", s, " ") + ANSI_CYAN + "|");
        }
            System.out.println(ANSI_CYAN + "                   --------------------------------------------------------------------------------------------\n" + ANSI_RESET);
        
        premirTecla ();
        
        System.out.print("\033[H\033[2J");
        System.out.flush();
        showLogo();
    }
    
    public static void premirTecla () {
        System.out.println ("\nPressione 'Enter' para voltar ao menu principal.");
        
        try {
            System.in.read();
        } catch (Exception e) {}
    }
    
    public void exitScreen (boolean login) {
        showLogo();
        if (!login)
            System.out.println("                                 \n\n     Excedeu o número de tentativas permitidas, contacte um admnistrador para reaver "
                            + "acesso à aplicação.");
            System.out.println("                                 \n\n     A encerrar, obrigado por utilizar ArmazémInteligente™ technologies.\n\n");
    }
    
    public static void showLogo () {
        
        System.out.print("\033[H\033[2J");
        System.out.flush();        
        System.out.println(ANSI_GREEN + "                                                                   _____ " + ANSI_GREEN + "      _       _ _                  _       \n" +
ANSI_RED + "                    /\\                                 " + ANSI_GREEN + "           |_   _|     | |     | (_)                | |      \n" +
ANSI_RED + "                   /  \\   _ __ _ __ ___   __ _ _______ _ __ ___  " + ANSI_GREEN + "   | |  _ __ | |_ ___| |_  __ _  ___ _ __ | |_ ___ \n" +
ANSI_RED + "                  / /\\ \\ | '__| '_ ` _ \\ / _` |_  / _ \\ '_ ` _ \\   " + ANSI_GREEN + " | | | '_ \\| __/ _ \\ | |/ _` |/ _ \\ '_ \\| __/ _ \\\n" +
ANSI_RED + "                 / ____ \\| |  | | | | | | (_| |/ /  __/ | | | | | " + ANSI_GREEN + " _| |_| | | | ||  __/ | | (_| |  __/ | | | ||  __/\n" +
ANSI_RED + "                /_/    \\_\\_|  |_| |_| |_|\\__,_/___\\___|_| |_| |_|" + ANSI_GREEN + " |_____|_| |_|\\__\\___|_|_|\\__, |\\___|_| |_|\\__\\___|\n" +
ANSI_GREEN + "                                                                                            __/ |                   \n" +
ANSI_GREEN + "                                                                                           |___/                    \n" +
"" + ANSI_RESET);
    }
    
    public void showMapa () {
        MapaThread m;
        Thread t = new Thread ((m = new MapaThread(model)));
        t.start();
        
        UI.premirTecla();
                
        m.interrupt();
        
        showLogo();
    }
    
    public boolean verificaLogin () {
        String user = null;
        String password = null;
        boolean sucesso = false;
        int tentativas = 0;
        
        showLogo();
        
        while (!sucesso && tentativas < 3) {
            try {
                System.out.println("\nInsira o seu nome:");
                user = scan.nextLine();
                System.out.println("Insira a sua password:");
                password = scan.nextLine();
            } catch (InputMismatchException e) {
                System.out.println(e.toString());
            }

            sucesso = this.model.login(user, password);

            if (!sucesso && ++tentativas < 3) {
                showLogo();
                System.out.println("Os dados que inseriu não são válidos, tente novamente.\n" 
                            + "Tentativas remanescentes: " + (3 - tentativas));
            }   
        }
        
        return sucesso;
    }
}


    


