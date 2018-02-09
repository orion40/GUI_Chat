
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

/**
 *
 * @author whoami
 */
public class GUI_Chat extends Application{
    
    private static enum ApplicationState {LOGON_SCREEN, CHAT_SCREEN};
    private static ApplicationState currentState;
    private static String versionString = "0.1";
    private static Stage mainStage;
    
    private static Label errorLabel;
    private static TextField usernameTextField;
    private static TextField serverIPTextField;
    //private static TextField chatPromptText;
    private static ArrayList<String> messageList = new ArrayList<>();
    
    private static ServerHandler serverHandler;
    private static ClientHandler client_stub;
    
    private static BorderPane createLoginLabelsPane(){
        BorderPane loginLabels = new BorderPane();
        
        loginLabels.setTop(new Label("Username"));
        loginLabels.setBottom(new Label("Server IP"));
        
        return loginLabels;
    }
    
    private static BorderPane createLoginTextFieldsPane(){
        BorderPane loginTextFieldsPane = new BorderPane();
        usernameTextField = new TextField("toto");
        serverIPTextField = new TextField("127.0.0.1");
        
        usernameTextField.setOnAction((ActionEvent) ->{
            serverIPTextField.requestFocus();
        });
        
        serverIPTextField.setOnAction((ActionEvent) -> {
            logon();
        });
        
        loginTextFieldsPane.setTop(usernameTextField);
        loginTextFieldsPane.setBottom(serverIPTextField);
        
        return loginTextFieldsPane;
    }
    
    private static BorderPane createLoginFieldsPane(){
        BorderPane loginFields = new BorderPane();
        
        loginFields.setLeft(createLoginLabelsPane());
        loginFields.setRight(createLoginTextFieldsPane());
        
        return loginFields;
    }
    
    private static BorderPane createLoginButtonsPane(){
        BorderPane loginButtonPane = new BorderPane();
        Button connectButton = new Button("Connect");
        Button cancelButton = new Button("Cancel");
        errorLabel = new Label("");
        
        cancelButton.setOnAction((ActionEvent t) -> {
            mainStage.close();
        });
        
        connectButton.setOnAction((ActionEvent t) -> {
            logon();
        });
        
        loginButtonPane.setLeft(cancelButton);
        loginButtonPane.setRight(connectButton);
        loginButtonPane.setTop(errorLabel);
        
        return loginButtonPane;
    }
    
    private static BorderPane createGUILoginClient() {
        BorderPane guiLoginClient = new BorderPane();
        
        guiLoginClient.setTop(createLoginFieldsPane());
        guiLoginClient.setBottom(createLoginButtonsPane());
        
        guiLoginClient.getBottom().prefWidth(guiLoginClient.getWidth());
        
        return guiLoginClient;
    }
    
    private static MenuBar createChatMenuPane(){
        MenuItem logoutMenuItem = new MenuItem("Logout");
        MenuItem exitMenuItem = new MenuItem("Exit");
        Menu fileMenu = new Menu("File");
        MenuBar menuBar = new MenuBar(fileMenu);
        
        fileMenu.getItems().addAll(logoutMenuItem, exitMenuItem);
        
        logoutMenuItem.setOnAction((ActionEvent e) -> {
            logout();
        });
        
        exitMenuItem.setOnAction((ActionEvent e) -> {
            exit();
        });
        
        return menuBar;
    }
    
    private static BorderPane createChatUserListPane(){
        BorderPane chatUserListPane = new BorderPane();
        ListView chatUserListView = new ListView();
        
        chatUserListPane.setCenter(chatUserListView);
        
        return chatUserListPane;
    }
    
    private static BorderPane createChatPromptPane(){
        BorderPane chatPromptPane = new BorderPane();
        TextField chatPromptText = new TextField();
        Button sendMessageButton = new Button("Send");
        
        sendMessageButton.setOnAction((ActionEvent t) -> {
            parseInput(chatPromptText.getText());
        });
        
        chatPromptPane.setCenter(chatPromptText);
        chatPromptPane.setRight(sendMessageButton);
        
        return chatPromptPane;
    }
    
    private static BorderPane createGUIChatClient(){
        BorderPane guiChatClient = new BorderPane();
        TextArea chatTextArea = new TextArea();
        /*ObservableList<String> observableMessagesList = FXCollections.observableArrayList(
        messageList -> new Observable[]{messageList.get(0)}
        );*/
        
        guiChatClient.setTop(createChatMenuPane());
        guiChatClient.setCenter(chatTextArea);
        guiChatClient.setRight(createChatUserListPane());
        guiChatClient.setBottom(createChatPromptPane());
        
        return guiChatClient;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage){
        mainStage = primaryStage;
        currentState = ApplicationState.LOGON_SCREEN;
        Scene scene = new Scene(createGUILoginClient());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("RMI Chat Client");
        primaryStage.show();
    }
    
