package br.com.danielschiavo.shop.controller.filestorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.danielschiavo.JwtUtilTest;
import br.com.danielschiavo.feign.pedido.FileStoragePedidoComumServiceClient;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;
import br.com.danielschiavo.shop.model.filestorage.PersistirOuRecuperarImagemPedidoDTO;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("dev")
class FileStoragePedidoControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	private String tokenUser = JwtUtilTest.generateTokenUser();
	
	private String tokenAdmin = JwtUtilTest.generateTokenAdmin();

	@MockBean
	private FileStoragePedidoComumServiceClient fileStoragePedidoComumServiceClient;
	
	@Autowired
	private JacksonTester<ArquivoInfoDTO> arquivoInfoDTOJson;
	
	@Autowired
	private JacksonTester<PersistirOuRecuperarImagemPedidoDTO> postImagemPedidoDTOJson;

	@Test
	@DisplayName("Pegar imagem pedido por nome deve retornar codigo http 200 quando token e dto é valido")
	void pegarImagemPedidoPorNome_TokenEDtoValido_DeveRetornarOk() throws IOException, Exception {
		//ARRANGE
		ArquivoInfoDTO arquivoInfoDTO = new ArquivoInfoDTO("Imagemum.jpeg", "Hello world".getBytes());
		when(fileStoragePedidoComumServiceClient.pegarImagemPedido(any(), any())).thenReturn(arquivoInfoDTO);
		
		//ACT
		String nomeFotoPerfil = "Nomequalquer.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/pedido/{nomeFotoPerfil}", nomeFotoPerfil)
				  				  .header("Authorization", "Bearer " + tokenAdmin))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var jsonEsperado = arquivoInfoDTOJson.write(arquivoInfoDTO).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}
	
	@Test
	@DisplayName("Pegar imagem pedido por nome deve retornar codigo http 403 quando usuario comum tenta acessar o endpoint")
	void pegarImagemPedidoPorNome_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfil = "Nomequalquer.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/pedido/{nomeFotoPerfil}", nomeFotoPerfil)
				  				  .header("Authorization", "Bearer " + tokenUser))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Pegar imagem pedido por nome deve retornar codigo http 403 quando tenta usar endpoint sem enviar token")
	void pegarImagemPedidoPorNome_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeFotoPerfil = "Nomequalquer.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/pedido/{nomeFotoPerfil}", nomeFotoPerfil))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Persistir ou recuperar imagem pedido deve retornar http 201 quando token e multipart são enviados")
	void persistirOuRecuperarImagemPedido_TokenAdminEArrayMultipart_DeveRetornarCreated() throws IOException, Exception {
		//ARRANGE
		String nomeQualquer = "Nomequalquer.jpeg";
		when(fileStoragePedidoComumServiceClient.persistirOuRecuperarImagemPedido(any(), any())).thenReturn(new ArquivoInfoDTO(nomeQualquer, null));
		
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/pedido")
								  .header("Authorization", "Bearer " + tokenAdmin)
								  .contentType(MediaType.APPLICATION_JSON)
								  .content(postImagemPedidoDTOJson.write(
										  new PersistirOuRecuperarImagemPedidoDTO("NomeQualquer.jpeg" , 1L)).getJson()))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
	}
	
	@Test
	@DisplayName("Persistir ou recuperar imagem pedido deve retornar http 403 quando usuario comum tenta acessar o endpoint")
	void persistirOuRecuperarImagemPedido_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/pedido")
								  .header("Authorization", "Bearer " + tokenUser))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Persistir ou recuperar imagem pedido deve retornar http 403 quando token não é enviado")
	void persistirOuRecuperarImagemPedido_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/pedido"))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
}
