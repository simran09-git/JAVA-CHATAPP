package com.javachat.ui;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.javachat.client.ChatClient;
import com.javachat.database.MessageDAO;
import com.javachat.database.UserDAO;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ChatWindow {

// =========================================================
// FIELDS
// =========================================================

private VBox messageBox;
private TextField messageInput;
private ChatClient chatClient;

private final String loggedInUsername;

private Label chatName;
private Label onlineStatus;
private Label lastSeenStatus;

private String selectedUser = null;

// =========================================================
// PERSISTENT UI SETTINGS
// =========================================================

private final Preferences preferences =
        Preferences.userRoot().node("JavaChat");

private ImageView wallpaperView;
private Region wallpaperTint;
private StackPane messageLayer;

// =========================================================
// SIDEBAR DATA
// =========================================================

// username -> last message label
private final Map<String, Label> lastMessageLabels =
        new HashMap<>();

// username -> time label
private final Map<String, Label> timeLabels =
        new HashMap<>();

// username -> unread badge label
private final Map<String, Label> unreadLabels =
        new HashMap<>();

// username -> unread count
private final Map<String, Integer> unreadCounts =
        new HashMap<>();

// username -> user row
private final Map<String, HBox> userRows =
        new HashMap<>();


// =========================================================
// CONSTRUCTOR
// =========================================================

public ChatWindow(String username) {

    this.loggedInUsername = username;
}


// =========================================================
// SHOW
// =========================================================

public void show(Stage stage) {

    // =====================================================
    // CONNECT TO SERVER
    // =====================================================

    try {

        chatClient =
                new ChatClient(
                        loggedInUsername
                );

        chatClient.connect();

        chatClient.startListening(
                new ChatClient.MessageListener() {

                    @Override
                    public void onMessageReceived(
                            String message) {

                        Platform.runLater(() -> {

                            handleIncomingMessage(
                                    message
                            );

                            scrollToBottom();
                        });
                    }


                    @Override
                    public void onDisconnected() {

                        Platform.runLater(() -> {

                            if (onlineStatus != null) {

                                onlineStatus.setText(
                                        "● Disconnected"
                                );

                                onlineStatus.setStyle(
                                        "-fx-text-fill: #ef4444;"
                                );
                            }
                        });
                    }
                }
        );

    } catch (IOException e) {

        System.out.println(
                "Could not connect to server."
        );

        System.out.println(
                "Error: " + e.getMessage()
        );
    }


    // =====================================================
    // ROOT
    // =====================================================

    BorderPane root =
            new BorderPane();

    root.getStyleClass().add(
            "chat-root"
    );


    // =====================================================
    // SIDEBAR
    // =====================================================

    VBox sidebar =
            new VBox(10);

    sidebar.getStyleClass().add(
            "sidebar"
    );

    sidebar.setPrefWidth(300);


    // =====================================================
    // LOGO
    // =====================================================

    Label logo =
            new Label("💬 JavaChat");

    logo.getStyleClass().add(
            "logo"
    );


    Label subtitle =
            new Label(
                    "Real-time messaging"
            );

    subtitle.getStyleClass().add(
            "subtitle"
    );


    VBox logoBox =
            new VBox(3);

    logoBox.getChildren().addAll(
            logo,
            subtitle
    );


    // =====================================================
    // SEARCH
    // =====================================================

    TextField searchField =
            new TextField();

    searchField.setPromptText(
            "Search conversations"
    );

    searchField.getStyleClass().add(
            "search-field"
    );


    // =====================================================
    // USER LIST
    // =====================================================

    VBox userList =
            new VBox(8);

    loadUsers(userList);

    VBox.setVgrow(
            userList,
            Priority.ALWAYS
    );


    // =====================================================
    // SEARCH FILTER
    // =====================================================

    searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> {

                String search =
                        newValue == null
                                ? ""
                                : newValue
                                .trim()
                                .toLowerCase();

                for (
                        Map.Entry<String, HBox> entry
                        : userRows.entrySet()
                ) {

                    String username =
                            entry.getKey();

                    HBox row =
                            entry.getValue();

                    boolean visible =
                            username
                                    .toLowerCase()
                                    .contains(search);

                    row.setVisible(visible);
                    row.setManaged(visible);
                }
            }
    );


    // =====================================================
    // SETTINGS
    // =====================================================

    Button settingsButton =
            new Button("⚙ Settings");

    settingsButton.getStyleClass().add(
            "sidebar-button"
    );
    settingsButton.setOnAction(
    event -> {

        SettingsWindow settingsWindow =
                new SettingsWindow();

        settingsWindow.show(
                stage
        );
    }

);
// =====================================================
// PROFILE
// =====================================================

    Button profileButton =
            new Button(
                    "👤 " + loggedInUsername
            );
            profileButton.setOnAction(
    event -> {

        ProfileWindow profileWindow =
                new ProfileWindow(
                        loggedInUsername
                );

        profileWindow.show(stage);
    }

);

    profileButton.getStyleClass().add(
            "profile-button"
    );


    sidebar.getChildren().addAll(
            logoBox,
            searchField,
            userList,
            settingsButton,
            profileButton
    );


    VBox.setMargin(
            logoBox,
            new Insets(
                    0,
                    0,
                    15,
                    0
            )
    );


    VBox.setMargin(
            searchField,
            new Insets(
                    0,
                    0,
                    10,
                    0
            )
    );


    root.setLeft(sidebar);


    // =====================================================
    // CHAT HEADER
    // =====================================================

    HBox chatHeader =
            new HBox(10);

    chatHeader.getStyleClass().add(
            "chat-header"
    );

    chatHeader.setAlignment(
            Pos.CENTER_LEFT
    );


    VBox userInfo =
            new VBox(3);


    chatName =
            new Label(
                    "Select a user"
            );

    chatName.getStyleClass().add(
            "chat-name"
    );


    onlineStatus =
            new Label(
                    "● Offline"
            );

    onlineStatus.getStyleClass().add(
            "online-status"
    );


    lastSeenStatus =
            new Label(
                    ""
            );

    lastSeenStatus.getStyleClass().add(
            "last-seen-status"
    );


    userInfo.getChildren().addAll(
            chatName,
            onlineStatus,
            lastSeenStatus
    );


    Region headerSpacer =
            new Region();

    HBox.setHgrow(
            headerSpacer,
            Priority.ALWAYS
    );


    Button searchButton =
            new Button("🔍");

    searchButton.getStyleClass().add(
            "header-button"
    );

searchButton.setOnAction(
event -> {

        if (messageBox == null) {
            return;
        }

        TextInputDialog dialog =
                new TextInputDialog();

        dialog.setTitle(
                "Search Messages"
        );

        dialog.setHeaderText(
                "Search in current chat"
        );

        dialog.setContentText(
                "Enter text:"
        );

        dialog.showAndWait().ifPresent(
                searchText -> {

                    String query =
                            searchText
                                    .trim()
                                    .toLowerCase();

                    if (query.isEmpty()) {
                        return;
                    }

                    boolean found = false;

                    for (var node :
                            messageBox.getChildren()) {

                        if (node instanceof VBox box) {

                            for (var child :
                                    box.getChildren()) {

                                if (child instanceof Label label) {

                                    String text =
                                            label.getText();

                                    if (text != null
                                            && text
                                            .toLowerCase()
                                            .contains(query)) {

                                        label.setStyle(
                                                "-fx-background-color: #fef08a;"
                                                        + "-fx-text-fill: #111827;"
                                                        + "-fx-background-radius: 8;"
                                                        + "-fx-padding: 6 10;"
                                        );

                                        found = true;
                                    }
                                }
                            }
                        }
                    }

                    if (!found) {

                        addReceivedMessage(
                                "🔍 No message found for: "
                                        + searchText,
                                "Now"
                        );

                        scrollToBottom();
                    }
                }
        );
    }

);

    Button callButton =
            new Button("📞");

    callButton.getStyleClass().add(
            "header-button"
    );

    callButton.setOnAction(event -> {
        if (selectedUser == null || selectedUser.trim().isEmpty()) {
            return;
        }

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JavaChat Call");
        alert.setHeaderText("Calling " + selectedUser);
        alert.setContentText("Voice call request started.");
        alert.showAndWait();
    });


    Button moreButton =
            new Button("⋮");

    moreButton.getStyleClass().add(
            "header-button"
    );

    // =====================================================
    // THREE-DOT MENU
    // =====================================================

    ContextMenu moreMenu = new ContextMenu();

    MenuItem contactInfoItem =
            new MenuItem("👤 Contact info");
    MenuItem menuSearchItem =
            new MenuItem("🔍 Search");
    MenuItem clearChatItem =
            new MenuItem("🗑 Clear chat");
    MenuItem muteItem =
            new MenuItem("🔕 Mute notifications");
    MenuItem wallpaperItem =
            new MenuItem("🎨 Wallpaper");
    MenuItem blockItem =
            new MenuItem("🚫 Block");
    MenuItem reportItem =
            new MenuItem("⚠ Report");

    contactInfoItem.setOnAction(event -> {
        if (selectedUser == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact info");
        alert.setHeaderText(selectedUser);
        alert.setContentText("JavaChat contact\n\nStatus: "
                + (onlineStatus == null ? "" : onlineStatus.getText()));
        alert.showAndWait();
    });

    menuSearchItem.setOnAction(event -> searchButton.fire());

    clearChatItem.setOnAction(event -> {
        if (messageBox != null) {
            messageBox.getChildren().clear();
            if (selectedUser != null) {
                updateLastMessagePreview(selectedUser, "Start a conversation", "");
            }
        }
    });

    muteItem.setOnAction(event -> {
        if (selectedUser == null) return;
        String key = "mute_" + selectedUser;
        boolean value = preferences.getBoolean(key, false);
        preferences.putBoolean(key, !value);
        muteItem.setText(!value ? "🔔 Unmute notifications" : "🔕 Mute notifications");
    });

    wallpaperItem.setOnAction(event -> chooseWallpaper(stage));

    blockItem.setOnAction(event -> {
        if (selectedUser == null) return;

        String key = "block_" + selectedUser;
        boolean currentlyBlocked = preferences.getBoolean(key, false);

        if (chatClient == null || !chatClient.isConnected()) {
            return;
        }

        if (currentlyBlocked) {

            chatClient.sendBlockCommand(
                    selectedUser,
                    false
            );

            // The local value is changed immediately so the UI
            // can stop hiding the contact. The server confirmation
            // will trigger the actual status re-check.
            preferences.putBoolean(
                    key,
                    false
            );

            blockItem.setText("🚫 Block");

            onlineStatus.setText(
                    "● Checking..."
            );

            onlineStatus.setStyle(
                    "-fx-text-fill: #f59e0b;"
            );

            lastSeenStatus.setText(
                    ""
            );

        } else {

            chatClient.sendBlockCommand(
                    selectedUser,
                    true
            );

            preferences.putBoolean(
                    key,
                    true
            );

            blockItem.setText("✅ Unblock");

            // Locally hide status immediately.
            onlineStatus.setText("● Offline");
            onlineStatus.setStyle(
                    "-fx-text-fill: #9ca3af;"
            );
            lastSeenStatus.setText(
                    "Last seen unavailable"
            );
        }
    });

    reportItem.setOnAction(event -> {
        if (selectedUser == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report");
        alert.setHeaderText("Report " + selectedUser);
        alert.setContentText("Report option selected.");
        alert.showAndWait();
    });

    moreMenu.getItems().addAll(
            contactInfoItem, menuSearchItem, clearChatItem,
            muteItem, wallpaperItem, blockItem, reportItem
    );

    moreButton.setOnAction(event -> {
        if (selectedUser == null) return;

        boolean muted = preferences.getBoolean("mute_" + selectedUser, false);
        muteItem.setText(muted ? "🔔 Unmute notifications" : "🔕 Mute notifications");

        boolean blocked = preferences.getBoolean("block_" + selectedUser, false);
        blockItem.setText(blocked ? "✅ Unblock" : "🚫 Block");

        styleContextMenu(moreMenu);
        moreMenu.show(moreButton, Side.BOTTOM, 0, 5);
    });


    chatHeader.getChildren().addAll(
            userInfo,
            headerSpacer,
            searchButton,
            callButton,
            moreButton
    );


    // =====================================================
    // MESSAGE AREA
    // =====================================================

    messageBox =
            new VBox(12);

    messageBox.getStyleClass().add(
            "message-box"
    );


    ScrollPane scrollPane =
            new ScrollPane(
                    messageBox
            );

    scrollPane.setFitToWidth(true);

    scrollPane.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER
    );

    scrollPane.getStyleClass().add(
            "message-scroll"
    );


    VBox.setVgrow(
            scrollPane,
            Priority.ALWAYS
    );


    // =====================================================
    // INPUT AREA
    // =====================================================

    HBox inputArea =
            new HBox(10);

    inputArea.getStyleClass().add(
            "input-area"
    );

    inputArea.setAlignment(
            Pos.CENTER
    );

// =====================================================
// ATTACHMENT BUTTON
// =====================================================

Button attachmentButton =
new Button("📎");

attachmentButton.getStyleClass().add(
"input-button"
);
attachmentButton.setOnAction(
event -> {

        if (selectedUser == null
                || selectedUser.trim().isEmpty()) {

            addReceivedMessage(
                    "Please select a user first.",
                    "Now"
            );

            return;
        }


        if (chatClient == null
                || !chatClient.isConnected()) {

            addReceivedMessage(
                    "Not connected to server.",
                    "Now"
            );

            return;
        }


        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Choose a file"
        );


        fileChooser.getExtensionFilters().addAll(

                new FileChooser.ExtensionFilter(
                        "Images",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif",
                        "*.webp"
                ),

                new FileChooser.ExtensionFilter(
                        "Documents",
                        "*.pdf",
                        "*.doc",
                        "*.docx",
                        "*.txt"
                ),

                new FileChooser.ExtensionFilter(
                        "All Files",
                        "*.*"
                )
        );


        File selectedFile =
                fileChooser.showOpenDialog(
                        attachmentButton
                                .getScene()
                                .getWindow()
                );


        if (selectedFile == null) {
            return;
        }


        try {

            chatClient.sendFile(
                    selectedUser,
                    selectedFile
            );


            addSentMessage(
                    "📎 "
                            + selectedFile.getName(),
                    "Now"
            );


            updateLastMessagePreview(
                    selectedUser,
                    "📎 "
                            + selectedFile.getName(),
                    "Now"
            );


            clearUnread(
                    selectedUser
            );


            scrollToBottom();


            System.out.println(
                    "File sent: "
                            + selectedFile.getName()
                            + " -> "
                            + selectedUser
            );


        } catch (Exception e) {

            System.out.println(
                    "File sending error: "
                            + e.getMessage()
            );


            addReceivedMessage(
                    "File could not be sent.",
                    "Now"
            );            }
    }

);
// =====================================================
// EMOJI BUTTON
// =====================================================

Button emojiButton =
new Button("😊");

emojiButton.getStyleClass().add(
"input-button"
);

// =====================================================
// EMOJI MENU
// =====================================================

ContextMenu emojiMenu =
new ContextMenu();

String[] emojis = {

    "😀", "😃", "😄", "😁",
    "😂", "🤣", "😊", "😍",
    "🥰", "😘", "😎", "🤗",

    "❤️", "💕", "💖", "💔",
    "👍", "👎", "👏", "🙏",
    "🔥", "✨", "🎉", "🎊",

    "😢", "😭", "😡", "😱",
    "🤔", "😴", "🙄", "😅"

};

// =====================================================
// ADD EMOJIS TO MENU
// =====================================================

for (String emoji : emojis) {

MenuItem item =
        new MenuItem(
                emoji
        );


item.setOnAction(
        event -> {

            int position =
                    messageInput
                            .getCaretPosition();


            messageInput.insertText(
                    position,
                    emoji
            );


            messageInput.requestFocus();
        }
);


emojiMenu.getItems().add(
        item
);

}

// =====================================================
// EMOJI BUTTON ACTION
// =====================================================

emojiButton.setOnAction(
event -> {

        emojiMenu.show(
                emojiButton,
                Side.BOTTOM,
                0,
                5
        );
    }

);
messageInput =
new TextField();

    messageInput.setPromptText(
            "Select a user and type a message..."
    );

    messageInput.getStyleClass().add(
            "message-input"
    );


    HBox.setHgrow(
            messageInput,
            Priority.ALWAYS
    );


    Button sendButton =
            new Button("➤");

    sendButton.getStyleClass().add(
            "send-button"
    );


    sendButton.setOnAction(
            event -> sendMessage()
    );


    messageInput.setOnAction(
            event -> sendMessage()
    );


    inputArea.getChildren().addAll(
            attachmentButton,
            emojiButton,
            messageInput,
            sendButton
    );


    // =====================================================
    // CHAT AREA
    // =====================================================

    BorderPane chatArea =
            new BorderPane();

    chatArea.setTop(
            chatHeader
    );

    // =====================================================
    // CHAT WALLPAPER LAYER
    // =====================================================

    wallpaperView = new ImageView();
    wallpaperView.setPreserveRatio(false);
    wallpaperView.setSmooth(true);
    wallpaperView.setOpacity(0.95);
    wallpaperView.setMouseTransparent(true);

    wallpaperTint = new Region();
    wallpaperTint.setStyle(
            "-fx-background-color: rgba(37, 99, 235, 0.16);"
    );
    wallpaperTint.setMouseTransparent(true);

    messageLayer = new StackPane();

    wallpaperView.fitWidthProperty().bind(messageLayer.widthProperty());
    wallpaperView.fitHeightProperty().bind(messageLayer.heightProperty());
    wallpaperTint.prefWidthProperty().bind(messageLayer.widthProperty());
    wallpaperTint.prefHeightProperty().bind(messageLayer.heightProperty());

    makeTransparentScrollPane(scrollPane);

    messageLayer.getChildren().addAll(
            wallpaperView, wallpaperTint, scrollPane
    );

    chatArea.setCenter(messageLayer);

    chatArea.setBottom(
            inputArea
    );


    root.setCenter(
            chatArea
    );


    // =====================================================
    // SCENE
    // =====================================================

    Scene scene =
            new Scene(
                    root,
                    1200,
                    750
            );


    // =====================================================
    // CSS
    // =====================================================

    var css =
            getClass().getResource(
                    "/css/chat.css"
            );


    if (css != null) {

        scene.getStylesheets().add(
                css.toExternalForm()
        );

    } else {

        System.out.println(
                "WARNING: chat.css not found!"
        );
    }


    // =====================================================
    // STAGE
    // =====================================================

    stage.setTitle(
            "JavaChat - "
                    + loggedInUsername
    );

    stage.setScene(scene);

    applySavedDarkMode(scene);

    stage.setMinWidth(
            950
    );

    stage.setMinHeight(
            600
    );

stage.setOnCloseRequest(event -> {

if (chatClient != null) {

    Thread disconnectThread =
            new Thread(() -> {

                try {

                    chatClient.disconnect();

                } catch (Exception e) {

                    System.out.println(
                            "Disconnect error: "
                                    + e.getMessage()
                    );
                }
            });

    disconnectThread.setDaemon(true);

    disconnectThread.start();
}

});
stage.show();
}

