package br.com.danielschiavo.shop.controller.filestorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.danielschiavo.JwtUtilTest;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;
import br.com.danielschiavo.shop.service.filestorage.FileStoragePerfilService;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("dev")
class FileStoragePerfilControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	private String tokenUser = JwtUtilTest.generateTokenUser();
	
	private String tokenAdmin = JwtUtilTest.generateTokenAdmin();

	@MockBean
	private FileStoragePerfilService fileStorageService;
	
	@Autowired
	private JacksonTester<ArquivoInfoDTO> arquivoInfoDTOJson;
	
	@Test
	@DisplayName("Deletar foto perfil deve retornar http 204 quando token e parametro válidos são enviados")
	void deletarFotoPerfil_TokenAdminEParametroValido_DeveRetornarOkNoContent() throws IOException, Exception {
		//ARRANGE
		doNothing().when(fileStorageService).deletarFotoPerfilNoDisco(any());
		
		//ACT
		String nomeFotoPerfilAntiga = "Nomequalquer.jpeg";
		var response = mvc.perform(delete("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfilAntiga}", nomeFotoPerfilAntiga)
								  .header("Authorization", "Bearer " + tokenAdmin))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	@DisplayName("Deletar foto perfil deve retornar http 403 quando token de usuario comum é enviado")
	void deletarFotoPerfil_TokenUserEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfilAntiga = "Nomequalquer.jpeg";
		var response = mvc.perform(delete("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfilAntiga}", nomeFotoPerfilAntiga)
								  .header("Authorization", "Bearer " + tokenUser))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Deletar foto perfil deve retornar http 403 quando token não é enviado")
	void deletarFotoPerfil_NenhumTokenEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfilAntiga = "Nomequalquer.jpeg";
		var response = mvc.perform(delete("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfilAntiga}", nomeFotoPerfilAntiga))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Pegar foto perfil por nome deve retornar codigo http 200 quando token e dto é valido")
	void pegarFotoPerfilPorNome_TokenEDtoValido_DeveRetornarOk() throws IOException, Exception {
		//ARRANGE
		ArquivoInfoDTO arquivoInfoDTO = new ArquivoInfoDTO("Imagemum.jpeg", "Hello world".getBytes());
		when(fileStorageService.pegarFotoPerfilPorNome(any())).thenReturn(arquivoInfoDTO);
		
		//ACT
		String nomeFotoPerfil = "Nomequalquer.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfil}", nomeFotoPerfil)
				  				  .header("Authorization", "Bearer " + tokenAdmin))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var jsonEsperado = arquivoInfoDTOJson.write(arquivoInfoDTO).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}
	
	@Test
	@DisplayName("Pegar foto perfil por nome deve retornar codigo http 403 quando usuario comum tenta acessar o endpoint")
	void pegarFotoPerfilPorNome_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfil = "Nomequalquer.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfil}", nomeFotoPerfil)
				  				  .header("Authorization", "Bearer " + tokenUser))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Pegar foto perfil por nome deve retornar codigo http 403 quando tenta usar endpoint sem enviar token")
	void pegarFotoPerfilPorNome_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfil = "Nomequalquer.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfil}", nomeFotoPerfil))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Cadastrar foto perfil deve retornar http 201 quando token e multipart são enviados")
	void cadastrarFotoPerfil_TokenAdminEArrayMultipart_DeveRetornarCreated() throws IOException, Exception {
		//ARRANGE
		byte[] bytesImagem = "Hello world".getBytes();
		ArquivoInfoDTO arquivoInfoDTO = new ArquivoInfoDTO("Imagemum.jpeg", bytesImagem);
		when(fileStorageService.persistirFotoPerfil(any(), any())).thenReturn(arquivoInfoDTO);
		
		//ACT
        MockMultipartFile file1 = new MockMultipartFile("foto", "Imagemum.jpeg", MediaType.TEXT_PLAIN_VALUE, bytesImagem);
		var response = mvc.perform(multipart("/shop/admin/filestorage/foto-perfil")
								  .file(file1)
								  .contentType(MediaType.MULTIPART_FORM_DATA)
								  .header("Authorization", "Bearer " + tokenAdmin))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        var jsonEsperado = arquivoInfoDTOJson.write(arquivoInfoDTO).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}
	
	@Test
	@DisplayName("Cadastrar foto perfil deve retornar http 403 quando usuario comum tenta acessar o endpoint")
	void cadastrarFotoPerfil_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/foto-perfil")
								  .header("Authorization", "Bearer " + tokenUser))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Cadastrar foto perfil deve retornar http 403 quando token não é enviado")
	void cadastrarFotoPerfil_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/foto-perfil"))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Alterar foto perfil deve retornar http 403 quando usuario comum tenta acessar o endpoint")
	void alterarFotoPerfil_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfilAntiga = "Nomequalquer.jpeg";
		var response = mvc.perform(put("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfilAntiga}", nomeFotoPerfilAntiga)
				.header("Authorization", "Bearer " + tokenUser))
				.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Alterar foto perfil deve retornar http 403 quando token não é enviado")
	void alterarFotoPerfil_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfilAntiga = "Nomequalquer.jpeg";
		var response = mvc.perform(put("/shop/admin/filestorage/foto-perfil/{nomeFotoPerfilAntiga}", nomeFotoPerfilAntiga))
								.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
}
