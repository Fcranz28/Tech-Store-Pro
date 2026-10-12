package com.techstorepro.techstorepro.devolucion;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.techstorepro.techstorepro.auth.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DevolucionController.class, properties = {
    "app.jwt.secret=test-returns-secret-at-least-32-bytes", "app.jwt.issuer=techstore-pro"
})
@Import(SecurityConfig.class)
class DevolucionSecurityTests {
    @Autowired MockMvc mvc;
    @MockitoBean DevolucionService service;

    @Test void unauthenticatedCannotSeeReturns() throws Exception {
        mvc.perform(get("/devoluciones")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void clientCannotManageReturns() throws Exception {
        mvc.perform(get("/devoluciones/gestion").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void operatorCanManageButCannotSeeClientList() throws Exception {
        mvc.perform(get("/devoluciones/gestion").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR"))))
            .andExpect(status().isOk());
        mvc.perform(get("/devoluciones").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR"))))
            .andExpect(status().isForbidden());
    }
    @Test void clientCanSeeOwnReturns() throws Exception {
        when(service.propias(any())).thenReturn(List.of());
        mvc.perform(get("/devoluciones").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk());
    }
}