// =========================================================
// LOAD USERS
// =========================================================

private void loadUsers(
        VBox userList) {

    List<String> users =
            UserDAO.getAllUsers(
                    loggedInUsername
            );


    if (users.isEmpty()) {

        Label empty =
                new Label(
                        "No other users registered"
                );

        empty.setStyle(
                "-fx-text-fill: #9ca3af;"
                        + "-fx-padding: 15;"
        );

        userList.getChildren().add(
                empty
        );

        return;
    }


    for (String username : users) {

        unreadCounts.put(
                username,
                0
        );


        HBox userItem =
                createUser(
                        username
                );


        userRows.put(
                username,
                userItem
        );


        userList.getChildren().add(
                userItem
        );


        loadLatestMessagePreview(
                username
        );
    }
}


// =========================================================
// CREATE USER ITEM
// =========================================================

private HBox createUser(
        String username) {

    HBox user =
            new HBox(12);

    user.getStyleClass().add(
            "user-item"
    );

    user.setAlignment(
            Pos.CENTER_LEFT
    );


    // =====================================================
    // AVATAR
    // =====================================================

    Label avatar =
            new Label(
                    username
                            .substring(
                                    0,
                                    1
                            )
                            .toUpperCase()
            );

    avatar.getStyleClass().add(
            "avatar"
    );


    // =====================================================
    // USER INFO
    // =====================================================

    VBox info =
            new VBox(4);

    HBox.setHgrow(
            info,
            Priority.ALWAYS
    );


    Label nameLabel =
            new Label(
                    username
            );

    nameLabel.getStyleClass().add(
            "user-name"
    );


    Label messageLabel =
            new Label(
                    "Start a conversation"
            );

    messageLabel.getStyleClass().add(
            "last-message"
    );

    messageLabel.setMaxWidth(
            190
    );


    info.getChildren().addAll(
            nameLabel,
            messageLabel
    );


    // =====================================================
    // RIGHT SIDE
    // =====================================================

    VBox rightInfo =
            new VBox(3);

    rightInfo.setAlignment(
            Pos.TOP_RIGHT
    );


    Label timeLabel =
            new Label(
                    ""
            );

    timeLabel.getStyleClass().add(
            "message-time"
    );


    Label unreadLabel =
            new Label(
                    ""
            );

    unreadLabel.setVisible(
            false
    );

    unreadLabel.setManaged(
            false
    );

    unreadLabel.setMinWidth(
            20
    );

    unreadLabel.setMinHeight(
            20
    );

    unreadLabel.setAlignment(
            Pos.CENTER
    );

    unreadLabel.setStyle(
            "-fx-background-color: #2563eb;"
                    + "-fx-text-fill: white;"
                    + "-fx-font-size: 10px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-radius: 10px;"
                    + "-fx-padding: 2px 6px;"
    );


    rightInfo.getChildren().addAll(
            timeLabel,
            unreadLabel
    );


    // =====================================================
    // STORE LABEL REFERENCES
    // =====================================================

    lastMessageLabels.put(
            username,
            messageLabel
    );

    timeLabels.put(
            username,
            timeLabel
    );

    unreadLabels.put(
            username,
            unreadLabel
    );


    // =====================================================
    // BUILD ROW
    // =====================================================

    user.getChildren().addAll(
            avatar,
            info,
            rightInfo
    );


    // =====================================================
    // SELECT USER
    // =====================================================

    user.setOnMouseClicked(
            event -> selectUser(
                    username
            )
    );


    return user;
}