    private static void logout(){
        try {
            serverHandler.disconnect(client_stub);
        } catch (RemoteException ex) {
            Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentState = ApplicationState.LOGON_SCREEN;
        mainStage.setScene(new Scene(createGUILoginClient()));
        Rectangle2D screenRectangle = Screen.getPrimary().getVisualBounds();
        mainStage.setX((screenRectangle.getWidth() - mainStage.getWidth()) / 2);
        mainStage.setY((screenRectangle.getHeight() - mainStage.getHeight()) / 2);
    }
    
    
    private static void logon() {
        try {
            ClientHandlerImpl client = new ClientHandlerImpl(usernameTextField.getText(), messageList);
            client_stub = (ClientHandler) UnicastRemoteObject.exportObject(client, 0);
            
            // Get remote object reference
            Registry registry = LocateRegistry.getRegistry(serverIPTextField.getText());
            try {
                serverHandler = (ServerHandler) registry.lookup("ServerHandler");
            } catch (NotBoundException ex) {
                Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AccessException ex) {
                Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (serverHandler.connect(client_stub) == true){
                Scene scene = new Scene(createGUIChatClient());
                mainStage.setScene(scene);
                Rectangle2D screenRectangle = Screen.getPrimary().getVisualBounds();
                mainStage.setX((screenRectangle.getWidth() - mainStage.getWidth()) / 2);
                mainStage.setY((screenRectangle.getHeight() - mainStage.getHeight()) / 2);
                
                currentState = ApplicationState.CHAT_SCREEN;
                
            } else {
                errorLabel.setText("Unable to connect.");
            }
            
        } catch (RemoteException ex) {
            errorLabel.setText("Unable to connect.");
        }
    }
    
    private static void exit(){
        switch (currentState){
            case CHAT_SCREEN:
                try {
                    serverHandler.disconnect(client_stub);
                } catch (RemoteException ex) {
                    Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
            case LOGON_SCREEN:
                mainStage.close();
                System.exit(0);
                break;
            default:
                throw new IllegalStateException();
        }
    }
    
    private static void parseInput(String input) {
        if (input.startsWith("/")){
            executeCommand(input);
        }else{
            try {
                serverHandler.sendMessage(client_stub, input);
            } catch (RemoteException ex) {
                Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void executeCommand(String input) {
        // Remove leading /
        input = input.substring(1).toLowerCase();
        switch (input){
            case "help":
                printHelp();
                break;
            case "logout":
            case "quit":
                logout();
                break;
            case "history":
                getHistory();
                break;
            case "allhistory":
                getAllHistory();
                break;
            case "users":
                getUsers();
                break;
            default:
                System.out.println("Command unknown.");
                break;
        }
    }
    
    private static void printHelp() {
        System.out.println("RMI Chat V" + versionString);
        System.out.println("Commands :");
        System.out.println("/help\tPrint this message.");
        System.out.println("/logout\tDisconnect the client and exit.");
        System.out.println("/quit\tDisconnect the client and exit.");
        System.out.println("/history\tGet the server session message history.");
        System.out.println("/allhistory\tGet the server complete message history.");
        System.out.println("/users\tGet current user list.");
    }
    
    private static void getHistory() {
        ArrayList<String> history;
        try {
            history = serverHandler.getHistory();
            
            for (String line : history){
                System.out.println(line);
            }
            System.out.println("");
        } catch (RemoteException ex) {
            System.out.println("Unable to get history.");
        }
    }
    
    private static void getUsers() {
        ArrayList<String> users;
        try {
            users = serverHandler.getConnectedUsers();
            
            for (String user : users){
                System.out.println(user);
            }
            System.out.println("");
        } catch (RemoteException ex) {
            System.out.println("Unable to get user list.");
            System.out.println(ex.getMessage());
        }
    }
    
    private static void getAllHistory() {
        ArrayList<String> history;
        try {
            history = serverHandler.getAllHistory();
            
            for (String line : history){
                System.out.println(line);
            }
            System.out.println("");
        } catch (RemoteException ex) {
            System.out.println("Unable to get history.");
            System.out.println(ex.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        exit();
    }
}
