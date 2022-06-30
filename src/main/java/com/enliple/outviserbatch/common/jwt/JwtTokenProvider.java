package com.enliple.outviserbatch.common.jwt;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

@Component
public class JwtTokenProvider {

	private final static String BEARER = "Bearer";

	@Value("${jwt.secretkey}")
	private String secretKey;

	/**
	 * 토큰 추출
	 * 
	 * @param req
	 * @return
	 */
	public String extract(HttpServletRequest req) {

		Enumeration<String> headers = req.getHeaders(HttpHeaders.AUTHORIZATION);

		while (headers.hasMoreElements()) {
			String value = headers.nextElement();

			if (value.startsWith(BEARER)) {
				return value.substring(BEARER.length()).trim();
			}
		}

		return "";
	}

	/**
	 * 토큰 유효성 검사
	 * 
	 * @param token
	 * @return
	 */
	public boolean validateToken(String token) {

		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey.getBytes("UTF-8")).parseClaimsJws(token);

			if (claims.getBody().getExpiration().before(new Date())) {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * 토큰에 해당하는 값 리턴
	 * 
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	public String getData(String token) throws Exception {

		return Jwts.parser().setSigningKey(secretKey.getBytes("UTF-8")).parseClaimsJws(token).getBody().getSubject();
	}
}