// =========================================================
// LOAD LAST MESSAGE PREVIEW
// =========================================================

private void loadLatestMessagePreview(
        String username) {

    List<MessageDAO.ChatMessage> history =
            MessageDAO.getConversation(
                    loggedInUsername,
                    username
            );


    if (history.isEmpty()) {

        updateLastMessagePreview(
                username,
                "Start a conversation",
                ""
        );

        return;
    }


    MessageDAO.ChatMessage latest =
            history.get(
                    history.size() - 1
            );


    updateLastMessagePreview(
            username,
            latest.getMessage(),
            formatTime(
                    latest.getSentAt()
            )
    );
}


// =========================================================
// UPDATE LAST MESSAGE PREVIEW
// =========================================================

private void updateLastMessagePreview(
        String username,
        String message,
        String time) {

    Label messageLabel =
            lastMessageLabels.get(
                    username
            );

    Label timeLabel =
            timeLabels.get(
                    username
            );


    if (messageLabel != null) {

        messageLabel.setText(
                message
        );
    }


    if (timeLabel != null) {

        timeLabel.setText(
                time == null
                        ? ""
                        : time
        );
    }
}


// =========================================================
// INCREMENT UNREAD
// =========================================================

private void incrementUnread(
        String username) {

    int count =
            unreadCounts.getOrDefault(
                    username,
                    0
            );

    count++;


    unreadCounts.put(
            username,
            count
    );


    Label unreadLabel =
            unreadLabels.get(
                    username
            );


    if (unreadLabel != null) {

        unreadLabel.setText(
                count > 99
                        ? "99+"
                        : String.valueOf(count)
        );

        unreadLabel.setVisible(
                true
        );

        unreadLabel.setManaged(
                true
        );
    }
}


