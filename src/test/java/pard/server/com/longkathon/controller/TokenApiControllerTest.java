package pard.server.com.longkathon.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.config.jwt.JwtFactory;
import pard.server.com.longkathon.config.jwt.JwtProperties;
import pard.server.com.longkathon.config.jwt.refreshToken.RefreshToken;
import pard.server.com.longkathon.config.jwt.refreshToken.RefreshTokenRepository;
import pard.server.com.longkathon.config.jwt.token.CreateAccessTokenRequest;


import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    UserRepo userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        userRepository.deleteAll();
    }

    @DisplayName("createNewAccessToken: 새로운 액세스 토큰을 발급한다.")
    @Test
    public void createNewAccessToken() throws Exception {
        // given
        final String url = "/api/token";

        User testUser = userRepository.save(User.builder()
                        .name("홍길동")
                        .studentId("20250001")
                        .grade("1")              // String 타입이므로 "1"
                        .semester("1")           // String 타입이므로 "1"
                        .department("컴퓨터공학과")
                        .firstMajor("컴퓨터공학과")
                        .secondMajor("전자공학과")
                        .gpa("3.50")             // String 타입이므로 "3.50"
                        .email("user@gmail.com")
                        .build()
        );

        String refreshToken = JwtFactory.builder()
                .claims(Map.of("userId", testUser.getUserId()))
                .build()
                .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(testUser.getUserId(), refreshToken));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

}