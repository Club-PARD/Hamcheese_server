package pard.server.com.longkathon.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSendbirdMappingRepo extends JpaRepository<UserSendbirdMapping, Long> {
    Optional<UserSendbirdMapping> findBySendbirdUserId(String sendbirdUserId);
}