// =========================================================
// CLEAR UNREAD
// =========================================================

private void clearUnread(
        String username) {

    unreadCounts.put(
            username,
            0
    );


    Label unreadLabel =
            unreadLabels.get(
                    username
            );


    if (unreadLabel != null) {

        unreadLabel.setText(
                ""
        );

        unreadLabel.setVisible(
                false
        );

        unreadLabel.setManaged(
                false
        );
    }
}


// =========================================================
// SELECT USER
// =========================================================

private void selectUser(
        String username) {

    selectedUser = username;

    loadWallpaperForSelectedUser();


    // Clear unread count
    clearUnread(
            username
    );


    chatName.setText(
            username
    );

    lastSeenStatus.setText(
            ""
    );


    // =====================================================
    // CHECK ONLINE STATUS
    // =====================================================

    onlineStatus.setText(
            "● Checking..."
    );

    onlineStatus.setStyle(
            "-fx-text-fill: #f59e0b;"
    );


    if (chatClient != null
            && chatClient.isConnected()) {

        chatClient.checkUserStatus(
                username
        );

        } else {

            onlineStatus.setText(
                    "● Disconnected"
            );

            onlineStatus.setStyle(
                    "-fx-text-fill: #ef4444;"
            );
        }


    // =====================================================
    // INPUT
    // =====================================================

    messageInput.setPromptText(
            "Message " + username + "..."
    );


    // =====================================================
    // CLEAR CURRENT CHAT
    // =====================================================

    messageBox.getChildren().clear();


    // =====================================================
    // LOAD HISTORY
    // =====================================================

    loadChatHistory(
            username
    );


    scrollToBottom();
}


