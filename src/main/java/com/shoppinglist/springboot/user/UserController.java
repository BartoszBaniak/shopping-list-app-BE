package com.shoppinglist.springboot.user;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
public class UserController {
    @RestController
    @RequestMapping("api/users")
    public class CustomerController {
        private final UserService userService;

        public CustomerController(UserService userService) {
            this.userService = userService;
        }

        @GetMapping
        public List < User > getCustomers() {
            return userService.getAllUsers();
        }

        @GetMapping("{id}")
        public User getUser(@PathVariable("id") Integer id) {
            return userService.getUserById(id);
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
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteUser(@PathVariable("id") Integer id) {
            userService.deleteUser(id);
        }

        @PutMapping("{id}")
        public void updateUser(
                @PathVariable("id") Integer id,
                @RequestBody UserUpdateRequest request
        ) {
            userService.updateUser(id, request);
        }
    }
}