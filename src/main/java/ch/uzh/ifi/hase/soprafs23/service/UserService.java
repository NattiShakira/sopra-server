package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    // This method creates a new User object.
    // First, it takes a newUser User object and then sets a token, status and creation date attributes to it.
    // Then, is newUser passes the check (that there's no user with the same Username in the repo, an instance
    // of newUser is saved into a repo)
    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setCreation_date(new Date());
        checkIfUserExists(newUser);
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush(); // To save

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @see User
     */
    // Method for Register, checks if a user with the same Username exits. If yes, deny entry, if no, create new user
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        String message = "The provided username is not unique. Therefore, the user could not be registered!";
        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    message);
        }
    }

    public User getUser(User userToBeLoggedIn) {
        User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername());

        String messageNoUser = "The user with the provided username does not exist!";
        String messageWrongPassword = "The provided password is wrong!";
        if (userByUsername == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    messageNoUser);
        }
        else if (!(userByUsername.getPassword().equals(userToBeLoggedIn.getPassword()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    messageWrongPassword);
        }

        log.debug("The user is allowed to login: {}", userByUsername);
        userByUsername.setStatus(UserStatus.ONLINE);
        return userByUsername;
    }

    /*
       * @param userById
       * @throws org.springframework.web.server.ResponseStatusException
       * @see User

     */
    public User getUserProfile(long id) {
        String message = "User with id %d was not found!";
        return userRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(message, id)));
    }

    public void updateUserProfile(UserPutDTO userPutDTO, long id) {
        String messageId = "User with id %d was not found!";
        User userToUpdate = userRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(messageId, id)));

        if (userPutDTO.getUsername() != null) {
            userToUpdate.setUsername(userPutDTO.getUsername());
        }

        if (userPutDTO.getBirthday() != null) {
            userToUpdate.setBirthday(userPutDTO.getBirthday());
        }

        if (userPutDTO.getStatus() != null && !userPutDTO.getStatus().isEmpty()) {
            userToUpdate.setStatus(UserStatus.valueOf(userPutDTO.getStatus()));
        }
        userRepository.save(userToUpdate);
    }
}
