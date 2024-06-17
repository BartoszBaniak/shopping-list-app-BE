package com.shoppinglist.springboot.shoppingList;

import com.shoppinglist.springboot.user.ApiError;
import com.shoppinglist.springboot.user.User;
import com.shoppinglist.springboot.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        String authenticatedUserId = userService.getUserIDFromAccessToken(request);
        if (authenticatedUserId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (!authenticatedUserId.equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to view shopping lists for this user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<ShoppingListDTO> userShoppingLists = shoppingListService.findShoppingListsByUserId(userId);

        if (userShoppingLists.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "No shopping lists found for this user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        for (ShoppingListDTO shoppingListDTO : userShoppingLists) {
            Long shoppingListId = shoppingListDTO.getId(); // Pobranie ID listy zakupów

            String status = shoppingListService.getShoppingListStatus(shoppingListId);
            shoppingListDTO.setStatus(status);
        }

        return ResponseEntity.ok(userShoppingLists);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShoppingList(@RequestParam String name, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in access token");
        }

        User user = userService.getUserById(userId);

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("Name cannot be empty!");
        }

        ShoppingList newList = new ShoppingList();
        newList.setName(name);
        newList.setUser(user); // Przypisanie użytkownika do listy zakupów
        newList.setStatus("Active");

        ShoppingList savedList = shoppingListService.createShoppingList(newList);

        return ResponseEntity.ok(savedList);
    }

    @GetMapping("/{shoppingListId}")
    public ResponseEntity<?> getShoppingList(@PathVariable Long shoppingListId, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to view this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<ShoppingListItem> items = shoppingListService.findAllItemsByShoppingListId(shoppingListId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", shoppingList.getId());
        response.put("name", shoppingList.getName());

        List<Map<String, Object>> itemsResponse = new ArrayList<>();
        for (ShoppingListItem item : items) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("productName", item.getProduct().getName());
            itemInfo.put("quantity", item.getQuantity());
            itemInfo.put("category", item.getProduct().getCategory());
            itemsResponse.add(itemInfo);
        }
        response.put("items", itemsResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{shoppingListId}/products/add")
    public ResponseEntity<?> addProductsToList(@PathVariable Long shoppingListId, @RequestBody Map<String, Integer> productQuantities, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in access token");
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shopping list not found");
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to modify this shopping list");
        }

        shoppingListService.saveShoppingList(shoppingList, productQuantities);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shoppingListId}/products/update")
    public ResponseEntity<?> updateProductQuantities(@PathVariable Long shoppingListId, @RequestBody Map<String, Integer> productQuantities, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {

            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to modify this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            shoppingListService.updateProductQuantities(shoppingList, productQuantities);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ApiError error = new ApiError("Internal Server Error", null, "Failed to update product quantities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{shoppingListId}/products/delete")
    public ResponseEntity<?> deleteProductsFromList(@PathVariable Long shoppingListId, @RequestParam List<String> productNames, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to delete products from this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            shoppingListService.deleteProductsFromList(shoppingList, productNames);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ApiError error = new ApiError("Error", null, e.getMessage()); // Zmieniono typ błędu na ogólny
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error); // Zmieniono status na BAD_REQUEST
        }
    }

    @DeleteMapping("/{shoppingListId}/delete")
    public ResponseEntity<?> deleteShoppingList(@PathVariable Long shoppingListId, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to delete this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            shoppingListService.deleteShoppingList(shoppingList);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ApiError error = new ApiError("Internal Server Error", null, "Failed to delete shopping list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{shoppingListId}/items")
    public ResponseEntity<?> getShoppingListItems(@PathVariable Long shoppingListId) {
        // Find the shopping list
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<ShoppingListItem> items = shoppingListService.findAllItemsByShoppingListId(shoppingListId);

        if (items.isEmpty()) {

            return ResponseEntity.ok(Collections.emptyList());
        } else {
            List<Map<String, Object>> itemsResponse = new ArrayList<>();
            for (ShoppingListItem item : items) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("productName", item.getProduct().getName());
                itemInfo.put("quantity", item.getQuantity());
                itemInfo.put("category", item.getProduct().getCategory());
                itemsResponse.add(itemInfo);
            }
            return ResponseEntity.ok(itemsResponse);
        }
    }

    @PutMapping("/{shoppingListId}/updateName")
    public ResponseEntity<?> updateShoppingListName(@PathVariable Long shoppingListId, @RequestParam String newName, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to modify this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        shoppingList.setName(newName);

        shoppingListService.updateShoppingListName(shoppingList);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shoppingListId}/updateStatus")
    public ResponseEntity<?> updateShoppingListStatus(@PathVariable Long shoppingListId, @RequestParam String status, HttpServletRequest request) {

        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to modify this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        shoppingList.setStatus(status);

        shoppingListService.updateShoppingListStatus(shoppingList);

        return ResponseEntity.ok().build();
    }
}