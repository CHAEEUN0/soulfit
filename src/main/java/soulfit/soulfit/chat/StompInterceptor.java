package soulfit.soulfit.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.util.JwtUtil;

@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new SecurityException("인증 토큰이 필요합니다.");
            }

            String jwtToken = authHeader.substring(7);

            if (!jwtUtil.validateToken(jwtToken)) {
                throw new SecurityException("유효하지 않은 토큰입니다.");
            }

            String username = jwtUtil.extractUsername(jwtToken);

            accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, null));
        }
        return message;
    }
}