// =========================================================
// LOAD CHAT HISTORY
// =========================================================

private void loadChatHistory(
        String username) {

    List<MessageDAO.ChatMessage> history =
            MessageDAO.getConversation(
                    loggedInUsername,
                    username
            );


    if (history.isEmpty()) {

        addReceivedMessage(
                "No previous messages.",
                ""
        );

        updateLastMessagePreview(
                username,
                "Start a conversation",
                ""
        );

        return;
    }


    MessageDAO.ChatMessage latest =
            history.get(
                    history.size() - 1
            );


    updateLastMessagePreview(
            username,
            latest.getMessage(),
            formatTime(
                    latest.getSentAt()
            )
    );


    for (
            MessageDAO.ChatMessage chatMessage
            : history
    ) {

        String sender =
                chatMessage.getSender();

        String text =
                chatMessage.getMessage();

        String time =
                formatTime(
                        chatMessage.getSentAt()
                );


        if (sender.equals(
                loggedInUsername
        )) {

            addSentMessage(
                    text,
                    time
            );

        } else {

            addReceivedMessage(
                    text,
                    time
            );
        }
    }


    scrollToBottom();
}


// =========================================================
// FORMAT TIME
// =========================================================

private String formatTime(
        java.sql.Timestamp timestamp) {

    if (timestamp == null) {

        return "";
    }


    String time =
            timestamp
                    .toLocalDateTime()
                    .toLocalTime()
                    .toString();


    if (time.length() >= 5) {

        return time.substring(
                0,
                5
        );
    }


    return time;
}


