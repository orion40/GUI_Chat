
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
/**
 *
 * @author whoami
 */
public class GUI_Chat extends Application {
    
    private static enum ApplicationState {
        LOGON_SCREEN, CHAT_SCREEN
    };
    private static ApplicationState currentState;
    private static String versionString = "0.2";
    private static Stage mainStage;
    
    private static Label errorLabel;
    private static TextField usernameTextField;
    private static TextField serverIPTextField;
    private static TextArea chatTextArea;
    private static ListView chatUserListView;
    private static TextField chatPromptText;
    
    private static ObservableList<String> messagesList;
    private static ObservableList<String> userList;
    
    private static ArrayList<String> clientMessageHistory;
    private static int clientMessageHistoryOffset;
    
    private static ServerHandler serverHandler;
    private static ClientHandler client_stub;
    
    private static BorderPane createLoginLabelsPane() {
        BorderPane loginLabels = new BorderPane();
        
        loginLabels.setTop(new Label("Username"));
        loginLabels.setBottom(new Label("Server IP"));
        
        return loginLabels;
    }
    
    private static BorderPane createLoginTextFieldsPane() {
        BorderPane loginTextFieldsPane = new BorderPane();
        usernameTextField = new TextField("toto");
        serverIPTextField = new TextField("127.0.0.1");
        
        usernameTextField.setOnAction((ActionEvent) -> {
            serverIPTextField.requestFocus();
        });
        
        serverIPTextField.setOnAction((ActionEvent) -> {
            login();
        });
        
        loginTextFieldsPane.setTop(usernameTextField);
        loginTextFieldsPane.setBottom(serverIPTextField);
        
        return loginTextFieldsPane;
    }
    
    private static BorderPane createLoginFieldsPane() {
        BorderPane loginFields = new BorderPane();
        
        loginFields.setLeft(createLoginLabelsPane());
        loginFields.setRight(createLoginTextFieldsPane());
        
        return loginFields;
    }
    
    private static BorderPane createLoginButtonsPane() {
        BorderPane loginButtonPane = new BorderPane();
        Button connectButton = new Button("Connect");
        Button cancelButton = new Button("Cancel");
        errorLabel = new Label("");
        
        cancelButton.setOnAction((ActionEvent t) -> {
            mainStage.close();
        });
        
        connectButton.setOnAction((ActionEvent t) -> {
            login();
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
    
    private static MenuBar createChatMenuPane() {
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
    
    private static BorderPane createChatUserListPane() {
        BorderPane chatUserListPane = new BorderPane();
        chatUserListView = new ListView(userList);
        userList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (String s : c.getAddedSubList()) {
                            chatUserListView.getItems().add(s);
                        }
                    } else if (c.wasRemoved()) {
                        for (String s : c.getRemoved()) {
                            chatUserListView.getItems().remove(s);
                        }
                    }
                }
            }
        });
        
        chatUserListPane.setCenter(chatUserListView);
        
