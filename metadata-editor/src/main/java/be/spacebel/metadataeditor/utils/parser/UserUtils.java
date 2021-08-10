/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.models.user.UserPreferences;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;

/**
 * Saving/Loading user preferences to/from a local file utilities
 *
 * @author mng
 */
public class UserUtils {

    private static final Logger LOG = Logger.getLogger(UserUtils.class);

    private static final byte[] KEY = "spbMEInterPasswd".getBytes();
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static void main(String[] args) throws Exception {
        List<UserPreferences> users = new ArrayList<>();
        UserPreferences user = new UserPreferences();
        user.setUsername("admin");
        user.setPassword("21232F297A57A5A743894A0E4A801FC3");
        user.setRole("Administrator");
        user.setCatalogueUrls(new String[]{"https://spb-kube-ergo-master.spb.spacebel.be/eo-catalogue/"});
        users.add(user);

        user = new UserPreferences();
        user.setUsername("test");
        user.setPassword("098f6bcd4621d373cade4e832627b4f6");
        user.setRole("Registered user");
        user.setCatalogueUrls(new String[]{"https://spb-kube-ergo-master.spb.spacebel.be/eo-catalogue/"});
        users.add(user);

        UserUtils.saveUsers("D:/apps/metadata-editor/users", users);

        List<UserPreferences> loadedUsers = UserUtils.loadUsers("D:/apps/metadata-editor/users");
        loadedUsers.forEach((loadedUser) -> {
            System.out.println(loadedUser.debug());
        });

    }

    /**
     * Save list of UserPreferences objects to a local file
     *
     * @param usersFile The local file
     * @param users List of user preferences
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void saveUsers(String usersFile, List<UserPreferences> users) throws IOException {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            UserList userList = new UserList();
            userList.setUsers(users);

            SealedObject sealedUserList = new SealedObject(userList, cipher);

            FileOutputStream fos = new FileOutputStream(usersFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            CipherOutputStream cos = new CipherOutputStream(bos, cipher);
            try (ObjectOutputStream oos = new ObjectOutputStream(cos)) {
                oos.writeObject(sealedUserList);
                oos.flush();
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            LOG.error("Error occurs while saving users: " + e.getMessage());
            throw new IOException(e);
        }
    }

    /**
     * Load list of UserPreferences objects from a local file
     *
     * @param usersFile The local file
     * @return List of UserPreferences objects
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<UserPreferences> loadUsers(String usersFile) throws IOException {
        LOG.debug("Load user from file " + usersFile);
        File file = new File(usersFile);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            UserList userList;

            CipherInputStream cipherInputStream = new CipherInputStream(new BufferedInputStream(new FileInputStream(usersFile)), cipher);
            try (ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream)) {
                SealedObject sealedObject = (SealedObject) inputStream.readObject();
                userList = (UserList) sealedObject.getObject(cipher);
                LOG.debug("User list: " + userList.getUsers());
            }
            return userList.getUsers();

        } catch (ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Error occurs while loading users: " + e.getMessage());
            throw new IOException(e);
        }
    }

    static class UserList implements Serializable {

        private static final long serialVersionUID = 1L;
        private List<UserPreferences> users;

        public UserList() {
        }

        public List<UserPreferences> getUsers() {
            return users;
        }

        public void setUsers(List<UserPreferences> users) {
            this.users = users;
        }

    }
}