// =========================================================
// HANDLE INCOMING MESSAGE
// =========================================================

private void handleIncomingMessage(
        String message) {

    if (message == null || message.trim().isEmpty()) {
        return;
    }

    message = message.trim();

    System.out.println(
            "CHAT WINDOW RECEIVED >>> " + message
    );

    // =====================================================
    // ONLINE / OFFLINE STATUS BROADCAST
    // =====================================================

    if (message.startsWith("STATUS:")) {

        String data =
                message.substring("STATUS:".length());

        String[] parts =
                data.split(":", 2);

        if (parts.length >= 2) {

            String username =
                    parts[0].trim();

            String status =
                    parts[1].trim();

            if (selectedUser != null
                    && selectedUser.equals(username)) {

                if (status.equalsIgnoreCase("ONLINE")) {

                    onlineStatus.setText("● Online");
                    onlineStatus.setStyle(
                            "-fx-text-fill: #22c55e;"
                    );
                    lastSeenStatus.setText("");

                } else if (status.equalsIgnoreCase("OFFLINE")) {

                    onlineStatus.setText("● Offline");
                    onlineStatus.setStyle(
                            "-fx-text-fill: #9ca3af;"
                    );
                }
            }
        }

        return;
    }

    // =====================================================
    // BLOCK CONFIRMATION
    // =====================================================

    if (message.startsWith("BLOCKED:")) {

        String blockedUser =
                message.substring("BLOCKED:".length()).trim();

        if (selectedUser != null
                && selectedUser.equals(blockedUser)) {

            onlineStatus.setText("● Offline");
            onlineStatus.setStyle(
                    "-fx-text-fill: #9ca3af;"
            );

            lastSeenStatus.setText(
                    "Last seen unavailable"
            );
        }

        return;
    }

    // =====================================================
    // UNBLOCK CONFIRMATION
    // =====================================================

    if (message.startsWith("UNBLOCKED:")) {

        String unblockedUser =
                message.substring("UNBLOCKED:".length()).trim();

        if (!unblockedUser.isEmpty()) {

            preferences.putBoolean(
                    "block_" + unblockedUser,
                    false
            );
        }

        if (selectedUser != null
                && selectedUser.equals(unblockedUser)
                && chatClient != null
                && chatClient.isConnected()) {

            onlineStatus.setText("● Checking...");
            onlineStatus.setStyle(
                    "-fx-text-fill: #f59e0b;"
            );

            lastSeenStatus.setText("");

            Platform.runLater(
                    () -> chatClient.checkUserStatus(
                            selectedUser
                    )
            );
        }

        return;
    }

    // =====================================================
    // STATUS RESPONSE
    // =====================================================

    if (message.startsWith("STATUS_RESPONSE:")) {

        String data =
                message.substring(
                        "STATUS_RESPONSE:".length()
                );

        String[] parts =
                data.split(":", 3);

        if (parts.length >= 2) {

            String username =
                    parts[0].trim();

            String status =
                    parts[1].trim();

            String lastSeen =
                    parts.length >= 3
                            ? parts[2].trim()
                            : "";

            System.out.println(
                    "STATUS RESPONSE PARSED >>> user="
                            + username
                            + " status="
                            + status
                            + " lastSeen="
                            + lastSeen
            );

            if (selectedUser != null
                    && selectedUser.equals(username)) {

                // =================================================
                // ONLINE
                // =================================================

                if (status.equalsIgnoreCase("ONLINE")) {

                    onlineStatus.setText("● Online");
                    onlineStatus.setStyle(
                            "-fx-text-fill: #22c55e;"
                    );

                    lastSeenStatus.setText("");

                    System.out.println(
                            "CHAT STATUS >>> "
                                    + username
                                    + " = ONLINE"
                    );

                }

                // =================================================
                // OFFLINE
                // =================================================

                else {

                    onlineStatus.setText("● Offline");
                    onlineStatus.setStyle(
                            "-fx-text-fill: #9ca3af;"
                    );

                    if (!lastSeen.isEmpty()
                            && !lastSeen.equalsIgnoreCase(
                                    "unavailable"
                            )) {

                        lastSeenStatus.setText(
                                "Last seen " + lastSeen
                        );

                    } else {

                        lastSeenStatus.setText(
                                "Last seen unavailable"
                        );
                    }

                    System.out.println(
                            "CHAT STATUS >>> "
                                    + username
                                    + " = OFFLINE"
                                    + " | Last seen = "
                                    + lastSeen
                    );
                }
            }
        }

        return;
    }

    // =====================================================
    // PRIVATE MESSAGE
    // =====================================================

    if (message.startsWith("FROM:")) {

        String data =
                message.substring(5);

        int separator =
                data.indexOf(':');

        if (separator > 0) {

            String sender =
                    data.substring(
                            0,
                            separator
                    ).trim();

            String text =
                    data.substring(
                            separator + 1
                    );

            updateLastMessagePreview(
                    sender,
                    text,
                    "Now"
            );

            // =================================================
            // CURRENT CHAT
            // =================================================

            if (selectedUser != null
                    && selectedUser.equals(sender)) {

                clearUnread(sender);

                // =================================================
                // FILE ATTACHMENT
                // =================================================

                if (text.startsWith("📎 FILE:")) {

                    try {

                        String fileData =
                                text.substring(
                                        "📎 FILE:".length()
                                );

                        int fileSeparator =
                                fileData.indexOf(':');

                        if (fileSeparator <= 0) {

                            addReceivedMessage(
                                    "📎 Invalid file received.",
                                    "Now"
                            );

                        } else {

                            String fileName =
                                    fileData.substring(
                                            0,
                                            fileSeparator
                                    ).trim();

                            String base64Data =
                                    fileData.substring(
                                            fileSeparator + 1
                                    ).trim();

                            byte[] fileBytes =
                                    Base64.getDecoder().decode(
                                            base64Data
                                    );

                            Path downloadFolder =
                                    Paths.get(
                                            System.getProperty(
                                                    "user.home"
                                            ),
                                            "Downloads",
                                            "JavaChat"
                                    );

                            Files.createDirectories(
                                    downloadFolder
                            );

                            Path savedFile =
                                    downloadFolder.resolve(
                                            Paths.get(
                                                    fileName
                                            ).getFileName()
                                    );

                            Files.write(
                                    savedFile,
                                    fileBytes
                            );

                            Label fileLabel =
                                    new Label(
                                            "📎 " + sender
                                                    + " sent: "
                                                    + fileName
                                                    + "\nClick to open"
                                    );

                            fileLabel.setWrapText(true);

                            fileLabel.getStyleClass().add(
                                    "received-message"
                            );

                            fileLabel.setOnMouseClicked(
                                    event -> {

                                        try {

                                            if (Desktop.isDesktopSupported()) {

                                                Desktop.getDesktop().open(
                                                        savedFile.toFile()
                                                );
                                            }

                                        } catch (Exception e) {

                                            System.out.println(
                                                    "Could not open file: "
                                                            + e.getMessage()
                                            );
                                        }
                                    }
                            );

                            messageBox.getChildren().add(
                                    fileLabel
                            );

                            System.out.println(
                                    "File received and saved: "
                                            + savedFile
                            );
                        }

                    } catch (Exception e) {

                        System.out.println(
                                "File receiving error: "
                                        + e.getMessage()
                        );

                        addReceivedMessage(
                                "📎 File could not be opened.",
                                "Now"
                        );
                    }

                } else {

                    addReceivedMessage(
                            text,
                            "Now"
                    );
                }

                scrollToBottom();

            } else {

                // =================================================
                // NOT CURRENT CHAT
                // =================================================

                incrementUnread(sender);
            }

            return;
        }
    }

    // =====================================================
    // NORMAL PRIVATE MESSAGE
    // =====================================================

    if (selectedUser != null) {

        String prefix =
                selectedUser + ":";

        if (message.startsWith(prefix)) {

            String text =
                    message.substring(
                            prefix.length()
                    ).trim();

            updateLastMessagePreview(
                    selectedUser,
                    text,
                    "Now"
            );

            addReceivedMessage(
                    text,
                    "Now"
            );

            clearUnread(
                    selectedUser
            );

            scrollToBottom();

            return;
        }
    }

    // =====================================================
    // SERVER MESSAGE
    // =====================================================

    if (message.startsWith("SERVER:")) {

        addReceivedMessage(
                message,
                "Now"
        );

        scrollToBottom();
    }
}


