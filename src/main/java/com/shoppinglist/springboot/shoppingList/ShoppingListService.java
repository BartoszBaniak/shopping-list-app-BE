package com.shoppinglist.springboot.shoppingList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMapping;
import com.shoppinglist.springboot.keywordMapping.KeywordCategoryMappingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingListService {

    @Autowired
    private ShoppingListDAO shoppingListDAO;

    @Autowired
    private ShoppingListItemDAO shoppingListItemDAO;

    @Autowired
    private productRepository productRepository;

    @Autowired
    private KeywordCategoryMappingRepository keywordCategoryMappingRepository;

    public ShoppingListService(ShoppingListDAO shoppingListDAO, ShoppingListItemDAO shoppingListItemDAO, productRepository productRepository, KeywordCategoryMappingRepository keywordCategoryMappingRepository) {
        this.shoppingListDAO = shoppingListDAO;
        this.shoppingListItemDAO = shoppingListItemDAO;
        this.productRepository = productRepository;
        this.keywordCategoryMappingRepository = keywordCategoryMappingRepository;
    }

    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListDAO.getAllShoppingLists();
    }

    public Optional<ShoppingList> getShoppingListById(Long id) {
        return shoppingListDAO.getShoppingListById(id);
    }

    public ShoppingList saveShoppingList(ShoppingList shoppingList) {
        shoppingListDAO.addShoppingList(shoppingList);
        return shoppingList;
    }

    public Optional<ShoppingList> updateShoppingList(Long id, ShoppingList shoppingListDetails) {
        return shoppingListDAO.getShoppingListById(id).map(existingShoppingList -> {
            existingShoppingList.setName(shoppingListDetails.getName());
            existingShoppingList.setItems(shoppingListDetails.getItems());
            shoppingListDAO.updateShoppingList(existingShoppingList);
            return existingShoppingList;
        });
    }

    public boolean deleteShoppingList(Long id) {
        if (shoppingListDAO.getShoppingListById(id).isPresent()) {
            shoppingListDAO.deleteShoppingListById(id);
            return true;
        } else {
            return false;
        }
    }

    public Optional<ShoppingList> addProductToShoppingList(Long id, ShoppingListItemRequest itemRequest) {
        Optional<ShoppingList> shoppingList = shoppingListDAO.getShoppingListById(id);
        if (shoppingList.isPresent()) {
            ShoppingList existingShoppingList = shoppingList.get();

            // Tworzymy nowy produkt na podstawie informacji dostarczonych przez żądanie
            Product product = new Product();
            product.setName(itemRequest.getProduct().getName());
            product.setCategory(resolveCategoryForProduct(itemRequest.getProduct().getName())); // Rozwiązujemy kategorię na podstawie nazwy produktu

            // Zapisujemy produkt za pomocą ProductRepository
            Product savedProduct = productRepository.save(product);

            // Tworzymy nowy element listy zakupów i ustawiamy informacje o produkcie i ilości
            ShoppingListItem item = new ShoppingListItem();
            item.setProduct(savedProduct); // Ustawiamy produkt
            item.setQuantity(itemRequest.getQuantity());
            item.setPurchased(false); // Ustawiamy na false, bo produkt nie jest jeszcze kupiony
            item.setShoppingList(existingShoppingList); // Ustawiamy listę zakupów

            // Zapisujemy element listy zakupów do bazy danych
            shoppingListItemDAO.addShoppingListItem(item);

            // Dodajemy element listy zakupów do istniejącej listy zakupów
            existingShoppingList.getItems().add(item);
            shoppingListDAO.updateShoppingList(existingShoppingList);

            return Optional.of(existingShoppingList);
        } else {
            return Optional.empty();
        }
    }

    // Metoda do rozwiązania kategorii na podstawie nazwy produktu
    private String resolveCategoryForProduct(String productName) {
        // Pobieramy wszystkie mapowania słów kluczowych na kategorie
        List<KeywordCategoryMapping> mappings = keywordCategoryMappingRepository.findAll();

        // Przeszukujemy mapowania i zwracamy pierwszą pasującą kategorię dla produktu
        for (KeywordCategoryMapping mapping : mappings) {
            if (productName.toLowerCase().contains(mapping.getKeyword().toLowerCase())) {
                return mapping.getCategory();
            }
        }

        // Jeśli nie znaleziono pasującego mapowania, zwracamy domyślną kategorię
        return "Inne";
    }
}