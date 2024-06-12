package com.shoppinglist.springboot.Token;

import java.util.Optional;

public interface TokenDAO {
    void addToken(Token token);

    Optional<Token> getTokenById(String userID);

    Optional<Token> getTokenByContent(String token);

    void deleteByContent(String tokenContent);

    void deleteAllTokens(String userID);

}