// =========================================================
// RECEIVED MESSAGE
// =========================================================

private void addReceivedMessage(
        String text,
        String time) {

    VBox container =
            new VBox(3);

    container.setAlignment(
            Pos.CENTER_LEFT
    );


    Label message =
            new Label(
                    text
            );

    message.setWrapText(
            true
    );

    message.getStyleClass().add(
            "received-message"
    );


    Label timestamp =
            new Label(
                    time
            );

    timestamp.getStyleClass().add(
            "timestamp"
    );


    container.getChildren().addAll(
            message,
            timestamp
    );


    messageBox.getChildren().add(
            container
    );
}


// =========================================================
// SENT MESSAGE
// =========================================================

private void addSentMessage(
        String text,
        String time) {

    VBox container =
            new VBox(3);

    container.setAlignment(
            Pos.CENTER_RIGHT
    );


    Label message =
            new Label(
                    text
            );

    message.setWrapText(
            true
    );

    message.getStyleClass().add(
            "sent-message"
    );


    Label timestamp =
            new Label(
                    time
            );

    timestamp.getStyleClass().add(
            "timestamp"
    );


    container.getChildren().addAll(
            message,
            timestamp
    );


    messageBox.getChildren().add(
            container
    );
}


// =========================================================
// SEND PRIVATE MESSAGE
// =========================================================

