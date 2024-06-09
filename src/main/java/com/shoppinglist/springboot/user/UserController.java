package com.shoppinglist.springboot.user;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.shoppinglist.springboot.Token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping("{uuid}")
    public ResponseEntity < ? > getUser(@PathVariable("uuid") String id, HttpServletRequest httpRequest) {
        return userService.getUserDetails(id, userService, httpRequest);
    }

    @GetMapping("email/{email}")
    public User getUserByEmail(@PathVariable("email") String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping
    public ResponseEntity < ? > addUser(@RequestBody UserRegistrationRequest request) {
        ResponseEntity < ? > response = userService.addUser(request);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @DeleteMapping("{id}")
    public ResponseEntity < ? > deleteUser(@PathVariable("id") String id, HttpServletRequest request) {
        ResponseEntity < ? > responseEntity = userService.deleteUser(id, request);
        return ResponseEntity.status(responseEntity.getStatusCode()).build();
    }


    @PutMapping("{uuid}")
    public ResponseEntity < ? > updateUser(
            @PathVariable("uuid") String uuid,
            @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        return userService.updateUser(uuid, request, httpRequest);
    }

    @PatchMapping("/pass/{uuid}")
    public ResponseEntity < ? > changePassword(@PathVariable String uuid, @RequestBody ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        ResponseEntity < ? > checkAuthorizationResult = userService.checkAuthorization(request);
        if (checkAuthorizationResult.getStatusCode() != HttpStatus.OK) {
            return checkAuthorizationResult;
        }
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId != null) {
            if (userId.equals(uuid)) {
                return userService.changePassword(uuid, changePasswordRequest);
            } else {
                Error error = new Error("General", null, "You can only modify your own password");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else {
            Error error = new Error("Access", null, "Getting user id from access token failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }



}