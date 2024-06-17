package com.shoppinglist.springboot.user;

import com.shoppinglist.springboot.Token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getUser(@PathVariable("uuid") String id, HttpServletRequest httpRequest) {
        return userService.getUserDetails(id, userService, httpRequest);
    }

    @GetMapping("email/{email}")
    public User getUserByEmail(@PathVariable("email") String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody UserRegistrationRequest request) {
        ResponseEntity<?> response = userService.addUser(request);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PutMapping("{uuid}")
    public ResponseEntity<?> updateUser(
            @PathVariable("uuid") String uuid,
            @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        return userService.updateUser(uuid, request, httpRequest);
    }

}