private void sendMessage() {

    String text =
            messageInput
                    .getText()
                    .trim();


    if (text.isEmpty()) {

        return;
    }


    if (selectedUser == null
            || selectedUser.trim().isEmpty()) {

        addReceivedMessage(
                "Please select a user first.",
                "Now"
        );

        return;
    }


    if (chatClient == null
            || !chatClient.isConnected()) {

        addReceivedMessage(
                "Not connected to server.",
                "Now"
        );

        return;
    }


    try {

        chatClient.sendPrivateMessage(
                selectedUser,
                text
        );


        // =================================================
        // SHOW SENT MESSAGE
        // =================================================

        addSentMessage(
                text,
                "Now"
        );


        // =================================================
        // UPDATE SIDEBAR PREVIEW
        // =================================================

        updateLastMessagePreview(
                selectedUser,
                text,
                "Now"
        );


        // A sent message is not unread
        clearUnread(
                selectedUser
        );


        messageInput.clear();

        scrollToBottom();


    } catch (Exception e) {

        System.out.println(
                "Error sending message: "
                        + e.getMessage()
        );


        addReceivedMessage(
                "Message could not be sent.",
                "Now"
        );
    }
}

// =========================================================
// SCROLL TO BOTTOM
// =========================================================

private void scrollToBottom() {

    if (messageBox == null) {

        return;
    }

    if (
            messageBox.getParent()
                    instanceof ScrollPane scrollPane
    ) {

        Platform.runLater(
                () ->
                        scrollPane.setVvalue(
                                1.0
                        )
        );
    }
}

// =========================================================
// LOGOUT
// =========================================================

private void logout(Stage stage) {

// Login screen immediately show
LoginWindow loginWindow =
        new LoginWindow();

loginWindow.show(stage);


// Disconnect server in background
Thread logoutThread =
        new Thread(() -> {

            try {

                if (chatClient != null) {

                    chatClient.disconnect();
                }

            } catch (Exception e) {

                System.out.println(
                        "Logout error: "
                                + e.getMessage()
                );
            }

        });

logoutThread.setDaemon(true);
logoutThread.start();


System.out.println(
        loggedInUsername
                + " logged out."
);

}

// =========================================================
// PERSISTENT CHAT WALLPAPER
// =========================================================

private String wallpaperPreferenceKey(String username) {
    if (username == null || username.trim().isEmpty()) {
        return "wallpaper_default";
    }
    return "wallpaper_" + username.trim();
}

private void loadWallpaperForSelectedUser() {
    if (wallpaperView == null) return;

    String path = preferences.get(
            wallpaperPreferenceKey(selectedUser),
            ""
    );

    if (path == null || path.trim().isEmpty()) {
        wallpaperView.setImage(null);
        return;
    }

    File file = new File(path);

    if (!file.exists() || !file.isFile()) {
        wallpaperView.setImage(null);
        return;
    }

    wallpaperView.setImage(
            new Image(file.toURI().toString())
    );
}

private void chooseWallpaper(Stage stage) {
    if (selectedUser == null || selectedUser.trim().isEmpty()
            || wallpaperView == null) {
        return;
    }

    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choose chat wallpaper");
    chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                    "Images",
                    "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"
            )
    );

    File file = chooser.showOpenDialog(stage);

    if (file == null) return;

    preferences.put(
            wallpaperPreferenceKey(selectedUser),
            file.getAbsolutePath()
    );

    wallpaperView.setImage(
            new Image(file.toURI().toString())
    );
}

private void makeTransparentScrollPane(ScrollPane scrollPane) {
    messageBox.setStyle(
            "-fx-background-color: transparent;"
    );

    scrollPane.setStyle(
            "-fx-background-color: transparent;"
                    + "-fx-background: transparent;"
    );

    Platform.runLater(() -> {
        javafx.scene.Node viewport =
                scrollPane.lookup(".viewport");

        if (viewport != null) {
            viewport.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-background: transparent;"
            );
        }

        javafx.scene.Node content =
                scrollPane.lookup(".content");

        if (content != null) {
            content.setStyle(
                    "-fx-background-color: transparent;"
            );
        }
    });
}

private void applySavedDarkMode(Scene scene) {
    if (scene == null) return;

    var darkCss = getClass().getResource("/css/dark.css");
    if (darkCss == null) return;

    String darkUrl = darkCss.toExternalForm();
    boolean dark = preferences.getBoolean("darkMode", false);

    if (dark) {
        if (!scene.getStylesheets().contains(darkUrl)) {
            scene.getStylesheets().add(darkUrl);
        }
    } else {
        scene.getStylesheets().remove(darkUrl);
    }
}

private void styleContextMenu(ContextMenu menu) {
    boolean dark = preferences.getBoolean("darkMode", false);

    menu.setStyle(
            "-fx-background-color: "
                    + (dark ? "#202c33" : "#ffffff") + ";"
                    + "-fx-background-radius: 8;"
                    + "-fx-padding: 6;"
    );

    String textColor = dark ? "#ffffff" : "#111827";

    for (MenuItem item : menu.getItems()) {
        item.setStyle(
                "-fx-text-fill: " + textColor + ";"
        );
    }
}

}