        return chatUserListPane;
    }
    
    private static BorderPane createChatPromptPane() {
        BorderPane chatPromptPane = new BorderPane();
        Button sendMessageButton = new Button("Send");
        chatPromptText = new TextField();
        
        sendMessageButton.setOnAction((ActionEvent t) -> {
            parseInput(chatPromptText.getText());
            chatPromptText.clear();
        });
        
        // Handle chat history
        chatPromptText.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                switch (t.getCode()) {
                    case UP:
                        if (clientMessageHistoryOffset < clientMessageHistory.size()){
                            clientMessageHistoryOffset++;
                        }
                        if (!clientMessageHistory.isEmpty() && clientMessageHistoryOffset > 0){
                            chatPromptText.setText(clientMessageHistory.get(clientMessageHistory.size() - clientMessageHistoryOffset));
                        }
                        break;
                    case DOWN:
                        if (clientMessageHistoryOffset >= 0){
                            clientMessageHistoryOffset--;
                        }
                        if (!clientMessageHistory.isEmpty() && clientMessageHistoryOffset > 0){
                            chatPromptText.setText(clientMessageHistory.get(clientMessageHistory.size() - clientMessageHistoryOffset));
                        } else if (clientMessageHistoryOffset == 0){
                            // TODO: put text that was here in a buffer and return to it
                            chatPromptText.setText("");
                        }
                        break;
                    case ENTER:
                        parseInput(chatPromptText.getText());
                        chatPromptText.clear();
                        break;
                }
            }
        });
        
        chatPromptPane.setCenter(chatPromptText);
        chatPromptPane.setRight(sendMessageButton);
        
        return chatPromptPane;
    }
    
    private static BorderPane createGUIChatClient() {
        BorderPane guiChatClient = new BorderPane();
        chatTextArea = new TextArea();
        chatTextArea.setEditable(false);
        messagesList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (String s : c.getAddedSubList()) {
                            displayMessages(s);
                        }
                    }
                }
            }
        });
        
        guiChatClient.setTop(createChatMenuPane());
        guiChatClient.setCenter(chatTextArea);
        guiChatClient.setRight(createChatUserListPane());
        guiChatClient.setBottom(createChatPromptPane());
        
        return guiChatClient;
    }
    
    private static void displayMessages(String s) {
        chatTextArea.appendText(s + "\n");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        currentState = ApplicationState.LOGON_SCREEN;
        Scene scene = new Scene(createGUILoginClient());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("RMI Chat Client");
        primaryStage.show();
    }
    
    private static void logout() {
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
    
    private static void login() {
        try {
            messagesList = FXCollections.observableArrayList();
            userList = FXCollections.observableArrayList();
            ClientHandlerImpl client = new ClientHandlerImpl(usernameTextField.getText(), messagesList, userList);
            client_stub = (ClientHandler) UnicastRemoteObject.exportObject(client, 0);
            // Get remote object reference
            Registry registry = LocateRegistry.getRegistry(serverIPTextField.getText());
            try {
                serverHandler = (ServerHandler) registry.lookup("ServerHandler");
            } catch (NotBoundException | AccessException ex) {
                Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (serverHandler.connect(client_stub) == true) {
                
                // Obtaining user list
                ArrayList<String> users = serverHandler.getConnectedUsers();
                clientMessageHistory = new ArrayList<>();
                
                if (!users.isEmpty()) {
                    userList.addAll(users);
                }
                // Creating main interface
                Scene scene = new Scene(createGUIChatClient());
                mainStage.setScene(scene);
                chatPromptText.requestFocus();
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
    
    private static void exit() {
        switch (currentState) {
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
        if (!input.isEmpty()) {
            if (input.startsWith("/")) {
                executeCommand(input);
            } else {
                try {
                    serverHandler.sendMessage(client_stub, input);
                } catch (RemoteException ex) {
                    Logger.getLogger(GUI_Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            clientMessageHistory.add(input);
            clientMessageHistoryOffset = 0;
        }
    }
    
    private static void executeCommand(String input) {
        // Remove leading '/'
        input = input.substring(1).toLowerCase();
        switch (input) {
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
                addToMessages("Command unknown.");
                break;
        }
    }
    
    private static void addToMessages(String s) {
        messagesList.add(s);
    }
    
    private static void addToMessages(ArrayList<String> list) {
        messagesList.addAll(list);
    }
    
    private static void printHelp() {
        addToMessages("RMI Chat V" + versionString);
        addToMessages("Commands :");
        addToMessages("/help\t\tPrint this message.");
        addToMessages("/logout\tDisconnect the client and exit.");
        addToMessages("/quit\t\tDisconnect the client and exit.");
        addToMessages("/history\tGet the server session message history.");
        addToMessages("/allhistory\tGet the server complete message history.");
        addToMessages("/users\tGet current user list.");
    }
    
    private static void getHistory() {
        ArrayList<String> history;
        try {
            history = serverHandler.getHistory();
            
            for (String line : history) {
                addToMessages(line);
            }
            addToMessages("");
        } catch (RemoteException ex) {
            addToMessages("Unable to get history.");
        }
    }
    
    private static void getUsers() {
        ArrayList<String> users;
        try {
            users = serverHandler.getConnectedUsers();
            
            if (!users.isEmpty()) {
                userList.addAll(users);
            }
            
        } catch (RemoteException ex) {
            addToMessages("Unable to get user list.");
            addToMessages(ex.getMessage());
        }
    }
    
    private static void getAllHistory() {
        ArrayList<String> history;
        try {
            history = serverHandler.getAllHistory();
            
            for (String line : history) {
                addToMessages(line);
            }
            addToMessages("");
        } catch (RemoteException ex) {
            addToMessages("Unable to get history.");
            addToMessages(ex.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        exit();
    }
}
