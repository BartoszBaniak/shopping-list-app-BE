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
        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String authenticatedUserId = userService.getUserIDFromAccessToken(request);
        if (authenticatedUserId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Sprawdzenie, czy żądany użytkownik zgadza się z zalogowanym użytkownikiem
        if (!authenticatedUserId.equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to view shopping lists for this user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Pobranie list zakupów użytkownika
        List<ShoppingListDTO> userShoppingLists = shoppingListService.findShoppingListsByUserId(userId);

        if (userShoppingLists.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "No shopping lists found for this user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // Uzupełnienie statusów list zakupów
        for (ShoppingListDTO shoppingListDTO : userShoppingLists) {
            Long shoppingListId = shoppingListDTO.getId(); // Pobranie ID listy zakupów
            // Tutaj możesz użyć odpowiedniej metody z ShoppingListService, aby pobrać status listy
            String status = shoppingListService.getShoppingListStatus(shoppingListId);
            shoppingListDTO.setStatus(status);
        }

        return ResponseEntity.ok(userShoppingLists);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShoppingList(@RequestParam String name, HttpServletRequest request) {
        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            // Jeśli identyfikator użytkownika nie został znaleziony, zwróć błąd
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in access token");
        }

        // Pobranie użytkownika na podstawie identyfikatora
        User user = userService.getUserById(userId);

        // Sprawdzenie, czy nazwa listy nie jest pusta
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("Name cannot be empty!");
        }

        // Utworzenie nowej listy zakupów
        ShoppingList newList = new ShoppingList();
        newList.setName(name);
        newList.setUser(user); // Przypisanie użytkownika do listy zakupów
        newList.setStatus("Active");
        // Zapisanie listy w bazie danych
        ShoppingList savedList = shoppingListService.createShoppingList(newList);

        // Zwrócenie odpowiedzi
        return ResponseEntity.ok(savedList);
    }

    @GetMapping("/{shoppingListId}")
    public ResponseEntity<?> getShoppingList(@PathVariable Long shoppingListId, HttpServletRequest request) {
        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Pobranie listy zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy lub ma do niej dostęp
        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to view this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Znajdź elementy listy zakupów
        List<ShoppingListItem> items = shoppingListService.findAllItemsByShoppingListId(shoppingListId);

        // Przygotuj odpowiedź
        Map<String, Object> response = new HashMap<>();
        response.put("id", shoppingList.getId());
        response.put("name", shoppingList.getName());
        // Dodaj inne informacje o liście zakupów, jeśli potrzeba

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
    public ResponseEntity<?> addProductsToList(
            @PathVariable Long shoppingListId,
            @RequestBody Map<String, Integer> productQuantities,
            HttpServletRequest request) {

        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            // Jeśli identyfikator użytkownika nie został znaleziony, zwróć błąd
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in access token");
        }

        // Pobierz listę zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shopping list not found");
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy
        if (!shoppingList.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to modify this shopping list");
        }

        // Zapisz listę zakupów z nowymi produktami
        shoppingListService.saveShoppingList(shoppingList, productQuantities);

        return ResponseEntity.ok().build();
    }
    @PutMapping("/{shoppingListId}/products/update")
    public ResponseEntity<?> updateProductQuantities(
            @PathVariable Long shoppingListId,
            @RequestBody Map<String, Integer> productQuantities,
            HttpServletRequest request) {

        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            // Jeśli identyfikator użytkownika nie został znaleziony, zwróć błąd
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Pobierz listę zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy
        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to modify this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Zaktualizuj ilości produktów na liście zakupów
        try {
            shoppingListService.updateProductQuantities(shoppingList, productQuantities);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ApiError error = new ApiError("Internal Server Error", null, "Failed to update product quantities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{shoppingListId}/products/delete")
    public ResponseEntity<?> deleteProductsFromList(
            @PathVariable Long shoppingListId,
            @RequestParam List<String> productNames,
            HttpServletRequest request) {

        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Pobierz listę zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy
        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to delete products from this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Usuń produkty z listy zakupów
        try {
            shoppingListService.deleteProductsFromList(shoppingList, productNames);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ApiError error = new ApiError("Error", null, e.getMessage()); // Zmieniono typ błędu na ogólny
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error); // Zmieniono status na BAD_REQUEST
        }
    }
    @DeleteMapping("/{shoppingListId}/delete")
    public ResponseEntity<?> deleteShoppingList(
            @PathVariable Long shoppingListId,
            HttpServletRequest request) {

        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Pobierz listę zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy
        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to delete this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Usuń listę zakupów wraz z jej elementami
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

        // Retrieve all items for the shopping list
        List<ShoppingListItem> items = shoppingListService.findAllItemsByShoppingListId(shoppingListId);

        // Prepare the response
        if (items.isEmpty()) {
            // If there are no items, return an empty response
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
    public ResponseEntity<?> updateShoppingListName(
            @PathVariable Long shoppingListId,
            @RequestParam String newName,
            HttpServletRequest request) {

        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Pobranie listy zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy
        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to modify this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Aktualizacja nazwy listy
        shoppingList.setName(newName);

        // Zapisanie zaktualizowanej listy w bazie danych
        shoppingListService.updateShoppingListName(shoppingList);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shoppingListId}/updateStatus")
    public ResponseEntity<?> updateShoppingListStatus(
            @PathVariable Long shoppingListId,
            @RequestParam String status,
            HttpServletRequest request) {

        // Sprawdzenie uwierzytelnienia
        ResponseEntity<?> authorizationResult = userService.checkAuthorization(request);
        if (authorizationResult.getStatusCode() != HttpStatus.OK) {
            return authorizationResult;
        }

        // Pobranie identyfikatora użytkownika z tokenu uwierzytelniającego
        String userId = userService.getUserIDFromAccessToken(request);
        if (userId == null) {
            ApiError error = new ApiError("Unauthorized", null, "User ID not found in access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Pobranie listy zakupów
        Optional<ShoppingList> optionalShoppingList = shoppingListService.findShoppingListById(shoppingListId);
        if (optionalShoppingList.isEmpty()) {
            ApiError error = new ApiError("Not Found", null, "Shopping list not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ShoppingList shoppingList = optionalShoppingList.get();

        // Sprawdź, czy użytkownik jest właścicielem listy
        if (!shoppingList.getUser().getId().equals(userId)) {
            ApiError error = new ApiError("Forbidden", null, "You are not authorized to modify this shopping list");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Zaktualizuj status listy zakupów
        shoppingList.setStatus(status);

        // Zapisz zaktualizowaną listę w bazie danych
        shoppingListService.updateShoppingListStatus(shoppingList);

        return ResponseEntity.ok().build();
    }
}