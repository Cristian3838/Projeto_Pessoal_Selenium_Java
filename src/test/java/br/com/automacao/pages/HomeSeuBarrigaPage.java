package br.com.automacao.pages;

import org.openqa.selenium.By;

import br.com.automacao.base.BasePage;

public class HomeSeuBarrigaPage extends BasePage{
	
	public void EscreveEmail(String texto) {
		escrever(By.xpath("//input[@placeholder='Email']"), texto);
	}
	
	public void EscrevePassword(String texto) {
		escrever(By.xpath("//input[@placeholder='Password']"), texto);
	}
	

	public void deveClicarNoBotão() {
		clicaNoElemento(By.xpath("//button[text()='Entrar']"));
	}
	
	public String obterMensagemSucesso() {
		return retornaTexto(By.cssSelector("div[role='alert']"));
	}

}
