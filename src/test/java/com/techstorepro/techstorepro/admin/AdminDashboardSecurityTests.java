package com.techstorepro.techstorepro.admin;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.techstorepro.techstorepro.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminDashboardController.class, properties = {
    "app.jwt.secret=test-dashboard-secret-at-least-32-bytes", "app.jwt.issuer=techstore-pro"
})
@Import(SecurityConfig.class)
class AdminDashboardSecurityTests {
    @Autowired MockMvc mvc;
    @MockitoBean AdminDashboardService service;

    @Test
    void sinSesionNoSePuedeConsultarResumen() throws Exception {
        mvc.perform(get("/admin/resumen")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void clienteNoPuedeConsultarResumenAunqueEsteAutenticado() throws Exception {
        mvc.perform(get("/admin/resumen").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void administradorPuedeConsultarResumen() throws Exception {
        mvc.perform(get("/admin/resumen").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
        verify(service).resumen();
    }
}
