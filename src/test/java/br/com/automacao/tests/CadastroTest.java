package br.com.automacao.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import br.com.automacao.base.BaseTest;
import br.com.automacao.pages.CadastroPage;

public class CadastroTest extends BaseTest {

	private CadastroPage page;

	@BeforeEach
	public void setup() {
		page = new CadastroPage();
	}

	@Test
	public void deveCadastrarNovoUsuarioComSucesso() {
		test.info("Iniciando teste de validação de cadastro de e-mail.");

		page.botãoNovoUsuario();
		page.escreverTextoNome("Fulano de Tal Silva");
		page.escreverTextoEmail("katana169@katana169.com.br");
		page.escreverTextoSenha("123456");
		page.botaoCadastrar();

		String mensagem = page.obterMensagemAlerta();
		Assertions.assertEquals("Usuário inserido com sucesso", mensagem);

		test.pass("Mensagem de inserção validada com sucesso: " + mensagem); 
	}

	@Test
	public void naoDevePermitirCadastroComEmailJaCadastrado() {
		test.info("Iniciando teste de validação de e-mail duplicado.");

		page.botãoNovoUsuario();
		page.escreverTextoNome("Fulano de Tal Silva");
		page.escreverTextoEmail("fulano@fulano.com.br");
		page.escreverTextoSenha("123456");

		page.botaoCadastrar();

		String mensagem = page.obterMensagemAlerta();
		Assertions.assertEquals("Endereço de email já utilizado", mensagem);

		test.pass("Mensagem de erro validada com sucesso: " + mensagem);
	}

	@Test
	public void deveValidarObrigatoriedadeApenasDoCampoNome() {
		test.info("Iniciando teste de validação de campo nome.");

		page.botãoNovoUsuario();
		page.escreverTextoEmail("katana129@katana129.com.br");
		page.escreverTextoSenha("123456");

		page.botaoCadastrar();

		String mensagem = page.obterMensagemAlerta();
		Assertions.assertEquals("Nome é um campo obrigatório", mensagem);

		test.pass("Validação de obrigatoriedade de campo nome realizada com sucesso: " + mensagem);

	}

	@Test
	public void deveValidarObrigatoriedadeApenasDoCampoSenha() {

		test.info("Iniciando teste de validação de campo senha.");

		page.botãoNovoUsuario();
		page.escreverTextoNome("Fulano de Tal Silva");
		page.escreverTextoEmail("katana130@katana130.com.br");

		page.botaoCadastrar();

		String mensagem = page.obterMensagemAlerta();
		Assertions.assertEquals("Senha é um campo obrigatório", mensagem);

		test.pass("Validação de obrigatoriedade de campo senha realizada com sucesso: " + mensagem);

	}

	@Test
	public void deveValidarObrigatoriedadeApenasDoCampoEmail() {

		test.info("Iniciando teste de validação de campo Email.");

		page.botãoNovoUsuario();
		page.escreverTextoNome("Fulano de Tal Silva");
		page.escreverTextoSenha("123456");

		page.botaoCadastrar();

		String mensagem = page.obterMensagemAlerta();
		Assertions.assertEquals("Email é um campo obrigatório", mensagem);

		test.pass("Validação de obrigatoriedade de campo senha realizada com sucesso: " + mensagem);

	}

}
