package server.utils;

import dao.UserDao;
import model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserUtils {

    private static final Logger LOGGER = Logger.getLogger(UserUtils.class.getSimpleName());

    private static final int REQUEST_DISCONNECT_USER = 3;
    private static final int REQUEST_ACCEPT_MESSAGE = 10;
    private static final int REQUEST_ADD_FRIEND_ID = 11;

    private User currentUser;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
    private UserDao userDao;

    private boolean isConnected = true;

    public UserUtils(User currentUser, ObjectInputStream input, ObjectOutputStream output){
        this.currentUser = currentUser;
        this.output = output;
        this.input = input;
        userDao = new UserDao();
    }

    public void acceptUserRequest(){
        try{
            LOGGER.log(Level.INFO, "Waiting for user request");
            int request = input.readInt();
            switch (request){
                case REQUEST_ACCEPT_MESSAGE:
                    acceptMessage();
                    break;
                case REQUEST_ADD_FRIEND_ID:
                    addFriend();
                    break;
                case REQUEST_DISCONNECT_USER:
                    isConnected = false;
                    break;

                default:
                    LOGGER.log(Level.WARNING, "Request unrecognised");
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Error while reading request");
            isConnected = false;
        }
    }

    private void acceptMessage() {

    }

    private void addFriend(){
        try {
            String username = (String) input.readObject();
            User friend = userDao.userExists(username);
            if(friend != null){
                currentUser.addFriend(friend);
                output.writeBoolean(true);  //notifies that the searched user exists
                output.writeObject(currentUser);
                updateUser();
            }else {
                output.writeBoolean(false); //notifies that the searched user doesn't exist
            }
            output.flush();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Couldn't add friend");
        }
    }

    private void updateUser() {
        if(userDao.updateUser(currentUser))
            System.out.println("account updated successfully");
        else
            System.out.println("couldnt update account");

    }

    public boolean isConnected() {
        return isConnected;
    }
}
