package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    /**
    * UserControllerTest
    * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
    * request without actually sending them over the network.
    * This tests if the UserController works.
    */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /**
    * Test for endpoint "/users", GET, status OK (200).
    */
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("Username");
    user.setPassword("Password");
    user.setStatus(UserStatus.ONLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].password", is(user.getPassword())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    /**
    * Test for endpoint "/users", POST, status CREATED (201).
    */
    @Test
    public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("Password");
    user.setUsername("Username");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setBirthday(null);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setPassword("Password");
    userPostDTO.setUsername("Username");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // make the request
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // validate the result
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.password", is(user.getPassword())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
        .andExpect(jsonPath("$.token", is(user.getToken())))
        .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
    }

    /**
    * Helper Method to convert userPostDTO into a JSON string such that the input
    * can be processed
    * Input will look like this: {"name": "Test User", "username": "testUsername"}
    *
    * @return string
    */
    private String asJsonString(final Object object) {
        try {
          return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              String.format("The request body could not be created.%s", e.toString()));
        }
    }

    /**
     * Test for endpoint "/users", POST, status CONFLICT (409).
     */
    @Test
    public void createUser_invalidInput_CONFLICT() throws Exception {
        // given
        String errorMessage = "The provided username is not unique. Therefore, the user could not be registered!";
        ResponseStatusException conflictException = new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("Password");
        userPostDTO.setUsername("Username");

        given(userService.createUser(Mockito.any())).willThrow(conflictException);

        // make the request
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // validate the result
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict())
                .andExpect(status().reason(is(errorMessage)));
    }

    /**
     * Test for endpoint "/users/{id}", GET, status OK (200).
     */
    @Test
    public void getUserProfile_validInput_OK() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("Password");
        user.setUsername("Username");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        user.setBirthday(null);

        long id = user.getId();

        given(userService.getUserProfile(id)).willReturn(user);

        // make the request
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}", id);

        // validate the result
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.token", is(user.getToken())))
                .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
    }

    /**
     * Test for endpoint "/users/{id}", GET, status NOT_FOUND (404).
     */
    @Test
    public void getUserProfile_invalidInput_NOT_FOUND() throws Exception {
        // given
        long invalidId = 34756;
        String errorMessage = "User with id 34756 was not found!";

        ResponseStatusException notFoundException = new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);

        given(userService.getUserProfile(invalidId)).willThrow(notFoundException);

        // make the request
        MockHttpServletRequestBuilder getRequest = get("/users/{id}", invalidId);

        // validate the result
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(status().reason(is(errorMessage)));
    }
}