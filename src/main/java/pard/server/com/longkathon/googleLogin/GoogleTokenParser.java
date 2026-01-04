package pard.server.com.longkathon.googleLogin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class GoogleTokenParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    // traceId를 받아서 컨트롤러 로그와 연결
    public static GoogleUserInfo parse(String idToken, String traceId) throws Exception {
        log.info("[{}][04] TokenParser 진입", traceId);

        if (idToken == null || idToken.isBlank()) {
            log.warn("[{}][04-1] idToken이 비어있음", traceId);
            throw new IllegalArgumentException("idToken is empty");
        }

        // JWT: header.payload.signature
        String[] parts = idToken.split("\\.");
        log.info("[{}][04-2] JWT split 완료 (parts={})", traceId, parts.length);

        if (parts.length < 2) {
            log.warn("[{}][04-3] JWT 형식 이상 (parts<2)", traceId);
            throw new IllegalArgumentException("Invalid token format");
        }

        String payloadPart = parts[1];
        log.info("[{}][04-4] payloadPart 길이={}", traceId, payloadPart.length());

        // padding 보정
        int pad = (4 - (payloadPart.length() % 4)) % 4;
        payloadPart = payloadPart + "=".repeat(pad);
        log.info("[{}][04-5] Base64 padding 보정 완료 (pad={})", traceId, pad);

        // decode
        String payloadJson = new String(
                Base64.getUrlDecoder().decode(payloadPart),
                StandardCharsets.UTF_8
        );
        log.info("[{}][04-6] payload 디코드 완료 (jsonLength={})", traceId, payloadJson.length());
        // 필요하면 payloadJson 일부만 출력(너무 길면 지저분해짐)
        log.info("[{}][04-7] payloadJson(앞부분 120자): {}", traceId,
                payloadJson.substring(0, Math.min(120, payloadJson.length()))
        );

        Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);
        log.info("[{}][04-8] JSON -> Map 파싱 완료 (keys={})", traceId, payload.keySet());

        String email = (String) payload.get("email");
        String socialId = (String) payload.get("sub");

        log.info("[{}][05] 추출 완료: email={}, sub(socialId)={}", traceId, email, socialId);

        return new GoogleUserInfo(email, socialId);
    }
}
