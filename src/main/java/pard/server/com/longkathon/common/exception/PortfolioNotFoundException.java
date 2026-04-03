package pard.server.com.longkathon.common.exception;

/**
 * 포트폴리오를 찾을 수 없을 때 발생하는 예외
 */
public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(String message) {
        super(message);
    }
}
