package com.om.smartpost.service;

import com.om.smartpost.dto.response.AuthResponse;
import com.om.smartpost.entity.RefreshToken;
import com.om.smartpost.entity.User;
import com.om.smartpost.repository.RefreshTokenRepository;
import com.om.smartpost.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh}")
    private Long refreshTokenDurationMs;

    final private RefreshTokenRepository refreshTokenRepository;
    final private UserRepository userRepository;
    final private JwtService jwtService;

//    create a refresh token method.
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("user not exist")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

//    validate the refresh token expiration
    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }

//    find token stored.
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public int deleteAllRefreshTokensForUser(User user) {
        return refreshTokenRepository.deleteByUser(user);
    }



    //    refresh token (get a new pair of token's)
    public AuthResponse refreshToken(String requestRefreshToken) {
        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // delete all the old refresh tokens
                    deleteAllRefreshTokensForUser(user);

                    // Generate new refresh token and save
                    RefreshToken newRefreshToken = new RefreshToken();
                    newRefreshToken.setUser(user);
                    newRefreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
                    newRefreshToken.setToken(UUID.randomUUID().toString());
                    refreshTokenRepository.save(newRefreshToken);

                    // generate new access token
                    String accessToken = jwtService.generateToken(user.getUsername(),user.getRole().name());

                    return new AuthResponse(accessToken, newRefreshToken.getToken(), user.getUsername(), user.getRole());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is invalid"));
    }

}
