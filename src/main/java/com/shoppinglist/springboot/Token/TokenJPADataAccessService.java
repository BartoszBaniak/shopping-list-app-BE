package com.shoppinglist.springboot.Token;

import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class TokenJPADataAccessService implements TokenDAO {
    private final TokenRepository tokenRepository;

    public TokenJPADataAccessService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void addToken(Token token) {
        tokenRepository.save(token);
    }

    @Override
    public Optional < Token > getTokenById(Integer userID) {
        return tokenRepository.findById(userID);
    }

    @Override
    public Optional < Token > getTokenByContent(String token) {
        return tokenRepository.findByContent(token);
    }
    public void deleteByContent(String tokenContent) {
        tokenRepository.deleteByContent(tokenContent);
    }

    public void deleteAllTokens(Integer userID) {
        tokenRepository.deleteAllByUserID(userID);
    }

}