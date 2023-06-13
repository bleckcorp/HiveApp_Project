package com.bctech.hive.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.bctech.hive.constant.SecurityConstants;
import com.bctech.hive.dto.response.TokenResponse;
import com.bctech.hive.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class JwtUtils {

    public static TokenResponse generateToken(Authentication authentication, com.bctech.hive.entity.User currentUser) {
        User user = (User) authentication.getPrincipal();

        String userName = user.getUsername();

        Map<String, Object> payload = new HashMap<>();
        payload.put("address", currentUser.getAddress());
        payload.put("fullName", currentUser.getFullName());
        payload.put("email", currentUser.getEmail());
        payload.put("verifiedStatus", currentUser.getIsVerified());


        Algorithm algorithm = Algorithm.HMAC256(SecurityConstants.SECRET.getBytes());
        String access_token = JWT.create()
                .withSubject(userName)
                .withExpiresAt(new Date(System.currentTimeMillis()+ SecurityConstants.EXPIRATION_TIME))
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withPayload( payload)
                .withClaim("roles", authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
                ).sign(algorithm);

        return new TokenResponse(access_token);
    }


    public static UsernamePasswordAuthenticationToken verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SecurityConstants.SECRET.getBytes());
            JWTVerifier verify = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verify.verify(token);
            String username = decodedJWT.getSubject();
            String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            stream(roles).forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role));
            });

            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } catch (Exception exception) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
    }
}
