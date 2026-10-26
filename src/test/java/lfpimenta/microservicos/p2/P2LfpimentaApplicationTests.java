package lfpimenta.microservicos.p2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
@AutoConfigureMockMvc
class P2LfpimentaApplicationTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	MeterRegistry registry;

	@Autowired
	JdbcTemplate jdbc;

	private ResultActions json(String metodo, String url, String corpo) throws Exception {
		var req = metodo.equals("PUT") ? put(url) : post(url);
		return mvc.perform(req.contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private ResultActions login(String user, String senha) throws Exception {
		return json("POST", "/login", "{\"username\":\"" + user + "\",\"senha\":\"" + senha + "\"}");
	}

	@Test
	void fluxoCompleto() throws Exception {
		double validos = registry.get("login.validos").counter().count();
		double invalidos = registry.get("login.invalidos").counter().count();
		login("maria", "maria").andExpect(status().isOk());
		login("maria", "errada").andExpect(status().isUnauthorized());
		login("maria", "maria").andExpect(status().isOk());
		login("fantasma", "x").andExpect(status().isUnauthorized());
		login("manutencao", "x").andExpect(status().isUnauthorized());
		login("manutencao", "x").andExpect(status().isUnauthorized());
		login("manutencao", "manut").andExpect(status().isUnauthorized());
		assertEquals(1, registry.get("usuarios.bloqueados").gauge().value());
		assertEquals(2, registry.get("login.validos").counter().count() - validos);
		assertEquals(5, registry.get("login.invalidos").counter().count() - invalidos);
		assertEquals(false, jdbc.queryForObject("SELECT enabled FROM users WHERE username='manutencao'", Boolean.class));
		mvc.perform(get("/")).andExpect(status().isUnauthorized())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().doesNotExist("WWW-Authenticate"));
		mvc.perform(get("/").with(httpBasic("maria", "maria"))).andExpect(status().isForbidden());
		mvc.perform(get("/").with(httpBasic("admin", "admin"))).andExpect(status().isForbidden());
		mvc.perform(put("/unlock/manutencao").with(httpBasic("maria", "maria"))).andExpect(status().isForbidden());
		mvc.perform(get("/").with(httpBasic("manutencao", "manut"))).andExpect(status().isUnauthorized());
		mvc.perform(put("/unlock/manutencao").with(httpBasic("admin", "admin"))).andExpect(status().isOk());
		mvc.perform(put("/unlock/naoexiste").with(httpBasic("admin", "admin"))).andExpect(status().isNotFound());
		assertEquals(0, registry.get("usuarios.bloqueados").gauge().value());
		login("manutencao", "manut").andExpect(status().isOk());
		mvc.perform(post("/").with(httpBasic("manutencao", "manut")).contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"joao\",\"senha\":\"j123\"}")).andExpect(status().isCreated());
		mvc.perform(post("/").with(httpBasic("manutencao", "manut")).contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"joao\",\"senha\":\"j123\"}")).andExpect(status().isConflict());
		mvc.perform(post("/").with(httpBasic("manutencao", "manut")).contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"\",\"senha\":\"12345678901\"}")).andExpect(status().isBadRequest());
		mvc.perform(get("/").with(httpBasic("manutencao", "manut"))).andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.username=='joao')]").exists())
				.andExpect(jsonPath("$[0].senha").doesNotExist());
		login("joao", "j123").andExpect(status().isOk());
		json("PUT", "/trocarsenha/joao", "{\"atual\":\"errada\",\"nova\":\"novaaa\"}").andExpect(status().isUnauthorized());
		json("PUT", "/trocarsenha/joao", "{\"atual\":\"j123\",\"nova\":\"novaaa\"}").andExpect(status().isOk());
		json("PUT", "/trocarsenha/zzz", "{\"atual\":\"a\",\"nova\":\"b\"}").andExpect(status().isNotFound());
		login("joao", "j123").andExpect(status().isUnauthorized());
		login("joao", "novaaa").andExpect(status().isOk());
		System.out.println("PWD=" + jdbc.queryForList("SELECT username,password,enabled FROM users"));
		mvc.perform(get("/").with(httpBasic("joao", "novaaa"))).andExpect(status().isForbidden());
	}

	@Test
	void expoeDocumentacao() throws Exception {
		mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths['/login']").exists());
	}

}
