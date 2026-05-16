package br.com.automacao.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.automacao.base.BaseTest;
import br.com.automacao.pages.HomeSeuBarrigaPage;

public class HomeSeuBarrigaTest extends BaseTest{
	
	private HomeSeuBarrigaPage page;
	
	@BeforeEach
	public void setup() {
		page = new HomeSeuBarrigaPage();
	}
	
	
	@Test
	public void deverealizarLogin() {
		page.EscreveEmail("katana169@katana169.com.br");
		page.EscrevePassword("123456");
		page.deveClicarNoBotão();
		
		String mensagemSucesso = page.obterMensagemSucesso();
		Assertions.assertEquals("Bem vindo, Fulano de Tal Silva!", mensagemSucesso);
		
	}

}
