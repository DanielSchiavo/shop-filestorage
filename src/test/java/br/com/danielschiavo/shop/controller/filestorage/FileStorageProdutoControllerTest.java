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
import java.util.ArrayList;
import java.util.List;

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
import br.com.danielschiavo.shop.service.filestorage.FileStorageProdutoService;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("dev")
class FileStorageProdutoControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	private String tokenUser = JwtUtilTest.generateTokenUser();
	
	private String tokenAdmin = JwtUtilTest.generateTokenAdmin();

	@MockBean
	private FileStorageProdutoService fileStorageService;
	
	@Autowired
	private JacksonTester<List<ArquivoInfoDTO>> listaArquivoInfoDTOJson;
	
	@Autowired
	private JacksonTester<ArquivoInfoDTO> arquivoInfoDTOJson;
	
	@Test
	@DisplayName("Deletar arquivo produto deve retornar http 204 quando token e parametro válidos são enviados")
	void deletarArquivoProduto_TokenAdminEParametroValido_DeveRetornarOkNoContent() throws IOException, Exception {
		//ARRANGE
		doNothing().when(fileStorageService).deletarArquivoProdutoNoDisco(any());

		//ACT
		String nomeArquivo = "Nomequalquer.jpeg";
		var response = mvc.perform(delete("/shop/admin/filestorage/arquivo-produto/{nomeArquivo}", nomeArquivo)
								  .header("Authorization", "Bearer " + tokenAdmin))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	@DisplayName("Deletar arquivo produto deve retornar http 403 quando token de usuario comum é enviado")
	void deletarArquivoProduto_TokenUserEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeArquivo = "Nomequalquer.jpeg";
		var response = mvc.perform(delete("/shop/admin/filestorage/arquivo-produto/{nomeArquivo}", nomeArquivo)
								  .header("Authorization", "Bearer " + tokenUser))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Deletar arquivo produto deve retornar http 403 quando token não é enviado")
	void deletarArquivoProduto_NenhumTokenEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeArquivo = "Nomequalquer.jpeg";
		var response = mvc.perform(delete("/shop/admin/filestorage/arquivo-produto/{nomeArquivo}", nomeArquivo))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Mostrar arquivo produto por lista de nomes deve retornar codigo http 200 quando token e dto é valido")
	void mostrarArquivoProdutoPorListaDeNomes_TokenEDtoValido_DeveRetornarOk() throws IOException, Exception {
		//ARRANGE
		byte[] bytesImagem = "Hello world".getBytes();
		ArquivoInfoDTO arquivoInfoDTO = new ArquivoInfoDTO("Imagemum.jpeg", bytesImagem);
		ArquivoInfoDTO arquivoInfoDTO2 = new ArquivoInfoDTO("Imagemdois.jpeg", bytesImagem);
		List<ArquivoInfoDTO> listaArquivoInfoDTO = new ArrayList<>(List.of(arquivoInfoDTO, arquivoInfoDTO2));
		when(fileStorageService.mostrarArquivoProdutoPorListaDeNomes(any())).thenReturn(listaArquivoInfoDTO);
		
		//ACT
		String imagem1 = "Imagemum.jpeg";
		String imagem2 = "Imagemdois.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/arquivo-produto?arquivo={imagem1}&arquivo={imagem2}", imagem1, imagem2)
				  				  .header("Authorization", "Bearer " + tokenAdmin)
								  .contentType(MediaType.APPLICATION_JSON))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var jsonEsperado = listaArquivoInfoDTOJson.write(listaArquivoInfoDTO).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}
	
	@Test
	@DisplayName("Mostrar arquivo produto por lista de nomes deve retornar codigo http 403 quando usuario comum tenta acessar o endpoint")
	void mostrarArquivoProdutoPorListaDeNomes_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(get("/shop/admin/filestorage/arquivo-produto")
				  				  .header("Authorization", "Bearer " + tokenUser))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Mostrar arquivo produto por lista de nomes deve retornar codigo http 403 quando tenta usar endpoint sem enviar token")
	void mostrarArquivoProdutoPorListaDeNomes_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(get("/shop/admin/filestorage/arquivo-produto"))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Mostrar arquivo produto por nome deve retornar codigo http 200 quando token e dto é valido")
	void mostrarArquivoProdutoPorNome_TokenEDtoValido_DeveRetornarOk() throws IOException, Exception {
		//ARRANGE
		ArquivoInfoDTO arquivoInfoDTO = new ArquivoInfoDTO("Imagemum.jpeg", "Hello world".getBytes());
		when(fileStorageService.pegarArquivoProdutoPorNome(any())).thenReturn(arquivoInfoDTO);
		
		//ACT
		String nomeArquivo = "Imagemum.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/arquivo-produto/{nomeArquivo}", nomeArquivo)
				  				  .header("Authorization", "Bearer " + tokenAdmin))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        var jsonEsperado = arquivoInfoDTOJson.write(arquivoInfoDTO).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}
	
	
	@Test
	@DisplayName("Mostrar arquivo produto por lista de nomes deve retornar codigo http 403 quando usuario comum tenta acessar o endpoint")
	void mostrarArquivoProdutoPorNome_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeArquivo = "Imagemum.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/arquivo-produto/{nomeArquivo}", nomeArquivo)
				  				  .header("Authorization", "Bearer " + tokenUser))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Mostrar arquivo produto por lista de nomes deve retornar codigo http 403 quando tenta usar endpoint sem enviar token")
	void mostrarArquivoProdutoPorNome_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		String nomeArquivo = "Imagemum.jpeg";
		var response = mvc.perform(get("/shop/admin/filestorage/arquivo-produto/{nomeArquivo}", nomeArquivo))
								  .andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Cadastrar array arquivo produto deve retornar http 201 quando token e multipart são enviados")
	void cadastrarArrayArquivoProduto_TokenAdminEArrayMultipart_DeveRetornarCreated() throws IOException, Exception {
		//ARRANGE
		byte[] bytesImagem = "Hello world".getBytes();
		ArquivoInfoDTO arquivoInfoDTO = new ArquivoInfoDTO("Imagemum.jpeg", bytesImagem);
		ArquivoInfoDTO arquivoInfoDTO2 = new ArquivoInfoDTO("Imagemdois.jpeg", bytesImagem);
		List<ArquivoInfoDTO> listaArquivoInfoDTO = new ArrayList<>(List.of(arquivoInfoDTO, arquivoInfoDTO2));
		when(fileStorageService.persistirArrayArquivoProduto(any(), any())).thenReturn(listaArquivoInfoDTO);
		
		//ACT
        MockMultipartFile file1 = new MockMultipartFile("arquivos", "Imagemum.jpeg", MediaType.TEXT_PLAIN_VALUE, bytesImagem);
        MockMultipartFile file2 = new MockMultipartFile("arquivos", "Imagemdois.jpeg", MediaType.TEXT_PLAIN_VALUE, bytesImagem);
		var response = mvc.perform(multipart("/shop/admin/filestorage/arquivo-produto/array")
								  .file(file1)
								  .file(file2)
								  .header("Authorization", "Bearer " + tokenAdmin)
								  .contentType(MediaType.MULTIPART_FORM_DATA))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        var jsonEsperado = listaArquivoInfoDTOJson.write(listaArquivoInfoDTO).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}
	
	@Test
	@DisplayName("Cadastrar array arquivo produto deve retornar http 403 quando usuario comum tenta acessar o endpoint")
	void cadastrarArrayArquivoProduto_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/arquivo-produto/array")
								  .header("Authorization", "Bearer " + tokenUser))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Cadastrar array arquivo produto deve retornar http 403 quando token não é enviado")
	void cadastrarArrayArquivoProduto_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(post("/shop/admin/filestorage/arquivo-produto/array"))
						.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Alterar arquivo produto deve retornar http 403 quando usuario comum tenta acessar o endpoint")
	void alterarArquivoProduto_TokenUser_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(put("/shop/admin/filestorage/arquivo-produto")
				.header("Authorization", "Bearer " + tokenUser))
				.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
	@Test
	@DisplayName("Alterar um arquivo produto deve retornar http 403 quando token não é enviado")
	void alterarArquivoProduto_TokenNaoEnviado_DeveRetornarForbidden() throws IOException, Exception {
		//ACT
		var response = mvc.perform(put("/shop/admin/filestorage/arquivo-produto"))
								.andReturn().getResponse();
		
		//ASSERT
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
	}
	
}
