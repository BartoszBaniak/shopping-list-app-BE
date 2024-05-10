package com.shoppinglist.springboot.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
public class UserController {
    @RestController
    @RequestMapping("api/v1/users")
    public class CustomerController {
        private final UserService userService;

        public CustomerController(UserService userService) {
            this.userService = userService;
        }

        @GetMapping
        public List<User> getCustomers() {
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
        @ResponseStatus(HttpStatus.CREATED)
        public void addUser(@RequestBody UserRegistrationRequest request) {
            userService.addUser(request);
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
