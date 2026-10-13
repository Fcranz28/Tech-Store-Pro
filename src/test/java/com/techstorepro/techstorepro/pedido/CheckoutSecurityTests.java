package com.techstorepro.techstorepro.pedido;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.techstorepro.techstorepro.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PedidoController.class, properties = {
    "app.jwt.secret=test-checkout-secret-at-least-32-bytes", "app.jwt.issuer=techstore-pro"
})
@Import(SecurityConfig.class)
class CheckoutSecurityTests {
    @Autowired MockMvc mvc;
    @MockitoBean PedidoService service;
    private static final String INPUT = "{\"nombreDestinatario\":\"Cliente Demo\",\"telefonoEntrega\":\"999888777\",\"direccionEntrega\":\"Calle Demo 123\"}";
    @Test void checkoutRequiresLoginAndClientRole() throws Exception {
        mvc.perform(post("/pedidos/checkout").contentType(MediaType.APPLICATION_JSON).content(INPUT)).andExpect(status().isUnauthorized());
        mvc.perform(post("/pedidos/checkout").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR")))
            .contentType(MediaType.APPLICATION_JSON).content(INPUT)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void invalidDeliveryNeverCreatesOrder() throws Exception {
        mvc.perform(post("/pedidos/checkout").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(MediaType.APPLICATION_JSON).content("{\"nombreDestinatario\":\" \",\"telefonoEntrega\":\"abc\",\"direccionEntrega\":\"\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void clientCheckoutUsesJwtSubject() throws Exception {
        mvc.perform(post("/pedidos/checkout").with(jwt().jwt(token -> token.subject("buyer@example.com"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(MediaType.APPLICATION_JSON).content(INPUT)).andExpect(status().isCreated());
        verify(service).checkout(eq("buyer@example.com"), any(CheckoutRequest.class));
    }
}
