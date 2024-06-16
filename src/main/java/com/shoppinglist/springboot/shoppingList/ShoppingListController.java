package com.shoppinglist.springboot.shoppingList;

import com.shoppinglist.springboot.user.User;
import com.shoppinglist.springboot.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shoppinglist.springboot.user.ApiError;

import java.util.*;

@RestController
@RequestMapping("/api/shoppingLists")
public class ShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserShoppingLists(@PathVariable String userId, HttpServletRequest request) {
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        List<ShoppingListDTO> userShoppingLists = shoppingListService.findShoppingListsByUserId(userId);

        if (userShoppingLists.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "No shopping lists found for the user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.ok(userShoppingLists);
    }

    @PostMapping
    public ResponseEntity<?> createShoppingList(@RequestBody ShoppingList shoppingList, HttpServletRequest request) {
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String authenticatedUserId = userService.getUserIDFromAccessToken(request);
        if (authenticatedUserId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (shoppingList.getUser() == null || !shoppingList.getUser().getId().equals(authenticatedUserId)) {
            ApiError error = new ApiError("Bad Request", null, "User ID does not match the authenticated user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        ShoppingList createdShoppingList = shoppingListService.createShoppingList(shoppingList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShoppingList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShoppingList(@PathVariable Long id, @RequestBody ShoppingList shoppingList, HttpServletRequest request) {
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String authenticatedUserId = userService.getUserIDFromAccessToken(request);
        if (authenticatedUserId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> existingShoppingList = shoppingListService.findShoppingListById(id);
        if (existingShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList updatedShoppingList = existingShoppingList.get();

        if (!updatedShoppingList.getUser().getId().equals(authenticatedUserId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to update this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        if (shoppingList.getName() != null) {
            updatedShoppingList.setName(shoppingList.getName());
        }

        if (shoppingList.getStatus() != null) { // Obsługa pola status
            updatedShoppingList.setStatus(shoppingList.getStatus());
        }

        shoppingListService.updateShoppingListName(updatedShoppingList);

        return ResponseEntity.ok(updatedShoppingList);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShoppingListStatus(@PathVariable Long id, @RequestBody Map<String, String> updateRequest, HttpServletRequest request) {
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String authenticatedUserId = userService.getUserIDFromAccessToken(request);
        if (authenticatedUserId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> existingShoppingList = shoppingListService.findShoppingListById(id);
        if (existingShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = existingShoppingList.get();

        if (!shoppingList.getUser().getId().equals(authenticatedUserId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to update the status of this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        String newStatus = updateRequest.get("status");
        if (newStatus == null) {
            ApiError error = new ApiError("Bad Request", null, "Status is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            Status status = Status.valueOf(newStatus); // Konwersja String na Status
            shoppingListService.updateShoppingListStatus(id, status);
            return ResponseEntity.ok("Status updated successfully");
        } catch (IllegalArgumentException e) {
            ApiError error = new ApiError("Bad Request", null, "Invalid status value");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ApiError error = new ApiError("Error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShoppingList(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String authenticatedUserId = userService.getUserIDFromAccessToken(request);
        if (authenticatedUserId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> existingShoppingList = shoppingListService.findShoppingListById(id);
        if (existingShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = existingShoppingList.get();

        if (!shoppingList.getUser().getId().equals(authenticatedUserId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to delete this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        shoppingListService.deleteShoppingList(shoppingList);

        return ResponseEntity.ok("Shopping list deleted successfully");
    }
}
